package com.vahotel.service;

import com.vahotel.model.PasswordResetToken;
import com.vahotel.model.Role;
import com.vahotel.repo.AccountRepository;
import com.vahotel.repo.PasswordResetTokenRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordResetService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final AccountRepository accounts;
    private final PasswordResetTokenRepository tokens;
    private final PasswordEncoder passwordEncoder;
    private final EmailNotificationService email;

    public PasswordResetService(AccountRepository accounts, PasswordResetTokenRepository tokens,
                                PasswordEncoder passwordEncoder, EmailNotificationService email) {
        this.accounts = accounts;
        this.tokens = tokens;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
    }

    @Transactional
    public void requestReset(String emailAddress, String resetUrl) {
        String normalizedEmail = emailAddress == null ? "" : emailAddress.trim().toLowerCase();
        var account = accounts.findByEmail(normalizedEmail).orElse(null);
        if (account == null || account.getRole() != Role.CUSTOMER || !account.isEnabled()
                || !account.isEmailVerified() || !email.isConfigured()) return;

        tokens.deleteByEmail(normalizedEmail);
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        tokens.save(new PasswordResetToken(hash(token), normalizedEmail, LocalDateTime.now().plusMinutes(30)));
        if (!email.sendPasswordReset(account.getEmail(), resetUrl + "?token=" + token)) {
            tokens.deleteByEmail(normalizedEmail);
        }
    }

    @Transactional
    public boolean resetPassword(String rawToken, String newPassword) {
        if (rawToken == null || rawToken.isBlank() || newPassword == null
                || !newPassword.matches("(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{10,}")
                || newPassword.chars().anyMatch(Character::isWhitespace)) return false;
        var token = tokens.findByTokenHash(hash(rawToken)).orElse(null);
        if (token == null || token.isUsed() || token.getExpiresAt().isBefore(LocalDateTime.now())) return false;
        var account = accounts.findByEmail(token.getEmail()).orElse(null);
        if (account == null || account.getRole() != Role.CUSTOMER || !account.isEnabled()) return false;
        account.setPasswordHash(passwordEncoder.encode(newPassword));
        token.setUsed(true);
        return true;
    }

    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
