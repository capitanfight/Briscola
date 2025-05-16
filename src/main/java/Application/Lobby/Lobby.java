package Application.Lobby;

import Application.Game.GameTeam;
import Application.Models.Container;
import com.briscola4legenDs.briscola.Room.Team;

import java.util.ArrayList;
import java.util.List;

public class Lobby extends Container<LobbyTeam> {
    public Lobby() {
        teams = new LobbyTeam[] {
                new LobbyTeam(),
                new LobbyTeam()
        };
    }

    public boolean canGameStart() {
        return (getNPlayers() == 2 || getNPlayers() == 4) && teams[0].isReady() && teams[1].isReady();
    }

    public boolean isEmpty() {
        return teams[0].isEmpty() && teams[1].isEmpty();
    }

    public void changeTeam(long playerId, int team) {
        if (teams[team].hasPlayer(playerId))
            return;

        LobbyPlayer player = teams[0].hasPlayer(playerId) ? teams[0].getPlayer(playerId) : teams[1].getPlayer(playerId);

        teams[team].addPlayer(player);
        teams[(team + 1) % teams.length].removePlayer(player);
    }

    public Long[] getPlayersId() {
        Long[] ids = new Long[getNPlayers()];
        int playerN = 0;
        for (int i = 0; i < teams[0].getPlayers().size(); i++)
            for (LobbyTeam team : teams)
                if (playerN < getNPlayers())
                    ids[playerN++] = team.getPlayers().get(i).getId();
        return ids;
    }

    public List<Team> getTeamPlayersId() {
        return List.of(
                new Team(0, teams[0].getPlayerIds()),
                new Team(1, teams[1].getPlayerIds())
        );
    }

    public LobbyPlayer[] getPlayers() {
        ArrayList<LobbyPlayer> players = new ArrayList<>();
        for (LobbyTeam team : teams)
            players.addAll(team.getPlayers());
        return players.toArray(new LobbyPlayer[0]);
    }

    @Override
    public String toString() {
        return "Lobby{\n %s \n}".formatted(super.toString());
    }
}