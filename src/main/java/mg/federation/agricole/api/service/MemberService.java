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
                String collectivityId = create.getCollectivityIdentifier();
                CollectivityEntity collectivity = collectivityRepository.findById(conn, collectivityId)
                        .orElseThrow(() -> new ResourceNotFoundException("Collectivity not found"));

                int totalMembers = memberRepository.countAll(conn);
                boolean isFirstMember = (totalMembers == 0);

                List<String> refereeIds = null;
                if (!isFirstMember) {
                    List<String> refereeIdsStr = create.getReferees();
                    if (refereeIdsStr == null || refereeIdsStr.size() < 2) {
                        throw new BusinessRuleException("At least 2 referees required");
                    }
                    refereeIds = refereeIdsStr;

                    for (String rid : refereeIds) {
                        if (!membershipRepository.hasSeniorRole(conn, rid)) {
                            throw new BusinessRuleException("Referee " + rid + " is not a SENIOR member");
                        }
                    }

                    int countInTarget = membershipRepository.countRefereesInCollectivity(conn, refereeIds, collectivityId);
                    int countOutside = refereeIds.size() - countInTarget;
                    if (countInTarget < countOutside) {
                        throw new BusinessRuleException("Number of referees from target collectivity must be at least equal to outsiders");
                    }
                }

                if (create.getRegistrationFeePaid() == null || !create.getRegistrationFeePaid()) {
                    throw new BusinessRuleException("Registration fee must be paid");
                }
                if (create.getMembershipDuesPaid() == null || !create.getMembershipDuesPaid()) {
                    throw new BusinessRuleException("Membership dues must be paid");
                }

                String memberId = generateMemberId();
                MemberEntity newMember = new MemberEntity();
                newMember.setId(memberId);
                newMember.setFirstName(create.getFirstName());
                newMember.setLastName(create.getLastName());
                newMember.setBirthDate(create.getBirthDate());
                newMember.setGender(create.getGender().name());
                newMember.setAddress(create.getAddress());
                newMember.setProfession(create.getProfession());
                newMember.setPhoneNumber(create.getPhoneNumber());
                newMember.setEmail(create.getEmail());
                newMember.setDateAdhesionFederation(LocalDate.now());
                memberRepository.insert(conn, newMember);

                MembershipEntity membership = new MembershipEntity();
                membership.setMemberId(memberId);
                membership.setCollectivityId(collectivityId);
                membership.setOccupation(create.getOccupation().name());
                membership.setRegistrationFeePaid(true);
                membership.setMembershipDuesPaid(true);
                membership.setDateAdhesion(LocalDate.now());
                membership.setPaymentDate(LocalDate.now());
                membershipRepository.insert(conn, membership);

                if (!isFirstMember && refereeIds != null && !refereeIds.isEmpty()) {
                    LocalDate now = LocalDate.now();
                    for (String sponsorId : refereeIds) {
                        ReferenceEntity ref = new ReferenceEntity();
                        ref.setCandidateId(memberId);
                        ref.setSponsorId(sponsorId);
                        ref.setRelationNature("unknown");
                        ref.setSponsorshipDate(now);
                        referenceRepository.insert(conn, ref);
                    }
                }

                List<Member> refereeDtos = new ArrayList<>();
                if (!isFirstMember && refereeIds != null) {
                    for (String rid : refereeIds) {
                        Optional<MemberEntity> optRef = memberRepository.findById(conn, rid);
                        optRef.ifPresent(refEntity -> refereeDtos.add(toMemberDto(refEntity, null)));
                    }
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

    private String generateMemberId() {
        return "M" + UUID.randomUUID().toString().substring(0, 8);
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
}