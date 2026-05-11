package com.group01.asm2.db;

import com.group01.asm2.configs.DatabaseConfig;
import com.group01.asm2.exceptions.AppException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class SqlExecutor {
    private SqlExecutor() {
    }

    public static <T> Optional<T> queryOne(
        String sql,
        SqlBinder binder,
        ResultSetMapper<T> mapper
    ) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            return queryOne(conn, sql, binder, mapper);
        } catch (AppException exception) {
            throw exception;
        } catch (Exception exception) {
            throw AppException.database("Database query failed.");
        }
    }

    public static <T> Optional<T> queryOne(
        Connection conn,
        String sql,
        SqlBinder binder,
        ResultSetMapper<T> mapper
    ) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            binder.bind(ps);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                return Optional.of(mapper.map(rs));
            }

        } catch (AppException exception) {
            throw exception;
        } catch (Exception exception) {
            throw AppException.database("Database query failed.");
        }
    }

    public static <T> List<T> queryMany(
        String sql,
        SqlBinder binder,
        ResultSetMapper<T> mapper
    ) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            return queryMany(conn, sql, binder, mapper);
        } catch (AppException exception) {
            throw exception;
        } catch (Exception exception) {
            throw AppException.database("Database query failed.");
        }
    }

    public static <T> List<T> queryMany(
        Connection conn,
        String sql,
        SqlBinder binder,
        ResultSetMapper<T> mapper
    ) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            binder.bind(ps);

            List<T> results = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapper.map(rs));
                }
            }

            return results;

        } catch (AppException exception) {
            throw exception;
        } catch (Exception exception) {
            throw AppException.database("Database query failed.");
        }
    }

    public static int update(
        String sql,
        SqlBinder binder
    ) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            return update(conn, sql, binder);
        } catch (AppException exception) {
            throw exception;
        } catch (Exception exception) {
            throw AppException.database("Database update failed.");
        }
    }

    public static int update(
        Connection conn,
        String sql,
        SqlBinder binder
    ) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            binder.bind(ps);
            return ps.executeUpdate();

        } catch (AppException exception) {
            throw exception;
        } catch (Exception exception) {
            throw AppException.database("Database update failed.");
        }
    }
}