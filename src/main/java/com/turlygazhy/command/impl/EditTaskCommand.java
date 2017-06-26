package com.turlygazhy.command.impl;

import com.turlygazhy.Bot;
import com.turlygazhy.command.Command;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.SQLException;

/**
 * Created by user on 6/11/17.
 */
public class EditTaskCommand extends Command {
    private final int taskId;

    public EditTaskCommand(int taskId) throws SQLException {
        super();
        this.taskId = taskId;
    }

    @Override
    public boolean execute(Update update, Bot bot) throws SQLException, TelegramApiException {
        //todo ihere
        return false;
    }
}
