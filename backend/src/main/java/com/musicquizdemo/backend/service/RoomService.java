// RoomRepository로 Redis에 저장
package com.musicquizdemo.backend.service;

import com.musicquizdemo.backend.domain.Room;
import com.musicquizdemo.backend.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    public String createRoom() {
        // 랜덤 방 번호 생성
        String roomId = UUID.randomUUID().toString().substring(0, 8);

        // 방 객체 생성
        Room room = new Room(roomId);

        // Redis에 방 정보 저장
        roomRepository.saveRoom(room);

        return roomId;
    }
}
