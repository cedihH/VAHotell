package com.vahotel.web;

import com.vahotel.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PasswordResetController {
    private final PasswordResetService resetService;
    @Value("${va.hotel.base-url:}")
    private String configuredBaseUrl;

    public PasswordResetController(PasswordResetService resetService) {
        this.resetService = resetService;
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() { return "forgot-password"; }

    @PostMapping("/forgot-password")
    public String requestReset(@RequestParam String email, HttpServletRequest request, RedirectAttributes redirect) {
        String baseUrl = configuredBaseUrl == null || configuredBaseUrl.isBlank()
                ? request.getRequestURL().toString().replace(request.getRequestURI(), request.getContextPath())
                : configuredBaseUrl.replaceAll("/+$", "");
        resetService.requestReset(email, baseUrl + "/reset-password");
        redirect.addFlashAttribute("submitted", true);
        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordForm(@RequestParam(required = false) String token, Model model) {
        model.addAttribute("token", token);
        model.addAttribute("validToken", token != null && !token.isBlank());
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token, @RequestParam String password,
                                @RequestParam String confirmPassword, RedirectAttributes redirect) {
        if (!password.equals(confirmPassword)) {
            redirect.addFlashAttribute("resetError", "The passwords do not match.");
            redirect.addAttribute("token", token);
            return "redirect:/reset-password";
        }
        if (!password.matches("(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{10,}")
                || password.chars().anyMatch(Character::isWhitespace)) {
            redirect.addFlashAttribute("resetError", "Use at least 10 characters with uppercase, lowercase, a number, and a special character, without spaces.");
            redirect.addAttribute("token", token);
            return "redirect:/reset-password";
        }
        if (!resetService.resetPassword(token, password)) {
            redirect.addFlashAttribute("resetError", "This reset link is invalid or expired. Request a new one.");
            return "redirect:/forgot-password";
        }
        redirect.addFlashAttribute("passwordReset", true);
        return "redirect:/login?customer=true";
    }
}
