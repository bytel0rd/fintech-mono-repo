package com.interswitch.middleware.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interswitch.middleware.integrations.banking.BankOperations;
import com.interswitch.middleware.integrations.banking.params.*;
import com.interswitch.middleware.integrations.bills.BillsOperation;
import com.interswitch.middleware.integrations.bills.params.Bill;
import com.interswitch.middleware.integrations.bills.params.PaidBillResponse;
import com.interswitch.middleware.integrations.bills.params.ValidateBill;
import com.interswitch.middleware.integrations.bills.params.ValidateBillResponse;
import com.interswitch.middleware.models.Transaction;
import com.interswitch.middleware.models.User;
import com.interswitch.middleware.params.*;
import com.interswitch.middleware.repo.TransactionRepo;
import com.interswitch.middleware.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MiddlewareService {

    // configs are ideally pulled under @Refreshed annotation for refreshing configs.
    @Value("${registration.timeoutInSec:300}")
    private Long registerTimeoutInSec;

    @Value("${account.pinLength:4}")
    private int accountPinLength;

    @Value("${collections.accountNumber:0000000000}")
    private String collectionsAccount;

    public static String MOCKED_OTP_CODE = "5341";

    private final BankOperations bankIntegration;
    private final BillsOperation billsOperation;
    private final UserRepo userRepo;
    private final TransactionRepo transactionRepo;
    private final RedisService redis;


    private final ObjectMapper mapper = new ObjectMapper();
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public ApiResponse<RegisterInitiateSessionResponse> initiateRegistration(RegisterInitiateReq req) {
        ApiResponse<RegisterInitiateSessionResponse> response = new ApiResponse<>(ApiResponse.ERROR_CODE, ApiResponse.GENERAL_ERROR_MESSAGE);
        try {
            if (!isNumericOfLength(req.getBvn(), 11)) {
                response.setMessage("BVN should be 11 digits");
                return response;
            }
            if (userRepo.findByBvn(req.getBvn()).isPresent()) {
                response.setMessage("BVN account already exist, please login");
                return response;
            }

            // should cache (successful) BVN here because of cost and multiple retries.
            ApiResponse<BvnData> bvnLookupResponse = bankIntegration.lookupBVNData(req.getBvn());
            if (!bvnLookupResponse.isSuccessful()) {
                response.setMessage("Invalid bvn details");
                return response;
            }

            BvnData bvnData = bvnLookupResponse.getData();
            if (!bvnData.getDob().equalsIgnoreCase(req.getDob())) {
                response.setMessage("Invalid bvn details");
                return response;
            }
            RegistrationStates.InitiatedState initiatedState = RegistrationStates.InitiatedState.builder()
                    .sessionId(UUID.randomUUID().toString())
                    .req(req)
                    .creationMode(RegistrationStates.AccountCreationMode.CreateAccount)
                    .bvnData(bvnData)
                    .accountDetails(null)
                    .otpCode(MOCKED_OTP_CODE) // OTP can be hashed.
                    .isOTPVerified(false)
                    .build();

            ApiResponse<AccountDetails> accountDetailsApiResponse = bankIntegration.getAccountDetailsByBvn(req.getBvn());
            if (accountDetailsApiResponse.isSuccessful()) {
                if (!accountDetailsApiResponse.getData().getAccounts().isEmpty()) {
                    initiatedState.setCreationMode(RegistrationStates.AccountCreationMode.PullExistingAccount);
                    initiatedState.setAccountDetails(accountDetailsApiResponse.getData());
                }
            }

            redis.setValue(initiatedState.getSessionId(), mapper.writeValueAsString(initiatedState));
            String phoneNumber = bvnData.getPhoneNumber();
            sendSMS(phoneNumber, String.format("Your OTP code is: %s", MOCKED_OTP_CODE));
            RegisterInitiateSessionResponse session = RegisterInitiateSessionResponse.builder().sessionId(initiatedState.getSessionId()).build();
            String maskedPhoneNumber = "***" + phoneNumber.substring(phoneNumber.length() - 3, phoneNumber.length() - 1);
            response = new ApiResponse<>(ApiResponse.SUCCESS_CODE, String.format("OTP sent to %s phoneNumber", maskedPhoneNumber));
            response.setData(session);
        } catch (Exception e) {
            log.error("Initiate registration failed", e);
        }
        return response;
    }

    public void sendSMS(String destination, String message) {
        log.info("SENT SMS TO: {} | {}", destination, message);
    }

    public ApiResponse<?> verifyOtp(VerifyOtpReq otpReq) {
        ApiResponse<?> response = new ApiResponse<>(ApiResponse.ERROR_CODE, ApiResponse.GENERAL_ERROR_MESSAGE);
        try {
            String cachedSession = redis.getValue(otpReq.getSessionId());
            if (StringUtils.isBlank(cachedSession)) {
                response.setMessage("Invalid session or session expired");
                return response;
            }
            RegistrationStates.InitiatedState initiatedState = mapper.readValue(cachedSession, RegistrationStates.InitiatedState.class);
            if (!initiatedState.getOtpCode().equalsIgnoreCase(otpReq.getOtpCode())) {
                response.setMessage("Invalid OTP");
                return response;
            }
            initiatedState.setOTPVerified(true);
            redis.setValue(initiatedState.getSessionId(), mapper.writeValueAsString(initiatedState));
            response = new ApiResponse<>(ApiResponse.SUCCESS_CODE, "Continue to setup password");
        } catch (Exception e) {
            log.error("Verify registration OTP failed", e);
        }
        return response;
    }

    public ApiResponse<CompleteRegistrationReq> setupPassword(SetupPasswordReq setupPasswordReq) {
        ApiResponse<CompleteRegistrationReq> response = new ApiResponse<>(ApiResponse.ERROR_CODE, ApiResponse.GENERAL_ERROR_MESSAGE);
        try {
            // skipping strong password compliance here;
            if (setupPasswordReq.getPassword().length() < 8) {
                response.setMessage("Password should be a minium of 8 characters");
                return response;
            }

            String cachedSession = redis.getValue(setupPasswordReq.getSessionId());
            if (StringUtils.isBlank(cachedSession)) {
                response.setMessage("Invalid session or session expired");
                return response;
            }
            RegistrationStates.InitiatedState initiatedState = mapper.readValue(cachedSession, RegistrationStates.InitiatedState.class);
            if (!initiatedState.isOTPVerified()) {
                response.setMessage("OTP verification required");
                return response;
            }

            String primaryAccountNumber = null;
            String accountName = null;
            String platformId = null;

            switch (initiatedState.getCreationMode()) {
                case PullExistingAccount -> {
                    AccountDetails accountDetails = initiatedState.getAccountDetails();
                    platformId = accountDetails.getPlatformId();
                    primaryAccountNumber = accountDetails.getAccounts().get(0).getAccountNumber();
                    accountName = accountDetails.getAccounts().get(0).getAccountName();
                }
                case CreateAccount -> {
                    CreateAccountReq createAccountReq = CreateAccountReq.builder()
                            .bvn(initiatedState.getReq().getBvn())
                            .dob(initiatedState.getReq().getDob())
                            .firstName(initiatedState.getBvnData().getFirstName())
                            .lastName(initiatedState.getBvnData().getLastName())
                            .middleName(initiatedState.getBvnData().getMiddleName())
                            .gender(initiatedState.getBvnData().getGender())
                            .phoneNumber(initiatedState.getBvnData().getPhoneNumber())
                            .build();
                    ApiResponse<CreateAccountResponse> createAccountResponse = bankIntegration.createAccount(createAccountReq);
                    if (!createAccountResponse.isSuccessful()) {
                        response.setMessage(createAccountResponse.getMessage());
                        return response;
                    }
                    primaryAccountNumber = createAccountResponse.getData().getAccountNumber();
                    platformId = createAccountResponse.getData().getPlatformId();
                    accountName = createAccountResponse.getData().getAccountName();
                }
            }

            User user = User.builder()
                    .primaryAccountNumber(primaryAccountNumber)
                    .platformId(platformId)
                    .bvn(initiatedState.getReq().getBvn())
                    .passwordHash(passwordEncoder.encode(setupPasswordReq.getPassword()))
                    .firstName(initiatedState.getBvnData().getFirstName())
                    .lastName(initiatedState.getBvnData().getLastName())
                    .phoneNumber(initiatedState.getBvnData().getPhoneNumber())
                    .dob(initiatedState.getReq().getDob())
                    .createdAt(new Date())
                    .build();

            userRepo.save(user);

            CompleteRegistrationReq completeRegistrationReq = new CompleteRegistrationReq();
            completeRegistrationReq.setAccountName(accountName);
            completeRegistrationReq.setDemoPhoneNumber(user.getPhoneNumber());
            completeRegistrationReq.setAccountNumber(primaryAccountNumber);
            response = new ApiResponse<>(ApiResponse.SUCCESS_CODE, "Account created, proceed to login");
            response.setData(completeRegistrationReq);
        } catch (Exception e) {
            log.error("Complete registration setup failed", e);
        }
        return response;

    }

    public ApiResponse<LoginResponse> login(LoginReq loginReq) {
        ApiResponse<LoginResponse> response = new ApiResponse<>(ApiResponse.ERROR_CODE, ApiResponse.GENERAL_ERROR_MESSAGE);

        try {
            Optional<User> savedUser = userRepo.findByPhoneNumber(loginReq.getPhoneNumber());
            if (savedUser.isEmpty()) {
                response.setMessage("Invalid credentials");
                return response;
            }
            User user = savedUser.get();
            if (!passwordEncoder.matches(loginReq.getPassword(), user.getPasswordHash())) {
                // should account locking here, for failed passwords and bruteforce attempts
                response.setMessage("Invalid credentials");
                return response;
            }
            String sessionId = UUID.randomUUID().toString();
            boolean requirePinSetup = user.getPinHash() == null;
            user.setLastLoginTime(new Date());

            // crude auth session flow
            SessionData sessionData = SessionData.builder()
                    .userId(user.getId())
                    .channel(loginReq.getChannel())
                    .platformId(user.getPlatformId())
                    .firstName(user.getFirstName())
                    .build();

            redis.setValue(sessionId, mapper.writeValueAsString(sessionData));
            user = userRepo.save(user);
            ApiResponse<AccountDetailResponse> accountResponse = getAccountDetails(user.getId());
            if (!accountResponse.isSuccessful()) {
                response.setMessage(accountResponse.getMessage());
                return response;
            }
            ApiResponse<List<TransactionDto>> transactionsResponse = getTransactionHistory(user.getId());
            if (!accountResponse.isSuccessful()) {
                response.setMessage(accountResponse.getMessage());
                return response;
            }
            List<TransactionDto> transactions = transactionsResponse.getData();
            LoginResponse loginRes = LoginResponse.builder()
                    .sessionId(sessionId)
                    .firstName(user.getFirstName())
                    .accounts(accountResponse.getData().getAccounts())
                    .pinSetupRequired(requirePinSetup)
                    .recentTransactions(transactions.subList(0, Math.min(5, transactions.size())))
                    .build();
            ApiResponse<LoginResponse> successfulResponse = new ApiResponse<>(ApiResponse.SUCCESS_CODE, requirePinSetup ? "PIN setup required" : ApiResponse.GENERAL_SUCCESS_MESSAGE);
            successfulResponse.setData(loginRes);
            return successfulResponse;
        } catch (Exception e) {
            log.error("Login failed: ", e);
        }
        return response;
    }

    public ApiResponse<?> setupPin(SetupPinReq setupPinReq) {
        ApiResponse<?> response = new ApiResponse<>(ApiResponse.ERROR_CODE, ApiResponse.GENERAL_ERROR_MESSAGE);
        try {
            if (!isNumericOfLength(setupPinReq.getPin(), accountPinLength)) {
                response.setMessage(String.format("Pin of %s required", accountPinLength));
                return response;
            }
            Optional<User> savedUser = userRepo.findById(setupPinReq.getUserId());
            if (savedUser.isEmpty()) {
                log.error("Failed to find userID: {}", setupPinReq.getUserId());
                response.setMessage("Unknown Account; contact administrator");
                return response;
            }
            User user = savedUser.get();
            if (StringUtils.isNotBlank(user.getPinHash())) {
                response.setMessage("PIN setup already completed");
                return response;
            }
            user.setPinHash(passwordEncoder.encode(setupPinReq.getPin()));
            userRepo.save(user);
            return new ApiResponse<>(ApiResponse.SUCCESS_CODE, ApiResponse.GENERAL_SUCCESS_MESSAGE);
        } catch (Exception e) {
            log.error("PIN setup failed", e);
        }
        return response;
    }


    public ApiResponse<AccountDetailResponse> getAccountDetails(Long userId) {
        ApiResponse<AccountDetailResponse> response = new ApiResponse<>(ApiResponse.ERROR_CODE, ApiResponse.GENERAL_ERROR_MESSAGE);
        try {

            Optional<User> savedUser = userRepo.findById(userId);
            if (savedUser.isEmpty()) {
                response.setMessage("Unknown Account");
                return response;
            }
            User user = savedUser.get();

            ApiResponse<AccountDetails> accountDetailsResponse = bankIntegration.getAccountDetails(user.getPrimaryAccountNumber());
            if (!accountDetailsResponse.isSuccessful()) {
                return ApiResponse.Error(accountDetailsResponse.getMessage());
            }
            AccountDetails details = accountDetailsResponse.getData();
            AccountDetailResponse resp = new AccountDetailResponse();
            resp.setFirstName(user.getFirstName());
            resp.setLastName(user.getLastName());
            resp.setAccounts(details.getAccounts());
            return ApiResponse.Success(resp);
        } catch (Exception e) {
            log.error("Get Account details failed", e);
        }
        return response;
    }

    public ApiResponse<BankIntraBankLookupResponse> intraBankLookup(String accountNumber) {
        return bankIntegration.intraBankLookup(accountNumber);
    }

    public ApiResponse<IntraBankTransferResponse> intraBankTransfer(IntraBankTransferReq transferReq) {
        if (transferReq.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return new ApiResponse<>(ApiResponse.ERROR_CODE, "Invalid amount");
        }
        ApiResponse<IntraBankTransferResponse> response = new ApiResponse<>(ApiResponse.ERROR_CODE, ApiResponse.GENERAL_ERROR_MESSAGE);

        Optional<User> savedUser = userRepo.findById(transferReq.getUserId());
        if (savedUser.isEmpty()) {
            response.setMessage("Unknown Account");
            return response;
        }
        User user = savedUser.get();

        if (StringUtils.isBlank(user.getPinHash())) {
            response.setMessage("Pin setup required");
            return response;
        }
        if (!passwordEncoder.matches(transferReq.getPin(), user.getPinHash())) {
            response.setMessage("Incorrect PIN");
            return response;
        }

        Transaction txn = Transaction.builder()
                .userId(transferReq.getUserId())
                .transactionRef(transferReq.getTransactionRef())
                .sourceAccountNumber(transferReq.getSourceAccountNumber())
                .destinationAccountNumber(transferReq.getDestinationAccountNumber())
                .destinationAccountName(transferReq.getDestinationAccountName())
                .destinationBankCode(null)
                .amount(transferReq.getAmount())
                .charge(java.math.BigDecimal.ZERO)
                .status(TransactionStatus.INITIATED)
                .transactionType(TransactionType.INTRA_BANK_TRANSFER)
                .message("Intra-bank transfer initiated")
                .narration(StringUtils.isNotBlank(transferReq.getNarration()) ? transferReq.getNarration() : "INTRA/" + transferReq.getDestinationAccountNumber())
                .createdAt(new java.util.Date())
                .channel(transferReq.getChannel())
                .build();
        transactionRepo.save(txn);

        response = bankIntegration.intraBankTransfer(transferReq);
        txn.setStatus(response.isSuccessful() ? TransactionStatus.SUCCESSFUL : TransactionStatus.FAILED);
        txn.setMessage(response.isSuccessful() ? "Intra-bank transfer successful" : response.getMessage());
        transactionRepo.save(txn);
        return response;
    }

    public ApiResponse<BankInterBankLookupResponse> interBankLookup(BankInterBankLookupRequest lookupRequest) {
        ApiResponse<BankInterBankLookupResponse> response = new ApiResponse<>(ApiResponse.ERROR_CODE, ApiResponse.GENERAL_ERROR_MESSAGE);
        try {
            response = bankIntegration.interBankLookup(lookupRequest.getDestinationBankCode(), lookupRequest.getAccountNumber());
        } catch (Exception e) {
            log.error("IntraBank lookup failed", e);
        }
        return response;
    }

    public ApiResponse<InterBankTransferResponse> interBankTransfer(InterBankTransferReq transferReq) {
        ApiResponse<InterBankTransferResponse> response = new ApiResponse<>(ApiResponse.ERROR_CODE, ApiResponse.GENERAL_ERROR_MESSAGE);
        try {
            if (transferReq.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return new ApiResponse<>(ApiResponse.ERROR_CODE, "Invalid amount");
            }
            Optional<User> savedUser = userRepo.findById(transferReq.getUserId());
            if (savedUser.isEmpty()) {
                response.setMessage("Unknown Account");
                return response;
            }
            User user = savedUser.get();

            if (StringUtils.isBlank(user.getPinHash())) {
                response.setMessage("Pin setup required");
                return response;
            }
            if (!passwordEncoder.matches(transferReq.getPin(), user.getPinHash())) {
                response.setMessage("Incorrect PIN");
                return response;
            }

            Transaction txn = Transaction.builder()
                    .userId(transferReq.getUserId())
                    .transactionRef(transferReq.getTransactionRef())
                    .sourceAccountNumber(transferReq.getSourceAccountNumber())
                    .destinationAccountNumber(transferReq.getDestinationAccountNumber())
                    .destinationAccountName(transferReq.getDestinationAccountName())
                    .destinationBankCode(transferReq.getDestinationBankCode())
                    .amount(transferReq.getAmount())
                    .charge(java.math.BigDecimal.ZERO)
                    .status(TransactionStatus.INITIATED)
                    .transactionType(TransactionType.INTER_BANK_TRANSFER)
                    .message("Inter-bank transfer initiated")
                    .narration(StringUtils.isNotBlank(transferReq.getNarration()) ? transferReq.getNarration() : "OTB/" + transferReq.getDestinationAccountNumber())
                    .createdAt(new java.util.Date())
                    .channel(transferReq.getChannel())
                    .build();
            transactionRepo.save(txn);

            response = bankIntegration.interBankTransfer(transferReq);
            txn.setStatus(response.isSuccessful() ? TransactionStatus.SUCCESSFUL : TransactionStatus.FAILED);
            txn.setMessage(response.isSuccessful() ? "Inter-bank transfer successful" : response.getMessage());
            transactionRepo.save(txn);
        } catch (Exception e) {
            log.error("Other bank transfer failed", e);
        }
        return response;
    }

    public ApiResponse<List<Bank>> getBanks() {
        return bankIntegration.getBanks();
    }

    // not an extensive transaction history intentionally
    public ApiResponse<List<TransactionDto>> getTransactionHistory(Long userId) {
        ApiResponse<List<TransactionDto>> response = new ApiResponse<>(ApiResponse.ERROR_CODE, ApiResponse.GENERAL_ERROR_MESSAGE);
        try {
            List<Transaction> txns = transactionRepo.findByUserId(userId);
            List<TransactionDto> dtos = txns.stream().map(txn -> {
                TransactionDto dto = new TransactionDto();
                dto.setSourceAccountNumber(txn.getSourceAccountNumber());
                dto.setTransactionRef(txn.getTransactionRef());
                dto.setCharge(txn.getCharge());
                dto.setAmount(txn.getAmount());
                dto.setStatus(txn.getStatus());
                dto.setTransactionType(txn.getTransactionType());
                dto.setCreatedAt(txn.getCreatedAt());
                dto.setChannel(txn.getChannel());
                dto.setNarration(txn.getNarration());
                dto.setDestinationBankCode(txn.getDestinationBankCode());
                if (txn.getTransactionType() == TransactionType.BILL_PAYMENT) {
                    dto.setBeneficiary(txn.getBeneficiary());
                } else {
                    dto.setBeneficiary(txn.getDestinationAccountNumber());
                    dto.setDestinationAccountName(txn.getDestinationAccountName());
                }
                return dto;
            }).toList();
            return ApiResponse.Success(dtos);
        } catch (Exception e) {
            log.error("Get transaction history failed", e);
        }
        return response;
    }

    public ApiResponse<List<Bill>> getBills() {
        return billsOperation.getBills();
    }

    public ApiResponse<ValidateBillResponse> validateBill(ValidateBill validateBill) {
        return billsOperation.validateBill(validateBill);
    }

    public ApiResponse<PaidBillResponse> payBills(PayBillReq payBillReq) {
        ApiResponse<PaidBillResponse> response = new ApiResponse<>(ApiResponse.ERROR_CODE, ApiResponse.GENERAL_ERROR_MESSAGE);
        try {
            payBillReq.setTransactionRef(UUID.randomUUID().toString());
            ApiResponse<ValidateBillResponse> validatedBillResponse = validateBill(payBillReq);
            if (!validatedBillResponse.isSuccessful()) {
                response.setMessage(validatedBillResponse.getMessage());
                return response;
            }
            ValidateBillResponse validateBill = validatedBillResponse.getData();
            // Pin validation
            Optional<User> savedUser = userRepo.findById(payBillReq.getUserId());
            if (savedUser.isEmpty()) {
                response.setMessage("Unknown Account");
                return response;
            }
            User user = savedUser.get();
            if (StringUtils.isBlank(user.getPinHash())) {
                response.setMessage("Pin setup required");
                return response;
            }
            if (!passwordEncoder.matches(payBillReq.getPin(), user.getPinHash())) {
                response.setMessage("Incorrect PIN");
                return response;
            }

            // Save transaction before vending
            Transaction txn = Transaction.builder()
                    .userId(payBillReq.getUserId())
                    .transactionRef(payBillReq.getTransactionRef())
                    .sourceAccountNumber(user.getPrimaryAccountNumber())
                    .destinationAccountNumber(collectionsAccount)
                    .destinationAccountName("Collections Account")
                    .destinationBankCode(null)
                    .amount(validateBill.getAmount())
                    .charge(java.math.BigDecimal.ZERO)
                    .status(TransactionStatus.INITIATED)
                    .transactionType(TransactionType.BILL_PAYMENT)
                    .message("Bill payment initiated")
                    .narration("Bill/" + payBillReq.getBeneficiary())
                    .createdAt(new java.util.Date())
                    .channel(payBillReq.getChannel())
                    .build();
            transactionRepo.save(txn);

            ApiResponse<PaidBillResponse> billResponse = billsOperation.payBills(payBillReq);
            txn.setStatus(billResponse.isSuccessful() ? TransactionStatus.SUCCESSFUL : TransactionStatus.FAILED);
            txn.setMessage(billResponse.isSuccessful() ? "Bill payment successful" : billResponse.getMessage());
            transactionRepo.save(txn);
            return billResponse;
        } catch (Exception e) {
            log.error("Bill payment failed", e);
        }
        return response;
    }

    public static boolean isNumericOfLength(String input, int length) {
        return input != null && input.matches("\\d{" + length + "}");
    }

    // crude session management
    public Optional<SessionData> getSession(String sessionId) {
        try {
            SessionData sessionData = mapper.readValue(redis.getValue(sessionId), SessionData.class);
            return Optional.ofNullable(sessionData);
        } catch (Exception e) {
            log.error("Session Error");
        }
        return Optional.empty();
    }
}
