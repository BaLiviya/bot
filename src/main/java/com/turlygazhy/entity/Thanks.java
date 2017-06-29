package com.turlygazhy.entity;

/**
 * Created by bal on 26.06.17.
 */
public class Thanks {

    private int id;
    private String text;
    private String date;
    private Long userId;
    private String voiceMessageId;
    private boolean hasAudio;

    public Thanks(){}
    public Thanks(int id) { this.id = id; }
    public Thanks(Long userId) {this.userId = userId; }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getVoiceMessageId() {
        return voiceMessageId;
    }

    public void setVoiceMessageId(String voiceMessageId) {
        this.voiceMessageId = voiceMessageId;
    }

    public boolean isHasAudio() {
        return hasAudio;
    }

    public void setHasAudio(boolean hasAudio) {
        this.hasAudio = hasAudio;
    }
}
