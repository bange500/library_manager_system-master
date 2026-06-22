/*
 * 图书管理系统 - 数据库初始化脚本
 *
 * 包含全部建表、示例数据、外键约束
 * 数据库名: library-manager-system
 * 字符集: utf8mb4
 */

SET FOREIGN_KEY_CHECKS=0;

-- ============================================================
-- 1. admin（管理员表）
-- ============================================================
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin` (
    `admin_id`    INT(11)      NOT NULL AUTO_INCREMENT,
    `admin_name`  VARCHAR(20)  DEFAULT NULL,
    `admin_pwd`   VARCHAR(200) DEFAULT NULL COMMENT 'BCrypt 密文',
    `admin_email` VARCHAR(20)  DEFAULT NULL,
    PRIMARY KEY (`admin_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

INSERT INTO `admin` VALUES ('1', 'admin', '123456', '501455447@qq.com');

-- ============================================================
-- 2. dept（部门表）
-- ============================================================
DROP TABLE IF EXISTS `dept`;
CREATE TABLE `dept` (
    `dept_id`   INT(11)     NOT NULL AUTO_INCREMENT,
    `dept_name` VARCHAR(20) DEFAULT NULL,
    PRIMARY KEY (`dept_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;

INSERT INTO `dept` VALUES ('1', '信息工程学院');
INSERT INTO `dept` VALUES ('2', '体育学院');
INSERT INTO `dept` VALUES ('3', '美术学院');
INSERT INTO `dept` VALUES ('4', '电子工程学院');

-- ============================================================
-- 3. user（用户表）
-- ============================================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `user_id`    INT(11)      NOT NULL AUTO_INCREMENT,
    `user_name`  VARCHAR(20)  DEFAULT NULL,
    `user_pwd`   VARCHAR(200) DEFAULT NULL COMMENT 'BCrypt 密文',
    `user_email` VARCHAR(30)  DEFAULT NULL,
    `dept_id`    INT(11)      DEFAULT NULL COMMENT '所属院系ID',
    PRIMARY KEY (`user_id`),
    KEY `dept_id` (`dept_id`),
    CONSTRAINT `user_ibfk_1` FOREIGN KEY (`dept_id`) REFERENCES `dept` (`dept_id`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8;

INSERT INTO `user` (`user_id`, `user_name`, `user_pwd`, `user_email`, `dept_id`) VALUES
('1',  'user1',       '123456', '501455447@qq.com', '1'),
('2',  'zbw',         '123456', '501455447@qq.com', '1'),
('5',  'user2',       '123456', '501455447@qq.com', '2'),
('6',  'LeBronJames', '123456', '501455447@qq.com', '2'),
('7',  '科比',        '123456', '501455447@qq.com', '3'),
('8',  '柏拉图',      '123456', '501455447@qq.com', '4'),
('9',  '拿破仑',      '123456', '501455447@qq.com', '1'),
('10', '欧文',        '123456', '501455447@qq.com', '2'),
('11', '库兹马',      '123456', '501455447@qq.com', '3'),
('13', '魔术师',      '123456', '501455447@qq.com', '4'),
('16', '周杰伦',      '123456', '501455447@qq.com', '1'),
('23', 'Variation',   '123456', '501455447@qq.com', '1');

-- ============================================================
-- 4. book_category（图书类别表）
-- ============================================================
DROP TABLE IF EXISTS `book_category`;
CREATE TABLE `book_category` (
    `category_id`   INT(11)     NOT NULL AUTO_INCREMENT,
    `category_name` VARCHAR(20) DEFAULT NULL,
    PRIMARY KEY (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=50 DEFAULT CHARSET=utf8;

INSERT INTO `book_category` VALUES
('1',  '小说'),
('2',  '历史'),
('3',  '计算机'),
('4',  '哲学'),
('5',  '社会科学'),
('6',  '政治法律'),
('7',  '军事科学'),
('8',  '中国文学'),
('9',  '外国文学'),
('10', '外国传记'),
('11', '英语'),
('12', '俄国小说'),
('13', '心理学'),
('14', '言情小说'),
('15', '武侠小说'),
('16', '环境科学'),
('17', '纪实文学');

-- ============================================================
-- 5. book（图书表）
-- ============================================================
DROP TABLE IF EXISTS `book`;
CREATE TABLE `book` (
    `book_id`           INT(11)      NOT NULL AUTO_INCREMENT,
    `book_name`         VARCHAR(200) NOT NULL,
    `book_author`       VARCHAR(50)  DEFAULT NULL,
    `book_publish`      VARCHAR(50)  DEFAULT NULL,
    `book_category`     INT(11)      DEFAULT NULL,
    `book_price`        DOUBLE       DEFAULT NULL,
    `book_introduction` TEXT         COMMENT '内容简介',
    `isbn`              VARCHAR(20)  DEFAULT NULL COMMENT 'ISBN编号',
    `publish_date`      DATE         DEFAULT NULL COMMENT '出版日期',
    `total_stock`       INT(11)      DEFAULT 0  COMMENT '总库存',
    PRIMARY KEY (`book_id`),
    KEY `book_category` (`book_category`) USING BTREE,
    CONSTRAINT `book_ibfk_1` FOREIGN KEY (`book_category`) REFERENCES `book_category` (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=72 DEFAULT CHARSET=utf8;

INSERT INTO `book` (`book_id`, `book_name`, `book_author`, `book_publish`, `book_category`, `book_price`, `book_introduction`, `isbn`, `publish_date`, `total_stock`) VALUES
('1',  '巨人的陨落',            '肯.福莱特',       '江苏凤凰文艺出版社', '1',  '129',  '在第一次世界大战中发生的故事',               '978-7-534-29768-2', '2016-05-01', 5),
('2',  '三体',                  '刘慈欣',           '南京大学出版社',     '1',  '68',   '科幻小说',                                    '978-7-562-07666-5', '2012-01-01', 3),
('3',  '复活',                  '列夫.托尔斯泰',    '上海译文出版社',     '1',  '19',   '俄国小说',                                    '978-7-532-74819-8', '2010-01-01', 2),
('6',  '平凡的世界',            '路遥',             '上海文艺出版社',     '1',  '88',   '孙少平和孙少安两兄弟...',                      '978-7-532-19027-0', '2012-03-01', 4),
('15', '白鹿原',                '陈忠实',           '南京出版社',         '1',  '36',   '当代小说',                                    '978-7-534-29581-3', '2012-06-01', 3),
('16', '计算机网络',            '谢希仁',           '电子工业出版社',     '3',  '49',   '计算机专业书籍',                               '978-7-121-30348-5', '2017-01-01', 5),
('17', '霍乱时期的爱情',        '加西亚·马尔克斯',  '译林出版社',         '9',  '39',   '外国小说',                                    '978-7-544-75986-0', '2014-06-01', 2),
('18', '天才在左疯子在右',      '高铭',             '北京联合出版公司',   '1',  '39.8', '心理学',                                      '978-7-550-24251-9', '2015-01-01', 3),
('19', '废都',                  '贾平凹',           '商务印书馆',         '8',  '29',   '当代小说',                                    '978-7-100-05736-8', '2008-01-01', 2),
('20', 'jQuery',                'Ryan',             '中国电力出版社',     '3',  '78',   'js库',                                        '978-7-512-30123-4', '2013-05-01', 3),
('21', 'python数据爬虫',        '张博文',           '清华大学出版社',     '3',  '52',   '带你走进爬虫的世界',                           '978-7-302-45678-9', '2018-07-01', 4),
('22', '入门python可视化',      'variation',        '电子大学出版社',     '8',  '61',   '探究数据背后的秘密',                           '978-7-121-34567-8', '2019-02-01', 3),
('71', 'Springboot从入门到实践','筱威',             '北京大学出版社',     '3',  '78',   '带你走进spirngboot',                           '978-7-301-23456-7', '2020-01-01', 5);

-- ============================================================
-- 6. announcement（公告/活动表）
-- ============================================================
DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement` (
    `id`           INT           AUTO_INCREMENT PRIMARY KEY,
    `title`        VARCHAR(200)  NOT NULL COMMENT '标题',
    `content`      TEXT          COMMENT '详细内容(HTML)',
    `summary`      VARCHAR(500)  COMMENT '摘要',
    `cover_image`  VARCHAR(500)  COMMENT '封面图片路径',
    `type`         VARCHAR(20)   NOT NULL DEFAULT 'announcement' COMMENT '类型: announcement=公告, activity=活动',
    `is_carousel`  TINYINT(1)    DEFAULT 0 COMMENT '是否轮播展示',
    `publisher_id` INT           COMMENT '发布管理员ID',
    `create_time`  DATETIME      DEFAULT CURRENT_TIMESTAMP,
    `update_time`  DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`publisher_id`) REFERENCES `admin` (`admin_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告/活动表';

INSERT INTO `announcement` (`title`, `content`, `summary`, `cover_image`, `type`, `is_carousel`, `publisher_id`) VALUES
('2024年读书月活动开幕',
'<h3>书香满校园 — 2024读书月正式启动</h3><p>为营造浓厚的校园阅读氛围，图书馆将于4月1日至4月30日举办"书香满校园"读书月系列活动。</p><p>活动内容包括：</p><ul><li>名师导读讲座 — 每周一场</li><li>21天阅读打卡挑战</li><li>好书推荐展</li><li>读书分享会</li></ul>',
'书香满校园 — 2024读书月正式启动',
'/images/lib1.jpg', 'activity', 1, 1),

('图书馆延长开放时间通知',
'<h3>关于延长图书馆开放时间的通知</h3><p>图书馆自2024年3月1日起调整开放时间：主阅览区 7:00-22:30，自习室 6:30-23:00。</p>',
'图书馆自3月1日起延长开放时间',
'/images/lib2.jpg', 'announcement', 1, 1),

('"悦读之星"评选活动',
'<h3>2024年度"悦读之星"评选活动</h3><p>图书馆现面向全校学生开展"悦读之星"评选活动。一等奖 Kindle 电子书阅读器。</p>',
'评选年度借阅之星，Kindle阅读器等你来拿',
'/images/lib3.jpg', 'activity', 1, 1),

('新书推荐 — 2024春季新书上架',
'<h3>2024年春季新书推荐</h3><p>图书馆最新采购的2000余册新书已完成编目上架，涵盖文学、科技、经管、艺术等多个学科领域。</p>',
'2000余册新书已上架',
'/images/lib4.jpg', 'announcement', 1, 1),

('数据库检索培训讲座',
'<h3>如何高效利用图书馆数据库 — 检索技巧培训</h3><p>时间：2024年4月15日 14:30-16:00，地点：图书馆三楼报告厅。</p>',
'4月15日数据库检索技巧培训',
'/images/lib5.jpg', 'activity', 1, 1),

('关于规范占座行为的公告',
'<h3>关于规范自习室占座行为的通知</h3><p>离开座位超过30分钟请自觉带走物品。严禁隔夜占座。</p>',
'离开座位超30分钟请自觉带走物品',
'/images/lib1.jpg', 'announcement', 0, 1),

('寒假借书规则调整',
'<h3>寒假期间借书规则调整通知</h3><p>自2024年12月15日起借出的图书，归还日期统一延长至2024年3月5日。假期借书上限调整为8本。</p>',
'寒假借书上限提升至8本，归还日期延长至3月5日',
'/images/lib2.jpg', 'announcement', 0, 1),

('毕业季图书捐赠倡议',
'<h3>"书香传递 爱心永续" — 毕业季图书捐赠倡议</h3><p>将手中不再需要的教材捐赠给图书馆，部分纳入馆藏，部分捐赠给贫困山区学校。</p>',
'毕业季图书捐赠活动',
'/images/lib3.jpg', 'activity', 0, 1);

-- ============================================================
-- 7. borrowingbooks（借阅记录表）
-- ============================================================
DROP TABLE IF EXISTS `borrowingbooks`;
CREATE TABLE `borrowingbooks` (
    `id`      INT(11) NOT NULL AUTO_INCREMENT,
    `user_id` INT(11) DEFAULT NULL,
    `book_id` INT(11) DEFAULT NULL,
    `date`    DATE    DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `user_id` (`user_id`) USING BTREE,
    KEY `book_id` (`book_id`) USING BTREE,
    CONSTRAINT `borrowingbooks_ibfk_1` FOREIGN KEY (`book_id`) REFERENCES `book` (`book_id`),
    CONSTRAINT `borrowingbooks_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=62 DEFAULT CHARSET=utf8;

INSERT INTO `borrowingbooks` VALUES
('9',  '5', '1',  '2020-08-04'),
('28', '5', '19', '2020-08-01'),
('31', '2', '20', '2020-08-02'),
('55', '1', '21', '2020-08-19'),
('57', '1', '17', '2020-08-19'),
('61', '1', '16', '2020-08-21');

-- ============================================================
-- 8. reservation（预约表）
-- ============================================================
DROP TABLE IF EXISTS `reservation`;
CREATE TABLE `reservation` (
    `id`           INT      AUTO_INCREMENT PRIMARY KEY,
    `user_id`      INT      NOT NULL COMMENT '预约用户ID',
    `book_id`      INT      NOT NULL COMMENT '预约图书ID',
    `reserve_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '预约时间',
    `status`       TINYINT  DEFAULT 0 COMMENT '0=排队中 1=待借阅(已通知) 2=已借出 3=已取消',
    `notify_time`  DATETIME NULL COMMENT '通知时间',
    INDEX `idx_user` (`user_id`),
    INDEX `idx_book` (`book_id`),
    FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`),
    FOREIGN KEY (`book_id`) REFERENCES `book` (`book_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书预约表';

SET FOREIGN_KEY_CHECKS=1;
