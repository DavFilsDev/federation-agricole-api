package mg.federation.agricole.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CreateMembershipFee {
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate eligibleFrom;
    private String frequency; // WEEKLY, MONTHLY, ANNUALLY, PUNCTUALLY
    private BigDecimal amount;
    private String label;

    // Constructeurs
    public CreateMembershipFee() {}

    // Getters et Setters
    public LocalDate getEligibleFrom() { return eligibleFrom; }
    public void setEligibleFrom(LocalDate eligibleFrom) { this.eligibleFrom = eligibleFrom; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
}
