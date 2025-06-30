package com.interswitch.middleware.integrations.banking.params;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDetails {
    private String accountName;
    private String platformId;

    @Builder.Default
    private List<AccountDetail> accounts = new ArrayList<>();

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountDetail {
        private String accountName;
        private String accountNumber;
        private String availableBalance;
        private String totalBalance;
    }
}
