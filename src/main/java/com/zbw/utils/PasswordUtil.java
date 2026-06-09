package com.zbw.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码工具类 — 使用 BCrypt 加密
 * 兼容旧有明文密码的平滑升级
 */
public class PasswordUtil {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * 判断字符串是否已是 BCrypt 密文（以 $2a$ 开头）
     */
    public static boolean isBcryptHash(String str) {
        return str != null && str.startsWith("$2a$");
    }

    /**
     * 加密明文密码
     */
    public static String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    /**
     * 验证密码
     * 兼容旧明文密码：若数据库中是明文，则直接字符串比较，验证通过后返回 true
     *
     * @param rawPassword     用户输入的明文密码
     * @param storedPassword  数据库中存储的密码（可能是 BCrypt 密文或旧明文）
     * @return true=验证通过, false=验证失败
     */
    public static boolean matches(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }
        // 已加密密码使用 BCrypt 验证
        if (isBcryptHash(storedPassword)) {
            return encoder.matches(rawPassword, storedPassword);
        }
        // 旧明文密码直接比较
        return storedPassword.equals(rawPassword);
    }

    /**
     * 检查是否需要升级密码（旧明文 → BCrypt）
     */
    public static boolean needsUpgrade(String storedPassword) {
        return storedPassword != null && !isBcryptHash(storedPassword);
    }
}
