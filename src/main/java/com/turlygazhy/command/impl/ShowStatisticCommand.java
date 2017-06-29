package com.turlygazhy.command.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.turlygazhy.Bot;
import com.turlygazhy.command.Command;
import com.turlygazhy.connection_pool.ConnectionPool;
import com.turlygazhy.entity.Task;
import com.turlygazhy.entity.User;
import com.turlygazhy.entity.WaitingType;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;
import org.telegram.telegrambots.api.methods.ParseMode;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.methods.send.SendVoice;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by daniyar on 30.05.17.
 */
public class ShowStatisticCommand extends Command {

    private List<User> users;
    private Long workerId;



    public ShowStatisticCommand() throws SQLException {
    }

    @Override
    public boolean execute(Update update, Bot bot) throws SQLException, TelegramApiException {

        if (waitingType == null) {
            users = userDao.getUsers(chatId);
            if (users.size() == 0) {
                sendMessage(10, chatId, bot);   // У вас нет сотрудников
                return true;
            }
            sendMessage(404, chatId, bot);
            waitingType = WaitingType.CHOOSE_USER;

            return false;
        }

        /*----------------------------Копка отчеты------------------------------------ */

        if (updateMessageText.equals(buttonDao.getButtonText(200))) {
            showUsers();

            return false;
        }

        if (updateMessageText.equals(buttonDao.getButtonText(204))) {
            sendMessage(401, chatId, bot);

            return false;
        }
   /*----------------------------Копка Отчет -> За неделю------------------------------------ */

        if (updateMessageText.equals(buttonDao.getButtonText(201))) {

            DateFormat dateFormat = new SimpleDateFormat("dd.MM");
            Date currentDate = new Date();
            Date current = new Date();
            Long time = currentDate.getTime();
            long anotherDate = -7;
            time = time + (60 * 60 * 24 * 1000 * anotherDate);
            currentDate = new Date(time);
            String dateBegin = (dateFormat.format(currentDate)).toString();
            String dateEnd = (dateFormat.format(current)).toString();

            pieCharts(update, bot, dateBegin, dateEnd);
            return false;
        }
   /*----------------------------Копка Отчет -> За Период------------------------------------ */
        if (updateMessageText.equals(buttonDao.getButtonText(203))) {
            sendMessage(402, chatId, bot);
            return false;
        }

        Pattern r = Pattern.compile("^\\d\\d(\\.|,)\\d\\d\\s?-\\s?\\d\\d(\\.|,)\\d\\d$");
        Matcher m = r.matcher(updateMessageText);

        if (m.matches()) {

            String txt = updateMessageText.trim();
            String dateBegin = txt.substring(0, 5);
            String dateEnd = txt.substring(txt.length() - 5);
            pieCharts(update, bot, dateBegin, dateEnd);

            return false;
        }

           /*----------------------------Копка Отчет -> За неделю------------------------------------ */

        if (updateMessageText.equals(buttonDao.getButtonText(202))) {

            DateFormat dateFormat = new SimpleDateFormat("MM");
            DateFormat dayFormat = new SimpleDateFormat("dd");
            Date currentDateMonth = new Date();
            Date currentDateDay = new Date();
            int month = Integer.parseInt(dateFormat.format(currentDateMonth));
            String day = (dayFormat.format(currentDateDay));
            String lastmonth = String.valueOf(month - 1);
            if (lastmonth.length() == 1) {
                lastmonth = "0" + lastmonth;
            } else {
                lastmonth = lastmonth;
            }
            String dateBegin = day + "." + lastmonth;

            DateFormat endDayFormat = new SimpleDateFormat("dd.MM");
            Date current = new Date();
            String dateEnd = (endDayFormat.format(current)).toString();

            pieCharts(update, bot, dateBegin, dateEnd);
            return false;
        }

 /*----------------------------Статистика сотрудника список------------------------------------ */
        switch (waitingType) {
            case CHOOSE_USER:
                if (updateMessageText.equals(buttonDao.getButtonText(10))) {
                    sendMessage(5, chatId, bot);
                    return true;
                }
                workerId=null;
                workerId = userDao.getChatIdByUserId(
                        Long.valueOf(updateMessageText.substring(3))
                );
                showUserStatistic(chatId, workerId, bot);
                return false;


        }
        return false;
    }




