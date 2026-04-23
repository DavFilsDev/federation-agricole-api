package mg.federation.agricole.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public class MemberPayment {
    private String id;
    private Integer amount;
    private String paymentMode;
    private FinancialAccount accountCredited;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate creationDate;

    public MemberPayment() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }

    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }

    public FinancialAccount getAccountCredited() { return accountCredited; }
    public void setAccountCredited(FinancialAccount accountCredited) { this.accountCredited = accountCredited; }

    public LocalDate getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDate creationDate) { this.creationDate = creationDate; }
}
