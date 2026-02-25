package com.campus.market.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campus.market.common.Result;
import com.campus.market.entity.User;
import com.campus.market.service.IUserService;
import com.campus.market.dto.UserLoginDTO;
import com.campus.market.utils.UserHolder;

/**
 * <p>
 * 用户基本信息表 前端控制器
 * </p>
 *
 * @author GraduationTutor
 * @since 2025-12-22
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private IUserService userService;

    /**
     * 用户注册接口
     * 
     * @param user 前端传来的 JSON 用户对象
     * @return 统一结果封装 Result
     */
    @PostMapping("/register")
    public Result<String> register(@RequestBody User user) {
        // 调用我们之前在 UserServiceImpl 中写的逻辑
        return userService.register(user);
    }

    /**
     * 用户登录接口
     * 
     * @param loginDTO 包含 username 和 password 的 DTO
     * @return 包含 Token 和 User 信息的 Result
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody UserLoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();

        if (username == null || password == null) {
            return Result.error("用户名或密码不能为空");
        }

        return userService.login(username, password);
    }

    /**
     * 获取用户信息 (用于测试 Token 是否有效)
     * 这是一个受保护的接口，后续我们会加上拦截器
     */
    @GetMapping("/info/{id}")
    public Result<User> getUserInfo(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user != null) {
            user.setPassword(null); // 安全起见，不返回密码
            return Result.success(user);
        }
        return Result.error("用户不存在");
    }

    @GetMapping("/me")
    public Result<String> getMyName() {
        String username = UserHolder.getUsername();
        return Result.success("当前登录用户是：" + username);
    }

    /**
     * 分页查询用户列表 (管理员)
     */
    @GetMapping("/list")
    public Result<com.baomidou.mybatisplus.core.metadata.IPage<User>> listUsers(
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "1") Integer page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") Integer size,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String keyword) {
        // TODO: 鉴权，仅管理员可调用
        return Result.success(userService.listUsers(page, size, keyword));
    }

    /**
     * 更新用户状态 (管理员)
     */
    @PostMapping("/status")
    public Result<String> updateUserStatus(@RequestBody Map<String, Object> params) {
        // TODO: 鉴权，仅管理员可调用
        Long userId = Long.valueOf(params.get("userId").toString());
        Byte status = Byte.valueOf(params.get("status").toString());
        if (userService.updateUserStatus(userId, status)) {
            return Result.success("操作成功");
        }
        return Result.error("操作失败");
    }

    /**
     * 更新个人信息
     * 
     * @param user 用户信息
     * @return 成功信息
     */
    @PostMapping("/update")
    public Result<User> updateUserInfo(@RequestBody User user) {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            return Result.error(401, "未登录或登录已过期");
        }

        user.setId(userId); // 强制只能修改自己的信息
        // 防止修改敏感字段
        user.setUsername(null);
        user.setPassword(null);
        user.setRole(null);
        user.setStatus(null);
        // user.setBalance(null); // 无此字段
        user.setCreditScore(null);

        if (userService.updateUserInfo(user)) {
            // 返回最新的用户信息以便前端更新缓存
            User newUser = userService.getById(userId);
            newUser.setPassword(null);
            return Result.success(newUser);
        }
        return Result.error("更新失败");
    }
}
