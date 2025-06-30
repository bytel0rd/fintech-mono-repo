package com.interswitch.middleware.integrations.banking.params;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CreateAccountReq {
    private String bvn;
    private String dob;
    private String firstName;
    private String lastName;
    private String middleName;
    private String gender;
    private String phoneNumber;
}
