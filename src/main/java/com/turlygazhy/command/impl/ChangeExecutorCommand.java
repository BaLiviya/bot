package com.turlygazhy.command.impl;

import com.turlygazhy.Bot;
import com.turlygazhy.command.Command;
import com.turlygazhy.entity.Task;
import com.turlygazhy.entity.User;
import com.turlygazhy.entity.WaitingType;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by user on 6/26/17.
 */
public class ChangeExecutorCommand extends Command {
    private Task task;
    private List<User> users;

    public ChangeExecutorCommand(int taskId) throws SQLException {
        super();
        task = taskDao.getTask(taskId);
    }

    @Override
    public boolean execute(Update update, Bot bot) throws SQLException, TelegramApiException {
        if (waitingType == null) {
            waitingType = WaitingType.TASK_WORKER;
            users = userDao.getUsers(chatId);
            sendMessage(78, getWorkersKeyboard(users));// Выберите работника
            return false;
        }
        switch (waitingType) {
            case TASK_WORKER:
                Long userId = Long.valueOf(updateMessageText.substring(3));
                Long taskWorker = userDao.getChatIdByUserId(userId);
                task.setUserId(taskWorker);
                taskDao.updateTask(task);
                informExecutor(task);
                return true;
        }
        return false;
    }
}
