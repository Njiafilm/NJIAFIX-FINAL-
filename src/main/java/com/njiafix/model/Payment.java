package com.njiafix.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "company_number", nullable = false, length = 30)
    private String companyNumber;
    @Column(name = "payment_ref", nullable = false, length = 100)
    private String paymentRef;
    @Column(name = "customer_phone", length = 20)
    private String customerPhone;
    @Column(nullable = false, length = 20)
    private String status = "confirmed";
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Payment() {}
    public Payment(String companyNumber, String paymentRef, String customerPhone) {
        this.companyNumber = companyNumber;
        this.paymentRef = paymentRef;
        this.customerPhone = customerPhone;
        this.status = "confirmed";
        this.createdAt = LocalDateTime.now();
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCompanyNumber() { return companyNumber; }
    public void setCompanyNumber(String companyNumber) { this.companyNumber = companyNumber; }
    public String getPaymentRef() { return paymentRef; }
    public void setPaymentRef(String paymentRef) { this.paymentRef = paymentRef; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
