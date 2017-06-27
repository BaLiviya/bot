package com.turlygazhy.tasks;

import com.turlygazhy.Bot;
import com.turlygazhy.connection_pool.ConnectionPool;
import com.turlygazhy.exception.CommandNotFoundException;
import org.springframework.scheduling.annotation.Scheduled;
import org.telegram.telegrambots.api.methods.ParseMode;
import org.telegram.telegrambots.api.methods.send.SendMessage;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SchedulingTasks {

    @Scheduled(cron = "0 0 12 * * ?")
    public void createData() throws SQLException, CommandNotFoundException {

        try {

            Bot bot = new Bot();
            /*-------------------Берем дату меняем формат------------------------*/
            DateFormat dateFormat = new SimpleDateFormat("dd.MM");
            Date currentDate = new Date();
            Long time = currentDate.getTime();
            long anotherDate = -1;
            time = time + (60 * 60 * 24 * 1000 * anotherDate);
            currentDate = new Date(time);
            String dateTime = (dateFormat.format(currentDate)).toString();

            /*----------------------Отправляем запрос в базу---------------------*/

            Connection connection = ConnectionPool.getConnection();
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT T.USER_ID, T.ADDED_BY_USER_ID,T.TEXT,T.DEADLINE ,U.NAME " +
                            "FROM PUBLIC.TASK AS T  LEFT JOIN PUBLIC.USER AS U ON T.USER_ID = U.CHAT_ID " +
                            "WHERE T.STATUS = 0 AND T.DEADLINE = ? ");
            ps.setString(1, dateTime);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                StringBuilder sb = new StringBuilder();
                sb.append("\n\n❗<b>️Задача не была выполнена \n    в поставленные сроки\n\n </b>   <b>Работник: </b>")
                        .append(rs.getString(5)).append("\n\n    <b>Задача:</b> ").append(rs.getString(3))
                        .append("\n\n    <b>Дедлайн: </b>").append(rs.getString(4)).append("\n\n    <b>Статус: </b>Выполняется ");


                System.out.println(sb);
                int foo = Integer.parseInt(rs.getString(1));
                bot.sendMessage(new SendMessage()
                        .setChatId(rs.getLong(2))
                        .setText(sb.toString())
                        .setParseMode(ParseMode.HTML)
                );

            }

            connection.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


/*
    @Scheduled(cron = "30 33 * * * ?")
    public void testCron() {
    }*/

}

