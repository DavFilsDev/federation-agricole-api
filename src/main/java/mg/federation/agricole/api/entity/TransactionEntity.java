package mg.federation.agricole.api.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionEntity {
    private String id;
    private String memberId;
    private String collectivityId;
    private BigDecimal amount;
    private String paymentMode;
    private String accountCreditedId;
    private String membershipFeeId;
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