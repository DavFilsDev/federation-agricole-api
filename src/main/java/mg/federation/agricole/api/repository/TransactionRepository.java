package mg.federation.agricole.api.repository;

import mg.federation.agricole.api.entity.TransactionEntity;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class TransactionRepository {

    public List<TransactionEntity> findByCollectivityIdAndDateRange(Connection conn, Long collectivityId, LocalDate from, LocalDate to) throws SQLException {
        String sql = "SELECT id, member_id, collectivity_id, amount, payment_mode, account_credited_id, membership_fee_id, creation_date " +
                "FROM transaction WHERE collectivity_id = ? AND creation_date BETWEEN ? AND ? " +
                "ORDER BY creation_date DESC";
        List<TransactionEntity> transactions = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, collectivityId);
            stmt.setDate(2, Date.valueOf(from));
            stmt.setDate(3, Date.valueOf(to));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                TransactionEntity transaction = new TransactionEntity();
                transaction.setId(rs.getLong("id"));
                transaction.setMemberId(rs.getLong("member_id"));
                transaction.setCollectivityId(rs.getLong("collectivity_id"));
                transaction.setAmount(rs.getBigDecimal("amount"));
                transaction.setPaymentMode(rs.getString("payment_mode"));
                transaction.setAccountCreditedId(rs.getLong("account_credited_id"));
                Long feeId = rs.getLong("membership_fee_id");
                if (!rs.wasNull()) {
                    transaction.setMembershipFeeId(feeId);
                }
                transaction.setCreationDate(rs.getDate("creation_date").toLocalDate());
                transactions.add(transaction);
            }
        }
        return transactions;
    }
}
