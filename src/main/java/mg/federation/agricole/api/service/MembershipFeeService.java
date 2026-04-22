package mg.federation.agricole.api.service;

import mg.federation.agricole.api.config.DataSource;
import mg.federation.agricole.api.dto.MembershipFee;
import mg.federation.agricole.api.entity.MembershipFeeEntity;
import mg.federation.agricole.api.exception.ResourceNotFoundException;
import mg.federation.agricole.api.repository.CollectivityRepository;
import mg.federation.agricole.api.repository.MembershipFeeRepository;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MembershipFeeService {

    private final DataSource dataSource;
    private final CollectivityRepository collectivityRepository;
    private final MembershipFeeRepository membershipFeeRepository;

    public MembershipFeeService(DataSource dataSource,
                                CollectivityRepository collectivityRepository,
                                MembershipFeeRepository membershipFeeRepository) {
        this.dataSource = dataSource;
        this.collectivityRepository = collectivityRepository;
        this.membershipFeeRepository = membershipFeeRepository;
    }

    public List<MembershipFee> getMembershipFees(String collectivityIdStr) {
        try (Connection conn = dataSource.getConnection()) {
            Long collectivityId = Long.parseLong(collectivityIdStr);

            // Vérifier que la collectivité existe
            if (collectivityRepository.findById(conn, collectivityId).isEmpty()) {
                throw new ResourceNotFoundException("Collectivity not found with id: " + collectivityIdStr);
            }

            // Récupérer les frais de cotisation
            List<MembershipFeeEntity> entities = membershipFeeRepository.findByCollectivityId(conn, collectivityId);

            // Convertir en DTOs
            return entities.stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());

        } catch (SQLException e) {
            throw new RuntimeException("Database error while fetching membership fees", e);
        }
    }

    private MembershipFee toDto(MembershipFeeEntity entity) {
        MembershipFee dto = new MembershipFee();
        dto.setId(String.valueOf(entity.getId()));
        dto.setEligibleFrom(entity.getEligibleFrom());
        dto.setFrequency(entity.getFrequency());
        dto.setAmount(entity.getAmount());
        dto.setLabel(entity.getLabel());
        dto.setStatus(entity.getStatus());
        return dto;
    }
}