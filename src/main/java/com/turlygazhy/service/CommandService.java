package com.turlygazhy.service;

import com.turlygazhy.command.Command;
import com.turlygazhy.command.impl.*;
import com.turlygazhy.entity.Button;
import com.turlygazhy.exception.CommandNotFoundException;

import java.sql.SQLException;

/**
 * Created by user on 1/2/17.
 */
public class CommandService extends Service {


    public Command getCommand(String text) throws SQLException, CommandNotFoundException {
        try {
            if (text.equals("script")) {
                return new ExecuteScriptCommand();
            }
            String closeText = messageDao.getMessageText(119);
            String editText = messageDao.getMessageText(120);
            String changeExecutorText = messageDao.getMessageText(121);
            String acceptButton = buttonDao.getButtonText(65);
            String rejectButton = buttonDao.getButtonText(66);
            String adminAccept = buttonDao.getButtonText(67);
            String adminReject = buttonDao.getButtonText(68);
            if (text.contains(adminAccept)) {
                int id = Integer.parseInt(text.replace(adminAccept, ""));
                return new AdminAcceptOrRejectTaskCommand(id);
            }
            if (text.contains(adminReject)) {
                int id = Integer.parseInt(text.replace(adminReject, ""));
                return new AdminAcceptOrRejectTaskCommand(id);
            }
            if (text.contains(acceptButton)) {
                int id = Integer.parseInt(text.replace(acceptButton, ""));
                return new AcceptCommand(id);
            }
            if (text.contains(rejectButton)) {
                int id = Integer.parseInt(text.replace(rejectButton, ""));
                return new RejectCommand(id);
            }
            if (text.contains(changeExecutorText)) {
                int id = Integer.parseInt(text.replace(changeExecutorText, ""));
                return new ChangeExecutorCommand(id);
            }
            if (text.contains(closeText)) {
                int id = Integer.parseInt(text.replace(closeText, ""));
                return new CloseTicketCommand();
            }
            if (text.contains(editText)) {
                int id = Integer.parseInt(text.replace(editText, ""));
                return new EditTaskCommand(id);
            }
        } catch (Exception ignored) {
        }
        Button button = buttonDao.getButton(text);
        return commandDao.getCommand(button.getCommandId());
    }
}
