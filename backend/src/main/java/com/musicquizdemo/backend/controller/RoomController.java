package com.musicquizdemo.backend.controller;

import com.musicquizdemo.backend.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;

    @PostMapping
    public Map<String, String> createRoom() {
        String roomId = roomService.createRoom();

        // {"roomId": "abc123"}
        Map<String, String> response = new HashMap<>();
        response.put("roomId", roomId);

        return response;
    }
}
