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

                // 7. Construire l'objet de réponse
                Collectivity response = new Collectivity();
                response.setId(String.valueOf(collectivityId));
                response.setLocation(create.getLocation());

                // Récupérer tous les membres avec leurs infos complètes pour la réponse
                Map<Long, Member> memberMap = new HashMap<>();
                for (MemberEntity m : members) {
                    Member dto = toMemberDto(m);
                    memberMap.put(m.getId(), dto);
                }

                // Structure
                CollectivityStructure respStruct = new CollectivityStructure();
                respStruct.setPresident(memberMap.get(Long.parseLong(structure.getPresident())));
                respStruct.setVicePresident(memberMap.get(Long.parseLong(structure.getVicePresident())));
                respStruct.setTreasurer(memberMap.get(Long.parseLong(structure.getTreasurer())));
                respStruct.setSecretary(memberMap.get(Long.parseLong(structure.getSecretary())));
                response.setStructure(respStruct);

                // Liste des membres (tous)
                response.setMembers(new ArrayList<>(memberMap.values()));

                conn.commit();
                return response;
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
}