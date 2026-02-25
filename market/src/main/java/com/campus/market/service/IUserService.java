package com.campus.market.service;

import com.campus.market.entity.User;

import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;

import com.campus.market.common.Result;

/**
 * <p>
 * 用户基本信息表 服务类
 * </p>
 *
 * @author GraduationTutor
 * @since 2025-12-22
 */
public interface IUserService extends IService<User> {
    Result<String> register(User user);

    Result<Map<String, Object>> login(String username, String password);

    /**
     * 分页查询用户列表 (管理员)
     * 
     * @param page    页码
     * @param size    每页大小
     * @param keyword 搜索关键词
     * @return 用户分页列表
     */
    com.baomidou.mybatisplus.core.metadata.IPage<User> listUsers(Integer page, Integer size, String keyword);

    /**
     * 更新用户状态 (管理员)
     * 
     * @param userId 用户ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateUserStatus(Long userId, Byte status);

    /**
     * 更新用户信息
     * 
     * @param user 用户信息
     * @return 是否成功
     */
    boolean updateUserInfo(User user);
}
