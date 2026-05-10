package com.group01.asm2.db;

import com.group01.asm2.configs.DatabaseConfig;
import com.group01.asm2.exceptions.AppException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

public final class SqlExecutor {

    private SqlExecutor() {
    }

    public static <T> Optional<T> queryOne(
        String sql,
        SqlBinder binder,
        ResultSetMapper<T> mapper
    ) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            binder.bind(ps);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                return Optional.of(mapper.map(rs));
            }

        } catch (Exception exception) {
            throw AppException.database("Database query failed.");
        }
    }

    public static int update(
        String sql,
        SqlBinder binder
    ) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            binder.bind(ps);
            return ps.executeUpdate();

        } catch (Exception exception) {
            throw AppException.database("Database update failed.");
        }
    }
}