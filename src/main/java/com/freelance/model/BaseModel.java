package com.freelance.model;

import com.freelance.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class BaseModel {
    protected Connection connection;

    public BaseModel() {
        this.connection = DatabaseConfig.getConnection();
    }

    protected ResultSet executeQuery(String query, Object... params) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(query);
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
        return stmt.executeQuery();
    }

    protected int executeUpdate(String query, Object... params) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(query);
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
        return stmt.executeUpdate();
    }
} 