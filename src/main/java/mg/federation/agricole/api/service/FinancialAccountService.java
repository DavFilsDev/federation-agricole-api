package mg.federation.agricole.api.service;

import mg.federation.agricole.api.config.DataSource;
import mg.federation.agricole.api.dto.FinancialAccount;
import mg.federation.agricole.api.exception.BusinessRuleException;
import mg.federation.agricole.api.exception.ResourceNotFoundException;
import mg.federation.agricole.api.repository.CollectivityRepository;
import mg.federation.agricole.api.repository.FinancialAccountRepository;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@Service
public class FinancialAccountService {

    private final DataSource dataSource;
    private final CollectivityRepository collectivityRepository;
    private final FinancialAccountRepository financialAccountRepository;

    public FinancialAccountService(DataSource dataSource,
                                   CollectivityRepository collectivityRepository,
                                   FinancialAccountRepository financialAccountRepository) {
        this.dataSource = dataSource;
        this.collectivityRepository = collectivityRepository;
        this.financialAccountRepository = financialAccountRepository;
    }

    public List<FinancialAccount> getFinancialAccounts(String collectivityIdStr, LocalDate atDate) {
        try (Connection conn = dataSource.getConnection()) {
            // MODIFICATION: Plus besoin de parsing, c'est déjà une String
            String collectivityId = collectivityIdStr;

            // Vérifier que la collectivité existe
            if (collectivityRepository.findById(conn, collectivityId).isEmpty()) {
                throw new ResourceNotFoundException("Collectivity not found with id: " + collectivityIdStr);
            }

            // Si la date est fournie, récupérer les soldes à cette date
            if (atDate != null) {
                return financialAccountRepository.findByCollectivityIdAndDate(conn, collectivityId, atDate);
            } else {
                // Sinon, récupérer les soldes actuels
                return financialAccountRepository.findByCollectivityId(conn, collectivityId);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Database error while fetching financial accounts", e);
        }
    }
}