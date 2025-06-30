package com.interswitch.middleware.integrations.banking.params;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
public class AccountDetails {
    private String accountName;
    private String platformId;

    @Builder.Default
    private List<AccountDetail> accounts = new ArrayList<>();

    @Getter
    @Setter
    @Builder
    public static class AccountDetail {
        private String accountName;
        private String accountNumber;
        private String availableBalance;
        private String totalBalance;
    }
}
