package com.interswitch.middleware.params;

import com.interswitch.middleware.integrations.banking.params.BankInterBankTransferReq;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IntraBankTransferReq extends BankInterBankTransferReq {
    @NotBlank
    private String pin;

    private Channel channel;
    private Long userId;
}
