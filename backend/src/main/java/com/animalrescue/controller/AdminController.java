package com.animalrescue.controller;

import com.animalrescue.common.Result;
import com.animalrescue.service.AdminService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * POST /api/admin/login
     * 接收 { username, password }，返回 token + 用户信息
     */
    @PostMapping("/login")
    public Result<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        if (username == null || password == null) {
            return Result.fail("用户名和密码不能为空");
        }
        try {
            Map<String, Object> data = adminService.login(username, password);
            return Result.ok("登录成功", data);
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }
}
