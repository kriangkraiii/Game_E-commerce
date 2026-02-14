package com.ecom.service;

import java.util.List;
import com.ecom.model.LoginLog;
import com.ecom.model.UserDtls;

public interface LoginLogService {
    void saveLoginLog(UserDtls user);

    List<LoginLog> getLogsByUser(UserDtls user);
}
