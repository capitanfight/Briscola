package com.briscola4legenDs.briscola.Room.REST;

import Application.Game.Card;
import Application.Lobby.LobbyPlayer;
import Application.Models.Player;
import com.briscola4legenDs.briscola.Assets.PayloadBuilder;
import com.briscola4legenDs.briscola.Room.Room;
import com.briscola4legenDs.briscola.Room.State;
import com.briscola4legenDs.briscola.Room.Team;
import com.briscola4legenDs.briscola.Room.Token;
import com.briscola4legenDs.briscola.Room.WebSocket.LobbySocketHandler;
import com.briscola4legenDs.briscola.Room.WebSocket.RoomSocketHandler;
import com.briscola4legenDs.briscola.User.REST.UserService;
import com.briscola4legenDs.briscola.User.User;
import com.briscola4legenDs.briscola.User.WebSocket.UserSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

@Service
public class RoomService {
    private final RoomLocalRepository roomLocalRepository;
    private final LobbySocketHandler lobbySocketHandler;
    private final UserService userService;

    private static final Logger log = LoggerFactory.getLogger(RoomService.class.getName());
    private final UserSocketHandler userSocketHandler;

    @Autowired
    public RoomService(RoomLocalRepository roomLocalRepository, LobbySocketHandler lobbySocketHandler, RoomSocketHandler roomSocketHandler, UserService userService, UserSocketHandler userSocketHandler) {
        this.roomLocalRepository = roomLocalRepository;
        this.lobbySocketHandler = lobbySocketHandler;
        this.userService = userService;
        lobbySocketHandler.setRoomService(this);
        roomSocketHandler.setRoomService(this);
        this.userSocketHandler = userSocketHandler;
    }

    public Collection<Room> getAllRooms() {
        return roomLocalRepository.getAllRooms();
    }

    public void rmvRooms() {
        for (Room room : getAllRooms())
            roomLocalRepository.remove(room.getId());

        log.info("All rooms removed");
    }

    public Room getRoomById(long roomId) {
        return roomLocalRepository.getRoomById(roomId);
    }

    public String getName(long roomId) {
        return roomLocalRepository.getRoomById(roomId).getName();
    }

    public long createRoom(String name, Room.Visibility visibility) {
        Room newRoom = new Room(name, visibility);
        roomLocalRepository.add(newRoom);

        log.info("Created new room with id {} and visibility {}", newRoom.getId(), visibility);

        if (visibility == Room.Visibility.PUBLIC)
            userSocketHandler.sendUpdateRoomMsg();

        return newRoom.getId();
    }

    public void addPlayer(Token token) {
        if (token.isPlayerIdEmpty())
            throw new IllegalArgumentException("Player id is empty");

        validateRoomId(token.getRoomId());

        Room room = roomLocalRepository.getRoomById(token.getRoomId());

        room.addPlayer(new LobbyPlayer(token.getPlayerId()), room.getNPlayers() % 2);

        sendRoomPlayersUpdate(token.getRoomId(), new long[]{token.getPlayerId()});

        log.info("Player {} added to room {}", token.getPlayerId(), token.getRoomId());
    }

    public void rmvPlayer(Token token) {
        validateToken(token);
        Room room = roomLocalRepository.getRoomById(token.getRoomId());

        boolean changeHost = room.getHostId() == token.getPlayerId();
        room.removePlayer(token.getPlayerId());

        sendRoomPlayersUpdate(token.getRoomId(), null);

        if (room.isLobbyEmpty()) {
            roomLocalRepository.remove(token.getRoomId());
            userSocketHandler.sendUpdateRoomMsg();
        }
        else if (changeHost) {
            try {
                lobbySocketHandler.multicastMessage(lobbySocketHandler.getPlayersInRoom(room.getId(), -1),
                        PayloadBuilder.createJsonMessage(
                                LobbySocketHandler.Code.UPDATE_HOST,
                                null));
            } catch (IOException ignored) {}
        }

        sendRoomPlayersUpdate(token.getRoomId(), null);

        log.info("Player {} removed from room {}", token.getPlayerId(), token.getRoomId());
    }

