package com.interswitch.middleware.client;

import com.interswitch.middleware.integrations.banking.params.BankInterBankLookupRequest;
import com.interswitch.middleware.integrations.banking.params.BankIntraBankLookupRequest;
import com.interswitch.middleware.integrations.bills.params.ValidateBill;
import com.interswitch.middleware.params.*;
import com.interswitch.middleware.service.MiddlewareService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class Operations {

    private final MiddlewareService middlewareSvc;

    @GetMapping("/status")
    public ApiResponse<?> health() {
        return new ApiResponse<>(ApiResponse.SUCCESS_CODE, "Alive");
    }

    @PostMapping("/auth/registration/initiate")
    public ApiResponse<?> initiateRegistration(@RequestBody @Valid RegisterInitiateReq req) {
        return middlewareSvc.initiateRegistration(req);
    }

    @PostMapping("/auth/registration/verify")
    public ApiResponse<?> verifyOtp(@RequestBody @Valid VerifyOtpReq otpReq) {
        return middlewareSvc.verifyOtp(otpReq);
    }

    @PostMapping("/auth/registration/complete")
    public ApiResponse<?> setupPassword(@RequestBody @Valid SetupPasswordReq setupPasswordReq) {
        return middlewareSvc.setupPassword(setupPasswordReq);
    }

    @PostMapping("/auth/login")
    public ApiResponse<?> login(@RequestBody @Valid LoginReq loginReq) {
        return middlewareSvc.login(loginReq);
    }

    @PostMapping("/onboarding/setup-pin")
    public ApiResponse<?> setupPin(@RequestBody @Valid SetupPinReq setupPinReq, @RequestHeader("sessionId") String sessionId) {
        ApiResponse<SessionData> sessionResp = getSession(sessionId);
        if (!sessionResp.isSuccessful()) {
            return sessionResp;
        }
        SessionData session = sessionResp.getData();
        setupPinReq.setUserId(session.getUserId());
        return middlewareSvc.setupPin(setupPinReq);
    }

    @GetMapping("/account/details")
    public ApiResponse<?> getAccountDetails(@RequestHeader("sessionId") String sessionId) {
        ApiResponse<SessionData> sessionResp = getSession(sessionId);
        if (!sessionResp.isSuccessful()) {
            return sessionResp;
        }
        SessionData session = sessionResp.getData();
        return middlewareSvc.getAccountDetails(session.getUserId());
    }

    @PostMapping("/transfers/intrabank/lookup")
    public ApiResponse<?> intraBankLookup(@RequestHeader("sessionId") String sessionId, @RequestBody @Valid BankIntraBankLookupRequest req) {
        ApiResponse<SessionData> sessionResp = getSession(sessionId);
        if (!sessionResp.isSuccessful()) {
            return sessionResp;
        }
        return middlewareSvc.intraBankLookup(req.getAccountNumber());
    }

    @PostMapping("/transfers/intrabank/transfer")
    public ApiResponse<?> intraBankTransfer(@RequestHeader("sessionId") String sessionId, @RequestBody @Valid IntraBankTransferReq req) {
        ApiResponse<SessionData> sessionResp = getSession(sessionId);
        if (!sessionResp.isSuccessful()) {
            return sessionResp;
        }
        SessionData session = sessionResp.getData();
        req.setUserId(session.getUserId());
        return middlewareSvc.intraBankTransfer(req);
    }

    @PostMapping("/transfers/interbank/lookup")
    public ApiResponse<?> interBankLookup(@RequestHeader("sessionId") String sessionId, @RequestBody @Valid BankInterBankLookupRequest req) {
        ApiResponse<SessionData> sessionResp = getSession(sessionId);
        if (!sessionResp.isSuccessful()) {
            return sessionResp;
        }
        return middlewareSvc.interBankLookup(req);
    }

    @PostMapping("/transfers/interbank/transfer")
    public ApiResponse<?> interBankTransfer(@RequestHeader("sessionId") String sessionId, @RequestBody @Valid InterBankTransferReq req) {
        ApiResponse<SessionData> sessionResp = getSession(sessionId);
        if (!sessionResp.isSuccessful()) {
            return sessionResp;
        }
        SessionData session = sessionResp.getData();
        req.setUserId(session.getUserId());
        return middlewareSvc.interBankTransfer(req);
    }

    @GetMapping("/banks")
    public ApiResponse<?> getBanks() {
        return middlewareSvc.getBanks();
    }

    @GetMapping("/transactions")
    public ApiResponse<?> getTransactionHistory(@RequestHeader("sessionId") String sessionId) {
        ApiResponse<SessionData> sessionResp = getSession(sessionId);
        if (!sessionResp.isSuccessful()) {
            return sessionResp;
        }
        SessionData session = sessionResp.getData();
        return middlewareSvc.getTransactionHistory(session.getUserId());
    }

    @GetMapping("/bills/list")
    public ApiResponse<?> getBills() {
        return middlewareSvc.getBills();
    }

    @PostMapping("/bills/validate")
    public ApiResponse<?> validateBill(@RequestHeader("sessionId") String sessionId, @RequestBody @Valid ValidateBill req) {
        return middlewareSvc.validateBill(req);
    }

    @PostMapping("/bills/pay")
    public ApiResponse<?> payBills(@RequestHeader("sessionId") String sessionId,
                                   @RequestBody @Valid PayBillReq req) {
        ApiResponse<SessionData> sessionResp = getSession(sessionId);
        if (!sessionResp.isSuccessful()) return sessionResp;
        SessionData session = sessionResp.getData();
        req.setUserId(session.getUserId());
        req.setChannel(Channel.MOBILE);
        return middlewareSvc.payBills(req);
    }

    // crude session implementation
// extract session from the header field: sessionId;
    private ApiResponse<SessionData> getSession(String sessionId) {
        Optional<SessionData> savedSession = middlewareSvc.getSession(sessionId);
        return savedSession.map(ApiResponse::Success)
                .orElseGet(() -> ApiResponse.Error("invalid or expired session"));
    }

}
