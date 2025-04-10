package com.briscola4legenDs.briscola.User.Friends;

public class FriendException extends RuntimeException {
    public enum Type {
        USER_ID_NOT_FOUND,
        FRIEND_ALREADY_EXISTS,
        FRIEND_NOT_EXISTS,
    }

    private final Type type;

    public FriendException(String message, Type type) {
        super(message);
        this.type = type;
    }

    public Type getType() {
        return type;
    }
}
