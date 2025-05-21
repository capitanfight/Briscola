package com.briscola4legenDs.briscola.Assets.WebSocket;

import com.briscola4legenDs.briscola.Room.WebSocket.RoomSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class MySocketHandler extends TextWebSocketHandler {
    protected final ConcurrentHashMap<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();
    protected final CopyOnWriteArrayList<WebSocketSession> sessionsWithoutId = new CopyOnWriteArrayList<>();

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessionsWithoutId.add(session);

        log.info("{}: Connection established for session: {}", getClassName(), session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        long id = findId(session);

        sessions.remove(id);
        sessionsWithoutId.remove(session);

        log.info("RoomSocketHandler: Connection closed for session: {} ({})", session.getId(), (id == -1 ? "" : "was registered with id: %s".formatted(id)));
    }

    public void unicastMessage(Long id, String message) throws IOException {
        if (!sessions.containsKey(id))
            throw new IOException("Session " + id + " not found");

        WebSocketSession session = sessions.get(id);

        if (session.isOpen())
            session.sendMessage(new TextMessage(message));
    }

    public void multicastMessage(Long[] ids, String message, MulticastMessageStrategy strategy) throws IOException {
        strategy.send(ids, message, sessions);
    }

    public void broadcastMessage(String message) throws IOException {
        for (WebSocketSession session : sessions.values())
            if (session.isOpen())
                session.sendMessage(new TextMessage(message));
    }

    protected long findId(WebSocketSession session) {
        for (Map.Entry<Long, WebSocketSession> entry : sessions.entrySet())
            if (entry.getValue().equals(session))
                return entry.getKey();
        return -1;
    }

    protected void register(long id, WebSocketSession session) {
        sessionsWithoutId.remove(session);
        sessions.put(id, session);

        log.info("RoomSocketHandler: Session {} registered with id: {}", session.getId(), id);
    }

    protected final String getClassName() {
        return this.getClass().getSimpleName();
    }
}
