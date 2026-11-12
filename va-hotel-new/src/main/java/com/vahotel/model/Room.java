package com.vahotel.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name="rooms")
public class Room {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, unique=true) private String roomNumber;
    @Column(nullable=false) private String roomType;
    @Column(nullable=false, length=2000) private String description;
    @Column(nullable=false, precision=12, scale=2) private BigDecimal nightlyPrice;
    @Column(nullable=false) private boolean active=true;
    private String photoPath;
    protected Room() {}
    public Room(String roomNumber,String roomType,String description,BigDecimal nightlyPrice){
        this.roomNumber=roomNumber;this.roomType=roomType;this.description=description;this.nightlyPrice=nightlyPrice;
    }
    public Long getId(){return id;} public String getRoomNumber(){return roomNumber;}
    public String getRoomType(){return roomType;} public String getDescription(){return description;}
    public BigDecimal getNightlyPrice(){return nightlyPrice;} public boolean isActive(){return active;}
    public void setActive(boolean v){active=v;} public void setNightlyPrice(BigDecimal v){nightlyPrice=v;}
    public void setDescription(String v){description=v;} public void setPhotoPath(String v){photoPath=v;}
}
