package com.group01.asm2.repositories;

import com.group01.asm2.exceptions.AppException;
import com.group01.asm2.models.TopUpRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Group 01
 */
public class TopUpRequestRepository {

    public TopUpRequest createTopUpRequest(Connection connection, TopUpRequest topUpRequest) {
        if (connection == null) {
            throw AppException.database("Database connection is required to create top-up request.");
        }

        String sql = """
            INSERT INTO top_up_requests (
                user_id,
                amount,
                status,
                created_at
            )
            VALUES (?, ?, ?, CURRENT_TIMESTAMP)
            RETURNING id, user_id, amount, status, created_at
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            bindCreateTopUpRequest(ps, topUpRequest);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapTopUpRequest(rs);
                }
            }

            throw AppException.database("Failed to create top-up request.");
        } catch (AppException ex) {
            throw ex;
        } catch (Exception ex) {
            throw AppException.database("Failed to create top-up request: " + ex.getMessage());
        }
    }

    public Optional<TopUpRequest> readTopUpRequestById(Connection connection, Integer id) {
        if (connection == null) {
            throw AppException.database("Database connection is required to read top-up request.");
        }

        String sql = """
            SELECT id, user_id, amount, status, created_at
            FROM top_up_requests
            WHERE id = ?
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapTopUpRequest(rs));
                }
            }

            return Optional.empty();
        } catch (AppException ex) {
            throw ex;
        } catch (Exception ex) {
            throw AppException.database("Failed to read top-up request: " + ex.getMessage());
        }
    }

    public Optional<TopUpRequest> readLatestPendingRequestByUserId(Connection connection, Integer userId) {
        if (connection == null) {
            throw AppException.database("Database connection is required to read pending top-up request.");
        }

        String sql = """
            SELECT id, user_id, amount, status, created_at
            FROM top_up_requests
            WHERE user_id = ?
              AND LOWER(status) = LOWER(?)
            ORDER BY created_at DESC
            LIMIT 1
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, TopUpRequest.STATUS_PENDING);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapTopUpRequest(rs));
                }
            }

            return Optional.empty();
        } catch (AppException ex) {
            throw ex;
        } catch (Exception ex) {
            throw AppException.database("Failed to read pending top-up request: " + ex.getMessage());
        }
    }

    public List<TopUpRequest> readTopUpRequestsByUserId(Connection connection, Integer userId) {
        if (connection == null) {
            throw AppException.database("Database connection is required to read user top-up requests.");
        }

        String sql = """
            SELECT id, user_id, amount, status, created_at
            FROM top_up_requests
            WHERE user_id = ?
            ORDER BY created_at DESC
        """;

        List<TopUpRequest> requests = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    requests.add(mapTopUpRequest(rs));
                }
            }

            return requests;
        } catch (AppException ex) {
            throw ex;
        } catch (Exception ex) {
            throw AppException.database("Failed to read user top-up requests: " + ex.getMessage());
        }
    }

    public List<TopUpRequest> readPendingTopUpRequests(Connection connection) {
        if (connection == null) {
            throw AppException.database("Database connection is required to read pending top-up requests.");
        }

        String sql = """
            SELECT id, user_id, amount, status, created_at
            FROM top_up_requests
            WHERE LOWER(status) = LOWER(?)
            ORDER BY created_at ASC
        """;

        List<TopUpRequest> requests = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, TopUpRequest.STATUS_PENDING);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    requests.add(mapTopUpRequest(rs));
                }
            }

            return requests;
        } catch (AppException ex) {
            throw ex;
        } catch (Exception ex) {
            throw AppException.database("Failed to read pending top-up requests: " + ex.getMessage());
        }
    }

    public boolean updateTopUpRequestStatus(Connection connection, Integer id, String status) {
        if (connection == null) {
            throw AppException.database("Database connection is required to update top-up request.");
        }

        String sql = """
            UPDATE top_up_requests
            SET status = ?
            WHERE id = ?
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);

            return ps.executeUpdate() > 0;
        } catch (AppException ex) {
            throw ex;
        } catch (Exception ex) {
            throw AppException.database("Failed to update top-up request status: " + ex.getMessage());
        }
    }

    public boolean deleteTopUpRequest(Connection connection, Integer id) {
        if (connection == null) {
            throw AppException.database("Database connection is required to delete top-up request.");
        }

        String sql = """
            DELETE FROM top_up_requests
            WHERE id = ?
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (AppException ex) {
            throw ex;
        } catch (Exception ex) {
            throw AppException.database("Failed to delete top-up request: " + ex.getMessage());
        }
    }

    private void bindCreateTopUpRequest(PreparedStatement ps, TopUpRequest topUpRequest) throws Exception {
        if (topUpRequest == null) {
            throw AppException.database("Top-up request is required.");
        }

        ps.setInt(1, topUpRequest.getUserId());
        ps.setDouble(2, topUpRequest.getAmount());
        ps.setString(3, topUpRequest.getStatus() == null ? TopUpRequest.STATUS_PENDING : topUpRequest.getStatus());
    }

    private TopUpRequest mapTopUpRequest(ResultSet rs) throws Exception {
        Timestamp createdAt = rs.getTimestamp("created_at");

        return new TopUpRequest(
            rs.getInt("id"),
            rs.getInt("user_id"),
            rs.getDouble("amount"),
            rs.getString("status"),
            createdAt == null ? null : createdAt.toLocalDateTime()
        );
    }
}