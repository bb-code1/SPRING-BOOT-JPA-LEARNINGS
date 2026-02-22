package com.zbs.learning.domain;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
@Entity
@DiscriminatorValue("CREDIT_CARD")
@Getter
@Setter
public class CreditCardPayment extends Payment {
    private String cardNumber;
    private String cardHolder;
}