package com.vahotel.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="payment_records")
public class PaymentRecord {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false) private Reservation reservation;
    @Column(nullable=false, precision=12, scale=2) private BigDecimal amount;
    @Column(nullable=false) private String method;
    @Column(nullable=false) private String status;
    @Column(nullable=false, unique=true) private String referenceCode;
    private LocalDateTime createdAt=LocalDateTime.now();
    @Column(precision=12, scale=2) private BigDecimal refundAmount=BigDecimal.ZERO;
    private String refundReferenceCode;
    private LocalDateTime refundedAt;
    protected PaymentRecord(){}
    public PaymentRecord(Reservation r,BigDecimal a,String m,String ref){reservation=r;amount=a;method=m;referenceCode=ref;status="SIMULATED_PAID";}
    public Long getId(){return id;} public Reservation getReservation(){return reservation;} public BigDecimal getAmount(){return amount;} public String getMethod(){return method;} public String getStatus(){return status;} public String getReferenceCode(){return referenceCode;} public LocalDateTime getCreatedAt(){return createdAt;} public BigDecimal getRefundAmount(){return refundAmount;} public String getRefundReferenceCode(){return refundReferenceCode;} public LocalDateTime getRefundedAt(){return refundedAt;} public void recordRefund(BigDecimal amount,String ref){this.refundAmount=amount;this.refundReferenceCode=ref;this.refundedAt=LocalDateTime.now();this.status=amount.compareTo(this.amount)==0?"SIMULATED_REFUNDED":"SIMULATED_PARTIALLY_REFUNDED";}
}
