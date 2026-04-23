package mg.federation.agricole.api.service;

import mg.federation.agricole.api.config.DataSource;
import mg.federation.agricole.api.dto.CreateMembershipFee;
import mg.federation.agricole.api.dto.MembershipFee;
import mg.federation.agricole.api.entity.MembershipFeeEntity;
import mg.federation.agricole.api.exception.BusinessRuleException;
import mg.federation.agricole.api.exception.ResourceNotFoundException;
import mg.federation.agricole.api.repository.CollectivityRepository;
import mg.federation.agricole.api.repository.MembershipFeeRepository;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MembershipFeeService {

    private final DataSource dataSource;
    private final CollectivityRepository collectivityRepository;
    private final MembershipFeeRepository membershipFeeRepository;

    // Liste des fréquences valides (selon l'enum Frequency)
    private static final List<String> VALID_FREQUENCIES = List.of("WEEKLY", "MONTHLY", "ANNUALLY", "PUNCTUALLY");

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

            if (collectivityRepository.findById(conn, collectivityId).isEmpty()) {
                throw new ResourceNotFoundException("Collectivity not found with id: " + collectivityIdStr);
            }

            List<MembershipFeeEntity> entities = membershipFeeRepository.findByCollectivityId(conn, collectivityId);
            return entities.stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());

        } catch (SQLException e) {
            throw new RuntimeException("Database error while fetching membership fees", e);
        }
    }

    // Nouvelle méthode : créer des frais de cotisation
    public List<MembershipFee> createMembershipFees(String collectivityIdStr, List<CreateMembershipFee> createFees) {
        if (createFees == null || createFees.isEmpty()) {
            throw new BusinessRuleException("At least one membership fee must be provided");
        }

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Long collectivityId = Long.parseLong(collectivityIdStr);

                // Vérifier que la collectivité existe
                if (collectivityRepository.findById(conn, collectivityId).isEmpty()) {
                    throw new ResourceNotFoundException("Collectivity not found with id: " + collectivityIdStr);
                }

                List<MembershipFee> createdFees = new ArrayList<>();

                for (CreateMembershipFee createFee : createFees) {
                    // Validation : amount ne peut pas être négatif
                    if (createFee.getAmount() == null || createFee.getAmount().compareTo(java.math.BigDecimal.ZERO) < 0) {
                        throw new BusinessRuleException("Amount cannot be negative");
                    }

                    // Validation : frequency doit être valide
                    if (createFee.getFrequency() == null || !VALID_FREQUENCIES.contains(createFee.getFrequency())) {
                        throw new BusinessRuleException("Invalid frequency. Allowed values: WEEKLY, MONTHLY, ANNUALLY, PUNCTUALLY");
                    }

                    // Validation : eligibleFrom ne peut pas être null
                    if (createFee.getEligibleFrom() == null) {
                        throw new BusinessRuleException("Eligible from date is required");
                    }

                    // Création de l'entité
                    MembershipFeeEntity entity = new MembershipFeeEntity();
                    entity.setCollectivityId(collectivityId);
                    entity.setEligibleFrom(createFee.getEligibleFrom());
                    entity.setFrequency(createFee.getFrequency());
                    entity.setAmount(createFee.getAmount());
                    entity.setLabel(createFee.getLabel());
                    entity.setStatus("ACTIVE"); // Par défaut ACTIVE

                    // Insertion en base
                    Long generatedId = membershipFeeRepository.insert(conn, entity);
                    entity.setId(generatedId);

                    createdFees.add(toDto(entity));
                }

                conn.commit();
                return createdFees;

            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Database error while creating membership fees", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database connection error", e);
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