    public void setPlayerReady(Token token, boolean ready) throws RuntimeException {
        validateToken(token);
        Room room = roomLocalRepository.getRoomById(token.getRoomId());

        if (room.isLobbyEmpty())
            return;
        LobbyPlayer player = (LobbyPlayer) room.getPlayer(token.getPlayerId());

        if (player.isReady() == ready)
            return;

        player.setReady(ready);

        log.info("Player {} in room {} is now {}", token.getPlayerId(), token.getRoomId(), ready ? "ready" : "not ready");

        sendRoomPlayersStateUpdate(room);

        if (checkIfGameCanStart(token.getRoomId())) {
            room.startGame();
            log.info("Game in room {} has started", token.getRoomId());

            try {
                lobbySocketHandler.multicastMessage(lobbySocketHandler.getPlayersInRoom(room.getId(), -1),
                        PayloadBuilder.createJsonMessage(
                                LobbySocketHandler.Code.START_GAME,
                                null));
            } catch (IOException ignored) {}
        }
    }

    public Card getBriscolaCard(long gameId) {
        validateRoomId(gameId);

        return roomLocalRepository.getRoomById(gameId).getBriscolaCard();
    }

    public Card[] getHand(Token token) {
        validateToken(token);

        return roomLocalRepository.getRoomById(token.getRoomId()).getHand(token.getPlayerId());
    }

    public long getTurnPlayerId(long gameId) {
        validateRoomId(gameId);

        return roomLocalRepository.getRoomById(gameId).getTurnPlayerId();
    }

    public void playCard(Token token, Card card) {
        validateToken(token);

        roomLocalRepository.getRoomById(token.getRoomId()).playCard(card, token.getPlayerId());
    }

    public Card[] getBoard(long gameId) {
        validateRoomId(gameId);

        return roomLocalRepository.getRoomById(gameId).getBoard();
    }

    public boolean isGameOver(long gameId) {
        validateRoomId(gameId);
        return roomLocalRepository.getRoomById(gameId).isGameEnded();
    }

    public List<Long> getWinner(long gameId) {
        validateRoomId(gameId);
        return roomLocalRepository.getRoomById(gameId).getWinner();
    }

    public int[] getPoints(long gameId) {
        validateRoomId(gameId);
        return roomLocalRepository.getRoomById(gameId).getPoints();
    }

    /**
     * Check if the game can start by controlling if all players of that room are ready.
     * @param gameId Id of the room to check.
     * @return True if all players are ready, false otherwise.
     */
    public boolean checkIfGameCanStart(long gameId) {
        validateRoomId(gameId);

        Room room = roomLocalRepository.getRoomById(gameId);

        return room.canGameStart();
    }

    /**
     * Check if the room exist.
     * @param roomId Id of the room to check.
     */
    private void validateRoomId(long roomId) {
        if (!roomLocalRepository.existsById(roomId))
            throw new IllegalArgumentException("Room with id: " + roomId + " does not exist");
    }

    /**
     * Check if the token is valid, controlling if is null, if it has null values and if the room exist.
     * @param token The token to check.
     */
    private void validateToken(Token token) {
        if (token == null)
            throw new IllegalArgumentException("Token is null");

        if (token.isEmpty())
            throw new IllegalArgumentException("Token is empty");

        validateRoomId(token.getRoomId());

        Room room = roomLocalRepository.getRoomById(token.getRoomId());
        if (room.getPlayer(token.getPlayerId()) == null)
            throw new IllegalArgumentException("Player with id: " + token.getPlayerId() + " does not exist");
    }

//    private long[] getRoomPlayersIds(long roomId) {
//        checkForRoomIdValidity(roomId);
//
//        return roomLocalRepository.getRoomById(roomId).getPlayersIds();
//    }

    private void sendRoomPlayersUpdate(long roomId, long[] playerIdToNotInclude) {
        if (playerIdToNotInclude == null)
            playerIdToNotInclude = new long[0];

        validateRoomId(roomId);

        ArrayList<Long> ids_temp = new ArrayList<>();
        for (Player player : roomLocalRepository.getRoomById(roomId).getPlayers()) {
            boolean canBeIncluded = true;
            for (long playerId : playerIdToNotInclude)
                if (player.getId() == playerId) {
                    canBeIncluded = false;
                    break;
                }
            if (canBeIncluded)
                ids_temp.add(player.getId());
        }

        Long[] ids = ids_temp.toArray(new Long[0]);

        try {
            lobbySocketHandler.multicastMessage(ids, PayloadBuilder.createJsonMessage(
                    LobbySocketHandler.Code.UPDATE_PLAYERS,
                    null)
            );
        } catch (IOException e) {
            roomLocalRepository.remove(roomId);

            throw new RuntimeException(e);
        }
    }

