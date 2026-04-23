package mg.federation.agricole.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CashAccount implements FinancialAccount {
    private String id;
    private BigDecimal amount;
    private String type = "CASH";

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

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
