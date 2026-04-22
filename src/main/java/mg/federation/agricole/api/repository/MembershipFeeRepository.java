package mg.federation.agricole.api.repository;

import mg.federation.agricole.api.entity.MembershipFeeEntity;
import org.springframework.stereotype.Repository;

import java.sql.*;
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
}
