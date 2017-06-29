package com.turlygazhy.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by Yerassyl_Turlygazhy on 13-Jun-17.
 */
public class ScriptExecutor {
    private final Connection connection;

    public ScriptExecutor(Connection connection) {
        this.connection = connection;
    }

    public void execute(String script) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(script);
        ps.execute();
    }
}
