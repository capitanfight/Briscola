package com.briscola4legenDs.briscola.User;

public class UserException extends RuntimeException {
    public enum Type {
        UsernameAlreadyTaken,
        EmailAlreadyTaken,
    }

    private final Type type;

    public UserException(String message, Type type) {
        super(message);
        this.type = type;
    }

    public Type getType() {
        return type;
    }
}
