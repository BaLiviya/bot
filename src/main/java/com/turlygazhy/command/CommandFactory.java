package com.turlygazhy.command;

import com.turlygazhy.command.impl.*;
import com.turlygazhy.exception.NotRealizedMethodException;

import java.sql.SQLException;

/**
 * Created by user on 1/2/17.
 */
public class CommandFactory {
    public static Command getCommand(long id) throws SQLException {
        CommandType type = CommandType.getType(id);
        switch (type) {
            case ADD_NEW_TASK:
                return new AddNewTaskCommand();
            case SHOW_TASKS:
                return new ShowTasksCommand();
            case SHOW_INFO:
                return new ShowInfoCommand();
            case SHOW_WORKERS_MENU:
                return new ShowWorkerMenuCommand();
            case SHOW_STATISTIC:
                return new ShowStatisticCommand();
            case SHOW_THANKS:
                return new ShowThanksCommand();
            case ADD_NEW_THANKS:
                return new AddNewThanksCommand();
            default:
                throw new NotRealizedMethodException("Not realized for type: " + type);
        }
    }
}
