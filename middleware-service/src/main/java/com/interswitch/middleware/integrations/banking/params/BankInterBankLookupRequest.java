package com.interswitch.middleware.integrations.banking.params;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BankInterBankLookupRequest extends BankIntraBankLookupRequest {
    @NotBlank
    private String destinationBankCode;
}
