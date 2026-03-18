package com.musicquizdemo.backend.websocket;

import com.musicquizdemo.backend.domain.Room;
import com.musicquizdemo.backend.domain.Song;
import com.musicquizdemo.backend.repository.RoomRepository;
import com.musicquizdemo.backend.service.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class GameWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final RoomRepository roomRepository;
    private final SongService songService;

    // 방 번호(roomId) 별로 접속해 있는 웹소켓 세션들을 관리
    private final Map<String, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    // 클라이언트가 웹소켓 연결에 성공했을 때 실행, 연결 설정 후 로직
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("웹소켓 연결 성공, sessionId: " + session.getId());
        session.sendMessage(new TextMessage("서버: 웹소켓 연결"));
    }

    // 클라이언트가 메세지를 보냈을 때 실행, 메시지 처리 로직
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 클라이언트가 보낸 JSON 메시지 읽어옴
        JsonNode jsonMessage = objectMapper.readTree(message.getPayload());
        String type = jsonMessage.get("type").asText();
        String roomId = jsonMessage.has("roomId") ? jsonMessage.get("roomId").asText() : null;

        if (roomId == null) return;

        Room room = roomRepository.getRoom(roomId);
        if (room == null) return;   // 없는 방이면 무시

        // 메시지 타입에 따라 행동
        switch (type) {
            case "JOIN":
                // 방 명부에 세션(유저) 추가
                roomSessions.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(session);
                String nickname = jsonMessage.get("nickname").asText();
                room.addPlayer(nickname);   // 방에 유저 추가(초기 점수 0)

                broadcastScore(roomId, room);
                break;

            case "START_GAME":
            case "NEXT_ROUND":
                Song song = songService.getRandomSong();
                room.setCourrentSong(song);
                room.setAnswered(false);    // 아무도 맞추지 않은 상태로 초기화

                // 유튜브 음악 재생 정보 모두에게 전송
                Map<String, Object> musicMsg = new HashMap<>();
                musicMsg.put("type", "MUSIC_START");
                musicMsg.put("youtubeId", song.getYoutubeId());
                musicMsg.put("start", song.getStart());
                broadcastToRoom(roomId, musicMsg);
                break;

            case "ANSWER":
                // 이미 맞춘 문제 or 문제가 나오지 않은 상태면 무시
                if (room.isAnswered() || room.getCourrentSong() == null) break;

                String answer = jsonMessage.get("answer").asText();
                String nick = jsonMessage.get("nickname").asText();

                // 정답 비교 (띄어쓰기, 대소문자 무시)
                String actualTitle = room.getCourrentSong().getTitle().replaceAll("\\s+", "").toLowerCase();
                String userAnswer = answer.replaceAll("\\s+", "").toLowerCase();

                // 정답자 점수 증가
                if (actualTitle.equals(userAnswer)) {
                    room.setAnswered(true); // 중복 정답 방지
                    room.incrementScore(nick);  // 정답자 점수 +1

                    Map<String, Object> correctMsg = new HashMap<>();
                    correctMsg.put("type", "CORRECT");
                    correctMsg.put("nickname", nick);
                    correctMsg.put("answer", room.getCourrentSong().getTitle());
                    broadcastToRoom(roomId, correctMsg);

                    // 점수판 업데이트
                    broadcastScore(roomId, room);
                }

        }
    }

    // 클라이언트가 연결을 끊었을 때 실행, 연결 종료 후 로직
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        roomSessions.values().forEach(sessions -> sessions.remove(session));
    }

    // ---- Broadcast 용 헬퍼 메서드 ----

    // 특정 방에 있는 모두에게 메시지 전달
    private void broadcastToRoom(String roomId, Object messageObj) throws IOException {
        Set<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions != null) {
            String messageJson = objectMapper.writeValueAsString(messageObj);
            TextMessage textMessage = new TextMessage(messageJson);
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                }
            }
        }
    }

    // 현재 방의 점수판 모두에게 전달
    private void broadcastScore(String roomId, Room room) throws IOException {
        Map<String, Object> scoreMsg = new HashMap<>();
        scoreMsg.put("type", "SCORE_UPDATE");
        scoreMsg.put("scores", room.getPlayers());
        broadcastToRoom(roomId, scoreMsg);
    }
}
