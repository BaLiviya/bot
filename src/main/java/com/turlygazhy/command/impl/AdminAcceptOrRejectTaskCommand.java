package com.turlygazhy.command.impl;

import com.turlygazhy.Bot;
import com.turlygazhy.command.Command;
import com.turlygazhy.entity.Task;
import com.turlygazhy.entity.WaitingType;
import org.telegram.telegrambots.api.methods.ParseMode;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.SQLException;

/**
 * Created by lol on 07.06.2017.
 */
public class AdminAcceptOrRejectTaskCommand extends Command {
    private Task task;

    public AdminAcceptOrRejectTaskCommand(int id) throws SQLException {
        task = taskDao.getTask(id);
    }

    @Override
    public boolean execute(Update update, Bot bot) throws SQLException, TelegramApiException {

        if (updateMessageText.contains(buttonDao.getButtonText(67))) {     // Принять
            task.setStatus(Task.Status.DONE);
            taskDao.updateTask(task);
            sendMessage(123, task.getAddedByUserId(), bot);
            return true;
        }

        if (updateMessageText.contains(buttonDao.getButtonText(68))) {     // Отклонить
            sendMessage(118, chatId, bot);
            waitingType = WaitingType.CAUSE;
            return false;
        }

        switch (waitingType) {
            case CAUSE:
                task.setStatus(Task.Status.REJECTED_BY_ADMIN);
                task.setCause(updateMessageText);
                taskDao.updateTask(task);
                bot.sendMessage(new SendMessage()
                        .setText(task.toString())
                        .setChatId(task.getUserId())
                        .setParseMode(ParseMode.HTML));
                sendMessage(123, chatId, bot);
                return true;
        }

        return false;
    }
}
