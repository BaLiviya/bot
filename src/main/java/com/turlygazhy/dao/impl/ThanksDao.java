package com.turlygazhy.dao.impl;

import com.turlygazhy.dao.AbstractDao;
import com.turlygazhy.entity.Thanks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bal on 26.06.17.
 */
public class ThanksDao extends AbstractDao {

    private final Connection connection;
    // 1. ID, 2. USER_ID, 3. DATE, 4. HAS_AUDIO, 5. VOICE_ID, 6. TEXT
    private static final String INSERT_INTO_THANKS = "INSERT INTO THANKS VALUES (default, ?, ?, ?, ?, ?,)";
    private static final String SELECT_FROM_THANKS = "SELECT * FROM THANKS WHERE USER_ID = ?";

    public ThanksDao(Connection connection) { this.connection = connection; }

    public Thanks insertThanks(Thanks thanks) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(INSERT_INTO_THANKS, PreparedStatement.RETURN_GENERATED_KEYS);
        ps.setLong(1, thanks.getUserId());
        ps.setString(2,thanks.getDate());
        ps.setBoolean(3, thanks.isHasAudio());
        if (thanks.isHasAudio()) {
            ps.setString(4, thanks.getVoiceMessageId());
            ps.setString(5, null);
        } else {
            ps.setString(5, thanks.getText());
            ps.setString(4, null);
        }
        ps.executeUpdate();
        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            thanks.setId(rs.getInt(1));
        }
        return thanks;
    }

    public List<Thanks> getThanks(Long chatID) throws SQLException {
        List<Thanks> thanks = new ArrayList<>();
        PreparedStatement ps = connection.prepareStatement(SELECT_FROM_THANKS);
        ps.setLong(1,chatID);
        ps.execute();
        ResultSet rs = ps.getResultSet();
        while (rs.next()) {
            thanks.add(parseThanks(rs));
        }
        return thanks;
    }

    private Thanks parseThanks(ResultSet rs) throws SQLException {
        Thanks thanks = new Thanks();
        thanks.setId(rs.getInt("ID"));
        thanks.setUserId(rs.getLong("USER_ID"));
        thanks.setDate(rs.getString("DATE"));
        thanks.setText(rs.getString("TEXT"));
        return thanks;
    }
}
