package com.briscola4legenDs.briscola.Room.WebSocket;

import Application.GameException;
import Application.Models.Player;
import com.briscola4legenDs.briscola.Assets.PayloadBuilder;
import com.briscola4legenDs.briscola.Room.REST.RoomService;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import org.json.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class RoomSocketHandler extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(RoomSocketHandler.class.getName());

    private static RoomService rs;

    private static final ConcurrentHashMap<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private static final CopyOnWriteArrayList<WebSocketSession> sessionsWithoutId = new CopyOnWriteArrayList<>();

    public enum Code implements com.briscola4legenDs.briscola.Assets.Code {
        SET_ID,
        UPDATE,
        UPDATE_MID_TURN,
        UPDATE_END_TURN,
        END_GAME
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessionsWithoutId.add(session);
        log.info("RoomSocketHandler: Connection established for session: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, CloseStatus status) {
        long id = findId(session);

        sessions.remove(id);
        sessionsWithoutId.remove(session);

        log.info("RoomSocketHandler: Connection closed for session: {} ({})", session.getId(), (id == -1 ? "" : "was registered with id: %s".formatted(id)));

        if (sessionsWithoutId.isEmpty() && sessions.isEmpty())
            rs.deleteEmptyRoom(rs.findRoomId(id));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JSONObject messageJson = new JSONObject(message.getPayload());

        Code code = Code.valueOf(messageJson.getString("code"));
        JSONObject payload = messageJson.getJSONObject("payload");

        switch (code) {
            case SET_ID -> {
                long id = payload.getLong("id");

                sessionsWithoutId.remove(session);
                sessions.put(id, session);

                log.info("RoomSocketHandler: Session {} registered with id: {}", session.getId(), payload.getLong("id"));
            }
            case UPDATE -> {
                long roomId = payload.getLong("roomId");
                if (!rs.getRoomById(roomId).shouldBeNewTurn())
                    multicastMessage(getPlayersInRoom(roomId), PayloadBuilder.createJsonMessage(Code.UPDATE_MID_TURN,
                            PayloadBuilder.createJsonPayload(new PayloadBuilder().addString("updateTurn", "true").build())));
                else {
                    multicastMessage(getPlayersInRoom(roomId), PayloadBuilder.createJsonMessage(Code.UPDATE_MID_TURN,
                            PayloadBuilder.createJsonPayload(new PayloadBuilder().addString("updateTurn", "false").build())));
                    // Thread.sleep(2000);
                    try {
                        rs.getRoomById(roomId).newTurn();
                        multicastMessage(getPlayersInRoom(roomId), PayloadBuilder.createJsonMessage(Code.UPDATE_END_TURN, null));
                    } catch (GameException e) {
                        if (!e.getType().equals(GameException.Type.GAME_END))
                            throw e;
                        multicastMessage(getPlayersInRoom(roomId), PayloadBuilder.createJsonMessage(Code.UPDATE_END_TURN, null));
                        multicastMessage(getPlayersInRoom(roomId), PayloadBuilder.createJsonMessage(Code.END_GAME, null));
                    }
                }
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
        RoomSocketHandler.rs = rs;
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

    public Long[] getPlayersInRoom(long roomId) {
        ArrayList<Long> ids = new ArrayList<>();
        for (Player player: rs.getRoomById(roomId).getPlayers())
            ids.add(player.getId());
        return ids.toArray(new Long[0]);
    }
}
