package mg.federation.agricole.api.entity;

import java.math.BigDecimal;

public class FinancialAccountEntity {
    private Long id;
    private String type; // CASH, MOBILE_BANKING, BANK
    private BigDecimal amount;

    public FinancialAccountEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
