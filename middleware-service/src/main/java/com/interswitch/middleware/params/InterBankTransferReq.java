package com.interswitch.middleware.params;

import com.interswitch.middleware.integrations.banking.params.BankInterBankTransferReq;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InterBankTransferReq extends BankInterBankTransferReq {
    @NotBlank
    private String pin;
    @NotNull
    private Channel channel;
    private Long userId;
}
