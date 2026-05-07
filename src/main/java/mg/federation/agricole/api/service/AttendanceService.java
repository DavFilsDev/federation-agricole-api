package mg.federation.agricole.api.service;

import mg.federation.agricole.api.config.DataSource;
import mg.federation.agricole.api.dto.*;
import mg.federation.agricole.api.entity.AttendanceEntity;
import mg.federation.agricole.api.entity.MemberEntity;
import mg.federation.agricole.api.exception.BusinessRuleException;
import mg.federation.agricole.api.exception.ResourceNotFoundException;
import mg.federation.agricole.api.repository.*;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AttendanceService {

    private final DataSource dataSource;
    private final CollectivityRepository collectivityRepository;
    private final ActivityRepository activityRepository;
    private final AttendanceRepository attendanceRepository;
    private final MemberRepository memberRepository;

    public AttendanceService(DataSource dataSource,
                             CollectivityRepository collectivityRepository,
                             ActivityRepository activityRepository,
                             AttendanceRepository attendanceRepository,
                             MemberRepository memberRepository) {
        this.dataSource = dataSource;
        this.collectivityRepository = collectivityRepository;
        this.activityRepository = activityRepository;
        this.attendanceRepository = attendanceRepository;
        this.memberRepository = memberRepository;
    }

    public List<ActivityMemberAttendance> createAttendances(String collectivityId, String activityId, List<CreateActivityMemberAttendance> attendances) {
        if (attendances == null || attendances.isEmpty()) {
            throw new BusinessRuleException("At least one attendance must be provided");
        }

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (collectivityRepository.findById(conn, collectivityId).isEmpty()) {
                    throw new ResourceNotFoundException("Collectivity not found with id: " + collectivityId);
                }

                var activityOpt = activityRepository.findById(conn, activityId);
                if (activityOpt.isEmpty()) {
                    throw new ResourceNotFoundException("Activity not found with id: " + activityId);
                }
                if (!activityOpt.get().getCollectivityId().equals(collectivityId)) {
                    throw new BusinessRuleException("Activity does not belong to this collectivity");
                }

                List<ActivityMemberAttendance> createdAttendances = new ArrayList<>();

                for (CreateActivityMemberAttendance att : attendances) {
                    String memberId = att.getMemberIdentifier();
                    AttendanceStatus status = att.getAttendanceStatus();

                    Optional<MemberEntity> memberOpt = memberRepository.findById(conn, memberId);
                    if (memberOpt.isEmpty()) {
                        throw new ResourceNotFoundException("Member not found with id: " + memberId);
                    }

                    if (attendanceRepository.isStatusConfirmed(conn, activityId, memberId)) {
                        throw new BusinessRuleException("Attendance status already confirmed for member " + memberId);
                    }

                    String attendanceId = attendanceRepository.upsertAttendance(conn, activityId, memberId, status);

                    ActivityMemberAttendance response = new ActivityMemberAttendance();
                    response.setId(attendanceId);
                    response.setAttendanceStatus(status);

                    MemberDescription memberDesc = new MemberDescription();
                    MemberEntity member = memberOpt.get();
                    memberDesc.setId(member.getId());
                    memberDesc.setFirstName(member.getFirstName());
                    memberDesc.setLastName(member.getLastName());
                    memberDesc.setEmail(member.getEmail());
                    response.setMemberDescription(memberDesc);

                    createdAttendances.add(response);
                }

                conn.commit();
                return createdAttendances;

            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Database error while creating attendances", e);
            } catch (BusinessRuleException e) {
                conn.rollback();
                throw e;
            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException("Unexpected error: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database connection error", e);
        }
    }

    public List<ActivityMemberAttendance> getAttendances(String collectivityId, String activityId) {
        try (Connection conn = dataSource.getConnection()) {
            if (collectivityRepository.findById(conn, collectivityId).isEmpty()) {
                throw new ResourceNotFoundException("Collectivity not found with id: " + collectivityId);
            }

            var activityOpt = activityRepository.findById(conn, activityId);
            if (activityOpt.isEmpty()) {
                throw new ResourceNotFoundException("Activity not found with id: " + activityId);
            }
            if (!activityOpt.get().getCollectivityId().equals(collectivityId)) {
                throw new BusinessRuleException("Activity does not belong to this collectivity");
            }

            List<AttendanceEntity> attendances = attendanceRepository.findByActivityId(conn, activityId);
            List<ActivityMemberAttendance> result = new ArrayList<>();

            for (AttendanceEntity att : attendances) {
                Optional<MemberEntity> memberOpt = memberRepository.findById(conn, att.getMemberId());
                if (memberOpt.isPresent()) {
                    ActivityMemberAttendance dto = new ActivityMemberAttendance();
                    dto.setId(att.getId());
                    dto.setAttendanceStatus(att.getStatus());

                    MemberDescription memberDesc = new MemberDescription();
                    MemberEntity member = memberOpt.get();
                    memberDesc.setId(member.getId());
                    memberDesc.setFirstName(member.getFirstName());
                    memberDesc.setLastName(member.getLastName());
                    memberDesc.setEmail(member.getEmail());
                    dto.setMemberDescription(memberDesc);

                    result.add(dto);
                }
            }

            return result;

        } catch (SQLException e) {
            throw new RuntimeException("Database error while fetching attendances", e);
        }
    }
}