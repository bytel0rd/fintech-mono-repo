package com.interswitch.middleware.params;

import com.interswitch.middleware.integrations.banking.params.AccountDetails;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AccountDetailResponse {
    private String firstName;
    private String lastName;
    private List<AccountDetails.AccountDetail> accounts = new ArrayList<>();
}
