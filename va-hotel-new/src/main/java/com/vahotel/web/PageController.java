package com.vahotel.web;

import com.vahotel.repo.RoomRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    private final RoomRepository rooms;

    public PageController(RoomRepository rooms) {
        this.rooms = rooms;
    }

    // The portal is the default page. It must render independently of room data.
    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/rooms")
    public String rooms(Model model) {
        model.addAttribute("rooms", rooms.findByActiveTrue());
        return "rooms";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
