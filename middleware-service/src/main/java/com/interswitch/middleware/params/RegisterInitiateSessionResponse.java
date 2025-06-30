package com.interswitch.middleware.params;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterInitiateSessionResponse {
    private String sessionId;
}
