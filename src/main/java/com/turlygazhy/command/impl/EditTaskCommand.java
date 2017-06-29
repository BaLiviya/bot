package com.turlygazhy.command.impl;

import com.turlygazhy.Bot;
import com.turlygazhy.command.Command;
import com.turlygazhy.entity.Task;
import com.turlygazhy.entity.WaitingType;
import com.turlygazhy.exception.CannotHandleUpdateException;
import org.telegram.telegrambots.api.methods.ParseMode;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.SQLException;

/**
 * Created by user on 6/11/17.
 */
public class EditTaskCommand extends Command {
    public static final String TEXT = "text";
    public static final String DEADLINE = "deadline";
    public final String DONE = messageDao.getMessageText(409);//Завершить
    private Task task;
    private int shownDates = 0;

    public EditTaskCommand(int taskId) throws SQLException {
        super();
        task = taskDao.getTask(taskId);
    }

    @Override
    public boolean execute(Update update, Bot bot) throws SQLException, TelegramApiException {
        if (updateMessageText.equals(messageDao.getMessageText(132))) {
            informExecutor(task);
            bot.sendMessage(new SendMessage()
                    .setText(task.toString())
                    .setChatId(task.getAddedByUserId())
                    .setParseMode(ParseMode.HTML)
                    .setReplyMarkup(getAfterRejectedKeyboard(task.getId()))
            );
            return true;
        }
        if (waitingType == null) {
            // TODO Marat тут проверка это текст или голос
            showTaskForChange();
            return false;
        }
        switch (waitingType) {
            case CHANGE_TYPE:
                String type = updateMessageText.replace(editText, "");
                if (type.equals(TEXT)) {
                    sendMessage(130, getInlineButton(messageDao.getMessageText(131)));
                    waitingType = WaitingType.NEW_TEXT;
                    return false;
                }
                if (type.equals(DEADLINE)) {
                    sendMessage(77, getDeadlineKeyboard(shownDates)); // Введите дедлайн
                    waitingType = WaitingType.TASK_DEADLINE;
                    return false;
                }
                throw new CannotHandleUpdateException();
            case NEW_TEXT:              //  Проверить потом как работает
                if (updateMessage.getVoice() == null) {
                    task.setHasAudio(false);
                    task.setText(updateMessageText);
                } else {
                    task.setHasAudio(true);
                    task.setVoiceMessageId(updateMessage.getVoice().getFileId());
                }
                taskDao.updateTask(task);
                showTaskForChange();
                return false;
            case TASK_DEADLINE:
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
                shownDates = 0;
                taskDao.updateTask(task);
                showTaskForChange();
                return false;
        }
        return false;
    }

    private void showTaskForChange() throws TelegramApiException, SQLException {
        //todo все кнопки должны быть в одной клаве
        String taskTextText = messageDao.getMessageText(133);
        String deadlineText = messageDao.getMessageText(134);
        sendMessage(taskTextText + "\n" + task.getText(), getInlineButton(editText, editText + TEXT));
        sendMessage(deadlineText + "\n" + task.getDeadline(), getInlineButton(editText, editText + DEADLINE));
        sendMessage(410, getInlineButton(DONE));//Для завершения нажмите эту кнопку
        waitingType = WaitingType.CHANGE_TYPE;
    }
}
