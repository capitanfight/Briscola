package com.briscola4legenDs.briscola.Configs;

import com.briscola4legenDs.briscola.Room.WebSocket.LobbySocketHandler;
import com.briscola4legenDs.briscola.Room.WebSocket.RoomSocketHandler;
import com.briscola4legenDs.briscola.User.WebSocket.UserSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new RoomSocketHandler(), "/ws/room").setAllowedOrigins("*");
        registry.addHandler(new LobbySocketHandler(), "/ws/lobby").setAllowedOrigins("*");
        registry.addHandler(new UserSocketHandler(), "/ws/user").setAllowedOrigins("*");
    }
}