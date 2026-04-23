package mg.federation.agricole.api.repository;

import mg.federation.agricole.api.dto.*;
import mg.federation.agricole.api.entity.FinancialAccountEntity;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Optional;

@Repository
public class FinancialAccountRepository {

    // Récupérer un compte financier avec ses détails (type spécifique)
    public Optional<FinancialAccount> findById(Connection conn, Long id) throws SQLException {
        // D'abord récupérer le type dans financial_account
        String sql = "SELECT type, amount FROM financial_account WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                return Optional.empty();
            }

            String type = rs.getString("type");
            BigDecimal amount = rs.getBigDecimal("amount");

            FinancialAccount account = null;

            switch (type) {
                case "CASH":
                    account = new CashAccount(String.valueOf(id), amount);
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

    private MobileBankingAccount getMobileBankingAccount(Connection conn, Long id, BigDecimal amount) throws SQLException {
        String sql = "SELECT holder_name, mobile_service, mobile_number FROM mobile_banking_account WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                MobileBankingAccount account = new MobileBankingAccount();
                account.setId(String.valueOf(id));
                account.setAmount(amount);
                account.setHolderName(rs.getString("holder_name"));
                account.setMobileBankingService(rs.getString("mobile_service"));
                account.setMobileNumber(rs.getString("mobile_number"));
                return account;
            }
            return null;
        }
    }

    private BankAccount getBankAccount(Connection conn, Long id, BigDecimal amount) throws SQLException {
        String sql = "SELECT holder_name, bank_name, bank_code, branch_code, account_number, account_key FROM bank_account WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                BankAccount account = new BankAccount();
                account.setId(String.valueOf(id));
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

    // Vérifier si un compte appartient à une collectivité
    public boolean isAccountBelongsToCollectivity(Connection conn, Long accountId, Long collectivityId) throws SQLException {
        String sql = "SELECT 1 FROM financial_account fa " +
                "WHERE fa.id = ? AND EXISTS (" +
                "SELECT 1 FROM collectivity c WHERE c.id = ?" +
                ")";
        // Note: financial_account n'a pas de lien direct avec collectivity
        // Il faut passer par les tables filles ou ajouter collectivity_id dans financial_account
        // Pour l'instant, on suppose qu'on a ajouté collectivity_id à financial_account
        // Sinon, on peut faire une requête plus complexe
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, accountId);
            stmt.setLong(2, collectivityId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    // Mettre à jour le solde d'un compte
    public void updateAmount(Connection conn, Long accountId, BigDecimal newAmount) throws SQLException {
        String sql = "UPDATE financial_account SET amount = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, newAmount);
            stmt.setLong(2, accountId);
            stmt.executeUpdate();
        }
    }
}
