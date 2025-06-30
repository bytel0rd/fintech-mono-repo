package com.interswitch.middleware.params;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyOtpReq {
    @NotBlank
    private String sessionId;

    @NotBlank
    private String otpCode;
}
