package com.vahotel.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="customer_reports")
public class CustomerReport {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(optional=false) private Account customer;
 @Column(nullable=false) private String subject;
 @Column(nullable=false,length=4000) private String message;
 @Column(nullable=false) private String status="OPEN";
 @Column(length=4000) private String managerReply;
 private LocalDateTime createdAt=LocalDateTime.now();
 protected CustomerReport(){}
 public CustomerReport(Account c,String s,String m){customer=c;subject=s;message=m;}
 public Long getId(){return id;} public Account getCustomer(){return customer;} public String getSubject(){return subject;} public String getMessage(){return message;} public String getStatus(){return status;} public String getManagerReply(){return managerReply;} public LocalDateTime getCreatedAt(){return createdAt;} public void setStatus(String v){status=v;} public void setManagerReply(String v){managerReply=v;}
}
