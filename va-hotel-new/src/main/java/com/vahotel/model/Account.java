package com.vahotel.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="accounts")
public class Account {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, unique=true) private String email;
    @Column(nullable=false) private String passwordHash;
    @Column(nullable=false) private String fullName;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private Role role;
    @Column(nullable=false) private boolean enabled = false;
    @Column(nullable=false) private boolean emailVerified = false;
    private String identityDocumentPath;
    private LocalDateTime createdAt = LocalDateTime.now();

    protected Account() {}
    public Account(String email, String passwordHash, String fullName, Role role, boolean enabled) {
        this.email=email; this.passwordHash=passwordHash; this.fullName=fullName; this.role=role; this.enabled=enabled;
    }
    public Long getId(){return id;} public String getEmail(){return email;}
    public String getPasswordHash(){return passwordHash;} public String getFullName(){return fullName;}
    public Role getRole(){return role;} public boolean isEnabled(){return enabled;}
    public boolean isEmailVerified(){return emailVerified;}
    public void setEnabled(boolean v){enabled=v;} public String getIdentityDocumentPath(){return identityDocumentPath;} public void setFullName(String v){fullName=v;} public void setEmailVerified(boolean v){emailVerified=v;}
    public void setPasswordHash(String v){passwordHash=v;} public void setIdentityDocumentPath(String v){identityDocumentPath=v;}
}
