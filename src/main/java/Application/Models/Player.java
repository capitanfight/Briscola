package Application.Models;

import Application.GameException;

public abstract class Player {
    protected final long id;

    public Player(long id) {
        if (id <= 0)
            throw new GameException("id is not valid", GameException.Type.INVALID_ID);
        this.id = id;
    }

    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        return " id=" + id;
    }
}
