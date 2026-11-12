package com.vahotel.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name="reservations")
public class Reservation {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false) private Account customer;
    @ManyToOne(optional=false) private Room room;
    @Column(nullable=false) private LocalDate checkIn;
    @Column(nullable=false) private LocalDate checkOut;
    @Column(nullable=false) private String bookingType; // REGULAR or PENCIL
    @Column(nullable=false) private String status; // PENDING, PAID, APPROVED, REJECTED, CANCELLED, COMPLETED, EXPIRED
    private String statusBeforeCancellation;
    @Column(nullable=false, precision=12, scale=2) private BigDecimal totalPrice;
    @Column(nullable=false, precision=12, scale=2) private BigDecimal requiredDownPayment;
    @Column(nullable=false, precision=12, scale=2) private BigDecimal amountPaid;
    protected Reservation() {}
    public Reservation(Account customer,Room room,LocalDate in,LocalDate out,String type,BigDecimal total){
        this.customer=customer;this.room=room;checkIn=in;checkOut=out;bookingType=type;totalPrice=total;
        requiredDownPayment="PENCIL".equals(type)?total.multiply(new BigDecimal("0.50")):total;
        amountPaid=BigDecimal.ZERO;status="PENDING";
    }
    public Long getId(){return id;} public Account getCustomer(){return customer;} public Room getRoom(){return room;} public LocalDate getCheckIn(){return checkIn;}
    public LocalDate getCheckOut(){return checkOut;} public String getStatus(){return status;}
    public BigDecimal getTotalPrice(){return totalPrice;} public BigDecimal getRequiredDownPayment(){return requiredDownPayment;}
    public BigDecimal getAmountPaid(){return amountPaid;} public String getBookingType(){return bookingType;}
    public void setStatus(String s){status=s;} public String getStatusBeforeCancellation(){return statusBeforeCancellation;} public void setStatusBeforeCancellation(String s){statusBeforeCancellation=s;} public void addPayment(BigDecimal a){amountPaid=amountPaid.add(a);}
}
