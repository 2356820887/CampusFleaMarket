package com.campus.market.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.Map;

/**
 * JWT 工具类
 */
public class JwtUtils {

    // 签名密钥 (建议从配置文件读取)
    private static final String SECRET_KEY = "CampusMarketKey";

    // 过期时间：24小时 (单位：毫秒)
    private static final long EXPIRATION_TIME = 86400000;

    /**
     * 生成 Token
     * 
     * @param username 用户名
     * @param claims   自定义载荷 (如角色信息)
     * @return JWT Token 字符串
     */
    public static String createToken(String username, Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    /**
     * 解析 Token
     * 
     * @param token JWT Token
     * @return Claims 对象
     */
    public static Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 验证 Token 是否过期
     * 
     * @param token JWT Token
     * @return true-已过期，false-未过期
     */
    public static boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 从 Token 中获取用户名
     * 
     * @param token JWT Token
     * @return 用户名
     */
    public static String getUsername(String token) {
        return parseToken(token).getSubject();
    }

    /**
     * 从 Token 中获取用户 ID
     * 
     * @param token JWT Token
     * @return 用户 ID
     */
    public static Long getUserId(String token) {
        Object userId = parseToken(token).get("userId");
        if (userId == null) {
            return null;
        }
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        }
        return (Long) userId;
    }
}