    private void showUserStatistic(Long chatId, Long workerId, Bot bot) throws SQLException, TelegramApiException {
        List<Task> tasks = taskDao.getTasks(workerId);
        StringBuilder sb = new StringBuilder();
        sb.append("").append(messageDao.getMessageText(111)).append("").append(tasks.size()).append("\n");
        for (Task.Status status : Task.Status.values()) {
            sb.append("").append(status.getStatusString(status)).append("");
            int count = 0;
            int countDoneTask = 0;
            for (Task task : tasks) {
                if (task.getStatus().equals(status)) {
                    if (status.equals(Task.Status.DONE)) {
                        String deadlineStr = task.getDeadline();
                        String dateOfCompletionStr = task.getDateOfCompletion();

                        int deadline = Integer.valueOf(deadlineStr.substring(0, 2))
                                + Integer.valueOf(deadlineStr.substring(3)) * 1000;

                        int dateOfCompletion = Integer.valueOf(dateOfCompletionStr.substring(0, 2))
                                + Integer.valueOf(dateOfCompletionStr.substring(3)) * 1000;
                        System.out.print(deadline + " " + dateOfCompletion);
                        if (deadline > dateOfCompletion) {
                            countDoneTask++;
                        }
                    }
                    count++;
                }
            }
            sb.append(count).append("\n");
            if (status.equals(Task.Status.DONE)) {
                sb.append("").append(messageDao.getMessageText(112)).append("").append(countDoneTask).append("\n");
            }
        }

        bot.sendMessage(new SendMessage()
                .setText(sb.toString())
                .setChatId(chatId)
                .setParseMode(ParseMode.HTML)
                .setReplyMarkup(keyboardMarkUpDao.select(messageDao.getMessage(104).getKeyboardMarkUpId())));
        sendMessage(405, chatId, bot);


    }

    private void showUsers() throws SQLException, TelegramApiException {
        workerId=null;
        sendMessage(104, getWorkersKeyboard(users));      // Выберите сотрудника
    }

//-----Метод для выевление диаограммы----

    private void pieCharts(Update update, Bot bot, String dateBegin, String dateEnd) throws SQLException, TelegramApiException {


        Connection connection = ConnectionPool.getConnection();
        PreparedStatement ps = connection.prepareStatement(
                "SELECT\n" +
                        "  STATUS,\n" +
                        "  count(U.ID) * 100 / rownum() AS percent\n" +
                        "FROM TASK AS T\n" +
                        "  LEFT JOIN USER U ON T.USER_ID = U.CHAT_ID\n" +
                        "WHERE ((T.DEADLINE BETWEEN ? AND ?) OR (T.DATE_BEGIN BETWEEN ? AND ?))\n" +
                        "      AND (T.STATUS = 0 OR T.STATUS = 1 OR T.STATUS = 3)\n" +
                        "      AND T.USER_ID = ?\n" +
                        "GROUP BY STATUS");

        ps.setString(1, dateBegin);
        ps.setString(2, dateEnd);
        ps.setString(3, dateBegin);
        ps.setString(4, dateEnd);
        ps.setLong(5, workerId);


        ResultSet rs = ps.executeQuery();

        PieChart chart = new PieChartBuilder()
                .width(400)
                .height(300)
                .title("Показатели поставленных задач")
                .build();

        // Customize Chart
        Color[] sliceColors = new Color[]{
                new Color(4, 224, 0),
                new Color(230, 105, 62),
                new Color(8, 26, 236)
        };


        chart.getStyler().setSeriesColors(sliceColors);
        chart.getStyler().setChartBackgroundColor(new Color(255, 255, 255));
        chart.getStyler().setLegendBackgroundColor(new Color(255, 255, 255));
        chart.getStyler().setPlotBackgroundColor(new Color(255, 255, 255));
        chart.getStyler().setPlotBorderColor(new Color(255, 255, 255));
        while (rs.next()) {
            // Series

            switch ((int) rs.getLong(1)) {
                case 0:
                    chart.addSeries("В процессе", rs.getInt(2));
                    break;
                case 1:
                    chart.addSeries("Выполненные", rs.getInt(2));
                    break;
                case 3:
                    chart.addSeries("Не выполненные", rs.getInt(2));
                    break;

            }
        }
        connection.close();

        System.out.println(chart);

        String pieChartFilePath = "C:\\piechart.jpg";

        try {
            BitmapEncoder.saveBitmap(chart, pieChartFilePath, BitmapEncoder.BitmapFormat.JPG);
        } catch (Exception e) {
            e.printStackTrace();
        }

        File file = new File(pieChartFilePath);
        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            bot.sendPhoto(new SendPhoto()
                    .setChatId(chatId)
                    .setNewPhoto("photo", fileInputStream)
            );
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }


    }


}


