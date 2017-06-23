package com.turlygazhy.command.impl;

import com.turlygazhy.Bot;
import com.turlygazhy.command.Command;
import com.turlygazhy.entity.Thanks;
import com.turlygazhy.entity.WaitingType;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.SQLException;

/**
 * Created by bal on 22.06.17.
 */
public class ShowThanksCommand extends Command {

    private WaitingType waitingType;
    private Thanks thanks;
    private int thanksId = 0;
    private int thanksType;

    public ShowThanksCommand() throws SQLException{}

    @Override
    public boolean execute(Update update, Bot bot) throws SQLException, TelegramApiException {
        if (waitingType == null) {
            sendMessage(121, chatId, bot);
            waitingType = WaitingType.CHOOSE_THANKS_TYPE;
            return false;
        }
        switch (waitingType) {
            case CHOOSE_THANKS_TYPE:
                return chooseThanksType(bot);
        }
        return false;
    }

    private boolean chooseThanksType(Bot bot) throws SQLException, TelegramApiException {
        if (updateMessageText.equals(buttonDao.getButtonText(69))) {             // Добавить благодарность
            thanksType = 0;
        } else if (updateMessageText.equals(buttonDao.getButtonText(70))) {      // Мои благодарности
            thanksType = 1;
        } else if (updateMessageText.equals(buttonDao.getButtonText(10))) {      // Назад
            sendMessage(5, chatId,bot);                                   // Главное меню
            return true;
        }
        if (thanksType == 0) {

        }
        return false;
    }
}
