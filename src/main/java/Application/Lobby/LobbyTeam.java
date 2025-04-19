package Application.Lobby;

import Application.Models.Team;

public class LobbyTeam extends Team<LobbyPlayer> {
    public boolean isReady() {
        if (players.isEmpty())
            return false;
        for (LobbyPlayer player: players)
            if (!player.isReady())
                return false;
        return true;
    }

    @Override
    public String toString() {
        return "LobbyTeam{\n %s \n}".formatted(super.toString());
    }
}
