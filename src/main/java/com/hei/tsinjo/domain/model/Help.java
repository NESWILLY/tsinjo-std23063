package com.hei.tsinjo.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Help {
    private Beneficiary beneficiary;
    private Payment payment;
    private String description;
}
