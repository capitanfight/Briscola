package com.briscola4legenDs.briscola.Room.REST;

import com.briscola4legenDs.briscola.Assets.PayloadBuilder;
import com.briscola4legenDs.briscola.Room.Room;
import com.briscola4legenDs.briscola.Room.Token;
import com.briscola4legenDs.briscola.Room.WebSocket.LobbySocketHandler;
import com.briscola4legenDs.briscola.Room.WebSocket.RoomSocketHandler;
import game.Card;
import game.Player;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

@Service
public class RoomService {
    private final RoomLocalRepository roomLocalRepository;
    private final RoomSocketHandler roomSocketHandler;
    private final LobbySocketHandler lobbySocketHandler;

    private static final Logger log = LoggerFactory.getLogger(RoomService.class.getName());

    @Autowired
    public RoomService(RoomLocalRepository roomLocalRepository, RoomSocketHandler roomSocketHandler, LobbySocketHandler lobbySocketHandler) {
        this.roomLocalRepository = roomLocalRepository;
        this.roomSocketHandler = roomSocketHandler;
        roomSocketHandler.setRoomService(this);
        this.lobbySocketHandler = lobbySocketHandler;
        lobbySocketHandler.setRoomService(this);
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

        return newRoom.getId();
    }

    public void addPlayer(Token token) {
        if (token.isPlayerIdEmpty())
            throw new IllegalArgumentException("Player id is empty");

        checkForRoomIdValidity(token.getRoomId());

        roomLocalRepository.getRoomById(token.getRoomId()).addPlayer(
                new Player(token.getPlayerId())
        );

        sendRoomPlayersUpdate(token.getRoomId());

        log.info("Player {} added to room {}", token.getPlayerId(), token.getRoomId());
    }

    public void rmvPlayer(Token token) {
        checkForTokenValidity(token);
        roomLocalRepository.getRoomById(token.getRoomId()).removePlayer(token.getPlayerId());

        if (roomLocalRepository.getRoomById(token.getRoomId()).isEmpty())
            roomLocalRepository.remove(token.getRoomId());

        sendRoomPlayersUpdate(token.getRoomId());

        log.info("Player {} removed from room {}", token.getPlayerId(), token.getRoomId());
    }

    public void rmvPlayer(long playerId) {
        Long roomId = null;
        for (Room r :roomLocalRepository.getAllRooms())
            for (Player p : r.getPlayers())
                if (p.getId() == playerId)
                    roomId = r.getId();

        if (roomId == null)
            throw new IllegalArgumentException("There is no room with a player that has this id: " + playerId);

        Token token = new Token(roomId, playerId);
        rmvPlayer(token);
    }

    public void setPlayerReady(Token token, boolean ready) throws RuntimeException {
        checkForTokenValidity(token);
        Room r = roomLocalRepository.getRoomById(token.getRoomId());

        if (r.getIsPlayerReady().get((r.getPlayerIdx(token.getPlayerId()))) == ready)
            return;

        r.setPlayerReady(r.getPlayerIdx(token.getPlayerId()), ready);

        log.info("Player {} in room {} is now {}", token.getPlayerId(), token.getRoomId(), ready ? "ready" : "not ready");

        sendRoomPlayersStateUpdate(r);

        if (checkIfGameCanStart(token.getRoomId())) {
            r.start();
            log.info("Game in room {} has started", token.getRoomId());
        }
    }

    public Card getBriscolaCard(long gameId) {
        checkForRoomIdValidity(gameId);

        return roomLocalRepository.getRoomById(gameId).getBriscolaCard();
    }

    public Card[] getHand(Token token) {
        checkForTokenValidity(token);

        return roomLocalRepository.getRoomById(token.getRoomId())
                .getPlayer(token.getPlayerId()).get()
                .getHand();
    }

    public long getTurnPlayerId(long gameId) {
        checkForRoomIdValidity(gameId);

        return roomLocalRepository.getRoomById(gameId).getTurnPlayerId();
    }

    public void playCard(long gameId, Card card) {
        checkForRoomIdValidity(gameId);

        roomLocalRepository.getRoomById(gameId).playCard(card);
    }

