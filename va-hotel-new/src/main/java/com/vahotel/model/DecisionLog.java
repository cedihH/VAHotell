package com.vahotel.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "manager_decision_logs")
public class DecisionLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private String actorEmail;
    @Column(nullable = false) private String actorRoles;
    @Column(nullable = false) private String action;
    @Column(nullable = false) private String subject;
    @Column(length = 1200) private String details;
    @Column(nullable = false) private LocalDateTime decidedAt = LocalDateTime.now();

    protected DecisionLog() {}

    public DecisionLog(String actorEmail, String actorRoles, String action, String subject, String details) {
        this.actorEmail = actorEmail;
        this.actorRoles = actorRoles;
        this.action = action;
        this.subject = subject;
        this.details = details;
    }

    public Long getId() { return id; }
    public String getActorEmail() { return actorEmail; }
    public String getActorRoles() { return actorRoles; }
    public String getAction() { return action; }
    public String getSubject() { return subject; }
    public String getDetails() { return details; }
    public LocalDateTime getDecidedAt() { return decidedAt; }
}
