package com.group01.asm2.repositories;

import com.group01.asm2.db.SqlExecutor;
import com.group01.asm2.dtos.UserProfileStatisticsDto;
import com.group01.asm2.enums.UserRole;
import com.group01.asm2.exceptions.AppException;
import com.group01.asm2.models.User;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

/**
 * @author Group 01
 */
public class UserRepository {

    public User createUser(User user) {
        String sql = """
            INSERT INTO persons (
                full_name,
                date_of_birth,
                email,
                phone,
                address,
                username,
                password,
                role,
                balance,
                rating,
                completed_sales_count,
                created_at,
                updated_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            RETURNING id,
                      full_name,
                      date_of_birth,
                      email,
                      phone,
                      address,
                      username,
                      password,
                      role,
                      created_at,
                      updated_at,
                      balance,
                      rating,
                      completed_sales_count
        """;

        return SqlExecutor.queryOne(
            sql,
            ps -> {
                ps.setString(1, user.getFullName());

                if (user.getDateOfBirth() == null) {
                    ps.setNull(2, Types.DATE);
                } else {
                    ps.setDate(2, Date.valueOf(user.getDateOfBirth()));
                }

                ps.setString(3, user.getEmail());
                ps.setString(4, user.getPhone());
                ps.setString(5, user.getAddress());
                ps.setString(6, user.getUsername());
                ps.setString(7, user.getPassword());
                ps.setString(8, user.getRole().name());
                ps.setBigDecimal(9, user.getBalance());
                ps.setDouble(10, user.getRating());
                ps.setInt(11, user.getCompletedSalesCount());
            },
            this::mapRowToUser
        ).orElseThrow(() -> AppException.database("Could not create user."));
    }

    public User readUserProfile(Integer id) {
        String sql = """
            SELECT id,
                   full_name,
                   date_of_birth,
                   email,
                   phone,
                   address,
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

        return SqlExecutor.queryOne(
            sql,
            ps -> ps.setInt(1, id),
            this::mapRowToUser
        ).orElse(null);
    }

    public List<User> readUsers() {
        String sql = """
            SELECT id,
                   full_name,
                   date_of_birth,
                   email,
                   phone,
                   address,
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

        return SqlExecutor.queryMany(
            sql,
            ps -> {
            },
            this::mapRowToUser
        );
    }

    public User updateUserProfile(User user) {
        String sql = """
            UPDATE persons
            SET full_name = ?,
                date_of_birth = ?,
                email = ?,
                phone = ?,
                address = ?,
                username = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
              AND role IN ('BUYER', 'SELLER')
            RETURNING id,
                      full_name,
                      date_of_birth,
                      email,
                      phone,
                      address,
                      username,
                      password,
                      role,
                      created_at,
                      updated_at,
                      balance,
                      rating,
                      completed_sales_count
        """;

        return SqlExecutor.queryOne(
            sql,
            ps -> {
                ps.setString(1, user.getFullName());

                if (user.getDateOfBirth() == null) {
                    ps.setNull(2, Types.DATE);
                } else {
                    ps.setDate(2, Date.valueOf(user.getDateOfBirth()));
                }

                ps.setString(3, user.getEmail());
                ps.setString(4, user.getPhone());
                ps.setString(5, user.getAddress());
                ps.setString(6, user.getUsername());
                ps.setInt(7, user.getId());
            },
            this::mapRowToUser
        ).orElseThrow(() -> AppException.notFound("User profile not found."));
    }

    public int deleteUser(Integer id) {
        String sql = """
            DELETE FROM persons
            WHERE id = ?
              AND role IN ('BUYER', 'SELLER')
        """;

        return SqlExecutor.update(
            sql,
            ps -> ps.setInt(1, id)
        );
    }

