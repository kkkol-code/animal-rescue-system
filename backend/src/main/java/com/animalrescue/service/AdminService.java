package com.animalrescue.service;

import com.animalrescue.entity.AdminUser;
import com.animalrescue.mapper.AdminUserMapper;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AdminService {

    private final AdminUserMapper adminUserMapper;

    public AdminService(AdminUserMapper adminUserMapper) {
        this.adminUserMapper = adminUserMapper;
    }

    /**
     * 登录校验：比对用户名密码，成功返回用户信息 + 简易 token
     */
    public Map<String, Object> login(String username, String password) {
        AdminUser user = adminUserMapper.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("用户名或密码错误");
        }

        String token = UUID.randomUUID().toString().replace("-", "");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("token", token);
        result.put("adminId", user.getAdminId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        return result;
    }
}
