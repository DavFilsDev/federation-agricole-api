package mg.federation.agricole.api.service;

import mg.federation.agricole.api.config.DataSource;
import mg.federation.agricole.api.dto.CollectivityLocalStatistics;
import mg.federation.agricole.api.dto.CollectivityOverallStatistics;
import mg.federation.agricole.api.exception.BusinessRuleException;
import mg.federation.agricole.api.exception.ResourceNotFoundException;
import mg.federation.agricole.api.repository.CollectivityRepository;
import mg.federation.agricole.api.repository.StatisticsRepository;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@Service
public class StatisticsService {

    private final DataSource dataSource;
    private final CollectivityRepository collectivityRepository;
    private final StatisticsRepository statisticsRepository;

    public StatisticsService(DataSource dataSource,
                             CollectivityRepository collectivityRepository,
                             StatisticsRepository statisticsRepository) {
        this.dataSource = dataSource;
        this.collectivityRepository = collectivityRepository;
        this.statisticsRepository = statisticsRepository;
    }

    public List<CollectivityLocalStatistics> getCollectivityLocalStatistics(String collectivityId, LocalDate from, LocalDate to) {
        // Validation des dates
        if (from == null || to == null) {
            throw new BusinessRuleException("Query parameters 'from' and 'to' are mandatory");
        }
        if (from.isAfter(to)) {
            throw new BusinessRuleException("'from' date cannot be after 'to' date");
        }

        try (Connection conn = dataSource.getConnection()) {
            // Vérifier que la collectivité existe
            if (collectivityRepository.findById(conn, collectivityId).isEmpty()) {
                throw new ResourceNotFoundException("Collectivity not found with id: " + collectivityId);
            }

            return statisticsRepository.getCollectivityLocalStatistics(conn, collectivityId, from, to);

        } catch (SQLException e) {
            throw new RuntimeException("Database error while fetching statistics", e);
        }
    }

    public List<CollectivityOverallStatistics> getOverallStatistics(LocalDate from, LocalDate to) {
        // Validation des dates
        if (from == null || to == null) {
            throw new BusinessRuleException("Query parameters 'from' and 'to' are mandatory");
        }
        if (from.isAfter(to)) {
            throw new BusinessRuleException("'from' date cannot be after 'to' date");
        }

        try (Connection conn = dataSource.getConnection()) {
            return statisticsRepository.getOverallStatistics(conn, from, to);

        } catch (SQLException e) {
            throw new RuntimeException("Database error while fetching overall statistics", e);
        }
    }
}