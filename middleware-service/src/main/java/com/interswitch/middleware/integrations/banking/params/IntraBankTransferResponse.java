package com.interswitch.middleware.integrations.banking.params;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class IntraBankTransferResponse {
    private String accountName;
    private String accountNumber;
    private String transactionRef;
    private String destinationBankCode;
    private BigDecimal amount;
}
