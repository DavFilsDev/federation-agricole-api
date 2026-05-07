package mg.federation.agricole.api.repository;

import mg.federation.agricole.api.dto.MemberOccupation;
import mg.federation.agricole.api.entity.ActivityEntity;
import mg.federation.agricole.api.dto.ActivityType;
import mg.federation.agricole.api.dto.WeeklyDay;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.sql.Date;
import java.util.*;

@Repository
public class ActivityRepository {

    public String insert(Connection conn, ActivityEntity activity) throws SQLException {
        String sql = "INSERT INTO collectivity_activity (id, collectivity_id, label, activity_type, recurrence_week_ordinal, recurrence_day_of_week, executive_date) VALUES (?, ?, ?, CAST(? AS activity_type_enum), ?, CAST(? AS weekly_day_enum), ?)";
        String id = UUID.randomUUID().toString();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.setString(2, activity.getCollectivityId());
            stmt.setString(3, activity.getLabel());
            stmt.setString(4, activity.getActivityType().name());

            if (activity.getRecurrenceWeekOrdinal() != null) {
                stmt.setInt(5, activity.getRecurrenceWeekOrdinal());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }

            if (activity.getRecurrenceDayOfWeek() != null) {
                stmt.setString(6, activity.getRecurrenceDayOfWeek().name());
            } else {
                stmt.setNull(6, Types.VARCHAR);
            }

            if (activity.getExecutiveDate() != null) {
                stmt.setDate(7, Date.valueOf(activity.getExecutiveDate()));
            } else {
                stmt.setNull(7, Types.DATE);
            }

            stmt.executeUpdate();
        }
        return id;
    }

    public void insertConcernedOccupations(Connection conn, String activityId, List<MemberOccupation> occupations) throws SQLException {
        String sql = "INSERT INTO activity_concerned_occupation (activity_id, occupation) VALUES (?, CAST(? AS member_occupation_enum))";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (MemberOccupation occupation : occupations) {
                stmt.setString(1, activityId);
                stmt.setString(2, occupation.name());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    public List<ActivityEntity> findByCollectivityId(Connection conn, String collectivityId) throws SQLException {
        String sql = "SELECT id, collectivity_id, label, activity_type, recurrence_week_ordinal, recurrence_day_of_week, executive_date, created_at FROM collectivity_activity WHERE collectivity_id = ? ORDER BY created_at DESC";
        List<ActivityEntity> activities = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, collectivityId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ActivityEntity activity = mapActivity(rs);
                activity.setMemberOccupationsConcerned(findOccupationsByActivityId(conn, activity.getId()));
                activities.add(activity);
            }
        }
        return activities;
    }

    public Optional<ActivityEntity> findById(Connection conn, String activityId) throws SQLException {
        String sql = "SELECT id, collectivity_id, label, activity_type, recurrence_week_ordinal, recurrence_day_of_week, executive_date, created_at FROM collectivity_activity WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, activityId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                ActivityEntity activity = mapActivity(rs);
                activity.setMemberOccupationsConcerned(findOccupationsByActivityId(conn, activity.getId()));
                return Optional.of(activity);
            }
            return Optional.empty();
        }
    }

    private List<MemberOccupation> findOccupationsByActivityId(Connection conn, String activityId) throws SQLException {
        String sql = "SELECT occupation FROM activity_concerned_occupation WHERE activity_id = ?";
        List<MemberOccupation> occupations = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, activityId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                occupations.add(MemberOccupation.valueOf(rs.getString("occupation")));
            }
        }
        return occupations;
    }

    private ActivityEntity mapActivity(ResultSet rs) throws SQLException {
        ActivityEntity activity = new ActivityEntity();
        activity.setId(rs.getString("id"));
        activity.setCollectivityId(rs.getString("collectivity_id"));
        activity.setLabel(rs.getString("label"));
        activity.setActivityType(ActivityType.valueOf(rs.getString("activity_type")));

        int weekOrdinal = rs.getInt("recurrence_week_ordinal");
        if (!rs.wasNull()) {
            activity.setRecurrenceWeekOrdinal(weekOrdinal);
        }

        String dayOfWeek = rs.getString("recurrence_day_of_week");
        if (dayOfWeek != null) {
            activity.setRecurrenceDayOfWeek(WeeklyDay.valueOf(dayOfWeek));
        }

        Date execDate = rs.getDate("executive_date");
        if (execDate != null) {
            activity.setExecutiveDate(execDate.toLocalDate());
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            activity.setCreatedAt(createdAt.toLocalDateTime());
        }

        return activity;
    }
}