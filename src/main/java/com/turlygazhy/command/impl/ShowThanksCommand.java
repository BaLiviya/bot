package com.turlygazhy.command.impl;

import com.turlygazhy.Bot;
import com.turlygazhy.command.Command;
import com.turlygazhy.entity.WaitingType;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.SQLException;

/**
 * Created by bal on 26.06.17.
 */
public class ShowThanksCommand extends Command {

    private WaitingType waitingType;

    public ShowThanksCommand() throws SQLException{}

    @Override
    public boolean execute(Update update, Bot bot) throws SQLException, TelegramApiException {
        if (waitingType == null) {
            sendMessage(126, chatId, bot);
            waitingType = WaitingType.CHOOSE_THANKS_TYPE;
            return false;
        }
        switch (waitingType) {
            case CHOOSE_THANKS_TYPE:
        }
        return false;
    }

    private boolean chooseThanksType(Bot bot) throws SQLException, TelegramApiException {
        if (updateMessageText.equals(buttonDao.getButtonText(10))) {
            sendMessage(5, chatId, bot);
            return true;
        }
        return false;
    }
}
