package com.musicquizdemo.backend.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@NoArgsConstructor  // 파라미터 없는 디폴트 연산자
public class Room {
    private String roomId;

    // 유저 닉네임과 점수를 매핑 (멀티스레드 환경을 고려해 ConcurrentHashMap 사용)
    private Map<String, Integer> players = new ConcurrentHashMap<>();

    private Song courrentSong;
    private boolean answered;   // 정답 맞춘 사람

    // 방을 처음 생성할 때 사용할 생성자
    public Room(String roomId) {
        this.roomId = roomId;
        this.answered = false;
    }

    // 유저 입장 시 점수 0점으로 세팅
    public void addPlayer(String nickname) {
        players.putIfAbsent(nickname, 0);
    }

    // 정답을 맞췄을 때 점수 1점 증가
    public void incrementScore(String nickname) {
        players.computeIfPresent(nickname, (k, score) -> score + 1);
    }

}

