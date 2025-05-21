package com.briscola4legenDs.briscola.Assets.WebSocket;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class IncludeMulticastMessageStrategy implements MulticastMessageStrategy {
    @Override
    public void send(Long[] ids, String message, ConcurrentHashMap<Long, WebSocketSession> sessions) throws IOException{
        for (long id : ids) {
            if (!sessions.containsKey(id))
                throw new IOException("Session " + id + " not found");

            WebSocketSession session = sessions.get(id);
            if (session.isOpen())
                session.sendMessage(new TextMessage(message));
        }
    }
}
