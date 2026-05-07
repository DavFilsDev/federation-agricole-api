package mg.federation.agricole.api.service;

import mg.federation.agricole.api.config.DataSource;
import mg.federation.agricole.api.dto.*;
import mg.federation.agricole.api.entity.ActivityEntity;
import mg.federation.agricole.api.exception.BusinessRuleException;
import mg.federation.agricole.api.exception.ResourceNotFoundException;
import mg.federation.agricole.api.repository.ActivityRepository;
import mg.federation.agricole.api.repository.CollectivityRepository;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ActivityService {

    private final DataSource dataSource;
    private final CollectivityRepository collectivityRepository;
    private final ActivityRepository activityRepository;

    public ActivityService(DataSource dataSource,
                           CollectivityRepository collectivityRepository,
                           ActivityRepository activityRepository) {
        this.dataSource = dataSource;
        this.collectivityRepository = collectivityRepository;
        this.activityRepository = activityRepository;
    }

    public List<CollectivityActivity> getActivities(String collectivityId) {
        try (Connection conn = dataSource.getConnection()) {
            if (collectivityRepository.findById(conn, collectivityId).isEmpty()) {
                throw new ResourceNotFoundException("Collectivity not found with id: " + collectivityId);
            }

            List<ActivityEntity> entities = activityRepository.findByCollectivityId(conn, collectivityId);
            return entities.stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());

        } catch (SQLException e) {
            throw new RuntimeException("Database error while fetching activities", e);
        }
    }

    public List<CollectivityActivity> createActivities(String collectivityId, List<CreateCollectivityActivity> createActivities) {
        if (createActivities == null || createActivities.isEmpty()) {
            throw new BusinessRuleException("At least one activity must be provided");
        }

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (collectivityRepository.findById(conn, collectivityId).isEmpty()) {
                    throw new ResourceNotFoundException("Collectivity not found with id: " + collectivityId);
                }

                List<CollectivityActivity> createdActivities = new ArrayList<>();

                for (CreateCollectivityActivity create : createActivities) {
                    boolean hasExecutiveDate = create.getExecutiveDate() != null;
                    boolean hasRecurrenceRule = create.getRecurrenceRule() != null;

                    if (hasExecutiveDate && hasRecurrenceRule) {
                        throw new BusinessRuleException("Cannot provide both executive date and recurrence rule");
                    }
                    if (!hasExecutiveDate && !hasRecurrenceRule) {
                        throw new BusinessRuleException("Either executive date or recurrence rule must be provided");
                    }

                    if (create.getLabel() == null || create.getLabel().trim().isEmpty()) {
                        throw new BusinessRuleException("Label is required");
                    }

                    if (create.getActivityType() == null) {
                        throw new BusinessRuleException("Activity type is required");
                    }

                    ActivityEntity entity = new ActivityEntity();
                    entity.setCollectivityId(collectivityId);
                    entity.setLabel(create.getLabel());
                    entity.setActivityType(create.getActivityType());
                    entity.setMemberOccupationsConcerned(create.getMemberOccupationConcerned());

                    if (hasExecutiveDate) {
                        entity.setExecutiveDate(create.getExecutiveDate());
                    } else {
                        entity.setRecurrenceWeekOrdinal(create.getRecurrenceRule().getWeekOrdinal());
                        entity.setRecurrenceDayOfWeek(create.getRecurrenceRule().getDayOfWeek());
                    }

                    String activityId = activityRepository.insert(conn, entity);

                    if (create.getMemberOccupationConcerned() != null && !create.getMemberOccupationConcerned().isEmpty()) {
                        activityRepository.insertConcernedOccupations(conn, activityId, create.getMemberOccupationConcerned());
                    }

                    entity.setId(activityId);
                    createdActivities.add(toDto(entity));
                }

                conn.commit();
                return createdActivities;

            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Database error while creating activities", e);
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

    private CollectivityActivity toDto(ActivityEntity entity) {
        CollectivityActivity dto = new CollectivityActivity();
        dto.setId(entity.getId());
        dto.setLabel(entity.getLabel());
        dto.setActivityType(entity.getActivityType());
        dto.setMemberOccupationConcerned(entity.getMemberOccupationsConcerned());
        dto.setExecutiveDate(entity.getExecutiveDate());

        if (entity.getRecurrenceWeekOrdinal() != null && entity.getRecurrenceDayOfWeek() != null) {
            MonthlyRecurrenceRule rule = new MonthlyRecurrenceRule();
            rule.setWeekOrdinal(entity.getRecurrenceWeekOrdinal());
            rule.setDayOfWeek(entity.getRecurrenceDayOfWeek());
            dto.setRecurrenceRule(rule);
        }

        return dto;
    }
}