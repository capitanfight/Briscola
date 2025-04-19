package com.briscola4legenDs.briscola.Room.WebSocket;

import com.briscola4legenDs.briscola.Room.REST.RoomService;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import org.json.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class RoomSocketHandler extends TextWebSocketHandler {
    private RoomService rs;

    private static final ConcurrentHashMap<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private static final CopyOnWriteArrayList<WebSocketSession> sessionsWithoutId = new CopyOnWriteArrayList<>();

    public enum Code implements com.briscola4legenDs.briscola.Assets.Code {
        SET_ID,
        GET_PLAYERS_INSIDE,
        GET_READY_PLAYERS,
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessionsWithoutId.add(session);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, CloseStatus status) {
        sessions.remove(findId(session));
        sessionsWithoutId.remove(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JSONObject messageJson = new JSONObject(message.getPayload());

        Code code = Code.valueOf(messageJson.getString("code"));
        JSONObject payload = messageJson.getJSONObject("payload");

        switch (code) {
            case SET_ID -> {
                sessionsWithoutId.remove(session);
                sessions.put(payload.getLong("id"), session);
            }
        }
    }

    public void multicastMessage(Long[] ids, String message) throws IOException {
        for (long id : ids) {
            if (!sessions.containsKey(id))
                throw new IOException("Session " + id + " not found");

            WebSocketSession session = sessions.get(id);
            if (session.isOpen())
                session.sendMessage(new TextMessage(message));
        }
    }

    public void broadcastMessage(String message) throws IOException {
        for (WebSocketSession session : sessions.values())
            if (session.isOpen())
                session.sendMessage(new TextMessage(message));
    }

    public void setRoomService(RoomService rs) {
        this.rs = rs;
    }

    private long getId(WebSocketSession session) {
        long id = -1;
        for (Map.Entry<Long, WebSocketSession> set : sessions.entrySet())
            if (set.getValue().getId().equals(session.getId()))
                id = set.getKey();
        return id;
    }

    private long findId(WebSocketSession session) {
        for (Map.Entry<Long, WebSocketSession> entry : sessions.entrySet())
            if (entry.getValue().equals(session))
                return entry.getKey();
        return -1;
    }
}
