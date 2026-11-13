package com.vahotel.web;

import com.vahotel.repo.RoomRepository;
import com.vahotel.repo.ReservationRepository;
import com.vahotel.model.Room;
import com.vahotel.model.Role;
import com.vahotel.model.Reservation;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PageController {
    private final RoomRepository rooms;
    private final ReservationRepository reservations;

    public PageController(RoomRepository rooms, ReservationRepository reservations) {
        this.rooms = rooms;
        this.reservations = reservations;
    }

    // The portal is the default page. It must render independently of room data.
    @GetMapping("/")
    public String home(Authentication authentication, Model model, CsrfToken csrfToken) {
        csrfToken.getToken();
        model.addAttribute("showChatWidget", !isManager(authentication));
        return "index";
    }

    @GetMapping("/rooms")
    public String rooms(@RequestParam(required = false) LocalDate checkIn,
                        @RequestParam(required = false) LocalDate checkOut,
                        @RequestParam(required = false) Long roomId,
                        Authentication authentication, Model model, CsrfToken csrfToken) {
        csrfToken.getToken();
        var activeRooms = rooms.findByActiveTrue();
        model.addAttribute("rooms", activeRooms);
        model.addAttribute("checkIn", checkIn);
        model.addAttribute("checkOut", checkOut);
        model.addAttribute("selectedRoomId", roomId);
        boolean datesSubmitted = checkIn != null || checkOut != null;
        boolean customerSignedIn = authentication != null && authentication.isAuthenticated()
                && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + Role.CUSTOMER.name()));
        boolean datesValid = customerSignedIn && checkIn != null && checkOut != null
                && checkIn.isAfter(LocalDate.now(java.time.ZoneId.of("Asia/Manila"))) && checkOut.isAfter(checkIn);
        model.addAttribute("datesSubmitted", datesSubmitted);
        model.addAttribute("datesValid", datesValid);
        model.addAttribute("availability", availability(activeRooms, checkIn, checkOut, datesValid));
        model.addAttribute("reservedDates", reservedDates(activeRooms));
        model.addAttribute("availabilityError", customerSignedIn && datesSubmitted && !datesValid ? "Choose a future check-in date and a later check-out date." : null);
        model.addAttribute("customerSignedIn", customerSignedIn);
        model.addAttribute("showChatWidget", !isManager(authentication));
        return "rooms";
    }

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/access-denied")
    public String accessDenied(Authentication authentication, Model model) {
        String destination = "/";
        if (authentication != null) {
            var roles = authentication.getAuthorities().stream().map(org.springframework.security.core.GrantedAuthority::getAuthority).collect(java.util.stream.Collectors.toSet());
            if (roles.contains("ROLE_SUPER_ADMIN")) destination = "/super-admin/dashboard";
            else if (roles.contains("ROLE_ONLINE_MANAGER")) destination = "/online-manager/dashboard";
            else if (roles.contains("ROLE_ONSITE_MANAGER")) destination = "/onsite-manager/dashboard";
            else if (roles.contains("ROLE_CUSTOMER")) destination = "/customer/dashboard";
        }
        model.addAttribute("dashboardUrl", destination);
        return "access-denied";
    }

    private Map<Long, Boolean> availability(java.util.List<Room> activeRooms, LocalDate in, LocalDate out, boolean datesValid) {
        Map<Long, Boolean> result = new LinkedHashMap<>();
        for (Room room : activeRooms) result.put(room.getId(), datesValid && reservations.countOverlaps(room, in, out) == 0);
        return result;
    }

    private Map<Long, java.util.List<String>> reservedDates(java.util.List<Room> activeRooms) {
        Map<Long, java.util.List<String>> result = new LinkedHashMap<>();
        var statuses = java.util.List.of("PENDING", "PAID", "APPROVED", "CANCELLATION_REQUESTED");
        var formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
        for (Room room : activeRooms) {
            var dates = new ArrayList<String>();
            for (Reservation reservation : reservations.findByRoom_IdAndStatusInOrderByCheckInAsc(room.getId(), statuses)) {
                if (reservation.getCheckOut().isBefore(LocalDate.now(java.time.ZoneId.of("Asia/Manila")))) continue;
                dates.add(reservation.getCheckIn().format(formatter) + " – " + reservation.getCheckOut().format(formatter));
            }
            result.put(room.getId(), dates);
        }
        return result;
    }

    private boolean isManager(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN")
                        || a.getAuthority().equals("ROLE_ONSITE_MANAGER")
                        || a.getAuthority().equals("ROLE_ONLINE_MANAGER"));
    }
}
