// repository/MembershipRepository.java
package mg.federation.agricole.api.repository;

import mg.federation.agricole.api.entity.MembershipEntity;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

@Repository
public class MembershipRepository {

    public void insert(Connection conn, MembershipEntity membership) throws SQLException {
        String sql = "INSERT INTO membership (member_id, collectivity_id, occupation, registration_fee_paid, membership_dues_paid, date_adhesion, payment_date) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, membership.getMemberId());
            stmt.setLong(2, membership.getCollectivityId());
            stmt.setString(3, membership.getOccupation());
            stmt.setBoolean(4, membership.getRegistrationFeePaid());
            stmt.setBoolean(5, membership.getMembershipDuesPaid());
            stmt.setDate(6, Date.valueOf(membership.getDateAdhesion()));
            stmt.setDate(7, membership.getPaymentDate() != null ? Date.valueOf(membership.getPaymentDate()) : null);
            stmt.executeUpdate();
        }
    }

    public List<MembershipEntity> findByCollectivityId(Connection conn, Long collectivityId) throws SQLException {
        String sql = "SELECT member_id, collectivity_id, occupation, registration_fee_paid, membership_dues_paid, date_adhesion, payment_date FROM membership WHERE collectivity_id = ?";
        List<MembershipEntity> list = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, collectivityId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                MembershipEntity m = new MembershipEntity();
                m.setMemberId(rs.getLong("member_id"));
                m.setCollectivityId(rs.getLong("collectivity_id"));
                m.setOccupation(rs.getString("occupation"));
                m.setRegistrationFeePaid(rs.getBoolean("registration_fee_paid"));
                m.setMembershipDuesPaid(rs.getBoolean("membership_dues_paid"));
                m.setDateAdhesion(rs.getDate("date_adhesion").toLocalDate());
                Date pd = rs.getDate("payment_date");
                m.setPaymentDate(pd != null ? pd.toLocalDate() : null);
                list.add(m);
            }
        }
        return list;
    }

    public boolean hasSeniorRole(Connection conn, Long memberId) throws SQLException {
        String sql = "SELECT 1 FROM membership WHERE member_id = ? AND occupation = 'SENIOR' LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, memberId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    public int countRefereesInCollectivity(Connection conn, List<Long> refereeIds, Long collectivityId) throws SQLException {
        if (refereeIds.isEmpty()) return 0;
        String placeholders = String.join(",", Collections.nCopies(refereeIds.size(), "?"));
        String sql = "SELECT COUNT(DISTINCT member_id) FROM membership WHERE member_id IN (" + placeholders + ") AND collectivity_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < refereeIds.size(); i++) {
                stmt.setLong(i + 1, refereeIds.get(i));
            }
            stmt.setLong(refereeIds.size() + 1, collectivityId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
            return 0;
        }
    }
}