    public boolean existsByUsernameExceptId(String username, Integer excludedUserId) {
        String sql = """
            SELECT 1
            FROM persons
            WHERE LOWER(username) = LOWER(?)
              AND (? IS NULL OR id <> ?)
            LIMIT 1
        """;

        return SqlExecutor.queryOne(
            sql,
            ps -> {
                ps.setString(1, username);

                if (excludedUserId == null) {
                    ps.setNull(2, Types.INTEGER);
                    ps.setNull(3, Types.INTEGER);
                } else {
                    ps.setInt(2, excludedUserId);
                    ps.setInt(3, excludedUserId);
                }
            },
            rs -> true
        ).orElse(false);
    }

    public boolean existsByEmailExceptId(String email, Integer excludedUserId) {
        String sql = """
            SELECT 1
            FROM persons
            WHERE LOWER(email) = LOWER(?)
              AND (? IS NULL OR id <> ?)
            LIMIT 1
        """;

        return SqlExecutor.queryOne(
            sql,
            ps -> {
                ps.setString(1, email);

                if (excludedUserId == null) {
                    ps.setNull(2, Types.INTEGER);
                    ps.setNull(3, Types.INTEGER);
                } else {
                    ps.setInt(2, excludedUserId);
                    ps.setInt(3, excludedUserId);
                }
            },
            rs -> true
        ).orElse(false);
    }

    public UserProfileStatisticsDto readProfileStatistics(Integer userId) {
        String sql = """
            SELECT p.balance,
                   p.rating,
                   COUNT(DISTINCT i.id) AS total_listings,
                   COUNT(DISTINCT CASE WHEN a.status = 'ACTIVE' THEN i.id END) AS active_listings,
                   COUNT(DISTINCT CASE WHEN a.status = 'SOLD' THEN i.id END) AS sold_listings
            FROM persons p
            LEFT JOIN items i ON i.seller_id = p.id
            LEFT JOIN auctions a ON a.item_id = i.id
            WHERE p.id = ?
              AND p.role IN ('BUYER', 'SELLER')
            GROUP BY p.id, p.balance, p.rating
        """;

        return SqlExecutor.queryOne(
            sql,
            ps -> ps.setInt(1, userId),
            rs -> new UserProfileStatisticsDto(
                getBigDecimalOrZero(rs, "balance"),
                rs.getInt("total_listings"),
                rs.getInt("active_listings"),
                rs.getInt("sold_listings"),
                rs.getDouble("rating")
            )
        ).orElse(UserProfileStatisticsDto.empty());
    }

    public UserProfileStatisticsDto readSellerStatistics(Integer sellerId) {
        String sql = """
            WITH sold_auction_prices AS (
                SELECT a.id AS auction_id,
                       MAX(b.amount) AS sale_price
                FROM auctions a
                INNER JOIN items i ON i.id = a.item_id
                LEFT JOIN bids b ON b.auction_id = a.id
                WHERE i.seller_id = ?
                  AND a.status = 'SOLD'
                GROUP BY a.id
            ),
            listing_counts AS (
                SELECT COUNT(DISTINCT i.id) AS total_listings,
                       COUNT(DISTINCT CASE WHEN a.status = 'SOLD' THEN i.id END) AS sold_listings
                FROM items i
                LEFT JOIN auctions a ON a.item_id = i.id
                WHERE i.seller_id = ?
            )
            SELECT COUNT(sap.auction_id) AS items_sold,
                   COALESCE(SUM(sap.sale_price), 0) AS total_revenue,
                   CASE
                       WHEN lc.total_listings = 0 THEN 0
                       ELSE (lc.sold_listings * 100.0 / lc.total_listings)
                   END AS sold_ratio
            FROM listing_counts lc
            LEFT JOIN sold_auction_prices sap ON TRUE
            GROUP BY lc.total_listings, lc.sold_listings
        """;

        UserProfileStatisticsDto statistics = SqlExecutor.queryOne(
            sql,
            ps -> {
                ps.setInt(1, sellerId);
                ps.setInt(2, sellerId);
            },
            rs -> {
                UserProfileStatisticsDto dto = new UserProfileStatisticsDto();

                dto.setItemsSold(rs.getInt("items_sold"));
                dto.setTotalRevenue(getBigDecimalOrZero(rs, "total_revenue"));
                dto.setSoldRatio(rs.getDouble("sold_ratio"));

                return dto;
            }
        ).orElse(UserProfileStatisticsDto.empty());

        fillAverageSalePriceByCategory(sellerId, statistics);
        fillListingTrend(sellerId, statistics);

        return statistics;
    }

