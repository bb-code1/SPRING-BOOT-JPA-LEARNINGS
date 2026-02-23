package com.zbs.booking.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("PAYPAL")
@Getter
@Setter
public class PaypalPayment extends Payment {
    private String email;
}