package com.turlygazhy.command.impl;

import com.turlygazhy.Bot;
import com.turlygazhy.command.Command;
import com.turlygazhy.entity.Task;
import com.turlygazhy.entity.User;
import com.turlygazhy.entity.WaitingType;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class AddNewTaskCommand extends Command {
    private Task task;
    private int shownDates = 0;
    private List<User> users;

    public AddNewTaskCommand() throws SQLException {
    }

    @Override
    public boolean execute(Update update, Bot bot) throws SQLException, TelegramApiException {
        if (waitingType == null) {
            return addNewTask();
        }

        switch (waitingType) {
            case TASK_WORKER:
                if (updateMessageText.equals(buttonDao.getButtonText(10))) {
                    sendMessage(5, chatId, bot);
                    return true;
                }
                return chooseTaskWorker(bot);

            case TASK_TEXT:
                if (updateMessageText != null) {
                    if (updateMessageText.equals(buttonDao.getButtonText(10))) {
                        waitingType = WaitingType.TASK_WORKER;
                        return addNewTask();
                    }
                }
                return setTaskText(bot);

            case TASK_DEADLINE:
                if (updateMessageText.equals(buttonDao.getButtonText(10))) {
                    updateMessageText = "";
                    waitingType = WaitingType.TASK_TEXT;
                    return chooseTaskWorker(bot);
                }
                return setTaskDeadline(bot);

        }
        return false;
    }

    private boolean addNewTask() throws SQLException, TelegramApiException {
        waitingType = WaitingType.TASK_WORKER;
        users = userDao.getUsers(chatId);

        if (users.size() == 0) {
            sendMessage(127);
            return true;
        }

        sendMessage(78, getWorkersKeyboard(users));// Выберите работника

        task = new Task(chatId);
        return false;
    }

    private boolean chooseTaskWorker(Bot bot) throws SQLException, TelegramApiException {
        users = userDao.getUsers(chatId);
        Long userId = Long.valueOf(updateMessageText.substring(3));
        Long taskWorker = null;
        if (task.getUserId() == null) {
            taskWorker = userDao.getChatIdByUserId(userId);
            task.setUserId(taskWorker);
        }
        waitingType = WaitingType.TASK_TEXT;
        String messageText = messageDao.getMessageText(76);
        sendMessage(messageText + " <b>" + userDao.getUserByChatId(taskWorker).getName() + "</b>"); // Опишите задание
        return false;
    }

    private boolean setTaskText(Bot bot) throws SQLException, TelegramApiException {
        if (updateMessage.getVoice() == null) {
            task.setText(updateMessageText);
            task.setHasAudio(false);
        } else {
            task.setHasAudio(true);
            task.setVoiceMessageId(updateMessage.getVoice().getFileId());
        }
        sendMessage(77, getDeadlineKeyboard(shownDates)); // Введите дедлайн
        waitingType = WaitingType.TASK_DEADLINE;
        return false;
    }

    private boolean setTaskDeadline(Bot bot) throws SQLException, TelegramApiException {
        if (updateMessageText.equals(nextText)) {
            shownDates++;
            bot.editMessageText(new EditMessageText()
                    .setMessageId(updateMessage.getMessageId())
                    .setChatId(chatId)
                    .setText(messageDao.getMessageText(77)) // Введите дедлайн
                    .setReplyMarkup(getDeadlineKeyboard(shownDates))
            );
            return false;
        }

        if (updateMessageText.equals(prevText)) {
            shownDates--;
            bot.editMessageText(new EditMessageText()
                    .setMessageId(updateMessage.getMessageId())
                    .setChatId(chatId)
                    .setText(messageDao.getMessageText(77)) // Введите дедлайн
                    .setReplyMarkup(getDeadlineKeyboard(shownDates))
            );
            return false;
        }
        DateFormat dateBeginsFormat = new SimpleDateFormat("dd.MM");
        Date dateBegins = new Date();
        String saveDataBegin = (dateBeginsFormat.format(dateBegins)).toString();
        task.setDeadline(updateMessageText);
       task.setDateBegin(saveDataBegin);
        task.setDeadline(updateMessageText);
        waitingType = null;
        taskDao.insertTask(task);
        informExecutor(task);
        sendMessage(79, chatId, bot); // Задание записано
        return true;
    }
}
