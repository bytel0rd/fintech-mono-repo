package com.interswitch.middleware.params;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RegisterInitiateSessionResponse {
    private String sessionId;
}
