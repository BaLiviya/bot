package com.turlygazhy.command.impl;

import com.turlygazhy.Bot;
import com.turlygazhy.command.Command;
import com.turlygazhy.entity.Task;
import com.turlygazhy.entity.User;
import com.turlygazhy.entity.WaitingType;
import org.telegram.telegrambots.api.methods.ParseMode;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendVoice;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.SQLException;
import java.util.ArrayList;
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
            sendMessage("Добавьте пожалуйста рабочих.");//todo Marat to DB
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

        task.setDeadline(updateMessageText);
        waitingType = null;
        taskDao.insertTask(task);
        informExecutor(bot);
        sendMessage(79, chatId, bot); // Задание записано
        return true;
    }

    private void informExecutor(Bot bot) throws SQLException, TelegramApiException { //передача задания
        StringBuilder sb = new StringBuilder();
        sendMessage(80, task.getUserId(), bot);
        if (task.isHasAudio()) {
            bot.sendVoice(new SendVoice()
                    .setVoice(task.getVoiceMessageId())
                    .setChatId(task.getUserId()));
        } else {
            sb.append("<b>").append(messageDao.getMessageText(96)).append("</b>").append(task.getText()).append("\n");
        }
        sb.append("<b>").append(messageDao.getMessageText(98)).append("</b>").append(task.getDeadline());
        bot.sendMessage(new SendMessage()
                .setChatId(task.getUserId())
                .setText(sb.toString())
                .setReplyMarkup(getTaskKeyboard())
                .setParseMode(ParseMode.HTML));
    }

    private InlineKeyboardMarkup getTaskKeyboard() throws SQLException {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(new InlineKeyboardButton()
                .setText(buttonDao.getButtonText(65))   // Accept
                .setCallbackData(buttonDao.getButtonText(65) + " " + task.getId()));
        row.add(new InlineKeyboardButton()
                .setText(buttonDao.getButtonText(66))   // Reject
                .setCallbackData(buttonDao.getButtonText(66) + " " + task.getId()));

        rows.add(row);
        keyboard.setKeyboard(rows);

        return keyboard;
    }
}
