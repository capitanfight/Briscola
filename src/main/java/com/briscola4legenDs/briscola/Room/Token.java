package com.briscola4legenDs.briscola.Room;

import java.util.Objects;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Token token)) return false;
        return Objects.equals(getRoomId(), token.getRoomId()) && Objects.equals(getPlayerId(), token.getPlayerId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRoomId(), getPlayerId());
    }

    @Override
    public String toString() {
        return "{" +
                "roomId=" + roomId +
                ", playerId=" + playerId +
                '}';
    }
}
