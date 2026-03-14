// Redis에 Room 데이터 저장하고 불러옴
package com.musicquizdemo.backend.repository;

import com.musicquizdemo.backend.domain.Room;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@RequiredArgsConstructor
public class RoomRepository {
    private final Map<String, Room> roomStore = new ConcurrentHashMap<>();

    // 방 정보 저장
    public void saveRoom(Room room) {
        roomStore.put(room.getRoomId(), room);
    }

    // 방 번호로 방 정보 불러오기
    public Room getRoom(String roomId) {
        return roomStore.get(roomId);
    }
}
