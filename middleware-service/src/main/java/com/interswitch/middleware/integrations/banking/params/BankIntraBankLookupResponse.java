package com.interswitch.middleware.integrations.banking.params;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class BankIntraBankLookupResponse {
    private String accountName;
    private String accountNumber;
}
