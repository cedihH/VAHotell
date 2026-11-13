package com.vahotel.service;
import com.vahotel.repo.AccountRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
@Service
public class AccountDetailsService implements UserDetailsService {
 private final AccountRepository accounts;
 @Value("${va.hotel.demo-multi-portal:true}") private boolean demoMultiPortal;
 public AccountDetailsService(AccountRepository accounts){this.accounts=accounts;}
 @Override public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
  var a=accounts.findByEmail(email.trim().toLowerCase()).orElseThrow(()->new UsernameNotFoundException("Account not found"));
  var user=User.withUsername(a.getEmail()).password(a.getPasswordHash()).disabled(!a.isEnabled());
  if(demoMultiPortal&&a.getEmail().equals("demo@localhost"))return user.roles("CUSTOMER","ONSITE_MANAGER","ONLINE_MANAGER","SUPER_ADMIN").build();
  return user.roles(a.getRole().name()).build();
 }
}
