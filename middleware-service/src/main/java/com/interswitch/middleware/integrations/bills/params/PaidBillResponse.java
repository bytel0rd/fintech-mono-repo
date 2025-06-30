package com.interswitch.middleware.integrations.bills.params;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaidBillResponse extends ValidateBillResponse{
    private String beneficiary;
    private String billCode;
    private String billName;
    private BigDecimal amount;
    private String transactionRef;
}
