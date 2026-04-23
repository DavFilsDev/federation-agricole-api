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
                List<String> memberIdsStr = create.getMembers();
                if (memberIdsStr == null || memberIdsStr.size() < 10) {
                    throw new BusinessRuleException("At least 10 members required");
                }

                // Convertir les IDs en Long
                List<Long> memberIds = memberIdsStr.stream().map(Long::parseLong).collect(Collectors.toList());
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

                // 4. Vérifier les postes spécifiques
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
                // Vérifier que ces 4 membres sont bien dans la liste des membres
                Set<String> memberIdSet = new HashSet<>(memberIdsStr);
                for (String roleId : roleIds) {
                    if (!memberIdSet.contains(roleId)) {
                        throw new BusinessRuleException("Role member " + roleId + " not in members list");
                    }
                }

                // 5. Insérer la collectivité (sans spécialité ni annual_dues car non fournis, on met des valeurs par défaut)
                CollectivityEntity collectivityEntity = new CollectivityEntity();
                collectivityEntity.setLocation(create.getLocation());
                collectivityEntity.setSpecialiteAgricole("inconnue");  // par défaut
                collectivityEntity.setAnnualDuesAmount(0);            // par défaut
                collectivityEntity.setDateCreation(LocalDate.now());
                collectivityEntity.setFederationApproval(true);
                // uniqueNumber et uniqueName sont null à la création
                collectivityEntity.setNumber(null);
                collectivityEntity.setName(null);
                Long collectivityId = collectivityRepository.insert(conn, collectivityEntity);

                // 6. Insérer les membreships
                LocalDate now = LocalDate.now();
                for (String memberIdStr : memberIdsStr) {
                    Long mid = Long.parseLong(memberIdStr);
                    MembershipEntity ms = new MembershipEntity();
                    ms.setMemberId(mid);
                    ms.setCollectivityId(collectivityId);
                    // Déterminer l'occupation
                    String occupation;
                    if (memberIdStr.equals(structure.getPresident())) {
                        occupation = "PRESIDENT";
                    } else if (memberIdStr.equals(structure.getVicePresident())) {
                        occupation = "VICE_PRESIDENT";
                    } else if (memberIdStr.equals(structure.getTreasurer())) {
                        occupation = "TREASURER";
                    } else if (memberIdStr.equals(structure.getSecretary())) {
                        occupation = "SECRETARY";
                    } else {
                        occupation = "JUNIOR";   // par défaut
                    }
                    ms.setOccupation(occupation);
                    ms.setRegistrationFeePaid(true);
                    ms.setMembershipDuesPaid(true);
                    ms.setDateAdhesion(now);
                    ms.setPaymentDate(now);
                    membershipRepository.insert(conn, ms);
                }

                // 7. Recharger l'entité collectivité pour avoir l'id et les champs à jour
                CollectivityEntity savedEntity = collectivityRepository.findById(conn, collectivityId)
                        .orElseThrow(() -> new RuntimeException("Collectivity not found after insertion"));

                conn.commit();

                // 8. Utiliser la méthode utilitaire toCollectivityDto pour construire la réponse
                //    (cette méthode récupère les membres et la structure automatiquement)
                return toCollectivityDto(savedEntity, conn);

            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error", e);
        }
    }

    private Member toMemberDto(MemberEntity entity) {
        Member dto = new Member();
        dto.setId(String.valueOf(entity.getId()));
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setBirthDate(entity.getBirthDate());
        dto.setGender(Gender.valueOf(entity.getGender()));
        dto.setAddress(entity.getAddress());
        dto.setProfession(entity.getProfession());
        dto.setPhoneNumber(entity.getPhoneNumber());
        dto.setEmail(entity.getEmail());
        // occupation non disponible ici, on met null
        return dto;
    }

    private Collectivity toCollectivityDto(CollectivityEntity entity, Connection conn) throws SQLException {
        // Récupérer tous les membres de cette collectivité (via membership)
        List<MembershipEntity> memberships = membershipRepository.findByCollectivityId(conn, entity.getId());

        // Construire la map des membres (id -> Member DTO)
        Map<Long, Member> memberMap = new HashMap<>();
        for (MembershipEntity ms : memberships) {
            Optional<MemberEntity> optMember = memberRepository.findById(conn, ms.getMemberId());
            optMember.ifPresent(memberEntity -> {
                Member memberDto = toMemberDto(memberEntity, null); // sans referees pour éviter récursion
                memberMap.put(memberEntity.getId(), memberDto);
            });
        }

        // Extraire la structure (président, vice-président, trésorier, secrétaire)
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

        // Construction du DTO Collectivity
        Collectivity dto = new Collectivity();
        dto.setId(String.valueOf(entity.getId()));
        dto.setLocation(entity.getLocation());
        dto.setNumber(entity.getNumber());
        dto.setName(entity.getName());
        dto.setStructure(struct);
        dto.setMembers(new ArrayList<>(memberMap.values()));

        return dto;
    }

    private Member toMemberDto(MemberEntity entity, List<Member> referees) {
        Member dto = new Member();
        dto.setId(String.valueOf(entity.getId()));
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setBirthDate(entity.getBirthDate());
        dto.setGender(Gender.valueOf(entity.getGender()));
        dto.setAddress(entity.getAddress());
        dto.setProfession(entity.getProfession());
        dto.setPhoneNumber(entity.getPhoneNumber());
        dto.setEmail(entity.getEmail());
        // occupation n'est pas stocké dans MemberEntity, on peut le laisser null ou le récupérer ailleurs
        dto.setReferees(referees);
        return dto;
    }

    public Collectivity updateCollectivityInformation(String idStr, CollectivityInformation info) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Long id = Long.parseLong(idStr);

                // Vérifier existence
                CollectivityEntity entity = collectivityRepository.findById(conn, id)
                        .orElseThrow(() -> new ResourceNotFoundException("Collectivity not found"));

                // Vérifier si déjà nom et numéro (ne peut pas être modifié)
                if (collectivityRepository.hasNameAndNumber(conn, id)) {
                    throw new BusinessRuleException("Name and number already set (immutable)");
                }

                // Vérifier unicité du nom
                if (collectivityRepository.isNameUsed(conn, info.getName(), id)) {
                    throw new BusinessRuleException("Name already used by another collectivity");
                }

                // Vérifier unicité du numéro
                if (collectivityRepository.isNumberUsed(conn, info.getNumber(), id)) {
                    throw new BusinessRuleException("Number already used by another collectivity");
                }

                // Mettre à jour
                collectivityRepository.updateNameAndNumber(conn, id, info.getName(), info.getNumber());

                // Recharger l'entité
                CollectivityEntity updated = collectivityRepository.findById(conn, id).get();
                conn.commit();

                // Convertir en DTO
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
            Long id = Long.parseLong(idStr);

            // Récupérer la collectivité
            CollectivityEntity entity = collectivityRepository.findByIdWithDetails(conn, id)
                    .orElseThrow(() -> new ResourceNotFoundException("Collectivity not found with id: " + idStr));

            // Convertir en DTO
            return toCollectivityDto(entity, conn);

        } catch (SQLException e) {
            throw new RuntimeException("Database error while fetching collectivity", e);
        }
    }
}