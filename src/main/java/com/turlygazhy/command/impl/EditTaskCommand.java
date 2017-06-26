package com.turlygazhy.command.impl;

import com.turlygazhy.Bot;
import com.turlygazhy.command.Command;
import com.turlygazhy.entity.Task;
import com.turlygazhy.entity.WaitingType;
import com.turlygazhy.exception.CannotHandleUpdateException;
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
    public static final String CANCEL = "Отмена";
    private Task task;
    private int shownDates = 0;

    public EditTaskCommand(int taskId) throws SQLException {
        super();
        task = taskDao.getTask(taskId);
    }

    @Override
    public boolean execute(Update update, Bot bot) throws SQLException, TelegramApiException {
        if (waitingType == null) {
            // TODO Marat тут проверка это текст или голос
            showTaskForChange();
            return false;
        }
        switch (waitingType) {
            case CHANGE_TYPE:
                String type = updateMessageText.replace(editText, "");
                if (type.equals(TEXT)) {
                    String sendTextText = "Пожалуйста отправьте текст";//todo Marat to DB
                    sendMessage(sendTextText, getInlineButton(CANCEL));
                    waitingType = WaitingType.NEW_TEXT;
                    return false;
                }
                if (type.equals(DEADLINE)) {
                    sendMessage(77, getDeadlineKeyboard(shownDates)); // Введите дедлайн
                    waitingType = WaitingType.TASK_DEADLINE;
                    return false;
                }
                throw new CannotHandleUpdateException();
            case NEW_TEXT://todo Marat тут принимать не только текст но и аудио
                task.setText(updateMessageText);
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

    private void showTaskForChange() throws TelegramApiException {
        String taskTextText = "<b>Текст задания:</b>";//todo Marat put it to db
        String taskDeadlineText = "<b>Крайний срок:</b>";//todo Marat put it to db
        sendMessage(taskTextText + "\n" + task.getText(), getInlineButton(editText, editText + TEXT));
        sendMessage(taskDeadlineText + "\n" + task.getDeadline(), getInlineButton(editText, editText + DEADLINE));
        waitingType = WaitingType.CHANGE_TYPE;
    }

}
