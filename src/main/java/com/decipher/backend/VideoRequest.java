package com.decipher.backend;

public class VideoRequest {
    private String videoUrl;
    private String question;

    public VideoRequest() {}

    public VideoRequest(String videoUrl, String question) {
        this.videoUrl = videoUrl;
        this.question = question;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}