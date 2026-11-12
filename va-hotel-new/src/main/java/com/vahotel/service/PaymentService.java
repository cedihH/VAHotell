package com.vahotel.service;

import com.vahotel.model.PaymentRecord;
import com.vahotel.model.Reservation;
import com.vahotel.repo.PaymentRecordRepository;
import com.vahotel.repo.ReservationRepository;
import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {
    private final PaymentRecordRepository payments;
    private final ReservationRepository reservations;

    public PaymentService(PaymentRecordRepository payments, ReservationRepository reservations) {
        this.payments = payments;
        this.reservations = reservations;
    }

    // Local simulation only. No real card or GCash account is charged.
    @Transactional
    public PaymentRecord simulatePayment(Long reservationId, BigDecimal amount, String method) {
        Reservation reservation = reservations.findById(reservationId).orElseThrow();
        if (!"PENDING".equals(reservation.getStatus())) {
            throw new IllegalArgumentException("Only pending reservations can be paid.");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Payment must be greater than zero.");
        }
        if (method == null || !Set.of("SIMULATED_VISA", "SIMULATED_GCASH").contains(method)) {
            throw new IllegalArgumentException("Choose Visa or GCash simulation.");
        }

        BigDecimal expected = reservation.getAmountPaid().signum() == 0
                ? reservation.getRequiredDownPayment()
                : reservation.getTotalPrice().subtract(reservation.getAmountPaid());

        if (amount.compareTo(expected) != 0) {
            throw new IllegalArgumentException("Payment amount must be exactly ₱" + expected + ".");
        }

        String reference = "VA-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        PaymentRecord record = payments.save(new PaymentRecord(reservation, amount, method, reference));
        reservation.addPayment(amount);
        if (reservation.getAmountPaid().compareTo(reservation.getTotalPrice()) >= 0) {
            reservation.setStatus("PAID");
        }
        reservations.save(reservation);
        return record;
    }


    @Transactional
    public PaymentRecord simulateWalkInPayment(Long reservationId, BigDecimal amount, String method) {
        Reservation reservation = reservations.findById(reservationId).orElseThrow();
        if (!"PENDING".equals(reservation.getStatus())) throw new IllegalArgumentException("Only pending walk-in reservations can be paid.");
        if (amount == null || amount.compareTo(reservation.getTotalPrice()) != 0) throw new IllegalArgumentException("Walk-in payment must equal the full reservation total.");
        if (method == null || !Set.of("ONSITE_SIMULATED_CASH", "ONSITE_SIMULATED_CARD").contains(method)) throw new IllegalArgumentException("Choose simulated cash or card.");
        String reference = "VA-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        PaymentRecord record = payments.save(new PaymentRecord(reservation, amount, method, reference));
        reservation.addPayment(amount);
        reservation.setStatus("PAID");
        reservations.save(reservation);
        return record;
    }


    @Transactional
    public PaymentRecord simulateRefund(Long paymentId, BigDecimal amount) {
        PaymentRecord record = payments.findById(paymentId).orElseThrow();
        if (amount == null || amount.signum() <= 0 || amount.compareTo(record.getAmount()) > 0) {
            throw new IllegalArgumentException("Refund must be positive and cannot exceed the original payment.");
        }
        if (record.getRefundAmount() != null && record.getRefundAmount().signum() > 0) {
            throw new IllegalArgumentException("This payment already has a refund recorded.");
        }
        String reference = "RF-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        record.recordRefund(amount, reference);
        Reservation reservation = record.getReservation();
        reservation.setAmountPaid(reservation.getAmountPaid().subtract(amount).max(BigDecimal.ZERO));
        if (reservation.getAmountPaid().signum() == 0 && "PAID".equals(reservation.getStatus())) {
            reservation.setStatus("CANCELLED");
        }
        reservations.save(reservation);
        return payments.save(record);
    }

    // Backward-compatible method for any older code that still calls this name.
    @Transactional
    public PaymentRecord simulateFullPayment(Long reservationId, BigDecimal amount, String method) {
        return simulatePayment(reservationId, amount, method);
    }
}
