package com.interswitch.middleware.params;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterInitiateReq {
    @NotBlank
    private String bvn;

    @NotBlank
    private String dob;
}
