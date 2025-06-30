package com.interswitch.middleware.params;

import com.interswitch.middleware.integrations.banking.params.AccountDetails;
import com.interswitch.middleware.integrations.banking.params.BvnData;
import lombok.*;

import java.util.Optional;

public class RegistrationStates {

    public static enum AccountCreationMode {
        CreateAccount,
        PullExistingAccount
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InitiatedState {
        private String sessionId;
        private AccountCreationMode creationMode;
        private RegisterInitiateReq req;
        private BvnData bvnData;
        private String otpCode;
        private boolean isOTPVerified;

        private AccountDetails accountDetails;


    }
}
