package mg.federation.agricole.api.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MembershipFeeEntity {
    private Long id;
    private Long collectivityId;
    private LocalDate eligibleFrom;
    private String frequency;
    private BigDecimal amount;
    private String label;
    private String status;

    // Constructeurs
    public MembershipFeeEntity() {}

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCollectivityId() { return collectivityId; }
    public void setCollectivityId(Long collectivityId) { this.collectivityId = collectivityId; }

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
