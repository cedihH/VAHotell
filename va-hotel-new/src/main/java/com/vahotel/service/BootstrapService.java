package com.vahotel.service;

import com.vahotel.model.Account;
import com.vahotel.model.Role;
import com.vahotel.model.Room;
import com.vahotel.repo.AccountRepository;
import com.vahotel.repo.RoomRepository;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BootstrapService implements CommandLineRunner {
    private final AccountRepository accounts;
    private final RoomRepository rooms;
    private final PasswordEncoder encoder;

    @Value("${va.hotel.bootstrap-demo-accounts:true}")
    private boolean seedDemoAccounts;

    public BootstrapService(AccountRepository accounts, RoomRepository rooms, PasswordEncoder encoder) {
        this.accounts = accounts;
        this.rooms = rooms;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (seedDemoAccounts) {
            seedAccount("admin@localhost", "Development Super Admin", Role.SUPER_ADMIN);
            seedAccount("online@localhost", "Online Manager", Role.ONLINE_MANAGER);
            seedAccount("onsite@localhost", "On-Site Manager", Role.ONSITE_MANAGER);
            seedAccount("guest@localhost", "Demo Guest", Role.CUSTOMER);
            seedAccount("demo@localhost", "VA Hotel Demonstrator", Role.CUSTOMER);
            accounts.findByEmail("demo@localhost").filter(a -> "VA Hotel Demonstrator".equals(a.getFullName())).ifPresent(a -> {
                if (!a.isEnabled()) { a.setEnabled(true); accounts.save(a); }
            });
        }

        // Seed missing sample rooms individually so existing SQLite files are upgraded too.
        seedRoom("101", "Standard", "Comfortable standard room with Wi-Fi, air conditioning, and a private bath.", "2500");
        seedRoom("102", "Standard", "Comfortable standard room with Wi-Fi, air conditioning, and a private bath.", "2500");
        seedRoom("103", "Standard", "Comfortable standard room with Wi-Fi, air conditioning, and a private bath.", "2500");
        seedRoom("201", "Deluxe", "Spacious deluxe room with a sitting area, Wi-Fi, and air conditioning.", "3500");
        seedRoom("202", "Deluxe", "Spacious deluxe room with a sitting area, Wi-Fi, and air conditioning.", "3500");
        seedRoom("203", "Deluxe", "Spacious deluxe room with a sitting area, Wi-Fi, and air conditioning.", "3500");
        seedRoom("301", "Suite", "Private suite with a separate lounge, Wi-Fi, and air conditioning.", "5000");
        seedRoom("302", "Suite", "Private suite with a separate lounge, Wi-Fi, and air conditioning.", "5000");
        seedRoom("303", "Suite", "Private suite with a separate lounge, Wi-Fi, and air conditioning.", "5000");
        seedRoom("401", "Family", "Family room with flexible sleeping space, Wi-Fi, and air conditioning.", "4500");
        seedRoom("402", "Family", "Family room with flexible sleeping space, Wi-Fi, and air conditioning.", "4500");
        seedRoom("403", "Family", "Family room with flexible sleeping space, Wi-Fi, and air conditioning.", "4500");
    }

    private void seedAccount(String email, String name, Role role) {
        accounts.findByEmail(email).orElseGet(() -> {
            Account account = new Account(email, encoder.encode("ChangeMe123!"), name, role, true);
            account.setEmailVerified(true);
            return accounts.save(account);
        });
    }

    private void seedRoom(String number, String category, String description, String price) {
        var existing = rooms.findByRoomNumber(number);
        if (existing.isEmpty()) {
            rooms.save(new Room(number, category, description, new BigDecimal(price)));
        } else if (existing.get().getDescription().startsWith("Sample ")) {
            existing.get().setDescription(description);
            rooms.save(existing.get());
        }
    }
}
