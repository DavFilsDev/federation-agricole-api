package mg.federation.agricole.api.repository;

import mg.federation.agricole.api.dto.*;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class FinancialAccountRepository {

    public Optional<FinancialAccount> findById(Connection conn, String id) throws SQLException {
        String sql = "SELECT type, amount FROM financial_account WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                return Optional.empty();
            }

            String type = rs.getString("type");
            BigDecimal amount = rs.getBigDecimal("amount");

            FinancialAccount account = null;

            switch (type) {
                case "CASH":
                    account = new CashAccount(id, amount);
                    break;

                case "MOBILE_BANKING":
                    account = getMobileBankingAccount(conn, id, amount);
                    break;

                case "BANK":
                    account = getBankAccount(conn, id, amount);
                    break;
            }

            return Optional.ofNullable(account);
        }
    }

    // MODIFICATION 2: getMobileBankingAccount avec String id
    private MobileBankingAccount getMobileBankingAccount(Connection conn, String id, BigDecimal amount) throws SQLException {
        String sql = "SELECT holder_name, mobile_service, mobile_number FROM mobile_banking_account WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                MobileBankingAccount account = new MobileBankingAccount();
                account.setId(id);
                account.setAmount(amount);
                account.setHolderName(rs.getString("holder_name"));
                account.setMobileBankingService(rs.getString("mobile_service"));
                account.setMobileNumber(rs.getString("mobile_number"));
                return account;
            }
            return null;
        }
    }

    private BankAccount getBankAccount(Connection conn, String id, BigDecimal amount) throws SQLException {
        String sql = "SELECT holder_name, bank_name, bank_code, branch_code, account_number, account_key FROM bank_account WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                BankAccount account = new BankAccount();
                account.setId(id);
                account.setAmount(amount);
                account.setHolderName(rs.getString("holder_name"));
                account.setBankName(rs.getString("bank_name"));
                account.setBankCode(rs.getInt("bank_code"));
                account.setBankBranchCode(rs.getInt("branch_code"));
                account.setBankAccountNumber(rs.getInt("account_number"));
                account.setBankAccountKey(rs.getInt("account_key"));
                return account;
            }
            return null;
        }
    }

    public boolean isAccountBelongsToCollectivity(Connection conn, String accountId, String collectivityId) throws SQLException {
        String sql = "SELECT 1 FROM financial_account WHERE id = ? AND collectivity_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, accountId);
            stmt.setString(2, collectivityId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    public void updateAmount(Connection conn, String accountId, BigDecimal newAmount) throws SQLException {
        String sql = "UPDATE financial_account SET amount = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, newAmount);
            stmt.setString(2, accountId);
            stmt.executeUpdate();
        }
    }

    public List<FinancialAccount> findByCollectivityId(Connection conn, String collectivityId) throws SQLException {
        String sql = "SELECT id, type, amount FROM financial_account WHERE collectivity_id = ?";
        List<FinancialAccount> accounts = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, collectivityId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String id = rs.getString("id");
                String type = rs.getString("type");
                BigDecimal amount = rs.getBigDecimal("amount");

                FinancialAccount account = getAccountWithDetails(conn, id, type, amount);
                if (account != null) {
                    accounts.add(account);
                }
            }
        }
        return accounts;
    }

    // MODIFICATION 7: findByCollectivityIdAndDate avec String collectivityId
    public List<FinancialAccount> findByCollectivityIdAndDate(Connection conn, String collectivityId, LocalDate atDate) throws SQLException {
        String sql = "SELECT " +
                "  fa.id, " +
                "  fa.type, " +
                "  COALESCE(SUM(t.amount), 0) as calculated_amount " +
                "FROM financial_account fa " +
                "LEFT JOIN transaction t ON t.account_credited_id = fa.id AND t.creation_date <= ? " +
                "WHERE fa.collectivity_id = ? " +
                "GROUP BY fa.id, fa.type";

        List<FinancialAccount> accounts = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(atDate));
            stmt.setString(2, collectivityId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String id = rs.getString("id");
                String type = rs.getString("type");
                BigDecimal amount = rs.getBigDecimal("calculated_amount");

                FinancialAccount account = getAccountWithDetails(conn, id, type, amount);
                if (account != null) {
                    accounts.add(account);
                }
            }
        }
        return accounts;
    }

    private FinancialAccount getAccountWithDetails(Connection conn, String id, String type, BigDecimal amount) throws SQLException {
        switch (type) {
            case "CASH":
                CashAccount cashAccount = new CashAccount(id, amount);
                cashAccount.setType("CASH");
                return cashAccount;

            case "MOBILE_BANKING":
                String mobileSql = "SELECT holder_name, mobile_service, mobile_number FROM mobile_banking_account WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(mobileSql)) {
                    stmt.setString(1, id);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        MobileBankingAccount mobileAccount = new MobileBankingAccount();
                        mobileAccount.setId(id);
                        mobileAccount.setAmount(amount);
                        mobileAccount.setType("MOBILE_BANKING");
                        mobileAccount.setHolderName(rs.getString("holder_name"));
                        mobileAccount.setMobileBankingService(rs.getString("mobile_service"));
                        mobileAccount.setMobileNumber(rs.getString("mobile_number"));
                        return mobileAccount;
                    }
                }
                break;

            case "BANK":
                String bankSql = "SELECT holder_name, bank_name, bank_code, branch_code, account_number, account_key FROM bank_account WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(bankSql)) {
                    stmt.setString(1, id);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        BankAccount bankAccount = new BankAccount();
                        bankAccount.setId(id);
                        bankAccount.setAmount(amount);
                        bankAccount.setType("BANK");
                        bankAccount.setHolderName(rs.getString("holder_name"));
                        bankAccount.setBankName(rs.getString("bank_name"));
                        bankAccount.setBankCode(rs.getInt("bank_code"));
                        bankAccount.setBankBranchCode(rs.getInt("branch_code"));
                        bankAccount.setBankAccountNumber(rs.getInt("account_number"));
                        bankAccount.setBankAccountKey(rs.getInt("account_key"));
                        return bankAccount;
                    }
                }
                break;
        }
        return null;
    }
}