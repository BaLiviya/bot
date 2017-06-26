package com.turlygazhy.command.impl;

import com.turlygazhy.Bot;
import com.turlygazhy.command.Command;
import com.turlygazhy.entity.Task;
import com.turlygazhy.entity.WaitingType;
import org.telegram.telegrambots.api.methods.ParseMode;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lol on 07.06.2017.
 */
public class AcceptOrRejectTaskCommand extends Command {
    Task task;

    public AcceptOrRejectTaskCommand() throws SQLException {
    }

    @Override
    public boolean execute(Update update, Bot bot) throws SQLException, TelegramApiException {
        if (task == null) {
            String data = update.getCallbackQuery().getData();
            int id = Integer.parseInt(data.split(" ")[1]);
            task = taskDao.getTask(id);
            updateMessageText = data.substring(0, data.indexOf(" "));
        }

        if (updateMessageText.equals(buttonDao.getButtonText(65))) {     // Принять
            task.setStatus(Task.Status.DOING);
            taskDao.updateTask(task);
            bot.sendMessage(new SendMessage()
                    .setText(task.toString())
                    .setChatId(task.getAddedByUserId())
                    .setParseMode(ParseMode.HTML));
            sendMessage("Задание записано", chatId, bot);
            return true;
        }

        if (updateMessageText.equals(buttonDao.getButtonText(66))) {     // Отклонить
            sendMessage(118, chatId, bot);
            waitingType = WaitingType.CAUSE;
            return false;
        }

        switch (waitingType) {
            case CAUSE:
                task.setStatus(Task.Status.REJECTED);
                task.setCause(updateMessageText);
                bot.sendMessage(new SendMessage()
                        .setText(task.toString())
                        .setChatId(task.getAddedByUserId())
                        .setParseMode(ParseMode.HTML)
                        .setReplyMarkup(getAfterRejectedKeyboard(task.getId()))
                );
                sendMessage("OK", chatId, bot);
                taskDao.updateTask(task);
                return true;
        }

        return false;
    }

    private ReplyKeyboard getAfterRejectedKeyboard(int taskId) throws SQLException {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();

        InlineKeyboardButton closeButton = new InlineKeyboardButton();
        String closeText = messageDao.getMessageText(119);
        closeButton.setText(closeText);
        closeButton.setCallbackData(closeText + taskId);

        InlineKeyboardButton editButton = new InlineKeyboardButton();
        String editText = messageDao.getMessageText(120);
        editButton.setText(editText);
        editButton.setCallbackData(editText + taskId);

        InlineKeyboardButton changeExecutorButton = new InlineKeyboardButton();
        String changeExecutorText = messageDao.getMessageText(121);
        changeExecutorButton.setText(changeExecutorText);
        changeExecutorButton.setCallbackData(changeExecutorText + taskId);

        row.add(closeButton);
        row.add(editButton);
        row2.add(changeExecutorButton);
        rows.add(row);
        rows.add(row2);

        keyboard.setKeyboard(rows);
        return keyboard;
    }
}
