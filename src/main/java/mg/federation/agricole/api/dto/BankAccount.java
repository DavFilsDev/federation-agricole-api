package mg.federation.agricole.api.dto;

import java.math.BigDecimal;

public class BankAccount implements FinancialAccount {
    private String id;
    private String holderName;
    private String bankName;
    private Integer bankCode;
    private Integer bankBranchCode;
    private Integer bankAccountNumber;
    private Integer bankAccountKey;
    private BigDecimal amount;

    public BankAccount() {}

    @Override
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getHolderName() { return holderName; }
    public void setHolderName(String holderName) { this.holderName = holderName; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public Integer getBankCode() { return bankCode; }
    public void setBankCode(Integer bankCode) { this.bankCode = bankCode; }

    public Integer getBankBranchCode() { return bankBranchCode; }
    public void setBankBranchCode(Integer bankBranchCode) { this.bankBranchCode = bankBranchCode; }

    public Integer getBankAccountNumber() { return bankAccountNumber; }
    public void setBankAccountNumber(Integer bankAccountNumber) { this.bankAccountNumber = bankAccountNumber; }

    public Integer getBankAccountKey() { return bankAccountKey; }
    public void setBankAccountKey(Integer bankAccountKey) { this.bankAccountKey = bankAccountKey; }

    @Override
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}