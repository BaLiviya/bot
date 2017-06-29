package com.turlygazhy.command.impl;

import com.turlygazhy.Bot;
import com.turlygazhy.command.Command;
import com.turlygazhy.entity.Task;
import org.telegram.telegrambots.api.methods.ParseMode;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.SQLException;

/**
 * Created by Yerassyl_Turlygazhy on 28-Jun-17.
 */
public class AcceptCommand extends Command {
    private Task task;

    public AcceptCommand(int id) throws SQLException {
        super();
        task = taskDao.getTask(id);
    }

    @Override
    public boolean execute(Update update, Bot bot) throws SQLException, TelegramApiException {
        task.setStatus(Task.Status.DOING);
        taskDao.updateTask(task);
        bot.sendMessage(new SendMessage()
                .setText(task.toString())
                .setChatId(task.getAddedByUserId())
                .setParseMode(ParseMode.HTML));
        sendMessage(408, chatId, bot);//Задание записано
        return true;
    }
}
