package com.briscola4legenDs.briscola.Room.WebSocket;

import Application.Models.Player;
import com.briscola4legenDs.briscola.Assets.PayloadBuilder;
import com.briscola4legenDs.briscola.Room.REST.RoomService;
import com.briscola4legenDs.briscola.Room.Token;
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
        UPDATE_PLAYERS,
        UPDATE_STATES,
        YOU_ARE_KICKED,
        UPDATE_HOST,
        UPDATE_TEAMS,
        START_GAME,
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        sessionsWithoutId.add(session);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        sessionsWithoutId.remove(session);
        sessions.remove(findId(session));
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) throws Exception {
        JSONObject messageJson = new JSONObject(message.getPayload());

        Code code = Code.valueOf(messageJson.getString("code"));
        JSONObject payload = messageJson.getJSONObject("payload");

        long roomId = payload.getLong("roomId");
        long playerId = payload.getLong("playerId");

        if (!rs.checkIfGameCanStart(roomId)) {
            switch (code) {
                case SET_ID -> {
                    sessionsWithoutId.remove(session);
                    sessions.put(playerId, session);
                }
                case UPDATE_PLAYERS ->
                        multicastMessage(getPlayersInRoom(roomId, playerId), PayloadBuilder.createJsonMessage(Code.UPDATE_PLAYERS, null));
                case UPDATE_STATES -> {
                    Long[] ids = getPlayersInRoom(roomId, playerId);
                    boolean canSendMessage = true;

                    for (Long id : ids) {
                        if (!sessionsWithoutId.contains(session) && !sessions.containsKey(id)) {
                            canSendMessage = false;
                            break;
                        }
                    }

                    if (canSendMessage)
                        multicastMessage(ids, PayloadBuilder.createJsonMessage(Code.UPDATE_STATES, null));
                }
                case YOU_ARE_KICKED ->
                        multicastMessage(new Long[]{roomId}, PayloadBuilder.createJsonMessage(Code.YOU_ARE_KICKED, null));
                case UPDATE_TEAMS ->
                        multicastMessage(getPlayersInRoom(roomId, -1), PayloadBuilder.createJsonMessage(Code.UPDATE_TEAMS, null));
            }
        } else
            sessions.remove(playerId);
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

    public Long[] getPlayersInRoom(long roomId, long playerToExclude) {
        ArrayList<Long> ids = new ArrayList<>();
        for (Player player: rs.getRoomById(roomId).getPlayers())
            if (player.getId() != playerToExclude)
                ids.add(player.getId());
        return ids.toArray(new Long[0]);
    }

    private void removePlayer(long playerId, long roomId) {
        rs.rmvPlayer(new Token(roomId, playerId));
        sessions.remove(playerId);
    }
}