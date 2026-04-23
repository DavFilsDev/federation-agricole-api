package mg.federation.agricole.api.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.math.BigDecimal;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = CashAccount.class, name = "CASH"),
        @JsonSubTypes.Type(value = MobileBankingAccount.class, name = "MOBILE_BANKING"),
        @JsonSubTypes.Type(value = BankAccount.class, name = "BANK")
})
public interface FinancialAccountResponse {
    String getId();
    BigDecimal getAmount();
}