    private void fillAverageSalePriceByCategory(
        Integer sellerId,
        UserProfileStatisticsDto statistics
    ) {
        String sql = """
            WITH sold_auction_prices AS (
                SELECT i.category_id,
                       MAX(b.amount) AS sale_price
                FROM auctions a
                INNER JOIN items i ON i.id = a.item_id
                LEFT JOIN bids b ON b.auction_id = a.id
                WHERE i.seller_id = ?
                  AND a.status = 'SOLD'
                GROUP BY a.id, i.category_id
            )
            SELECT category_id,
                   COALESCE(AVG(sale_price), 0) AS average_sale_price
            FROM sold_auction_prices
            GROUP BY category_id
            ORDER BY category_id ASC
        """;

        List<Map.Entry<Integer, BigDecimal>> rows = SqlExecutor.queryMany(
            sql,
            ps -> ps.setInt(1, sellerId),
            rs -> new AbstractMap.SimpleEntry<>(
                getIntegerOrNull(rs, "category_id"),
                getBigDecimalOrZero(rs, "average_sale_price")
            )
        );

        for (Map.Entry<Integer, BigDecimal> row : rows) {
            statistics.addAverageSalePriceByCategory(row.getKey(), row.getValue());
        }
    }

    private void fillListingTrend(
        Integer sellerId,
        UserProfileStatisticsDto statistics
    ) {
        String sql = """
            SELECT TO_CHAR(DATE_TRUNC('month', i.created_at), 'Mon') AS month_label,
                   COUNT(DISTINCT i.id) AS total_listings,
                   COUNT(DISTINCT CASE WHEN a.status = 'SOLD' THEN i.id END) AS sold_listings,
                   COUNT(DISTINCT CASE WHEN a.status IN ('UNSOLD', 'ENDED', 'CANCELLED') THEN i.id END) AS unsold_listings
            FROM items i
            LEFT JOIN auctions a ON a.item_id = i.id
            WHERE i.seller_id = ?
              AND i.created_at >= CURRENT_DATE - INTERVAL '6 months'
            GROUP BY DATE_TRUNC('month', i.created_at)
            ORDER BY DATE_TRUNC('month', i.created_at)
        """;

        List<Map.Entry<String, int[]>> rows = SqlExecutor.queryMany(
            sql,
            ps -> ps.setInt(1, sellerId),
            rs -> new AbstractMap.SimpleEntry<>(
                rs.getString("month_label"),
                new int[] {
                    rs.getInt("total_listings"),
                    rs.getInt("sold_listings"),
                    rs.getInt("unsold_listings")
                }
            )
        );

        for (Map.Entry<String, int[]> row : rows) {
            int[] values = row.getValue();

            statistics.addListingTrend(
                row.getKey(),
                values[0],
                values[1],
                values[2]
            );
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
            resultSet.getString("address"),
            resultSet.getString("username"),
            resultSet.getString("password"),
            UserRole.valueOf(resultSet.getString("role")),
            getLocalDateTime(resultSet, "created_at"),
            getLocalDateTime(resultSet, "updated_at"),
            resultSet.getBigDecimal("balance"),
            resultSet.getDouble("rating"),
            resultSet.getInt("completed_sales_count")
        );
    }

    private BigDecimal getBigDecimalOrZero(ResultSet rs, String columnName) throws Exception {
        BigDecimal value = rs.getBigDecimal(columnName);
        return value != null ? value : BigDecimal.ZERO;
    }

    private Integer getIntegerOrNull(ResultSet rs, String columnName) throws Exception {
        Object value = rs.getObject(columnName);

        if (value == null) {
            return null;
        }

        return ((Number) value).intValue();
    }

    private java.time.LocalDateTime getLocalDateTime(ResultSet rs, String columnName) throws Exception {
        Timestamp timestamp = rs.getTimestamp(columnName);
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}