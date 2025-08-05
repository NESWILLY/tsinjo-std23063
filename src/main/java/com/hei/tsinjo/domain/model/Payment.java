package com.hei.tsinjo.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Payment {
    private String pspPaymentId;
    private String pdpType;
    private Integer amount;
    private Instant creationDate;
    private String verificationStatus;
}
