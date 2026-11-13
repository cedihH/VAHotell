package com.vahotel.service;

import com.vahotel.model.DecisionLog;
import com.vahotel.repo.DecisionLogRepository;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class DecisionLogService {
    private final DecisionLogRepository logs;

    public DecisionLogService(DecisionLogRepository logs) { this.logs = logs; }

    public void record(Authentication actor, String action, String subject, String details) {
        if (actor == null || !actor.isAuthenticated()) return;
        String roles = actor.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replaceFirst("^ROLE_", ""))
                .sorted().collect(java.util.stream.Collectors.joining(", "));
        logs.save(new DecisionLog(actor.getName(), roles, action, subject, details));
    }

    public List<DecisionLog> recent() { return logs.findTop300ByOrderByDecidedAtDesc(); }
}
