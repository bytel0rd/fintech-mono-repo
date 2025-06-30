package com.interswitch.middleware.params;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SetupPinReq {
    @NotBlank
    private String pin;

    private Long userId;
}
