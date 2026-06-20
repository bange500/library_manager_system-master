-- 图书预约表
CREATE TABLE IF NOT EXISTS reservation (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL COMMENT '预约用户ID',
    book_id INT NOT NULL COMMENT '预约图书ID',
    reserve_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '预约时间',
    status TINYINT DEFAULT 0 COMMENT '0=排队中 1=待借阅(已通知) 2=已借出 3=已取消',
    notify_time DATETIME NULL COMMENT '通知时间',
    INDEX idx_user (user_id),
    INDEX idx_book (book_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书预约表';
