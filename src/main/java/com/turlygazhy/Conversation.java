package com.turlygazhy;

import com.turlygazhy.command.Command;
import com.turlygazhy.command.impl.ShowInfoCommand;
import com.turlygazhy.exception.CommandNotFoundException;
import com.turlygazhy.service.CommandService;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.SQLException;

/**
 * Created by Yerassyl_Turlygazhy on 11/27/2016.
 */
public class Conversation {
    private CommandService commandService = new CommandService();
    private Command command;

    public void handleUpdate(Update update, Bot bot) throws SQLException, TelegramApiException {
        try {
            org.telegram.telegrambots.api.objects.Message updateMessage = update.getMessage();
            String inputtedText;
            if (updateMessage == null) {
                inputtedText = update.getCallbackQuery().getData();
//                try {
//                    inputtedText = inputtedText.substring(0, inputtedText.indexOf(" "));//don't use this line
//                } catch (Exception ignored) {
//                }
                updateMessage = update.getCallbackQuery().getMessage();
            } else {
                inputtedText = updateMessage.getText();
            }

            try {
                command = commandService.getCommand(inputtedText);
            } catch (CommandNotFoundException e) {
                if (updateMessage.isGroupMessage()) {
                    return;
                }
                if (command == null) {
                    ShowInfoCommand showInfoCommand = new ShowInfoCommand();
                    int cannotHandleUpdateMessageId = 7;
                    showInfoCommand.initMessage(update, bot);
                    showInfoCommand.setMessageId(cannotHandleUpdateMessageId);
                    showInfoCommand.execute(update, bot);
                    return;
                }
            }
            command.initMessage(update, bot);
            boolean commandFinished = command.execute(update, bot);
            if (commandFinished) {
                command = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            ShowInfoCommand showInfoCommand = new ShowInfoCommand();
            int cannotHandleUpdateMessageId = 7;
            showInfoCommand.setMessageId(cannotHandleUpdateMessageId);
            showInfoCommand.execute(update, bot);
        }

    }
}
