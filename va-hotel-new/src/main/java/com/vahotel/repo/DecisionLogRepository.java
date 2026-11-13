package com.vahotel.repo;

import com.vahotel.model.DecisionLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DecisionLogRepository extends JpaRepository<DecisionLog, Long> {
    List<DecisionLog> findTop300ByOrderByDecidedAtDesc();
}
