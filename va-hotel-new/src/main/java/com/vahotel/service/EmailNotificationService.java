package com.vahotel.service;

import com.vahotel.model.PaymentRecord;
import com.vahotel.model.Reservation;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {
    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    private final ObjectProvider<JavaMailSender> mailSenders;

    @Value("${spring.mail.host:}")
    private String smtpHost;

    @Value("${va.hotel.email.from:${spring.mail.username:}}")
    private String fromAddress;

    public EmailNotificationService(ObjectProvider<JavaMailSender> mailSenders) {
        this.mailSenders = mailSenders;
    }

    public boolean sendReservationConfirmation(Reservation reservation) {
        String subject = "VA Hotel reservation confirmation #" + reservation.getId();
        String body = reservationDetails(reservation)
                + "\nThis email confirms the reservation request. Management approval may still be required."
                + "\nPayments in this application are simulated; no money was transferred."
                + "\nKeep this email as your reservation proof.";
        return send(reservation.getCustomer().getEmail(), subject, body, "reservation " + reservation.getId());
    }

    public boolean sendReservationProof(Reservation reservation) {
        String body = reservationDetails(reservation)
                + "\nThis is your current reservation proof. Payments in this application are simulated.";
        return send(reservation.getCustomer().getEmail(),
                "VA Hotel reservation proof #" + reservation.getId(), body, "reservation " + reservation.getId());
    }

    public boolean sendPaymentProof(PaymentRecord payment) {
        Reservation reservation = payment.getReservation();
        String body = reservationDetails(reservation)
                + "\nPayment reference: " + payment.getReferenceCode()
                + "\nPayment amount: PHP " + payment.getAmount().toPlainString()
                + "\nPayment method: " + payment.getMethod()
                + "\nPayment status: " + payment.getStatus()
                + "\nRecorded: " + payment.getCreatedAt()
                + "\nThis is a simulated payment record. No money was transferred.";
        return send(reservation.getCustomer().getEmail(),
                "VA Hotel simulated payment receipt #" + reservation.getId(), body, "reservation " + reservation.getId());
    }

    public boolean sendOtp(String recipient, String code) {
        String body = "Your VA Hotel email verification code is: " + code
                + "\nThis code expires in 10 minutes.\nDo not share this code with anyone.";
        return send(recipient, "Verify your VA Hotel email", body, "email verification");
    }

    public boolean isConfigured() {
        return smtpHost != null && !smtpHost.isBlank()
                && fromAddress != null && !fromAddress.isBlank()
                && mailSenders.getIfAvailable() != null;
    }

    public String deliveryFailureMessage() {
        return isConfigured()
                ? "Email delivery failed. Check SMTP settings and the recipient address, then retry."
                : "Email was not sent because SMTP is not configured.";
    }

    private boolean send(String recipient, String subject, String body, String context) {
        JavaMailSender mailSender = mailSenders.getIfAvailable();
        if (smtpHost == null || smtpHost.isBlank() || fromAddress == null || fromAddress.isBlank()
                || mailSender == null) {
            log.info("Email for {} was not sent because SMTP is not configured.", context);
            return false;
        }
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(body, htmlEmail(subject, body));
            mailSender.send(message);
            log.info("Email sent for {}.", context);
            return true;
        } catch (MailException | MessagingException | IllegalStateException exception) {
            log.warn("Email for {} could not be sent: {}", context, exception.getMessage());
            return false;
        }
    }

    private String reservationDetails(Reservation reservation) {
        return "VA HOTEL — RESERVATION PROOF\n"
                + "Reservation ID: " + reservation.getId() + "\n"
                + "Guest: " + reservation.getCustomer().getFullName() + "\n"
                + "Room: " + reservation.getRoom().getRoomNumber() + " (" + reservation.getRoom().getRoomType() + ")\n"
                + "Check-in: " + reservation.getCheckIn() + "\n"
                + "Check-out: " + reservation.getCheckOut() + "\n"
                + "Booking type: " + reservation.getBookingType() + "\n"
                + "Reservation status: " + reservation.getStatus() + "\n"
                + "Total: PHP " + reservation.getTotalPrice().toPlainString() + "\n"
                + "Amount paid: PHP " + reservation.getAmountPaid().toPlainString() + "\n"
                + "Balance: PHP " + reservation.getTotalPrice().subtract(reservation.getAmountPaid()).toPlainString();
    }

    private String htmlEmail(String subject, String body) {
        StringBuilder rows = new StringBuilder();
        for (String line : body.split("\\R")) {
            if (line.isBlank()) continue;
            int separator = line.indexOf(':');
            if (separator > 0) {
                rows.append("<tr><th style=\"padding:10px 12px;text-align:left;color:#607086;border-bottom:1px solid #e7edf5;\">")
                        .append(escape(line.substring(0, separator)))
                        .append("</th><td style=\"padding:10px 12px;text-align:right;color:#18345c;border-bottom:1px solid #e7edf5;font-weight:600;\">")
                        .append(escape(line.substring(separator + 1).trim())).append("</td></tr>");
            } else {
                rows.append("<tr><td colspan=\"2\" style=\"padding:10px 12px;color:#53647a;line-height:1.6;\">")
                        .append(escape(line)).append("</td></tr>");
            }
        }
        return "<!doctype html><html><body style=\"margin:0;background:#f2f5fa;font-family:Arial,sans-serif;color:#18345c;\">"
                + "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"padding:32px 12px;background:#f2f5fa;\"><tr><td align=\"center\">"
                + "<table role=\"presentation\" width=\"600\" cellspacing=\"0\" cellpadding=\"0\" style=\"max-width:600px;width:100%;background:#fff;border:1px solid #dce4f0;border-radius:16px;overflow:hidden;\">"
                + "<tr><td style=\"padding:24px 30px;background:#204d9b;color:#fff;border-bottom:5px solid #f58213;letter-spacing:2px;font-weight:bold;\">VA HOTEL <span style=\"float:right;color:#ffd3ad;font-size:12px;letter-spacing:1px;\">VISION · HOSPITALITY · SERVICE</span></td></tr>"
                + "<tr><td style=\"padding:26px 30px 8px;\"><h1 style=\"margin:0;font-size:22px;line-height:1.3;color:#18345c;\">" + escape(subject) + "</h1></td></tr>"
                + "<tr><td style=\"padding:12px 30px 28px;\"><table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">" + rows + "</table></td></tr>"
                + "<tr><td style=\"padding:18px 30px;background:#f7f9fd;color:#718098;font-size:12px;line-height:1.6;\">This message was sent by VA Hotel. Keep it as your reservation or payment proof. Never share payment passwords or verification codes.</td></tr>"
                + "</table></td></tr></table></body></html>";
    }

    private String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
