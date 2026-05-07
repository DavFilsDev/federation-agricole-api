package mg.federation.agricole.api.dto;

public class MonthlyRecurrenceRule {
    private Integer weekOrdinal;
    private WeeklyDay dayOfWeek;

    public MonthlyRecurrenceRule() {}

    public Integer getWeekOrdinal() { return weekOrdinal; }
    public void setWeekOrdinal(Integer weekOrdinal) { this.weekOrdinal = weekOrdinal; }

    public WeeklyDay getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(WeeklyDay dayOfWeek) { this.dayOfWeek = dayOfWeek; }
}