package com.turlygazhy.dao.impl;

import com.turlygazhy.connection_pool.ConnectionPool;
import com.turlygazhy.dao.AbstractDao;
import com.turlygazhy.entity.Task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by lol on 25.05.2017.
 */
public class TaskDao extends AbstractDao {
    //    private static final String SELECT_TASK = "SELECT * FROM TASK";
    private final Connection connection;
    private final String INSERT_INTO_TASK = "INSERT INTO TASK VALUES (default, ?, ?, ?, default, ?, ?, ?, NULL, NULL, NULL,?)"; //

    public TaskDao(Connection connection) {
        this.connection = connection;
    }




    public Task insertTask(Task task) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(INSERT_INTO_TASK, PreparedStatement.RETURN_GENERATED_KEYS);
        ps.setLong(1, task.getUserId());
        ps.setLong(2, task.getAddedByUserId());
        ps.setString(3, task.getDeadline());
        ps.setBoolean(4, task.isHasAudio());
        if (task.isHasAudio()) {
            ps.setString(5, task.getVoiceMessageId());
            ps.setString(6, null);
        } else {
            ps.setString(5, null);
            ps.setString(6, task.getText());
        }
        ps.setString(7, task.getDateBegin());
        ps.executeUpdate();
        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            task.setId(rs.getInt(1));
        }

        saveMessageTask(task, true);
        return task;
    }

    public List<Task> getTasks(Long userId) throws SQLException {
        return getTasks(userId, null);
    }

    public List<Task> getTasks(Long userId, Task.Status status) throws SQLException {
        PreparedStatement ps;
        if (status == null) {
            ps = connection.prepareStatement("SELECT * FROM TASK WHERE USER_ID = ?");
        } else {
            if (status.equals(Task.Status.DONE)) {
                ps = connection.prepareStatement("SELECT * FROM TASK WHERE STATUS = 1 AND USER_ID = ?");
            } else {
                ps = connection.prepareStatement("SELECT * FROM TASK WHERE STATUS != 1 AND USER_ID = ?");
            }
        }
        ps.setLong(1, userId);

        ps.execute();

        ResultSet rs = ps.getResultSet();
        ArrayList<Task> tasks = new ArrayList<>();
        while (rs.next()) {
            tasks.add(parseTask(rs));
        }
        return tasks;
    }

    public List<Task> getTasksAddedBy() throws SQLException {
        return getTasksAddedBy(null, null);
    }

    public List<Task> getTasksAddedBy(Long addedBy) throws SQLException {
        return getTasksAddedBy(addedBy, null);
    }

    public List<Task> getTasksAddedBy(Long addedBy, Task.Status status) throws SQLException {
        List<Task> tasks = new ArrayList<>();
        PreparedStatement ps;
        if (status == null) {
            ps = connection.prepareStatement("SELECT * FROM TASK WHERE ADDED_BY_USER_ID = ?");
        } else {
            if (status.equals(Task.Status.DONE)) {
                ps = connection.prepareStatement("SELECT * FROM TASK WHERE STATUS = 1 AND ADDED_BY_USER_ID = ?");
            } else {
                ps = connection.prepareStatement("SELECT * FROM TASK WHERE STATUS != 1 AND ADDED_BY_USER_ID = ?");
            }
        }
        ps.setLong(1, addedBy);
        ps.execute();
        ResultSet rs = ps.getResultSet();
        while (rs.next()) {
            tasks.add(parseTask(rs));
        }

        return tasks;
    }

    private Task parseTask(ResultSet rs) throws SQLException {
        Task task = new Task(rs.getInt(1));             // Task ID
        task.setUserId(rs.getLong(2));                  // Task Executor
        task.setAddedByUserId(rs.getLong(3));           // Added User ID
        task.setDeadline(rs.getString(4));              // Deadline
        task.setStatus(rs.getInt(5));                   // Status
        task.setHasAudio(rs.getBoolean(6));             // HasAudio
        if (task.isHasAudio()) {
            task.setVoiceMessageId(rs.getString(7));    // Task Voice
        } else {
            task.setText(rs.getString(8));              // Task Text
        }
        task.setReport(rs.getString(9));                // Report
        task.setCause(rs.getString(10));                // Cause
        task.setDateOfCompletion(rs.getString(11));     // Date of completion
        return task;
    }

    public void updateTask(Task task) throws SQLException {//todo апдейтать и аудио
        String UPDATE_TASK = "UPDATE TASK SET STATUS = ?, USER_ID = ?, REPORT = ?, CAUSE = ?, DATE_OF_COMPLETION = ?, TEXT=?, DEADLINE=? WHERE ID = ?";
        PreparedStatement ps = connection.prepareStatement(UPDATE_TASK);
        ps.setInt(1, task.getStatus().getId());
        ps.setLong(2, task.getUserId());
        ps.setString(3, task.getReport());
        ps.setString(4, task.getCause());
        ps.setString(5, task.getDateOfCompletion());
        ps.setString(6, task.getText());
        ps.setString(7, task.getDeadline());
        ps.setInt(8, task.getId());
        ps.execute();

        saveMessageTask(task, false);
    }


    public Task getTask(int id) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("SELECT * FROM TASK WHERE ID = ?");
        ps.setInt(1, id);
        ps.execute();

        ResultSet rs = ps.getResultSet();
        if (rs.next()) {
            return parseTask(rs);
        }
        return null;
    }

    public void saveMessageTask(Task task, boolean insert) throws SQLException {

        String text = null;
        String cause = null;


        if (insert) {

            text = "Задания: " + task.getText();

        } else {

            PreparedStatement lastTaskPs = connection.prepareStatement(
                    "SELECT * FROM TASKARKHIV WHERE MESSAGEID = ? ORDER BY ID DESC ");
            lastTaskPs.setInt(1, task.getId());
            ResultSet resultSet = lastTaskPs.executeQuery();
            resultSet.next();
            String textTask = "Задания: " + task.getText();

            if (textTask.equals(resultSet.getString(2))) {
                cause = "Уточнение: " + task.getCause();
            } else {
                text = "Задания: " + task.getText();
            }
        }

        PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO TASKARKHIV (TEXTMESSAGE, CAUSE, MESSAGEID) VALUES (?, ?, ?);");
        ps.setString(1, text);
        ps.setString(2, cause);
        ps.setInt(3, task.getId());
        ps.execute();

    }



    public List<String> getArkhivTaskText(Task task) throws SQLException {

        PreparedStatement ps = connection.prepareStatement("SELECT TEXTMESSAGE,CAUSE FROM TASKARKHIV WHERE MESSAGEID=?");
        ps.setInt(1, task.getId());
        ps.execute();

        List<String> list = new ArrayList<String>();
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {

            if (rs.getString(2) != null) {
                list.add(rs.getString(2));
            } else {
                list.add(rs.getString(1));

            }
        }

        return list;
    }

}
