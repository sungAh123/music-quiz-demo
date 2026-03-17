package com.musicquizdemo.backend.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class GameWebSocketHandler extends TextWebSocketHandler {

    // 클라이언트가 웹소켓 연결에 성공했을 때 실행
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("웹소켓 연결 성공, sessionId: " + session.getId());
        session.sendMessage(new TextMessage("서버: 웹소켓 연결"));
    }

    // 클라이언트가 메세지를 보냈을 때 실행
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("client message: " + payload);

        // 받은 메시지를 다시 클라이언트한테 보내줌(메아리)
        session.sendMessage(new TextMessage("메아리: " + payload));
    }

    // 클라이언트가 연결을 끊었을 때 실행
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("client connection established, sessionId: " + session.getId());
    }
}
