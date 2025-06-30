package com.interswitch.middleware.params;


import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {
    private String transactionRef;
    private String sourceAccountNumber;
    private String beneficiary;
    private String destinationAccountName;
    private String destinationBankCode;
    private BigDecimal amount;
    private BigDecimal charge;
    private TransactionStatus status;
    private String message;
    private Date createdAt;
    private Channel channel;
    private String billCode;
    private String billReference;
    private String narration;
    private TransactionType transactionType;
}
