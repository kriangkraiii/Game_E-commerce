package com.ecom.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ecom.model.LoginLog;
import com.ecom.model.UserDtls;
import com.ecom.repository.LoginLogRepository;
import com.ecom.service.LoginLogService;

import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginLogServiceImpl implements LoginLogService {

    @Autowired
    private LoginLogRepository loginLogRepository;

    @Override
    @Transactional
    public void saveLoginLog(UserDtls user) {
        LoginLog log = new LoginLog(user, LocalDateTime.now());
        loginLogRepository.save(log);
    }

    @Override
    public List<LoginLog> getLogsByUser(UserDtls user) {
        return loginLogRepository.findByUserIdOrderByLoginTimeDesc(user.getId());
    }
}
