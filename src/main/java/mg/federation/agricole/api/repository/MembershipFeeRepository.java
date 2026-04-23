package mg.federation.agricole.api.repository;

import mg.federation.agricole.api.entity.MembershipFeeEntity;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class MembershipFeeRepository {

    // MODIFICATION 1: findByCollectivityId avec String collectivityId
    public List<MembershipFeeEntity> findByCollectivityId(Connection conn, String collectivityId) throws SQLException {
        String sql = "SELECT id, collectivity_id, eligible_from, frequency, amount, label, status " +
                "FROM membership_fee WHERE collectivity_id = ? ORDER BY eligible_from DESC";
        List<MembershipFeeEntity> fees = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, collectivityId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                MembershipFeeEntity fee = new MembershipFeeEntity();
                fee.setId(rs.getString("id"));
                fee.setCollectivityId(rs.getString("collectivity_id"));
                fee.setEligibleFrom(rs.getDate("eligible_from").toLocalDate());
                fee.setFrequency(rs.getString("frequency"));
                fee.setAmount(rs.getBigDecimal("amount"));
                fee.setLabel(rs.getString("label"));
                fee.setStatus(rs.getString("status"));
                fees.add(fee);
            }
        }
        return fees;
    }

    // MODIFICATION 2: insert avec id explicite (plus de RETURNING id)
    public void insert(Connection conn, MembershipFeeEntity fee) throws SQLException {
        String sql = "INSERT INTO membership_fee (id, collectivity_id, eligible_from, frequency, amount, label, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, fee.getId());
            stmt.setString(2, fee.getCollectivityId());
            stmt.setDate(3, Date.valueOf(fee.getEligibleFrom()));
            stmt.setString(4, fee.getFrequency());
            stmt.setBigDecimal(5, fee.getAmount());
            stmt.setString(6, fee.getLabel());
            stmt.setString(7, fee.getStatus() != null ? fee.getStatus() : "ACTIVE");
            stmt.executeUpdate();
        }
    }

    // MODIFICATION 3: existsDuplicate avec String collectivityId
    public boolean existsDuplicate(Connection conn, String collectivityId, LocalDate eligibleFrom, String frequency, BigDecimal amount) throws SQLException {
        String sql = "SELECT 1 FROM membership_fee WHERE collectivity_id = ? AND eligible_from = ? AND frequency = ? AND amount = ? LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, collectivityId);
            stmt.setDate(2, Date.valueOf(eligibleFrom));
            stmt.setString(3, frequency);
            stmt.setBigDecimal(4, amount);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    // MODIFICATION 4: findById avec String id
    public Optional<MembershipFeeEntity> findById(Connection conn, String id) throws SQLException {
        String sql = "SELECT id, collectivity_id, eligible_from, frequency, amount, label, status " +
                "FROM membership_fee WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                MembershipFeeEntity fee = new MembershipFeeEntity();
                fee.setId(rs.getString("id"));
                fee.setCollectivityId(rs.getString("collectivity_id"));
                fee.setEligibleFrom(rs.getDate("eligible_from").toLocalDate());
                fee.setFrequency(rs.getString("frequency"));
                fee.setAmount(rs.getBigDecimal("amount"));
                fee.setLabel(rs.getString("label"));
                fee.setStatus(rs.getString("status"));
                return Optional.of(fee);
            }
            return Optional.empty();
        }
    }
}