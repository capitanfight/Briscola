package com.briscola4legenDs.briscola.Assets.WebSocket;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

public class ExcludeMulticastMessageStrategy implements MulticastMessageStrategy {
    @Override
    public void send(Long[] ids, String message, ConcurrentHashMap<Long, WebSocketSession> sessions) throws IOException {
        for (Enumeration<Long> iter = sessions.keys(); iter.hasMoreElements();) {
            long id = iter.nextElement();
            boolean isNotExcluded = true;

            if (!sessions.containsKey(id))
                throw new IOException("Session " + id + " not found");

            for (Long excludedId : ids)
                if (excludedId.equals(id)) {
                    isNotExcluded = false;
                    break;
                }

            WebSocketSession session = sessions.get(id);
            if (session.isOpen() && isNotExcluded)
                session.sendMessage(new TextMessage(message));
        }
    }
}
