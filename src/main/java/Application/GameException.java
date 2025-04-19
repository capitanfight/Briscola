package Application;

public class GameException extends RuntimeException {
    public enum Type {
        INVALID_ID,
        CANNOT_BE_NULL,
        CARD_NOT_FOUND,
        PLAYER_NOT_FOUND,
        ALREADY_FULL,
        ALREADY_CONTAINED,
        WRONG_VALUE,
        GAME_CANNOT_START,
        GAME_ALREADY_STARTED,
        NOT_YOUR_TURN,
        GAME_END,
    }

    private final Type type;
    public GameException(String msg, Type type) {
        super(msg);
        this.type = type;
    }

    public Type getType() {
        return type;
    }
}
