package com.interswitch.middleware.integrations.banking.params;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAccountResponse {
    private String accountName;
    private String accountNumber;
    private String platformId;
}
