package com.interswitch.middleware.params;

import com.interswitch.middleware.integrations.banking.params.AccountDetails;
import com.interswitch.middleware.integrations.banking.params.BvnData;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

public class RegistrationStates {

    public static enum AccountCreationMode {
        CreateAccount,
        PullExistingAccount
    }

    @Getter
    @Setter
    @Builder
    public static class InitiatedState {
        private String sessionId;
        private AccountCreationMode creationMode;
        private RegisterInitiateReq req;
        private BvnData bvnData;
        private String otpCode;
        private boolean isOTPVerified;

        @Builder.Default
        private Optional<AccountDetails> accountDetails = Optional.empty();


    }
}
