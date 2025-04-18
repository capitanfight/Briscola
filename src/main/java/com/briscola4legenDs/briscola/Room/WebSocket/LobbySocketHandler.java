package com.briscola4legenDs.briscola.Room.WebSocket;

import com.briscola4legenDs.briscola.Assets.PayloadBuilder;
import com.briscola4legenDs.briscola.Room.REST.RoomService;
import game.Player;
import lombok.NonNull;
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
public class LobbySocketHandler extends TextWebSocketHandler {
    private static RoomService rs;
    
    private static final ConcurrentHashMap<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private static final CopyOnWriteArrayList<WebSocketSession> sessionsWithoutId = new CopyOnWriteArrayList<>();

    public enum Code implements com.briscola4legenDs.briscola.Assets.Code {
        SET_ID,
        GET_PLAYERS_INSIDE,
        GET_READY_PLAYERS,
        REMOVE_PLAYER,
        YOU_ARE_KICKED,
        NEW_HOST
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        sessionsWithoutId.add(session);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
//        if (!sessions.contains(session))
        sessionsWithoutId.remove(session);
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) throws Exception {
        JSONObject messageJson = new JSONObject(message.getPayload());

        Code code = Code.valueOf(messageJson.getString("code"));
        JSONObject payload = messageJson.getJSONObject("payload");

        switch (code) {
            case SET_ID -> {
                sessionsWithoutId.remove(session);
                sessions.put(payload.getLong("id"), session);
            }
            case GET_PLAYERS_INSIDE -> multicastMessage(getPlayersInRoom(payload.getLong("roomId")), PayloadBuilder.createJsonMessage(Code.GET_PLAYERS_INSIDE, null));
            case GET_READY_PLAYERS -> multicastMessage(getPlayersInRoom(payload.getLong("roomId")), PayloadBuilder.createJsonMessage(Code.GET_READY_PLAYERS, null));
            case REMOVE_PLAYER -> removePlayer(payload.getLong("playerId"));
            case YOU_ARE_KICKED -> multicastMessage(new Long[]{payload.getLong("playerId")}, PayloadBuilder.createJsonMessage(Code.YOU_ARE_KICKED, null));
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
        LobbySocketHandler.rs = rs;
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

    private void removePlayer(long id) {
        rs.rmvPlayer(id);
        sessions.remove(id);
    }
}