package Application.Models;

import Application.GameException;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public abstract class Team<PlayerClass extends Player> {
    protected final CopyOnWriteArrayList<PlayerClass> players;

    public Team() {
        players = new CopyOnWriteArrayList<>();
    }

    public void addPlayer(PlayerClass player) {
        if (player == null)
            throw new GameException("Player cannot be null", GameException.Type.CANNOT_BE_NULL);
        if (players.contains(player))
            throw new GameException("Player is already in the stack", GameException.Type.ALREADY_CONTAINED);

        players.add(player);
    }

    public void removePlayer(PlayerClass player) {
        players.remove(player);
    }

    public boolean hasPlayer(long playerId) {
        return players.stream().anyMatch(p -> p.getId() == playerId);
    }

    public boolean isEmpty() {
        return players.isEmpty();
    }

    public int getNumPlayers() {
        return players.size();
    }

    public CopyOnWriteArrayList<PlayerClass> getPlayers() {
        return players;
    }

    public PlayerClass getPlayer(long playerId) {
        return players.stream().filter(p -> p.getId() == playerId).findFirst().orElse(null);
    }

    public PlayerClass getPlayer(int idx) {
        return players.get(idx);
    }

    public List<Long> getPlayerIds() {
        return players.stream().map(PlayerClass::getId).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return " players=" + players;
    }
}
