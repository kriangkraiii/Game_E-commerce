package com.ecom.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.ecom.model.LoginLog;

public interface LoginLogRepository extends JpaRepository<LoginLog, Integer> {
    List<LoginLog> findByUserIdOrderByLoginTimeDesc(Integer userId);
}
