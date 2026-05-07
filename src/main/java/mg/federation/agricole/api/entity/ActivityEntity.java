package mg.federation.agricole.api.entity;

import mg.federation.agricole.api.dto.ActivityType;
import mg.federation.agricole.api.dto.MemberOccupation;
import mg.federation.agricole.api.dto.WeeklyDay;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ActivityEntity {
    private String id;
    private String collectivityId;
    private String label;
    private ActivityType activityType;
    private List<MemberOccupation> memberOccupationsConcerned;
    private Integer recurrenceWeekOrdinal;
    private WeeklyDay recurrenceDayOfWeek;
    private LocalDate executiveDate;
    private LocalDateTime createdAt;

    public ActivityEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCollectivityId() { return collectivityId; }
    public void setCollectivityId(String collectivityId) { this.collectivityId = collectivityId; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public ActivityType getActivityType() { return activityType; }
    public void setActivityType(ActivityType activityType) { this.activityType = activityType; }

    public List<MemberOccupation> getMemberOccupationsConcerned() { return memberOccupationsConcerned; }
    public void setMemberOccupationsConcerned(List<MemberOccupation> memberOccupationsConcerned) { this.memberOccupationsConcerned = memberOccupationsConcerned; }

    public Integer getRecurrenceWeekOrdinal() { return recurrenceWeekOrdinal; }
    public void setRecurrenceWeekOrdinal(Integer recurrenceWeekOrdinal) { this.recurrenceWeekOrdinal = recurrenceWeekOrdinal; }

    public WeeklyDay getRecurrenceDayOfWeek() { return recurrenceDayOfWeek; }
    public void setRecurrenceDayOfWeek(WeeklyDay recurrenceDayOfWeek) { this.recurrenceDayOfWeek = recurrenceDayOfWeek; }

    public LocalDate getExecutiveDate() { return executiveDate; }
    public void setExecutiveDate(LocalDate executiveDate) { this.executiveDate = executiveDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}