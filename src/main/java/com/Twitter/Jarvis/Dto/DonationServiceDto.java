package com.Twitter.Jarvis.Dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Data
public class DonationServiceDto {
    private String fullName;
    private String email;
    private String country;
    private String state;
    private String amount;
    private String paymentId;
}
