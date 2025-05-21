package com.briscola4legenDs.briscola.Assets.WebSocket;

import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public interface MulticastMessageStrategy {
    void send(Long[] ids, String message, ConcurrentHashMap<Long, WebSocketSession> sessions) throws IOException;
}
