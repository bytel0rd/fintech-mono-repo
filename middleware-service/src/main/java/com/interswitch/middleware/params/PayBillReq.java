package com.interswitch.middleware.params;

import com.interswitch.middleware.integrations.bills.params.ValidateBill;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayBillReq extends ValidateBill {
    @NotBlank
    private String pin;
    @NotNull
    private Channel channel;
    private Long userId;
    private String transactionRef;

}
