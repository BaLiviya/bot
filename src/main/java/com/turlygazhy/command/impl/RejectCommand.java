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
 * Created by Yerassyl_Turlygazhy on 28-Jun-17.
 */
public class RejectCommand extends Command {
    private Task task;

    public RejectCommand(int id) throws SQLException {
        task = taskDao.getTask(id);
    }

    @Override
    public boolean execute(Update update, Bot bot) throws SQLException, TelegramApiException {

        if (waitingType==null){
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
                sendMessage(411, chatId, bot);//Отклонение записано
                taskDao.updateTask(task);


                return true;
        }

        return false;
    }
}