    public Card[] getBoard(long gameId) {
        checkForRoomIdValidity(gameId);

        return roomLocalRepository.getRoomById(gameId).getBoard();
    }

    public boolean isGameOver(long gameId) {
        checkForRoomIdValidity(gameId);
        return roomLocalRepository.getRoomById(gameId).isGameOver();
    }

    public long[] getWinner(long gameId) {
        checkForRoomIdValidity(gameId);
        return roomLocalRepository.getRoomById(gameId).getWinner();
    }

    public int[] getPoints(long gameId) {
        checkForRoomIdValidity(gameId);
        return roomLocalRepository.getRoomById(gameId).getPoints();
    }

    /**
     * Check if the game can start by controlling if all players of that room are ready.
     * @param gameId Id of the room to check.
     * @return True if all players are ready, false otherwise.
     */
    private boolean checkIfGameCanStart(long gameId) {
        checkForRoomIdValidity(gameId);

        Room room = roomLocalRepository.getRoomById(gameId);

        return room.areAllPlayersReady();
    }

    /**
     * Check if the room exist.
     * @param roomId Id of the room to check.
     */
    private void checkForRoomIdValidity(long roomId) {
        if (!roomLocalRepository.existsById(roomId))
            throw new IllegalArgumentException("Room with id: " + roomId + " does not exist");
    }

    /**
     * Check if the token is valid, controlling if is null, if it has null values and if the room exist.
     * @param token The token to check.
     */
    private void checkForTokenValidity(Token token) {
        if (token == null)
            throw new IllegalArgumentException("Token is null");

        if (token.isEmpty())
            throw new IllegalArgumentException("Token is empty");

        checkForRoomIdValidity(token.getRoomId());

        Room room = roomLocalRepository.getRoomById(token.getRoomId());
        if (!room.playerExists(token.getPlayerId()))
            throw new IllegalArgumentException("Player with id: " + token.getPlayerId() + " does not exist");
    }

    private Long[] getRoomPlayersIds(long roomId) {
        ArrayList<Long> ids = new ArrayList<>();
        for (Player player : roomLocalRepository.getRoomById(roomId).getPlayers())
            ids.add(player.getId());

        return ids.toArray(ids.toArray(new Long[0]));
    }

    private void sendRoomPlayersUpdate(long roomId) {
        PayloadBuilder payload = new PayloadBuilder();

        List<Long> ids_temp = new ArrayList<>(Arrays.stream(getRoomPlayersIds(roomId)).toList());
        ids_temp.remove(ids_temp.size() - 1);
        Long[] ids = ids_temp.toArray(new Long[0]);

        payload.addLongs("playersId", ids);

        try {
            lobbySocketHandler.multicastMessage(ids, PayloadBuilder.createJsonMessage(
                    RoomSocketHandler.Code.GET_PLAYERS_INSIDE,
                    PayloadBuilder.createJsonPayload(payload.build())
            ));
        } catch (IOException e) {
            roomLocalRepository.remove(roomId);

            throw new RuntimeException(e);
        }
    }

    private void sendRoomPlayersStateUpdate(Room r) {
        PayloadBuilder payload = new PayloadBuilder();

        Long[] ids = getRoomPlayersIds(r.getId());
        Boolean[] states = r.getIsPlayerReady().toArray(new Boolean[0]);

        ArrayList<Long> readyPlayersId = new ArrayList<>();

        for (int i = 0; i < ids.length; i++)
            if (states[i])
                readyPlayersId.add(ids[i]);

        payload.addLongs("readyPlayersId", readyPlayersId.toArray(new Long[0]));

        try {
            lobbySocketHandler.multicastMessage(ids, PayloadBuilder.createJsonMessage(
                    RoomSocketHandler.Code.GET_READY_PLAYERS,
                    PayloadBuilder.createJsonPayload(payload.build())
            ));
        } catch (IOException e) {
            roomLocalRepository.remove(r.getId());

            throw new RuntimeException(e);
        }
    }
}
