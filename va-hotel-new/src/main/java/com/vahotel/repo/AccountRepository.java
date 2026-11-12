package com.vahotel.repo;
import com.vahotel.model.Account;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AccountRepository extends JpaRepository<Account,Long>{ Optional<Account> findByEmail(String email); }
