package com.briscola4legenDs.briscola.User.WebSocket;

import com.briscola4legenDs.briscola.Assets.WebSocket.MySocketHandler;
import com.briscola4legenDs.briscola.Assets.PayloadBuilder;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

@Component
public class UserSocketHandler extends MySocketHandler {
    public enum Code implements com.briscola4legenDs.briscola.Assets.Code {
        SET_ID,
        UPDATE_FRIEND_LIST,
        UPDATE_FRIEND_REQUESTS,
        UPDATE_ROOM_LIST,
        UPDATE_FRIEND_STATE,
        UPDATE_LIST_FRIEND_STATE,
        LIST_FRIEND_STATE_REQUEST,
        INVITED
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        super.afterConnectionClosed(session, status);

        try {
            broadcastMessage("{ \"code\": \"UPDATE_FRIEND_STATE\", \"payload\": { \"id\": %s, \"state\": false } }".formatted(findId(session)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JSONObject messageJson = new JSONObject(message.getPayload());

        Code code = Code.valueOf(messageJson.getString("code"));
        JSONObject payload = messageJson.getJSONObject("payload");

        long id = -1;
        if (payload.has("id"))
            id = payload.getLong("id");

        switch (code) {
            case SET_ID -> {
                register(id, session);
                broadcastMessage("{ \"code\": \"UPDATE_FRIEND_STATE\", \"payload\": { \"id\": %s, \"state\": true } }".formatted(id));
            }
            case INVITED ->
                unicastMessage(id, PayloadBuilder.createJsonMessage(
                        Code.INVITED,
                        PayloadBuilder.createJsonPayload(new PayloadBuilder()
                                .addString("username", payload.getString("senderUsername"))
                                .addLong("roomId", payload.getLong("roomId"))
                                .build())
                ));
            case LIST_FRIEND_STATE_REQUEST -> {
                ArrayList<Long> ids = new ArrayList<>();
                for (Enumeration<Long> e = sessions.keys(); e.hasMoreElements();)
                    ids.add(e.nextElement());

                String msg = "{ \"code\": \"UPDATE_LIST_FRIEND_STATE\", \"payload\": %s }".formatted(Arrays.toString(ids.toArray(Long[]::new)));

                unicastMessage(id, msg);
            }
        }
    }

    public void sendUpdateRoomMsg() {
        try {
            broadcastMessage(
                    PayloadBuilder.createJsonMessage(
                            Code.UPDATE_ROOM_LIST,
                            null));
        } catch (IOException ignored) {}
    }
}