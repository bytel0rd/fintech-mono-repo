package com.interswitch.middleware.params;

import com.interswitch.middleware.integrations.banking.params.AccountDetails;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
public class LoginResponse {
    private String sessionId;
    private String firstName;
    private List<AccountDetails.AccountDetail> accounts = new ArrayList<>();
    private Boolean pinSetupRequired;
    private List<TransactionDto> recentTransactions = new ArrayList<>();
}
