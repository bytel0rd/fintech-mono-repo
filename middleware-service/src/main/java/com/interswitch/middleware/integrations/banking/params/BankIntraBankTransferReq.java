package com.interswitch.middleware.integrations.banking.params;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class BankIntraBankTransferReq {

    @NotBlank
    private String destinationAccountName;

    @NotBlank
    private String destinationAccountNumber;

    @NotBlank
    private String sourceAccountNumber;

    @NotBlank
    private String transactionRef;

    @NotNull
    private BigDecimal amount;

    private String narration = "";
}
