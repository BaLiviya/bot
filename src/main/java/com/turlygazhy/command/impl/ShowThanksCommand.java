package com.turlygazhy.command.impl;

import com.turlygazhy.Bot;
import com.turlygazhy.command.Command;
import com.turlygazhy.entity.Thanks;
import com.turlygazhy.entity.WaitingType;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by bal on 26.06.17.
 */
public class ShowThanksCommand extends Command {

    private WaitingType waitingType;
    private List<Thanks> thanks;
    private int shownDates = 0;

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
                return chooseThanksType(bot);
            case THANKS_CHOOSE_TEXT:
                return getTextThanks(bot);
        }
        return false;
    }

    private boolean chooseThanksType(Bot bot) throws SQLException, TelegramApiException {
        if (updateMessageText.equals(buttonDao.getButtonText(71))) {
            sendMessage(136,getDateKeyboard(shownDates));
            waitingType = WaitingType.THANKS_CHOOSE_TEXT;
        }else if (updateMessageText.equals(buttonDao.getButtonText(10))) {
            sendMessage(5, chatId, bot);
            return true;
        }
        return false;
    }

    private InlineKeyboardMarkup getDateKeyboard(int shownDates) {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        Date date = new Date();
        date.setDate(date.getDate() + (shownDates * 9));
        List<InlineKeyboardButton> row = null;
        for (int i = 1; i < 10; i++) {
            if (row == null) {
                row = new ArrayList<>();
            }
            InlineKeyboardButton button = new InlineKeyboardButton();
            int dateToString = date.getDate() - 1;
            String stringDate;
            if (dateToString > 9) {
                stringDate = String.valueOf(dateToString);
            } else {
                stringDate = "0" + dateToString;
            }
            int monthToString = date.getMonth() + 1;
            String stringMonth;
            if (monthToString > 9) {
                stringMonth = String.valueOf(monthToString);
            } else {
                stringMonth = "0" + monthToString;
            }
            String dateText = stringDate + "." + stringMonth;
            button.setText(dateText);
            button.setCallbackData(dateText);
            row.add(button);
            if (i % 3 == 0) {
                rows.add(row);
                row = null;
            }
            date.setDate(date.getDate() - 1);
        }
        if (shownDates > 0) {
            rows.add(getNextPrevRows(true, true));
        } else {
            rows.add(getNextPrevRows(false, true));
        }
        keyboard.setKeyboard(rows);
        return keyboard;
    }

    private boolean getTextThanks(Bot bot) throws SQLException, TelegramApiException {
        thanks = thanksDao.getThanks(chatId);
        for (Thanks thank: thanks) {
            if (updateMessageText.equals(thank.getDate())) {
                sendMessage("Ваше благодарность на " + updateMessageText + ": " + thank.getText());
                sendMessage(5,chatId,bot);
                return true;
            } else if (updateMessageText.equals(buttonDao.getButtonText(10))) {      // Назад
                sendMessage(5, chatId,bot);                                   // Главное меню
                return true;
            }
        }
        return false;
    }
}
