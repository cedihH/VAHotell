package com.vahotel.repo;
import com.vahotel.model.PaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PaymentRecordRepository extends JpaRepository<PaymentRecord,Long>{}
