package mg.federation.agricole.api.repository;

import mg.federation.agricole.api.dto.*;
import mg.federation.agricole.api.entity.FinancialAccountEntity;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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

    // Récupérer tous les comptes d'une collectivité
    public List<FinancialAccount> findByCollectivityId(Connection conn, Long collectivityId) throws SQLException {
        String sql = "SELECT id, type, amount FROM financial_account WHERE collectivity_id = ?";
        List<FinancialAccount> accounts = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, collectivityId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Long id = rs.getLong("id");
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

    // Récupérer les comptes d'une collectivité à une date donnée (solde historique)
    public List<FinancialAccount> findByCollectivityIdAndDate(Connection conn, Long collectivityId, LocalDate atDate) throws SQLException {
        // Pour le solde à une date donnée, on doit calculer la somme des transactions avant cette date
        // Pour chaque compte, solde = somme des transactions où account_credited_id = compte ET creation_date <= atDate
        // Moins les éventuels débits (si on avait des comptes avec débits)

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
            stmt.setLong(2, collectivityId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Long id = rs.getLong("id");
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

    // Méthode utilitaire pour récupérer un compte avec ses détails spécifiques
    private FinancialAccount getAccountWithDetails(Connection conn, Long id, String type, BigDecimal amount) throws SQLException {
        switch (type) {
            case "CASH":
                CashAccount cashAccount = new CashAccount(String.valueOf(id), amount);
                cashAccount.setType("CASH");
                return cashAccount;

            case "MOBILE_BANKING":
                String mobileSql = "SELECT holder_name, mobile_service, mobile_number FROM mobile_banking_account WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(mobileSql)) {
                    stmt.setLong(1, id);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        MobileBankingAccount mobileAccount = new MobileBankingAccount();
                        mobileAccount.setId(String.valueOf(id));
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
                    stmt.setLong(1, id);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        BankAccount bankAccount = new BankAccount();
                        bankAccount.setId(String.valueOf(id));
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
