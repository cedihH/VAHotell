package com.vahotel.service;

import com.vahotel.model.PaymentRecord;
import com.vahotel.model.Reservation;
import com.vahotel.model.Account;
import com.vahotel.model.CustomerReport;
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

    public boolean sendPasswordReset(String recipient, String resetUrl) {
        String body = "We received a request to reset your VA Hotel password.\n"
                + "Use this secure link within 30 minutes to choose a new password:\n" + resetUrl
                + "\nIf you did not request this, you can ignore this email.";
        return send(recipient, "Reset your VA Hotel password", body, "password reset");
    }

    public boolean sendRegistrationSubmitted(Account account) {
        String body = "Hello " + account.getFullName() + ",\n"
                + "Your VA Hotel registration is complete. We received your verified email and identity document.\n"
                + "Your account is now waiting for hotel management review. We will email you when a decision is made.";
        return send(account.getEmail(), "VA Hotel registration received", body, "registration review");
    }

    public boolean sendRegistrationApproved(Account account) {
        String body = "Hello " + account.getFullName() + ",\n"
                + "Your VA Hotel customer account has been approved. You can now sign in and request a room.";
        return send(account.getEmail(), "Your VA Hotel account is approved", body, "registration approval");
    }

    public boolean sendRegistrationRejected(String name, String recipient) {
        String body = "Hello " + name + ",\n"
                + "Hotel management could not approve your VA Hotel registration. Please contact the hotel if you need help.";
        return send(recipient, "Update on your VA Hotel registration", body, "registration decision");
    }

    public boolean sendReservationUpdate(Reservation reservation, String update) {
        String body = reservationDetails(reservation)
                + "\nUpdate: " + update
                + "\nCurrent reservation status: " + reservation.getStatus()
                + "\nThis message was sent because a reservation decision or stay update was recorded.";
        return send(reservation.getCustomer().getEmail(),
                "VA Hotel reservation update #" + reservation.getId(), body, "reservation " + reservation.getId());
    }

    public boolean sendRefundProof(PaymentRecord payment) {
        Reservation reservation = payment.getReservation();
        String body = reservationDetails(reservation)
                + "\nRefund reference: " + payment.getRefundReferenceCode()
                + "\nRefund amount: PHP " + payment.getRefundAmount().toPlainString()
                + "\nRefund status: " + payment.getStatus()
                + "\nRecorded: " + payment.getRefundedAt()
                + "\nThis is a simulated refund record. No money was transferred.";
        return send(reservation.getCustomer().getEmail(),
                "VA Hotel simulated refund update #" + reservation.getId(), body, "reservation " + reservation.getId());
    }

    public boolean sendCustomerReportReply(CustomerReport report) {
        String body = "Hello " + report.getCustomer().getFullName() + ",\n"
                + "Management replied to your message about: " + report.getSubject() + "\n"
                + report.getManagerReply() + "\n"
                + "Your message is now marked as resolved. Reply to hotel management if you need further help.";
        return send(report.getCustomer().getEmail(), "VA Hotel replied to your message", body,
                "customer message " + report.getId());
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
