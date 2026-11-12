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

    // Backward-compatible method for any older code that still calls this name.
    @Transactional
    public PaymentRecord simulateFullPayment(Long reservationId, BigDecimal amount, String method) {
        return simulatePayment(reservationId, amount, method);
    }
}
