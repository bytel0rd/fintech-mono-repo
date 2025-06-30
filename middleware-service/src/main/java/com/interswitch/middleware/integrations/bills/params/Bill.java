package com.interswitch.middleware.integrations.bills.params;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class Bill {
    private String name;
    private String code;
    private BigDecimal amount;
}
