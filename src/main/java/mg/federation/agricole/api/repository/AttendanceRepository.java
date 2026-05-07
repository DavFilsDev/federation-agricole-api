package mg.federation.agricole.api.repository;

import mg.federation.agricole.api.dto.AttendanceStatus;
import mg.federation.agricole.api.entity.AttendanceEntity;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

@Repository
public class AttendanceRepository {

    public boolean isStatusConfirmed(Connection conn, String activityId, String memberId) throws SQLException {
        String sql = "SELECT status FROM activity_member_attendance WHERE activity_id = ? AND member_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, activityId);
            stmt.setString(2, memberId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String status = rs.getString("status");
                return "ATTENDED".equals(status) || "MISSING".equals(status);
            }
            return false;
        }
    }

    public String upsertAttendance(Connection conn, String activityId, String memberId, AttendanceStatus status) throws SQLException {
        String selectSql = "SELECT id, status FROM activity_member_attendance WHERE activity_id = ? AND member_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
            stmt.setString(1, activityId);
            stmt.setString(2, memberId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String existingStatus = rs.getString("status");
                if ("ATTENDED".equals(existingStatus) || "MISSING".equals(existingStatus)) {
                    return rs.getString("id");
                }

                String updateSql = "UPDATE activity_member_attendance SET status = CAST(? AS attendance_status_enum), updated_at = ? WHERE id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setString(1, status.name());
                    updateStmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                    updateStmt.setString(3, rs.getString("id"));
                    updateStmt.executeUpdate();
                    return rs.getString("id");
                }
            } else {
                String insertSql = "INSERT INTO activity_member_attendance (id, activity_id, member_id, status) VALUES (?, ?, ?, CAST(? AS attendance_status_enum))";
                String id = UUID.randomUUID().toString();
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, id);
                    insertStmt.setString(2, activityId);
                    insertStmt.setString(3, memberId);
                    insertStmt.setString(4, status.name());
                    insertStmt.executeUpdate();
                    return id;
                }
            }
        }
    }

    public List<AttendanceEntity> findByActivityId(Connection conn, String activityId) throws SQLException {
        String sql = "SELECT id, activity_id, member_id, status, created_at, updated_at FROM activity_member_attendance WHERE activity_id = ?";
        List<AttendanceEntity> attendances = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, activityId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                AttendanceEntity attendance = mapAttendance(rs);
                attendances.add(attendance);
            }
        }
        return attendances;
    }

    public Optional<AttendanceEntity> findByActivityIdAndMemberId(Connection conn, String activityId, String memberId) throws SQLException {
        String sql = "SELECT id, activity_id, member_id, status, created_at, updated_at FROM activity_member_attendance WHERE activity_id = ? AND member_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, activityId);
            stmt.setString(2, memberId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapAttendance(rs));
            }
            return Optional.empty();
        }
    }

    private AttendanceEntity mapAttendance(ResultSet rs) throws SQLException {
        AttendanceEntity attendance = new AttendanceEntity();
        attendance.setId(rs.getString("id"));
        attendance.setActivityId(rs.getString("activity_id"));
        attendance.setMemberId(rs.getString("member_id"));
        attendance.setStatus(AttendanceStatus.valueOf(rs.getString("status")));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            attendance.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            attendance.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return attendance;
    }
}