package com.briscola4legenDs.briscola.Room.WebSocket;

import jakarta.persistence.Entity;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class RoomSocketHandler extends TextWebSocketHandler {
    private static final ConcurrentHashMap<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private static final CopyOnWriteArrayList<WebSocketSession> sessionsWithoutId = new CopyOnWriteArrayList<>();

    enum Code {
        SET_ID
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessionsWithoutId.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        if (sessions.contains(session))
            sessions.remove(findId(session));
        else if (sessionsWithoutId.contains(session))
            sessionsWithoutId.remove(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // code;payload

        Code code = Code.values()[Integer.parseInt(message.getPayload().split(";")[0])];
        String payload = message.getPayload().split(";")[1];

        switch (code) {
            case SET_ID -> {
                sessionsWithoutId.remove(session);
                sessions.put(Long.parseLong(payload), session);
            }

        }
    }

    public void broadcastMessage(String message) throws IOException {
        for (WebSocketSession session : sessions.values())
            if (session.isOpen())
                session.sendMessage(new TextMessage(message));
    }

    private long findId(WebSocketSession session) {
        for (Map.Entry<Long, WebSocketSession> entry : sessions.entrySet())
            if (entry.getValue().equals(session))
                return entry.getKey();
        return -1;
    }
}
