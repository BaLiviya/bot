package com.turlygazhy.command.impl;

import com.turlygazhy.Bot;
import com.turlygazhy.command.Command;
import com.turlygazhy.entity.Thanks;
import com.turlygazhy.entity.WaitingType;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by bal on 26.06.17.
 */
public class AddNewThanksCommand extends Command {

    private Thanks thanks;

    public AddNewThanksCommand() throws SQLException{}

    @Override
    public boolean execute(Update update, Bot bot) throws SQLException, TelegramApiException {
        if (waitingType == null) {
            return addNewThanks();
        }
        switch (waitingType) {
            case THANKS_TEXT:
                if (updateMessageText.equals(buttonDao.getButtonText(10))) {
                    sendMessage(5, chatId, bot);
                    return true;
                }
                return setThanksText(bot);
        }
        return false;
    }

    private boolean addNewThanks() throws SQLException, TelegramApiException {
        waitingType = WaitingType.THANKS_TEXT;
        String messageText = messageDao.getMessageText(124);
        sendMessage(messageText);
        thanks = new Thanks(chatId);
        return false;
    }

    private boolean setThanksText(Bot bot) throws SQLException, TelegramApiException {
        if (updateMessage.getVoice() == null) {
            thanks.setText(updateMessageText);
            thanks.setHasAudio(false);
        } else {
            thanks.setVoiceMessageId(updateMessage.getVoice().getFileId());
            thanks.setHasAudio(true);
        }
        sendMessage(125, chatId, bot);
        waitingType = null;
        thanks.setDate(getTime());
        thanksDao.insertThanks(thanks);
        return true;
    }

    private String getTime() {
        String result;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM");
        return result = "" + dateFormat.format(new Date());
    }
}
