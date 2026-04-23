package mg.federation.agricole.api.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionEntity {
    private String id;                 // Long → String (tx-C1-M1, tx-C2-M1, etc.)
    private String memberId;           // Long → String (C1-M1, C1-M2, etc.)
    private String collectivityId;     // Long → String (col-1, col-2, col-3)
    private BigDecimal amount;
    private String paymentMode;
    private String accountCreditedId;  // Long → String (C1-A-CASH, C2-A-MOBILE-1, etc.)
    private String membershipFeeId;    // Long → String (cot-1, cot-2, cot-3) (nullable)
    private LocalDate creationDate;

    public TransactionEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }

    public String getCollectivityId() { return collectivityId; }
    public void setCollectivityId(String collectivityId) { this.collectivityId = collectivityId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }

    public String getAccountCreditedId() { return accountCreditedId; }
    public void setAccountCreditedId(String accountCreditedId) { this.accountCreditedId = accountCreditedId; }

    public String getMembershipFeeId() { return membershipFeeId; }
    public void setMembershipFeeId(String membershipFeeId) { this.membershipFeeId = membershipFeeId; }

    public LocalDate getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDate creationDate) { this.creationDate = creationDate; }
}