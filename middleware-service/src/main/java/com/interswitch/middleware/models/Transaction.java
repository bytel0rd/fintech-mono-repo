package com.interswitch.middleware.models;

import com.interswitch.middleware.params.Channel;
import com.interswitch.middleware.params.TransactionStatus;
import com.interswitch.middleware.params.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(unique = true, nullable = false)
    private String transactionRef;

    @Column(nullable = false)
    private String sourceAccountNumber;

    @Column(nullable = false)
    private String destinationAccountNumber;

    @Column(nullable = false)
    private String destinationAccountName;

    @Column()
    private String destinationBankCode;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private BigDecimal charge;

    @Column(nullable = false)
    private TransactionType transactionType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Column()
    private String message;

    @Column(nullable = false)
    private Date createdAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Channel channel;

    @Column()
    private String billId;

    @Column()
    private String beneficiary;

    @Column()
    private String billReference;

    @Column()
    private String narration;

}
