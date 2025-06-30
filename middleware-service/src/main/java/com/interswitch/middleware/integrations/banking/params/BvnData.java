package com.interswitch.middleware.integrations.banking.params;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BvnData {
    private String bvn;
    private String dob;
    private String firstName;
    private String lastName;
    private String middleName;
    private String gender;
    private String phoneNumber;
}
