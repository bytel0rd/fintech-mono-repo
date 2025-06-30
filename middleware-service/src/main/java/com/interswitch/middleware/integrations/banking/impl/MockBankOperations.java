package com.interswitch.middleware.integrations.banking.impl;

import com.interswitch.middleware.integrations.banking.BankOperations;
import com.interswitch.middleware.integrations.banking.params.*;
import com.interswitch.middleware.params.ApiResponse;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.*;

@Service
public class MockBankOperations implements BankOperations {

    private static final String MOCK_ACCOUNT_NUMBER = "1234567890";
    private static final String MOCK_PLATFORM_ID = "PL" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

    @Override
    public ApiResponse<BvnData> lookupBVNData(String bvn) {
        // According to notes.md - Valid BVN starts with "2"
        if (!bvn.startsWith("2")) {
            return new ApiResponse<>(ApiResponse.ERROR_CODE, "Invalid BVN provided");
        }

        BvnData bvnData = BvnData.builder()
                .bvn(bvn)
                .dob("1990-10-10") // From notes.md
                .firstName("John")
                .lastName("Doe")
                .middleName("Middle")
                .gender("M")
                .phoneNumber("234801234" + new SecureRandom().nextLong(1000, 9999))
                .build();

        return ApiResponse.Success(bvnData);
    }

    @Override
    public ApiResponse<CreateAccountResponse> createAccount(CreateAccountReq createAccountReq) {
        CreateAccountResponse response = new CreateAccountResponse();
        long randomBit = new SecureRandom().nextLong(1000, 9999);
        response.setAccountName(createAccountReq.getFirstName() + " " + createAccountReq.getLastName());
        response.setAccountNumber("013456" + randomBit);
        response.setPlatformId(String.valueOf(randomBit));
        return ApiResponse.Success(response);
    }

    @Override
    public ApiResponse<AccountDetails> getAccountDetails(String accountNumber) {
        AccountDetails.AccountDetail detail = AccountDetails.AccountDetail.builder()
                .accountName("John Doe")
                .accountNumber(accountNumber)
                .availableBalance("1000000.00")
                .totalBalance("1000000.00")
                .build();

        AccountDetails accountDetails = AccountDetails.builder()
                .accountName("John Doe")
                .platformId("MOCKED_" + MOCK_PLATFORM_ID)
                .accounts(Collections.singletonList(detail))
                .build();

        return ApiResponse.Success(accountDetails);
    }

    @Override
    public ApiResponse<AccountDetails> getAccountDetailsByBvn(String bvn) {
        if (!bvn.startsWith("2")) {
            return new ApiResponse<>(ApiResponse.ERROR_CODE, "Invalid BVN provided");
        }

        long randomBit = new SecureRandom().nextLong(1000, 9999);
        return getAccountDetails("013456" + randomBit);
    }

    @Override
    public ApiResponse<BankIntraBankLookupResponse> intraBankLookup(String accountNumber) {
        BankIntraBankLookupResponse response = new BankIntraBankLookupResponse();
        response.setAccountName("John Doe");
        response.setAccountNumber(accountNumber);
        return ApiResponse.Success(response);
    }

    @Override
    public ApiResponse<IntraBankTransferResponse> intraBankTransfer(BankIntraBankTransferReq transferReq) {
        IntraBankTransferResponse response = new IntraBankTransferResponse();
        response.setAccountName(transferReq.getDestinationAccountName());
        response.setAccountNumber(transferReq.getDestinationAccountNumber());
        response.setTransactionRef(transferReq.getTransactionRef());
        response.setAmount(transferReq.getAmount());
        return ApiResponse.Success(response);
    }

    @Override
    public ApiResponse<BankInterBankLookupResponse> interBankLookup(String destinationBank, String accountNumber) {
        BankInterBankLookupResponse response = new BankInterBankLookupResponse();
        response.setAccountName("John Doe");
        response.setAccountNumber(accountNumber);
        response.setDestinationCode(destinationBank);
        return ApiResponse.Success(response);
    }

    @Override
    public ApiResponse<InterBankTransferResponse> interBankTransfer(BankInterBankTransferReq transferReq) {
        InterBankTransferResponse response = new InterBankTransferResponse();
        response.setAccountName(transferReq.getDestinationAccountName());
        response.setAccountNumber(transferReq.getDestinationAccountNumber());
        response.setTransactionRef(transferReq.getTransactionRef());
        response.setDestinationBankCode(transferReq.getDestinationBankCode());
        response.setAmount(transferReq.getAmount());
        return ApiResponse.Success(response);
    }

    @Override
    public ApiResponse<List<Bank>> getBanks() {
        List<Bank> banks = new ArrayList<>();
        Bank bank1 = new Bank();
        bank1.setName("Mock Bank 1");
        bank1.setCode("001");

        Bank bank2 = new Bank();
        bank2.setName("Mock Bank 2");
        bank2.setCode("002");

        banks.add(bank1);
        banks.add(bank2);

        return ApiResponse.Success(banks);
    }
}
