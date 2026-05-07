package mg.federation.agricole.api.dto;

import java.math.BigDecimal;

public class CollectivityLocalStatistics {
    private MemberDescription memberDescription;
    private BigDecimal earnedAmount;
    private BigDecimal unpaidAmount;

    public CollectivityLocalStatistics() {}

    public MemberDescription getMemberDescription() { return memberDescription; }
    public void setMemberDescription(MemberDescription memberDescription) { this.memberDescription = memberDescription; }

    public BigDecimal getEarnedAmount() { return earnedAmount; }
    public void setEarnedAmount(BigDecimal earnedAmount) { this.earnedAmount = earnedAmount; }

    public BigDecimal getUnpaidAmount() { return unpaidAmount; }
    public void setUnpaidAmount(BigDecimal unpaidAmount) { this.unpaidAmount = unpaidAmount; }
}