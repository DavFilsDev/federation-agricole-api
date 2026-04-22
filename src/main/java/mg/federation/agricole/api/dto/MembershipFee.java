package mg.federation.agricole.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDate;

public class MembershipFee {
    private String id;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate eligibleFrom;
    private String frequency; // WEEKLY, MONTHLY, ANNUALLY, PUNCTUALLY
    private BigDecimal amount;
    private String label;
    private String status; // ACTIVE, INACTIVE

    // Constructeurs
    public MembershipFee() {}

    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public LocalDate getEligibleFrom() { return eligibleFrom; }
    public void setEligibleFrom(LocalDate eligibleFrom) { this.eligibleFrom = eligibleFrom; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