    private void sendRoomPlayersStateUpdate(Room r) {
        PayloadBuilder payload = new PayloadBuilder();

        Long[] ids = r.getPlayersIds();
        Long[] readyPlayersId = Arrays.stream(((LobbyPlayer[]) r.getPlayers())).filter(LobbyPlayer::isReady).map(LobbyPlayer::getId).toArray(Long[]::new);

        payload.addLongs("readyPlayersId", readyPlayersId);

        try {
            lobbySocketHandler.multicastMessage(ids, PayloadBuilder.createJsonMessage(
                    LobbySocketHandler.Code.UPDATE_STATES,
                    PayloadBuilder.createJsonPayload(payload.build())
            ));
        } catch (IOException e) {
//            roomLocalRepository.remove(r.getId());

            throw new RuntimeException(e);
        }
    }

    public List<Long> getPlayersIds(long roomId) {
        validateRoomId(roomId);

        ArrayList<Long> ids = new ArrayList<>();

        for (Player player : roomLocalRepository.getRoomById(roomId).getPlayers())
            ids.add(player.getId());

        return ids;
    }

    public Boolean getPlayerState(Token token) {
        validateToken(token);

        Room room = roomLocalRepository.getRoomById(token.getRoomId());
        if (room.getPlayer(token.getPlayerId()) instanceof LobbyPlayer p)
            return p.isReady();
        return null;
    }

    public long getHostId(long id) {
        validateRoomId(id);
        return roomLocalRepository.getRoomById(id).getHostId();
    }

    public void changeTeam(Token token, int team) {
        validateToken(token);
        if (!(team == 0 || team == 1))
            throw new IllegalArgumentException("Invalid team");

        roomLocalRepository.getRoomById(token.getRoomId()).changeTeam(token.getPlayerId(), team);
    }

    public List<Team> getTeams(long id) {
        validateRoomId(id);

        return roomLocalRepository.getRoomById(id).getTeamPlayersId();
    }

    public int[] getNCollectedCards(long id) {
        validateRoomId(id);

        return roomLocalRepository.getRoomById(id).getTotalCollectedCards();
    }

    public int getNRemainingCards(long id) {
        validateRoomId(id);

        return roomLocalRepository.getRoomById(id).getNRemainingCards();
    }

    public void deleteEmptyRoom(long roomId) {
        if (roomId == -1)
            return;

        validateRoomId(roomId);

        roomLocalRepository.remove(roomId);
    }

    public long findRoomId(long playerId) {
        for (Room r : roomLocalRepository.getAllRooms()) {
            Player p = r.getPlayer(playerId);
            if (p != null)
                return r.getId();
        }

        return -1;
    }

    public List<User> getPlayers(long roomId) {
        validateRoomId(roomId);

        Long[] ids = roomLocalRepository.getRoomById(roomId).getPlayersIds();

        List<User> users = userService.getUsers(
                Arrays.asList(ids));

        for (int i = 0; i < users.size(); i++) {
            if (!users.get(i).getId().equals(ids[i])) {
                int idxForSwap = users.indexOf(User.builder().id(ids[i]).build());

                User temp = users.get(idxForSwap);
                users.set(idxForSwap, users.get(i));
                users.set(i, temp);
            }
        }

        return users;
    }

    public List<User> getPlayersOrdered(long roomId) {
        validateRoomId(roomId);

        return userService.getUsers(
                Arrays.asList(roomLocalRepository
                        .getRoomById(roomId)
                        .getPlayersIds()));
    }

    public List<State> getPlayersState(long roomId) {
        validateRoomId(roomId);
        ArrayList<State> states = new ArrayList<>();

        for (Player player : roomLocalRepository.getRoomById(roomId).getPlayers())
            if (player instanceof LobbyPlayer p)
                states.add(new State(p.getId(), p.isReady()));

        return states;
    }
}
