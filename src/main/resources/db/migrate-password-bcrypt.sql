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

-- 新增 ISBN、出版日期、总库存 字段
ALTER TABLE `book`
    ADD COLUMN `isbn` VARCHAR(20) DEFAULT NULL COMMENT 'ISBN编号' AFTER `book_introduction`,
    ADD COLUMN `publish_date` DATE DEFAULT NULL COMMENT '出版日期' AFTER `isbn`,
    ADD COLUMN `total_stock` INT(11) DEFAULT 0 COMMENT '总库存' AFTER `publish_date`;

-- 将内容简介字段从 VARCHAR(100) 扩展为 TEXT，支持更长内容
ALTER TABLE `book`
    MODIFY COLUMN `book_introduction` TEXT COMMENT '内容简介';