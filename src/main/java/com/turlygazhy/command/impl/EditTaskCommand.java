package com.turlygazhy.command.impl;

import com.turlygazhy.Bot;
import com.turlygazhy.command.Command;
import com.turlygazhy.entity.Button;
import com.turlygazhy.entity.Task;
import com.turlygazhy.entity.WaitingType;
import com.turlygazhy.exception.CannotHandleUpdateException;
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
 * Created by user on 6/11/17.
 */
public class EditTaskCommand extends Command {
    public static final String TEXT = "text";
    public static final String DEADLINE = "deadline";
    private Task task;

    public EditTaskCommand(int taskId) throws SQLException {
        super();
        task = taskDao.getTask(taskId);
    }

    @Override
    public boolean execute(Update update, Bot bot) throws SQLException, TelegramApiException {
        if (waitingType == null) {
            // TODO: 26-Jun-17 тут проверка это текст или голос
            String taskTextText = "<b>Текст задания:</b>";//todo Marat put it to db
            String taskDeadlineText = "<b>Крайний срок:</b>";//todo Marat put it to db
            sendMessage(taskTextText + "\n" + task.getText(), getInlineButton(editText, editText + TEXT));
            sendMessage(taskDeadlineText + "\n" + task.getDeadline(), getInlineButton(editText, editText + DEADLINE));
            waitingType = WaitingType.CHANGE_TYPE;
            return false;
        }
        switch (waitingType) {
            case CHANGE_TYPE:
                String type = updateMessageText.replace(editText, "");
                if (type.equals(TEXT)) {
// TODO: 26-Jun-17 ihere
                }
                if (type.equals(DEADLINE)) {

                }
                throw new CannotHandleUpdateException();
        }

        return false;
    }


}
