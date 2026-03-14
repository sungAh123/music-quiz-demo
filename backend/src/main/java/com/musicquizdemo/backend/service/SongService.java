package com.musicquizdemo.backend.service;

import com.musicquizdemo.backend.domain.Song;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class SongService {
    private final ObjectMapper objectMapper;

    @Getter
    private List<Song> songs = new ArrayList<>();

    @PostConstruct
    public void init() {
        try {
            // songs.json 파일 읽어옴
            InputStream inputStream = new ClassPathResource("/data/songs.json").getInputStream();
            songs = objectMapper.readValue(inputStream, new TypeReference<List<Song>>() {});
            System.out.println("Loaded " + songs.size() + " songs");
        } catch (IOException e) {
            throw new RuntimeException("파일 불러오기 실패", e);
        }
    }

    // 게임 시작 시 랜덤 노래 선택
    public Song getRandomSong() {
        if (songs.isEmpty()) return null;
        Random random = new Random();
        return songs.get(random.nextInt(songs.size()));
    }
}
