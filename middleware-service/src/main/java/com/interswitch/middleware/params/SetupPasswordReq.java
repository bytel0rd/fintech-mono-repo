package com.interswitch.middleware.params;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SetupPasswordReq {
    @NotBlank
    private String sessionId;

    @NotBlank
    private String password;
}
