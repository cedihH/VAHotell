package com.vahotel.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vahotel.model.Account;
import com.vahotel.model.Reservation;
import com.vahotel.model.Role;
import com.vahotel.model.Room;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.Multipart;
import java.util.Properties;

class EmailNotificationServiceTest {
    @Test
    void sendsReservationIdAndStayDetailsToCustomerAddress() throws Exception {
        @SuppressWarnings("unchecked")
        ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
        JavaMailSender sender = mock(JavaMailSender.class);
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(provider.getIfAvailable()).thenReturn(sender);
        when(sender.createMimeMessage()).thenReturn(mimeMessage);
        EmailNotificationService service = new EmailNotificationService(provider);
        ReflectionTestUtils.setField(service, "smtpHost", "smtp.test");
        ReflectionTestUtils.setField(service, "fromAddress", "hotel@test.example");

        boolean delivered = service.sendReservationConfirmation(sampleReservation());

        assertTrue(delivered);
        assertEquals("guest@example.com", mimeMessage.getAllRecipients()[0].toString());
        assertEquals("VA Hotel reservation confirmation #42", mimeMessage.getSubject());
        String content = flatten(mimeMessage.getContent());
        assertTrue(content.contains("Reservation ID"));
        assertTrue(content.contains("#f58213"));
        verify(sender).send(mimeMessage);
    }

    @Test
    void reportsNotSentWhenSmtpIsNotConfigured() {
        @SuppressWarnings("unchecked")
        ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
        EmailNotificationService service = new EmailNotificationService(provider);

        assertFalse(service.sendReservationConfirmation(sampleReservation()));
        assertTrue(service.deliveryFailureMessage().contains("SMTP is not configured"));
    }

    private Reservation sampleReservation() {
        Account guest = new Account("guest@example.com", "hash", "Demo Guest", Role.CUSTOMER, true);
        Room room = new Room("101", "Standard", "Test room", new BigDecimal("2500"));
        Reservation reservation = new Reservation(guest, room, LocalDate.of(2031, 2, 10),
                LocalDate.of(2031, 2, 11), "REGULAR", new BigDecimal("2500"));
        ReflectionTestUtils.setField(reservation, "id", 42L);
        return reservation;
    }

    private String flatten(Object content) throws Exception {
        if (!(content instanceof Multipart multipart)) return String.valueOf(content);
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < multipart.getCount(); i++) text.append(flatten(multipart.getBodyPart(i).getContent()));
        return text.toString();
    }
}
