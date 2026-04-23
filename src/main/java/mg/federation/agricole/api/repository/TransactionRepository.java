package mg.federation.agricole.api.repository;

import mg.federation.agricole.api.entity.TransactionEntity;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class TransactionRepository {

    public List<TransactionEntity> findByCollectivityIdAndDateRange(Connection conn, String collectivityId, LocalDate from, LocalDate to) throws SQLException {
        String sql = "SELECT id, member_id, collectivity_id, amount, payment_mode, account_credited_id, membership_fee_id, creation_date " +
                "FROM transaction WHERE collectivity_id = ? AND creation_date BETWEEN ? AND ? " +
                "ORDER BY creation_date DESC";
        List<TransactionEntity> transactions = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, collectivityId);
            stmt.setDate(2, Date.valueOf(from));
            stmt.setDate(3, Date.valueOf(to));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                TransactionEntity transaction = new TransactionEntity();
                transaction.setId(rs.getString("id"));
                transaction.setMemberId(rs.getString("member_id"));
                transaction.setCollectivityId(rs.getString("collectivity_id"));
                transaction.setAmount(rs.getBigDecimal("amount"));
                transaction.setPaymentMode(rs.getString("payment_mode"));
                transaction.setAccountCreditedId(rs.getString("account_credited_id"));
                String feeId = rs.getString("membership_fee_id");
                if (!rs.wasNull()) {
                    transaction.setMembershipFeeId(feeId);
                }
                transaction.setCreationDate(rs.getDate("creation_date").toLocalDate());
                transactions.add(transaction);
            }
        }
        return transactions;
    }

    public void insert(Connection conn, TransactionEntity transaction) throws SQLException {
        String sql = "INSERT INTO transaction (id, member_id, collectivity_id, amount, payment_mode, account_credited_id, membership_fee_id, creation_date) " +
                "VALUES (?, ?, ?, ?, CAST(? as payment_mode_enum), ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, transaction.getId());
            stmt.setString(2, transaction.getMemberId());
            stmt.setString(3, transaction.getCollectivityId());
            stmt.setBigDecimal(4, transaction.getAmount());
            stmt.setString(5, transaction.getPaymentMode());
            stmt.setString(6, transaction.getAccountCreditedId());
            if (transaction.getMembershipFeeId() != null) {
                stmt.setString(7, transaction.getMembershipFeeId());
            } else {
                stmt.setNull(7, Types.VARCHAR);
            }
            stmt.setDate(8, Date.valueOf(transaction.getCreationDate()));
            stmt.executeUpdate();
        }
    }
}