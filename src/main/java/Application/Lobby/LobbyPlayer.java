package Application.Lobby;

import Application.Models.Player;

public class LobbyPlayer extends Player {
    private boolean isReady;

    public LobbyPlayer(long id) {
        super(id);
        isReady = false;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

    @Override
    public String toString() {
        return "LobbyPlayer{\n isReady= %s \n %s \n}".formatted(isReady, super.toString());
    }
}