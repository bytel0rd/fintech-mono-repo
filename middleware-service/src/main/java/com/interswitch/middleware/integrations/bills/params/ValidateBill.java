package com.interswitch.middleware.integrations.bills.params;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ValidateBill {
    @NotBlank
    private String beneficiary;
    @NotBlank
    private String code;
}
