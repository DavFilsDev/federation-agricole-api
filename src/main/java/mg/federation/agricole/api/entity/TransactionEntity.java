package mg.federation.agricole.api.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionEntity {
    private Long id;
    private Long memberId;
    private Long collectivityId;
    private BigDecimal amount;
    private String paymentMode;
    private Long accountCreditedId;
    private Long membershipFeeId;
    private LocalDate creationDate;

    public TransactionEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }

    public Long getCollectivityId() { return collectivityId; }
    public void setCollectivityId(Long collectivityId) { this.collectivityId = collectivityId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }

    public Long getAccountCreditedId() { return accountCreditedId; }
    public void setAccountCreditedId(Long accountCreditedId) { this.accountCreditedId = accountCreditedId; }

    public Long getMembershipFeeId() { return membershipFeeId; }
    public void setMembershipFeeId(Long membershipFeeId) { this.membershipFeeId = membershipFeeId; }

    public LocalDate getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDate creationDate) { this.creationDate = creationDate; }
}
