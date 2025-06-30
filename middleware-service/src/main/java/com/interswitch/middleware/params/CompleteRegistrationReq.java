package com.interswitch.middleware.params;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompleteRegistrationReq {
    private String accountNumber;
    private String accountName;
}
