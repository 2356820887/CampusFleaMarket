package com.campus.market.utils;

/**
 * 用于在当前线程中存储用户信息
 */
public class UserHolder {
    private static final ThreadLocal<String> usernameTl = new ThreadLocal<>();
    private static final ThreadLocal<Long> userIdTl = new ThreadLocal<>();

    public static void saveUser(String username, Long userId) {
        usernameTl.set(username);
        userIdTl.set(userId);
    }

    public static String getUsername() {
        return usernameTl.get();
    }

    public static Long getUserId() {
        return userIdTl.get();
    }

    public static void removeUser() {
        usernameTl.remove();
        userIdTl.remove();
    }

}
