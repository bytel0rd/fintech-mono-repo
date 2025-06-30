package com.interswitch.middleware.integrations.banking.params;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BankInterBankLookupResponse extends BankIntraBankLookupResponse {
    private String destinationCode;
}
