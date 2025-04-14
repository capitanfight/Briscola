package com.briscola4legenDs.briscola.Room;

import com.briscola4legenDs.briscola.Room.WebSocket.RoomSocketHandler;
import game.Game;
import game.Player;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.util.concurrent.CopyOnWriteArrayList;

public class Room extends Game {
    public enum Visibility {
        PUBLIC, PRIVATE
    }

    private static long idGen = 1;

    private final RoomSocketHandler roomSocketHandler;

    private final String name;
    @Enumerated(EnumType.STRING)
    private final Visibility visibility;
    private final CopyOnWriteArrayList<Boolean> isPlayerReady;

    public Room(String name, Visibility visibility) {
        super(idGen++);
        if (name == null || name.isEmpty())
            throw new NullPointerException("Room name is null or empty");
        this.name = name;

        isPlayerReady = new CopyOnWriteArrayList<>();
        roomSocketHandler = new RoomSocketHandler();

        this.visibility = visibility;
    }

    public void setPlayerReady(int idx, boolean isPlayerReady) {
        this.isPlayerReady.set(idx, isPlayerReady);
    }

    public boolean areAllPlayersReady() throws IllegalArgumentException{
        boolean areAllPlayersReady = true;
        for (boolean b : isPlayerReady)
            areAllPlayersReady = areAllPlayersReady && b;
        return areAllPlayersReady;
    }

    public void addPlayer(Player player) throws IllegalArgumentException {
        super.addPlayer(player);
        isPlayerReady.add(false);
    }

    public void removePlayer(Long id) {
        int idx = super.getPlayerIdx(id);

        super.removePlayer(id);
        isPlayerReady.remove(idx);
    }

//    public long[] getPlayersIds() {
//        CopyOnWriteArrayList<Player> players = getPlayers();
//        long[] ids = new long[players.size()];
//
//        for (int i = 0; i < players.size(); i++)
//            ids[i] = players.get(i).getId();
//
//        return ids;
//    }

    public CopyOnWriteArrayList<Boolean> getIsPlayerReady() {
        return isPlayerReady;
    }

    public String getName() {
        return name;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    @Override
    public String toString() {
        return "Room{" + '\n' +
                "name='" + name + "'\n" +
                "isPlayerReady=" + isPlayerReady + "\n\n" +
                super.toString() + "\n}";
    }
}
