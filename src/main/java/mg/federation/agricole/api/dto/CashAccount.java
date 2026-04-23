package mg.federation.agricole.api.dto;

import java.math.BigDecimal;

public class CashAccount implements FinancialAccount {
    private String id;
    private BigDecimal amount;

    public CashAccount() {}

    public CashAccount(String id, BigDecimal amount) {
        this.id = id;
        this.amount = amount;
    }

    @Override
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @Override
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
