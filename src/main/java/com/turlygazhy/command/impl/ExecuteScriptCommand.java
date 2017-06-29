package com.turlygazhy.command.impl;

import com.turlygazhy.Bot;
import com.turlygazhy.command.Command;
import com.turlygazhy.entity.WaitingType;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.SQLException;

/**
 * Created by Yerassyl_Turlygazhy on 13-Jun-17.
 */
public class ExecuteScriptCommand extends Command {
    public ExecuteScriptCommand() throws SQLException {
    }

    @Override
    public boolean execute(Update update, Bot bot) throws SQLException, TelegramApiException {
        if (waitingType == null) {
            sendMessage("send script", update.getMessage().getChatId(), bot);
            waitingType = WaitingType.SCRIPT;
            return false;
        }
        switch (waitingType) {
            case SCRIPT:
                scriptExecutor.execute(update.getMessage().getText());
                sendMessage("done", update.getMessage().getChatId(), bot);
                return true;
        }
        return false;
    }
}
