package com.briscola4legenDs.briscola.Room;

public class Token {
    private final Long roomId;
    private final Long playerId;

    public Token(Long roomId, Long playerId) {
        this.roomId = roomId;
        this.playerId = playerId;
    }

    public boolean isEmpty() {
        return roomId == null || playerId == null;
    }

    public boolean isRoomIdEmpty() {
        return roomId == null;
    }

    public boolean isPlayerIdEmpty() {
        return playerId == null;
    }

    public Long getRoomId() {
        return roomId;
    }

    public Long getPlayerId() {
        return playerId;
    }

    @Override
    public String toString() {
        return "{" +
                "roomId=" + roomId +
                ", playerId=" + playerId +
                '}';
    }
}
