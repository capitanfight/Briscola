package com.briscola4legenDs.briscola.Room;

import Application.Game.Card;
import Application.Game.Game;
import Application.Game.GamePlayer;
import Application.Game.GameTeam;
import Application.GameException;
import Application.Lobby.Lobby;
import Application.Lobby.LobbyPlayer;
import Application.Lobby.LobbyTeam;
import Application.Models.Container;
import Application.Models.Player;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;

import java.util.Optional;

@Getter
public class Room {
    public enum Visibility {
        PUBLIC, PRIVATE
    }

    private static long idGenerator = 1;

    private final long id;

    @Enumerated(EnumType.STRING)
    private final Visibility visibility;
    private final String name;
    private long hostId;

    private Lobby lobby;
    private Game game;
    private long[] winner;
    private int[] points;

    @JsonIgnore
    private Player[] players;

    public Room(String name, Visibility visibility) {
        this.id = idGenerator++;
        lobby = new Lobby();

        this.name = name;
        this.visibility = visibility;
    }

    public void addPlayer(LobbyPlayer player, int team) {
        if (lobby == null)
            throw new GameException("Cannot add player because game is already started", GameException.Type.GAME_ALREADY_STARTED);
        if (lobby.isEmpty())
            hostId = player.getId();

        lobby.getTeam(team).addPlayer(player);
    }

    public void removePlayer(long id) {
        if (lobby == null)
            throw new GameException("Cannot remove player because game is already started", GameException.Type.GAME_ALREADY_STARTED);
        if (hostId == id)
            for (LobbyTeam team : lobby.getTeams())
                for (LobbyPlayer p : team.getPlayers())
                    if (p.getId() != id) {
                        hostId = p.getId();
                        break;
                    }

        LobbyPlayer player = null;
        int team = -1;
        for (int i = 0; i < lobby.getTeams().length; i++)
            for (LobbyPlayer p : lobby.getTeam(i).getPlayers())
                if (p.getId() == id) {
                    team = i;
                    player = p;
                    break;
                }

        lobby.getTeam(team).removePlayer(player);
    }

    @JsonIgnore
    public boolean canGameStart() {
        if (lobby != null)
            return lobby.canGameStart();
        return true;
    }

    public void startGame() {
        if (!canGameStart())
            throw new GameException("Cannot start game", GameException.Type.GAME_CANNOT_START);

        GameTeam[] teams = new GameTeam[] {
                new GameTeam(),
                new GameTeam(),
        };
        for (int i = 0; i < 2; i++)
            for (LobbyPlayer player : lobby.getTeam(i).getPlayers())
                teams[i].addPlayer(new GamePlayer(player.getId()));
        game = new Game(teams);
        game.start();

        lobby = null;
    }

    public void playCard(Card card, long playerId) {
        game.playCard(card, playerId);
    }

    @JsonIgnore
    public void changeTeam(long playerId, int team) {
        lobby.changeTeam(playerId, team);
    }

    public boolean isGameStarted() {
        return lobby == null && game != null;
    }

    public boolean isGameEnded() {
        return lobby == null && game == null;
    }

    @JsonIgnore
    public Player getPlayer(long playerId) {
        if (lobby != null)
            return lobby.getTeamByPlayerId(playerId).getPlayer(playerId);
        else if (game != null)
            return game.getTeamByPlayerId(playerId).getPlayer(playerId);
        return null;
    }

    @JsonIgnore
    public boolean isLobbyEmpty() {
        Lobby lobby = getLobby().orElseThrow(() -> new GameException("", GameException.Type.GAME_ALREADY_STARTED));
        return lobby.isEmpty();
    }

    @JsonIgnore
    private Optional<Lobby> getLobby() {
        return Optional.ofNullable(lobby);
    }

    @JsonIgnore
    private Optional<Game> getGame() {
        return Optional.ofNullable(game);
    }

    public int getNPlayers() {
        return lobby != null ? lobby.getNPlayers() : game.getNPlayers();
    }

    @JsonIgnore
    public Card getBriscolaCard() {
        return game.getBriscolaCard();
    }

    @JsonIgnore
    public long getTurnPlayerId() {
        return game.getTurnPlayerId();
    }

    @JsonIgnore
    public Card[] getHand(long playerId) {
        return game.getHand(playerId);
    }

    @JsonIgnore
    public Card[] getBoard() {
        return game.getBoard();
    }

    @JsonIgnore
    public Player[] getPlayers() {
        if (lobby != null)
            return lobby.getPlayers();
        else if (game != null)
            return game.getPlayers();
        return players;
    }

    @JsonIgnore
    public long[] getPlayersIds() {
        if (lobby != null)
            return lobby.getPlayersId();
        else if (game != null)
            return game.getPlayersId();
        return null;
    }

    @JsonIgnore
    public long[][] getTeamPlayersId() {
        if (lobby != null)
            return lobby.getTeamPlayersId();
        else if (game != null)
            return game.getTeamPlayersId();
        return null;
    }

    @JsonIgnore
    public int[] getTotalCollectedCards() {
        if (lobby != null)
            return new int[] {0, 0};
        else if (game != null)
            return game.getTotalCollectedCards();
        return null;
    }

    @JsonIgnore
    public void newTurn() {
        if (game != null)
            try {
                game.newTurn();
            } catch (GameException e) {
                if (e.getType() == GameException.Type.GAME_END) {
                    winner = game.getWinner();
                    points = game.getPoints();
                    players = game.getPlayers();
                    game = null;
                } else
                    throw e;
            }
    }

    public boolean shouldBeNewTurn() {
        if (game != null)
            return game.isShouldBeNewTurn();
        return false;
    }

    @JsonIgnore
    public int getNRemainingCards() {
        if (lobby != null)
            return 40;
        else if (game != null)
            return game.getNRemainingCards();
        return 0;
    }
}
