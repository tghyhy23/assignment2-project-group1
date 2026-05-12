package com.group01.asm2.seeds;

import com.group01.asm2.enums.ActivityActionType;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class ActivityLogSeed implements Seeder {

    @Override
    public void seed(Connection conn) throws Exception {
        insertActivityLog(
            conn,
            "systemadmin",
            ActivityActionType.CREATE_CATEGORY,
            "Category",
            null,
            "Seeded default auction categories."
        );

        insertActivityLog(
            conn,
            "seller1",
            ActivityActionType.CREATE_ITEM,
            "Item",
            null,
            "Seeded seller item listings."
        );

        insertActivityLog(
            conn,
            "auctionadmin",
            ActivityActionType.CREATE_AUCTION,
            "Auction",
            null,
            "Seeded default auctions."
        );

        insertActivityLog(
            conn,
            "buyer1",
            ActivityActionType.PLACE_BID,
            "Bid",
            null,
            "Seeded buyer bid history."
        );

        insertActivityLog(
            conn,
            "buyer2",
            ActivityActionType.REQUEST_TOP_UP,
            "Payment",
            null,
            "Seeded sample payment and balance activity."
        );
    }

    private void insertActivityLog(
        Connection conn,
        String actorUsername,
        ActivityActionType actionType,
        String targetEntity,
        Integer targetId,
        String description
    ) throws Exception {
        String sql = """
            INSERT INTO activity_logs (
                actor_id,
                actor_role,
                action_type,
                target_entity,
                target_id,
                description
            )
            SELECT p.id, p.role, ?, ?, ?, ?
            FROM persons p
            WHERE LOWER(p.username) = LOWER(?)
              AND NOT EXISTS (
                  SELECT 1
                  FROM activity_logs al
                  WHERE al.actor_id = p.id
                    AND al.action_type = ?
                    AND al.description = ?
              )
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, actionType.name());
            ps.setString(2, targetEntity);

            if (targetId == null) {
                ps.setObject(3, null);
            } else {
                ps.setInt(3, targetId);
            }

            ps.setString(4, description);
            ps.setString(5, actorUsername.trim().toLowerCase());

            ps.setString(6, actionType.name());
            ps.setString(7, description);

            ps.executeUpdate();
        }
    }
}