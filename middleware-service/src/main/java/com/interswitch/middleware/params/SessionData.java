package com.interswitch.middleware.params;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionData {
    private Channel channel;
    private String firstName;
    private String platformId;
    private Long userId;
}
