package com.vahotel.service;
import com.vahotel.repo.AccountRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
@Service
public class AccountDetailsService implements UserDetailsService {
 private final AccountRepository accounts;
 public AccountDetailsService(AccountRepository accounts){this.accounts=accounts;}
 @Override public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
  var a=accounts.findByEmail(email.trim().toLowerCase()).orElseThrow(()->new UsernameNotFoundException("Account not found"));
  return User.withUsername(a.getEmail()).password(a.getPasswordHash()).roles(a.getRole().name()).disabled(!a.isEnabled()).build();
 }
}
