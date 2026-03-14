package com.musicquizdemo.backend.domain;

import lombok.Data;

@Data
public class Song {
    private String title;
    private String artist;
    private String youtubeId;
    private int start;
}
