package mg.federation.agricole.api.dto;

import java.math.BigDecimal;

public class CollectivityOverallStatistics {
    private CollectivityInformation collectivityInformation;
    private Integer newMembersNumber;
    private BigDecimal overallMemberCurrentDuePercentage;

    public CollectivityOverallStatistics() {}

    public CollectivityInformation getCollectivityInformation() { return collectivityInformation; }
    public void setCollectivityInformation(CollectivityInformation collectivityInformation) { this.collectivityInformation = collectivityInformation; }

    public Integer getNewMembersNumber() { return newMembersNumber; }
    public void setNewMembersNumber(Integer newMembersNumber) { this.newMembersNumber = newMembersNumber; }

    public BigDecimal getOverallMemberCurrentDuePercentage() { return overallMemberCurrentDuePercentage; }
    public void setOverallMemberCurrentDuePercentage(BigDecimal overallMemberCurrentDuePercentage) { this.overallMemberCurrentDuePercentage = overallMemberCurrentDuePercentage; }
}