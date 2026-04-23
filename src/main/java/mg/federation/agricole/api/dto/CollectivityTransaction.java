package mg.federation.agricole.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CollectivityTransaction {
    private String id;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate creationDate;
    private BigDecimal amount;
    private String paymentMode; // CASH, MOBILE_BANKING, BANK_TRANSFER
    private FinancialAccount accountCredited;
    private Member memberDebited;

    // Constructeurs
    public CollectivityTransaction() {}

    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public LocalDate getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDate creationDate) { this.creationDate = creationDate; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }

    public FinancialAccount getAccountCredited() { return accountCredited; }
    public void setAccountCredited(FinancialAccount accountCredited) { this.accountCredited = accountCredited; }

    public Member getMemberDebited() { return memberDebited; }
    public void setMemberDebited(Member memberDebited) { this.memberDebited = memberDebited; }
}
