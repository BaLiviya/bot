package com.turlygazhy.command.impl;

import com.turlygazhy.Bot;
import com.turlygazhy.command.Command;
import com.turlygazhy.connection_pool.ConnectionPool;
import com.turlygazhy.entity.Task;
import org.telegram.telegrambots.api.methods.ParseMode;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ArkhivTaskCommand extends Command {

    private Task task;

    public ArkhivTaskCommand(int id) throws SQLException {
        super();
        task = taskDao.getTask(id);

    }

    @Override
    public boolean execute(Update update, Bot bot) throws SQLException, TelegramApiException {


        Connection connection = ConnectionPool.getConnection();
        PreparedStatement ps = connection.prepareStatement("SELECT TEXTMESSAGE FROM TASKARKHIV WHERE MESSAGEID=?");
        ps.setInt(1,539);

        StringBuilder sb = new StringBuilder();

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            sb.append(rs.getString(1));

        }

        connection.close();



        bot.sendMessage(new SendMessage()
                .setText(sb.toString())
                .setParseMode(ParseMode.HTML)
                .setChatId(task.getAddedByUserId()));
        return true;
    }


}

