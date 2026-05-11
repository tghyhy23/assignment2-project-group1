package com.group01.asm2.repositories;

/**
 * @author Group 01
 */

import com.group01.asm2.configs.DatabaseConfig;
import com.group01.asm2.enums.UserRole;
import com.group01.asm2.exceptions.AppException;
import com.group01.asm2.models.User;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {

    public User createUser(User user) {
        String sql = """
            INSERT INTO persons (
                full_name,
                date_of_birth,
                email,
                phone,
                username,
                password,
                role,
                balance,
                rating,
                completed_sales_count,
                created_at,
                updated_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        """;

        try (
            Connection connection = DatabaseConfig.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS
            )
        ) {
            statement.setString(1, user.getFullName());
            statement.setDate(2, user.getDateOfBirth() == null ? null : Date.valueOf(user.getDateOfBirth()));
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getPhone());
            statement.setString(5, user.getUsername());
            statement.setString(6, user.getPassword());
            statement.setString(7, user.getRole().name());
            statement.setBigDecimal(8, user.getBalance());
            statement.setDouble(9, user.getRating());
            statement.setInt(10, user.getCompletedSalesCount());

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw AppException.database("Could not create user.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw AppException.database("Could not read created user ID.");
                }

                return readUserById(generatedKeys.getInt(1));
            }

        } catch (AppException exception) {
            throw exception;
        } catch (Exception exception) {
            exception.printStackTrace();
            throw AppException.database("Could not create user.");
        }
    }

    public User readUserById(Integer id) {
        String sql = """
            SELECT id,
                   full_name,
                   date_of_birth,
                   email,
                   phone,
                   username,
                   password,
                   role,
                   created_at,
                   updated_at,
                   balance,
                   rating,
                   completed_sales_count
            FROM persons
            WHERE id = ?
              AND role IN ('BUYER', 'SELLER')
        """;

        try (
            Connection connection = DatabaseConfig.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                return mapRowToUser(resultSet);
            }

        } catch (Exception exception) {
            exception.printStackTrace();
            throw AppException.database("Could not read user profile.");
        }
    }

    public List<User> readUsers() {
        String sql = """
            SELECT id,
                   full_name,
                   date_of_birth,
                   email,
                   phone,
                   username,
                   password,
                   role,
                   created_at,
                   updated_at,
                   balance,
                   rating,
                   completed_sales_count
            FROM persons
            WHERE role IN ('BUYER', 'SELLER')
            ORDER BY id ASC
        """;

        try (
            Connection connection = DatabaseConfig.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery()
        ) {
            List<User> users = new ArrayList<>();

            while (resultSet.next()) {
                users.add(mapRowToUser(resultSet));
            }

            return users;

        } catch (Exception exception) {
            exception.printStackTrace();
            throw AppException.database("Could not read users.");
        }
    }

    public User updateUserProfile(User user) {
        String sql = """
            UPDATE persons
            SET full_name = ?,
                date_of_birth = ?,
                email = ?,
                phone = ?,
                username = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
              AND role IN ('BUYER', 'SELLER')
        """;

        try (
            Connection connection = DatabaseConfig.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, user.getFullName());
            statement.setDate(2, user.getDateOfBirth() == null ? null : Date.valueOf(user.getDateOfBirth()));
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getPhone());
            statement.setString(5, user.getUsername());
            statement.setInt(6, user.getId());

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw AppException.notFound("User profile not found.");
            }

            return readUserById(user.getId());

        } catch (AppException exception) {
            throw exception;
        } catch (Exception exception) {
            exception.printStackTrace();
            throw AppException.database("Could not update user profile.");
        }
    }

    public int deleteUser(Integer id) {
        String sql = """
            DELETE FROM persons
            WHERE id = ?
              AND role IN ('BUYER', 'SELLER')
        """;

        try (
            Connection connection = DatabaseConfig.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, id);

            return statement.executeUpdate();

        } catch (Exception exception) {
            exception.printStackTrace();
            throw AppException.database("Could not delete user.");
        }
    }

    public boolean existsByUsernameExceptId(String username, Integer excludedUserId) {
        String sql;

        if (excludedUserId == null) {
            sql = """
                SELECT 1
                FROM persons
                WHERE LOWER(username) = LOWER(?)
                LIMIT 1
            """;
        } else {
            sql = """
                SELECT 1
                FROM persons
                WHERE LOWER(username) = LOWER(?)
                  AND id <> ?
                LIMIT 1
            """;
        }

        try (
            Connection connection = DatabaseConfig.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, username);

            if (excludedUserId != null) {
                statement.setInt(2, excludedUserId);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (Exception exception) {
            exception.printStackTrace();
            throw AppException.database("Could not check username availability.");
        }
    }

    public boolean existsByEmailExceptId(String email, Integer excludedUserId) {
        String sql;

        if (excludedUserId == null) {
            sql = """
                SELECT 1
                FROM persons
                WHERE LOWER(email) = LOWER(?)
                LIMIT 1
            """;
        } else {
            sql = """
                SELECT 1
                FROM persons
                WHERE LOWER(email) = LOWER(?)
                  AND id <> ?
                LIMIT 1
            """;
        }

        try (
            Connection connection = DatabaseConfig.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, email);

            if (excludedUserId != null) {
                statement.setInt(2, excludedUserId);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (Exception exception) {
            exception.printStackTrace();
            throw AppException.database("Could not check email availability.");
        }
    }

    private User mapRowToUser(ResultSet resultSet) throws Exception {
        return new User(
            resultSet.getInt("id"),
            resultSet.getString("full_name"),
            resultSet.getDate("date_of_birth") == null
                ? null
                : resultSet.getDate("date_of_birth").toLocalDate(),
            resultSet.getString("email"),
            resultSet.getString("phone"),
            resultSet.getString("username"),
            resultSet.getString("password"),
            UserRole.valueOf(resultSet.getString("role")),
            resultSet.getTimestamp("created_at") == null
                ? null
                : resultSet.getTimestamp("created_at").toLocalDateTime(),
            resultSet.getTimestamp("updated_at") == null
                ? null
                : resultSet.getTimestamp("updated_at").toLocalDateTime(),
            resultSet.getBigDecimal("balance"),
            resultSet.getDouble("rating"),
            resultSet.getInt("completed_sales_count")
        );
    }
}