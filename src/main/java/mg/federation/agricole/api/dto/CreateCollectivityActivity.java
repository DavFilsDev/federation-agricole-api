package mg.federation.agricole.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.util.List;

public class CreateCollectivityActivity {
    private String label;
    private ActivityType activityType;
    private List<MemberOccupation> memberOccupationConcerned;
    private MonthlyRecurrenceRule recurrenceRule;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate executiveDate;

    public CreateCollectivityActivity() {}

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public ActivityType getActivityType() { return activityType; }
    public void setActivityType(ActivityType activityType) { this.activityType = activityType; }

    public List<MemberOccupation> getMemberOccupationConcerned() { return memberOccupationConcerned; }
    public void setMemberOccupationConcerned(List<MemberOccupation> memberOccupationConcerned) { this.memberOccupationConcerned = memberOccupationConcerned; }

    public MonthlyRecurrenceRule getRecurrenceRule() { return recurrenceRule; }
    public void setRecurrenceRule(MonthlyRecurrenceRule recurrenceRule) { this.recurrenceRule = recurrenceRule; }

    public LocalDate getExecutiveDate() { return executiveDate; }
    public void setExecutiveDate(LocalDate executiveDate) { this.executiveDate = executiveDate; }
}