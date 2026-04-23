package mg.federation.agricole.api.dto;

public class CreateMemberPayment {
    private Integer amount;
    private String membershipFeeIdentifier;
    private String accountCreditedIdentifier;
    private String paymentMode; // CASH, MOBILE_BANKING, BANK_TRANSFER

    public CreateMemberPayment() {}

    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }

    public String getMembershipFeeIdentifier() { return membershipFeeIdentifier; }
    public void setMembershipFeeIdentifier(String membershipFeeIdentifier) { this.membershipFeeIdentifier = membershipFeeIdentifier; }

    public String getAccountCreditedIdentifier() { return accountCreditedIdentifier; }
    public void setAccountCreditedIdentifier(String accountCreditedIdentifier) { this.accountCreditedIdentifier = accountCreditedIdentifier; }

    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }
}
