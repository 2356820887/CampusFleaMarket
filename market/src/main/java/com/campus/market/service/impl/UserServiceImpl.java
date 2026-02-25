package com.campus.market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.campus.market.common.Result;
import com.campus.market.entity.User;
import com.campus.market.mapper.UserMapper;
import com.campus.market.service.IUserService;
import com.campus.market.utils.JwtUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户基本信息表 服务实现类
 * </p>
 *
 * @author GraduationTutor
 * @since 2025-12-22
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Override
    public Result<String> register(User user) {

        // 1. 检查用户名是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", user.getUsername());
        if (this.baseMapper.selectOne(queryWrapper) != null) {
            return Result.error("用户名已存在");
        }
        if (StringUtils.isBlank(user.getUsername()) || StringUtils.isBlank(user.getPhone())) {
            return Result.error("用户名和手机号不能为空");
        }

        // 2. 密码加密 (使用 BCrypt)
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        user.setPassword(encoder.encode(user.getPassword()));

        // 3. 设置默认状态并保存
        if (this.save(user)) {
            return Result.success("注册成功");
        }
        return Result.error("注册失败，请稍后重试");
    }

    @Override
    public Result<Map<String, Object>> login(String username, String password) {
        // 1. 查询用户
        User user = this.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 2. 检查用户状态
        if (user.getStatus() != null && user.getStatus() == -1) {
            return Result.error("该账号已被永久封禁，无法登录");
        }

        // 3. 验证密码
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(password, user.getPassword())) {
            return Result.error("密码错误");
        }

        // 4. 生成 JWT Token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole());
        String token = JwtUtils.createToken(user.getUsername(), claims);

        // 5. 返回 Token 给前端
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("user", user); // 同时返回用户信息，方便前端展示
        return Result.success(map);
    }

    @Override
    public com.baomidou.mybatisplus.core.metadata.IPage<User> listUsers(Integer page, Integer size, String keyword) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> pageParam = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.like(User::getUsername, keyword)
                    .or()
                    .like(User::getNickname, keyword);
        }
        wrapper.orderByDesc(User::getCreatedAt);
        return this.page(pageParam, wrapper);
    }

    @Override
    public boolean updateUserStatus(Long userId, Byte status) {
        User user = new User();
        user.setId(userId);
        user.setStatus(status);
        return updateById(user);
    }

    @Override
    public boolean updateUserInfo(User user) {
        // 只更新非空字段，MyBatis-Plus 的 updateById 默认策略即为此 (IF NOT NULL)
        // 但为了安全，Controller 层已经滤除了敏感字段
        return updateById(user);
    }
}
