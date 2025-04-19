package Application.Models;

import Application.Game.GameTeam;

import java.util.Arrays;

public abstract class Container<TeamClass extends Team> {
    protected TeamClass[] teams;

    public TeamClass[] getTeams() {
        return teams;
    }

    public TeamClass getTeam(int idx) {
        return teams[idx];
    }

    public int getNPlayers() {
        return teams[0].getNumPlayers() + teams[1].getNumPlayers();
    }

    public TeamClass getTeamByPlayerId(long playerId) {
        return teams[0].hasPlayer(playerId) ? teams[0] : teams[1];
    }

    @Override
    public String toString() {
        return " teams=" + Arrays.toString(teams);
    }
}
