package com.interswitch.middleware.integrations.banking;

import com.interswitch.middleware.integrations.banking.params.*;
import com.interswitch.middleware.params.ApiResponse;

import java.util.List;

public interface BankOperations {
    public ApiResponse<BvnData> lookupBVNData(String bvn);
    public ApiResponse<CreateAccountResponse> createAccount(CreateAccountReq createAccountReq);

    public ApiResponse<AccountDetails> getAccountDetails(String accountNumber);
    public ApiResponse<AccountDetails> getAccountDetailsByBvn(String bvn);

    public ApiResponse<BankIntraBankLookupResponse> intraBankLookup(String accountNumber);
    public ApiResponse<IntraBankTransferResponse> intraBankTransfer(BankIntraBankTransferReq transferReq);

    public ApiResponse<BankInterBankLookupResponse> interBankLookup(String destinationBank, String accountNumber);
    public ApiResponse<InterBankTransferResponse> interBankTransfer(BankInterBankTransferReq transferReq);

    public ApiResponse<List<Bank>> getBanks();
}
