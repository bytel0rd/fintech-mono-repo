package com.interswitch.middleware.params;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginReq {
    @NotBlank
    private String phoneNumber;

    @NotBlank
    private String password;

    private Channel channel = Channel.MOBILE;
}
