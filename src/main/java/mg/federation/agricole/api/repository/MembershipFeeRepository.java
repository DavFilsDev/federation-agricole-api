package mg.federation.agricole.api.repository;

import mg.federation.agricole.api.entity.MembershipFeeEntity;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MembershipFeeRepository {

    public List<MembershipFeeEntity> findByCollectivityId(Connection conn, Long collectivityId) throws SQLException {
        String sql = "SELECT id, collectivity_id, eligible_from, frequency, amount, label, status " +
                "FROM membership_fee WHERE collectivity_id = ? ORDER BY eligible_from DESC";
        List<MembershipFeeEntity> fees = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, collectivityId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                MembershipFeeEntity fee = new MembershipFeeEntity();
                fee.setId(rs.getLong("id"));
                fee.setCollectivityId(rs.getLong("collectivity_id"));
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

    // Nouvelle méthode : insérer un frais de cotisation
    public Long insert(Connection conn, MembershipFeeEntity fee) throws SQLException {
        String sql = "INSERT INTO membership_fee (collectivity_id, eligible_from, frequency, amount, label, status) " +
                "VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, fee.getCollectivityId());
            stmt.setDate(2, Date.valueOf(fee.getEligibleFrom()));
            stmt.setString(3, fee.getFrequency());
            stmt.setBigDecimal(4, fee.getAmount());
            stmt.setString(5, fee.getLabel());
            stmt.setString(6, fee.getStatus() != null ? fee.getStatus() : "ACTIVE");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
            throw new SQLException("Insert failed, no ID returned");
        }
    }

    // Vérifier si un frais de cotisation existe déjà avec les mêmes caractéristiques (optionnel)
    public boolean existsDuplicate(Connection conn, Long collectivityId, LocalDate eligibleFrom, String frequency, BigDecimal amount) throws SQLException {
        String sql = "SELECT 1 FROM membership_fee WHERE collectivity_id = ? AND eligible_from = ? AND frequency = ? AND amount = ? LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, collectivityId);
            stmt.setDate(2, Date.valueOf(eligibleFrom));
            stmt.setString(3, frequency);
            stmt.setBigDecimal(4, amount);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }
}