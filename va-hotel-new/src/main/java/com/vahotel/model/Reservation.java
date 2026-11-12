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
    @Column(nullable=false) private String bookingType;
    @Column(nullable=false) private String status;
    private String statusBeforeCancellation;
    @Column(nullable=false, precision=12, scale=2) private BigDecimal totalPrice;
    @Column(nullable=false, precision=12, scale=2) private BigDecimal requiredDownPayment;
    @Column(nullable=false, precision=12, scale=2) private BigDecimal amountPaid;
    private boolean earlyCheckoutRequested=false;
    private LocalDate requestedCheckOut;
    private Long requestedUpgradeRoomId;
    private LocalDate checkedOutAt;

    protected Reservation() {}

    public Reservation(Account customer, Room room, LocalDate in, LocalDate out, String type, BigDecimal total) {
        this.customer = customer;
        this.room = room;
        this.checkIn = in;
        this.checkOut = out;
        this.bookingType = type;
        this.totalPrice = total;
        this.requiredDownPayment = total.multiply(new BigDecimal("0.50")).setScale(2, java.math.RoundingMode.HALF_UP);
        this.amountPaid = BigDecimal.ZERO;
        this.status = "PENDING";
    }

    public Long getId() { return id; }
    public Account getCustomer() { return customer; }
    public Room getRoom() { return room; }
    public LocalDate getCheckIn() { return checkIn; }
    public LocalDate getCheckOut() { return checkOut; }
    public String getStatus() { return status; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public BigDecimal getRequiredDownPayment() { return requiredDownPayment; }
    public BigDecimal getAmountPaid() { return amountPaid; }
    public String getBookingType() { return bookingType; }
    public void setStatus(String status) { this.status = status; }
    public void setCheckOut(LocalDate value){this.checkOut=value;}
    public void setTotalPrice(BigDecimal value){this.totalPrice=value;}
    public void setRoom(Room value){this.room=value;}
    public String getStatusBeforeCancellation() { return statusBeforeCancellation; }
    public void setStatusBeforeCancellation(String status) { this.statusBeforeCancellation = status; }
    public void addPayment(BigDecimal amount) { this.amountPaid = this.amountPaid.add(amount); }
    public void setAmountPaid(BigDecimal amount) { this.amountPaid = amount; }
    public boolean isEarlyCheckoutRequested(){return earlyCheckoutRequested;}
    public void setEarlyCheckoutRequested(boolean value){this.earlyCheckoutRequested=value;}
    public LocalDate getRequestedCheckOut(){return requestedCheckOut;}
    public void setRequestedCheckOut(LocalDate value){this.requestedCheckOut=value;}
    public Long getRequestedUpgradeRoomId(){return requestedUpgradeRoomId;}
    public void setRequestedUpgradeRoomId(Long value){this.requestedUpgradeRoomId=value;}
    public LocalDate getCheckedOutAt(){return checkedOutAt;}
    public void setCheckedOutAt(LocalDate value){this.checkedOutAt=value;}
}
