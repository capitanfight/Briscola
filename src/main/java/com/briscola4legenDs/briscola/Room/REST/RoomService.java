package com.briscola4legenDs.briscola.Room.REST;

import com.briscola4legenDs.briscola.Room.Room;
import com.briscola4legenDs.briscola.Room.Token;
import game.Card;
import game.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class RoomService {
    private final RoomLocalRepository roomLocalRepository;

    @Autowired
    public RoomService(RoomLocalRepository roomLocalRepository) {
        this.roomLocalRepository = roomLocalRepository;
    }

    public Collection<Room> getAllRooms() {
        return roomLocalRepository.getAll();
    }

    public void rmvRooms() {
        for (Room room : getAllRooms())
            roomLocalRepository.remove(room.getId());
    }

    public Room getRoomById(long roomId) {
        return roomLocalRepository.getRoomById(roomId);
    }

    public String getName(long roomId) {
        return roomLocalRepository.getRoomById(roomId).getName();
    }

    public long createRoom(String name) {
        Room newRoom = new Room(name);
        roomLocalRepository.add(newRoom);
        return newRoom.getId();
    }

    public void addPlayer(Token token) {
        if (token.isPlayerIdEmpty())
            throw new IllegalArgumentException("Player id is empty");

        checkForRoomIdValidity(token.getRoomId());

        roomLocalRepository.getRoomById(token.getRoomId()).addPlayer(
                new Player(token.getPlayerId())
        );
    }

    public void rmvPlayer(Token token) {
        checkForTokenValidity(token);
        roomLocalRepository.getRoomById(token.getRoomId()).removePlayer(token.getPlayerId());

        if (roomLocalRepository.getRoomById(token.getRoomId()).isEmpty())
            roomLocalRepository.remove(token.getRoomId());
    }

    public void setPlayerReady(Token token, boolean ready) throws RuntimeException {
        checkForTokenValidity(token);
        Room r = roomLocalRepository.getRoomById(token.getRoomId());

        r.setPlayerReady(r.getPlayerIdx(token.getPlayerId()), ready);

        if (checkIfGameCanStart(token.getRoomId()))
            r.start();
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

    private boolean checkIfGameCanStart(long gameId) {
        checkForRoomIdValidity(gameId);

        Room room = roomLocalRepository.getRoomById(gameId);

        return room.areAllPlayersReady();
    }

    private void checkForRoomIdValidity(long roomId) {
        if (!roomLocalRepository.existsById(roomId))
            throw new IllegalArgumentException("Room with id: " + roomId + " does not exist");
    }

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
}
