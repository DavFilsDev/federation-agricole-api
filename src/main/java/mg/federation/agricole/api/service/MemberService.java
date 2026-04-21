// service/MemberService.java
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
public class MemberService {

    private final DataSource dataSource;
    private final MemberRepository memberRepository;
    private final CollectivityRepository collectivityRepository;
    private final MembershipRepository membershipRepository;
    private final ReferenceRepository referenceRepository;

    public MemberService(DataSource dataSource,
                         MemberRepository memberRepository,
                         CollectivityRepository collectivityRepository,
                         MembershipRepository membershipRepository,
                         ReferenceRepository referenceRepository) {
        this.dataSource = dataSource;
        this.memberRepository = memberRepository;
        this.collectivityRepository = collectivityRepository;
        this.membershipRepository = membershipRepository;
        this.referenceRepository = referenceRepository;
    }

    public List<Member> createMembers(List<CreateMember> createList) {
        List<Member> results = new ArrayList<>();
        for (CreateMember create : createList) {
            results.add(createSingleMember(create));
        }
        return results;
    }

    private Member createSingleMember(CreateMember create) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Vérifier que la collectivité cible existe
                Long collectivityId = Long.parseLong(create.getCollectivityIdentifier());
                CollectivityEntity collectivity = collectivityRepository.findById(conn, collectivityId)
                        .orElseThrow(() -> new ResourceNotFoundException("Collectivity not found"));

                // 2. Vérifier les parrains (au moins 2)
                List<String> refereeIdsStr = create.getReferees();
                if (refereeIdsStr == null || refereeIdsStr.size() < 2) {
                    throw new BusinessRuleException("At least 2 referees required");
                }
                List<Long> refereeIds = refereeIdsStr.stream().map(Long::parseLong).collect(Collectors.toList());

                // Vérifier que chaque parrain existe et a le rôle SENIOR
                for (Long rid : refereeIds) {
                    if (!membershipRepository.hasSeniorRole(conn, rid)) {
                        throw new BusinessRuleException("Referee " + rid + " is not a SENIOR member");
                    }
                }

                // 3. Règle de proportion
                int countInTarget = membershipRepository.countRefereesInCollectivity(conn, refereeIds, collectivityId);
                int countOutside = refereeIds.size() - countInTarget;
                if (countInTarget < countOutside) {
                    throw new BusinessRuleException("Number of referees from target collectivity must be at least equal to outsiders");
                }

                // 4. Vérifier les paiements
                if (create.getRegistrationFeePaid() == null || !create.getRegistrationFeePaid()) {
                    throw new BusinessRuleException("Registration fee must be paid");
                }
                if (create.getMembershipDuesPaid() == null || !create.getMembershipDuesPaid()) {
                    throw new BusinessRuleException("Membership dues must be paid");
                }

                // 5. Insérer le membre
                MemberEntity newMember = new MemberEntity();
                newMember.setFirstName(create.getFirstName());
                newMember.setLastName(create.getLastName());
                newMember.setBirthDate(create.getBirthDate());
                newMember.setGender(create.getGender().name());
                newMember.setAddress(create.getAddress());
                newMember.setProfession(create.getProfession());
                newMember.setPhoneNumber(create.getPhoneNumber());
                newMember.setEmail(create.getEmail());
                newMember.setDateAdhesionFederation(LocalDate.now());
                Long memberId = memberRepository.insert(conn, newMember);

                // 6. Insérer membership
                MembershipEntity membership = new MembershipEntity();
                membership.setMemberId(memberId);
                membership.setCollectivityId(collectivityId);
                membership.setOccupation(create.getOccupation().name());
                membership.setRegistrationFeePaid(true);
                membership.setMembershipDuesPaid(true);
                membership.setDateAdhesion(LocalDate.now());
                membership.setPaymentDate(LocalDate.now());
                membershipRepository.insert(conn, membership);

                // 7. Insérer les parrainages (références)
                LocalDate now = LocalDate.now();
                for (Long sponsorId : refereeIds) {
                    ReferenceEntity ref = new ReferenceEntity();
                    ref.setCandidateId(memberId);
                    ref.setSponsorId(sponsorId);
                    ref.setRelationNature("unknown");  // non fourni par la spec
                    ref.setSponsorshipDate(now);
                    referenceRepository.insert(conn, ref);
                }

                // 8. Construire la réponse : récupérer tous les parrains complets
                List<Member> refereeDtos = new ArrayList<>();
                for (Long rid : refereeIds) {
                    Optional<MemberEntity> optRef = memberRepository.findById(conn, rid);
                    optRef.ifPresent(refEntity -> refereeDtos.add(toMemberDto(refEntity, null)));
                }

                Member response = toMemberDto(newMember, refereeDtos);
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
        // occupation non disponible ici, on met null
        dto.setReferees(referees);
        return dto;
    }
}