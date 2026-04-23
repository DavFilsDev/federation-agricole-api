package mg.federation.agricole.api.entity;

import java.math.BigDecimal;

public class FinancialAccountEntity {
    private String id;
    private String type;
    private BigDecimal amount;

    public FinancialAccountEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}