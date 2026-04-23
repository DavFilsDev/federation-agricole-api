// service/CollectivityService.java
package mg.federation.agricole.api.service;

import mg.federation.agricole.api.config.DataSource;
import mg.federation.agricole.api.dto.*;
import mg.federation.agricole.api.entity.*;
import mg.federation.agricole.api.exception.BusinessRuleException;
import mg.federation.agricole.api.exception.ResourceNotFoundException;
import mg.federation.agricole.api.repository.*;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CollectivityService {

    private final DataSource dataSource;
    private final MemberRepository memberRepository;
    private final CollectivityRepository collectivityRepository;
    private final MembershipRepository membershipRepository;

    public CollectivityService(DataSource dataSource,
                               MemberRepository memberRepository,
                               CollectivityRepository collectivityRepository,
                               MembershipRepository membershipRepository) {
        this.dataSource = dataSource;
        this.memberRepository = memberRepository;
        this.collectivityRepository = collectivityRepository;
        this.membershipRepository = membershipRepository;
    }

    public List<Collectivity> createCollectivities(List<CreateCollectivity> createList) {
        List<Collectivity> results = new ArrayList<>();
        for (CreateCollectivity create : createList) {
            results.add(createSingleCollectivity(create));
        }
        return results;
    }

    private Collectivity createSingleCollectivity(CreateCollectivity create) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Vérifier autorisation
                if (create.getFederationApproval() == null || !create.getFederationApproval()) {
                    throw new BusinessRuleException("Federation approval must be true");
                }

                // 2. Vérifier nombre de membres >= 10
                List<String> memberIds = create.getMembers();  // Déjà List<String>
                if (memberIds == null || memberIds.size() < 10) {
                    throw new BusinessRuleException("At least 10 members required");
                }

                // Récupérer les membres (plus besoin de conversion Long)
                List<MemberEntity> members = memberRepository.findByIds(conn, memberIds);
                if (members.size() != memberIds.size()) {
                    throw new ResourceNotFoundException("Some members not found");
                }

                // 3. Vérifier ancienneté >= 6 mois
                LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
                for (MemberEntity m : members) {
                    if (m.getDateAdhesionFederation().isAfter(sixMonthsAgo)) {
                        throw new BusinessRuleException("Member " + m.getId() + " has less than 6 months seniority");
                    }
                }

                CreateCollectivityStructure structure = create.getStructure();
                if (structure == null) {
                    throw new BusinessRuleException("Structure (president, vicePresident, treasurer, secretary) is required");
                }
                Set<String> roleIds = new HashSet<>();
                roleIds.add(structure.getPresident());
                roleIds.add(structure.getVicePresident());
                roleIds.add(structure.getTreasurer());
                roleIds.add(structure.getSecretary());
                if (roleIds.size() != 4) {
                    throw new BusinessRuleException("President, vice-president, treasurer and secretary must be distinct");
                }
                Set<String> memberIdSet = new HashSet<>(memberIds);
                for (String roleId : roleIds) {
                    if (!memberIdSet.contains(roleId)) {
                        throw new BusinessRuleException("Role member " + roleId + " not in members list");
                    }
                }

                String collectivityId = generateCollectivityId();

                CollectivityEntity collectivityEntity = new CollectivityEntity();
                collectivityEntity.setId(collectivityId);
                collectivityEntity.setLocation(create.getLocation());
                collectivityEntity.setSpecialiteAgricole("inconnue");
                collectivityEntity.setAnnualDuesAmount(0);
                collectivityEntity.setDateCreation(LocalDate.now());
                collectivityEntity.setFederationApproval(true);
                collectivityEntity.setNumber(null);
                collectivityEntity.setName(null);
                collectivityRepository.insert(conn, collectivityEntity);

                LocalDate now = LocalDate.now();
                for (String memberId : memberIds) {
                    MembershipEntity ms = new MembershipEntity();
                    ms.setMemberId(memberId);
                    ms.setCollectivityId(collectivityId);
                    String occupation;
                    if (memberId.equals(structure.getPresident())) {
                        occupation = "PRESIDENT";
                    } else if (memberId.equals(structure.getVicePresident())) {
                        occupation = "VICE_PRESIDENT";
                    } else if (memberId.equals(structure.getTreasurer())) {
                        occupation = "TREASURER";
                    } else if (memberId.equals(structure.getSecretary())) {
                        occupation = "SECRETARY";
                    } else {
                        occupation = "JUNIOR";
                    }
                    ms.setOccupation(occupation);
                    ms.setRegistrationFeePaid(true);
                    ms.setMembershipDuesPaid(true);
                    ms.setDateAdhesion(now);
                    ms.setPaymentDate(now);
                    membershipRepository.insert(conn, ms);
                }

                CollectivityEntity savedEntity = collectivityRepository.findById(conn, collectivityId)
                        .orElseThrow(() -> new RuntimeException("Collectivity not found after insertion"));

                conn.commit();

                return toCollectivityDto(savedEntity, conn);

            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error", e);
        }
    }

    // Helper pour générer un ID de collectivité (à adapter selon votre logique)
    private String generateCollectivityId() {
        // Pour simplifier, on peut utiliser un UUID ou un compteur
        // Dans un vrai système, vous pourriez avoir une séquence en base
        return "col-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private Member toMemberDto(MemberEntity entity) {
        Member dto = new Member();
        dto.setId(entity.getId());  // String, plus besoin de String.valueOf()
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setBirthDate(entity.getBirthDate());
        dto.setGender(Gender.valueOf(entity.getGender()));
        dto.setAddress(entity.getAddress());
        dto.setProfession(entity.getProfession());
        dto.setPhoneNumber(entity.getPhoneNumber());
        dto.setEmail(entity.getEmail());
        return dto;
    }

    private Collectivity toCollectivityDto(CollectivityEntity entity, Connection conn) throws SQLException {
        // Récupérer tous les membres de cette collectivité (via membership)
        List<MembershipEntity> memberships = membershipRepository.findByCollectivityId(conn, entity.getId());

        // Construire la map des membres (id -> Member DTO)
        Map<String, Member> memberMap = new HashMap<>();
        for (MembershipEntity ms : memberships) {
            Optional<MemberEntity> optMember = memberRepository.findById(conn, ms.getMemberId());
            optMember.ifPresent(memberEntity -> {
                Member memberDto = toMemberDto(memberEntity);
                memberMap.put(memberEntity.getId(), memberDto);
            });
        }

        CollectivityStructure struct = new CollectivityStructure();
        for (MembershipEntity ms : memberships) {
            Member m = memberMap.get(ms.getMemberId());
            if (m == null) continue;
            switch (ms.getOccupation()) {
                case "PRESIDENT":
                    struct.setPresident(m);
                    break;
                case "VICE_PRESIDENT":
                    struct.setVicePresident(m);
                    break;
                case "TREASURER":
                    struct.setTreasurer(m);
                    break;
                case "SECRETARY":
                    struct.setSecretary(m);
                    break;
            }
        }

        Collectivity dto = new Collectivity();
        dto.setId(entity.getId());
        dto.setLocation(entity.getLocation());
        dto.setNumber(entity.getNumber());
        dto.setName(entity.getName());
        dto.setStructure(struct);
        dto.setMembers(new ArrayList<>(memberMap.values()));

        return dto;
    }

    private Member toMemberDto(MemberEntity entity, List<Member> referees) {
        Member dto = new Member();
        dto.setId(entity.getId());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setBirthDate(entity.getBirthDate());
        dto.setGender(Gender.valueOf(entity.getGender()));
        dto.setAddress(entity.getAddress());
        dto.setProfession(entity.getProfession());
        dto.setPhoneNumber(entity.getPhoneNumber());
        dto.setEmail(entity.getEmail());
        dto.setReferees(referees);
        return dto;
    }

    public Collectivity updateCollectivityInformation(String idStr, CollectivityInformation info) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String id = idStr;

                CollectivityEntity entity = collectivityRepository.findById(conn, id)
                        .orElseThrow(() -> new ResourceNotFoundException("Collectivity not found"));

                if (collectivityRepository.hasNameAndNumber(conn, id)) {
                    throw new BusinessRuleException("Name and number already set (immutable)");
                }

                if (collectivityRepository.isNameUsed(conn, info.getName(), id)) {
                    throw new BusinessRuleException("Name already used by another collectivity");
                }

                if (collectivityRepository.isNumberUsed(conn, info.getNumber(), id)) {
                    throw new BusinessRuleException("Number already used by another collectivity");
                }

                collectivityRepository.updateNameAndNumber(conn, id, info.getName(), info.getNumber());

                CollectivityEntity updated = collectivityRepository.findById(conn, id)
                        .orElseThrow(() -> new ResourceNotFoundException("Collectivity not found after update"));
                conn.commit();

                return toCollectivityDto(updated, conn);

            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Collectivity getCollectivityById(String idStr) {
        try (Connection conn = dataSource.getConnection()) {
            String id = idStr;

            CollectivityEntity entity = collectivityRepository.findByIdWithDetails(conn, id)
                    .orElseThrow(() -> new ResourceNotFoundException("Collectivity not found with id: " + idStr));

            return toCollectivityDto(entity, conn);

        } catch (SQLException e) {
            throw new RuntimeException("Database error while fetching collectivity", e);
        }
    }
}