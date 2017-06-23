package com.turlygazhy.entity;

import com.turlygazhy.dao.DaoFactory;
import com.turlygazhy.dao.impl.MessageDao;

import java.sql.SQLException;
import java.util.Objects;

/**
 * Created by bal on 22.06.17.
 */
public class Thanks {

    private int id;
    private String text;
    private String dateOfCompletion;
    private Long userId;
    private Long addedByUserId;
    private String voiceMessageId;
    private boolean hasAudio;

    public Thanks() {
    }

    public Thanks(int id) {
        this.id = id;
    }

    public Thanks(Long addedByUserId) {
        this.addedByUserId = addedByUserId;
    }

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

    public String getDateOfCompletion() {
        return dateOfCompletion;
    }

    public void setDateOfCompletion(String dateOfCompletion) {
        this.dateOfCompletion = dateOfCompletion;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getAddedByUserId() {
        return addedByUserId;
    }

    public void setAddedByUserId(Long addedByUserId) {
        this.addedByUserId = addedByUserId;
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        MessageDao messageDao = DaoFactory.getFactory().getMessageDao();
        try {
            User user = DaoFactory.getFactory().getUserDao().getUserByChatId(this.userId);
            if (Objects.equals(user.getChatId(), this.getUserId())) {
                if (!this.isHasAudio()) {
                    sb.append("<b>").append(messageDao.getMessageText(122)).append("</b>\n").append(this.getText()).append("\n\n");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
