-- ============================================
-- BCrypt 密码加密迁移脚本
-- 将 user_pwd 和 admin_pwd 列从 varchar(20) 扩展为 varchar(200)
-- BCrypt 哈希值为固定 60 字符，varchar(200) 留足余量
-- ============================================

ALTER TABLE `user` MODIFY COLUMN `user_pwd` varchar(200) DEFAULT NULL;
ALTER TABLE `admin` MODIFY COLUMN `admin_pwd` varchar(200) DEFAULT NULL;

-- 验证列已修改
-- SHOW COLUMNS FROM `user` LIKE 'user_pwd';
-- SHOW COLUMNS FROM `admin` LIKE 'admin_pwd';
