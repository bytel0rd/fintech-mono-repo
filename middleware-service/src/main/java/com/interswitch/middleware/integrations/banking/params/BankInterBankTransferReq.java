package com.interswitch.middleware.integrations.banking.params;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BankInterBankTransferReq extends BankIntraBankTransferReq {
    private String destinationBankCode;
}
