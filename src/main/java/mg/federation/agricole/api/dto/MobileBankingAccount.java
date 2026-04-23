package mg.federation.agricole.api.dto;

import java.math.BigDecimal;

public class MobileBankingAccount implements FinancialAccount {
    private String id;
    private String holderName;
    private String mobileBankingService; // AIRTEL_MONEY, MVOLA, ORANGE_MONEY
    private String mobileNumber;
    private BigDecimal amount;

    public MobileBankingAccount() {}

    @Override
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getHolderName() { return holderName; }
    public void setHolderName(String holderName) { this.holderName = holderName; }

    public String getMobileBankingService() { return mobileBankingService; }
    public void setMobileBankingService(String mobileBankingService) { this.mobileBankingService = mobileBankingService; }

    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

    @Override
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
