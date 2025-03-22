DROP TABLE IF EXISTS "users";
DROP TABLE IF EXISTS "user_order";
DROP TABLE IF EXISTS "gender";
DROP TABLE IF EXISTS "role";
DROP TABLE IF EXISTS "user_role";
DROP TABLE IF EXISTS "menu";
DROP TABLE IF EXISTS "user_sensitive_info";

-- 用户表
CREATE TABLE IF NOT EXISTS "users" (
    id    INT PRIMARY KEY,
    name  VARCHAR(50),
    gender INT
);

-- 用户敏感信息表
CREATE TABLE IF NOT EXISTS "user_sensitive_info" (
    id         INT PRIMARY KEY,
    user_id    INT NOT NULL,
    phone      VARCHAR(15),
    id_card    VARCHAR(18),
    address    VARCHAR(255)
);

-- 用户订单表
CREATE TABLE IF NOT EXISTS "user_order" (
    id          INT PRIMARY KEY,
    user_id     INT,
    order_date  DATETIME,
    money       DECIMAL(10, 2)
);

-- 性别表
CREATE TABLE IF NOT EXISTS "gender" (
    id   INT PRIMARY KEY,
    name VARCHAR(50)
);

-- 角色表
CREATE TABLE IF NOT EXISTS "role" (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    name_cn    VARCHAR(50) NOT NULL,
    code       VARCHAR(10) NOT NULL,
    parent_id  INT
);

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS "user_role" (
    id      INT PRIMARY KEY,
    user_id INT NOT NULL,
    role_id INT NOT NULL
);

-- 菜单表
CREATE TABLE IF NOT EXISTS "menu" (
    id     INT PRIMARY KEY,
    label  VARCHAR(255),
    icon   VARCHAR(255),
    route  BOOLEAN
);

INSERT INTO "users" (id, name, gender) VALUES
(1, 'Alice', 23),
(2, 'Bob', 30),
(3, 'Charlie', 15),
(4, 'David', 14),
(5, 'Emma', 24),
(6, 'Frank', 40),
(7, 'Grace', 8),
(8, 'Henry', 28),
(9, 'Ivy', 45),
(10, 'Jack', 19),
(11, 'Kate', 24),
(12, 'Liam', 14),
(13, 'Mia', 27),
(14, 'Noah', 25),
(15, 'Olivia', 14),
(16, 'Peter', 37),
(17, 'Quinn', 8),
(18, 'Ryan', 47),
(19, 'Sophia', 24),
(20, 'Thomas', 51),
(21, 'Uma', 10),
(22, 'Victor', 14),
(23, 'Wendy', 36),
(24, 'Xavier', 17),
(25, 'Yara', 26),
(26, 'Zane', 14),
(27, 'Amy', 33),
(28, 'Ben', 16),
(29, 'Cathy', 31),
(30, 'Daniel', 14),
(31, 'Eva', 11),
(32, 'Felix', 24),
(33, 'Gina', 9),
(34, 'Harry', 42),
(35, 'Isabella', 21),
(36, 'Jason', 46),
(37, 'Karen', 13),
(38, 'Leo', 8),
(39, 'Megan', 43),
(40, 'Nathan', 7),
(41, 'Oliver', 50),
(42, 'Penny', 27),
(43, 'Quincy', 44),
(44, 'Rachel', 6),
(45, 'Samuel', 8),
(46, 'Tina', 34),
(47, 'Usher', 5),
(48, 'Vera', 48),
(49, 'William', 3),
(50, 'Xena', 40);

INSERT INTO "gender" (id, name) VALUES
(1, '顺性别女'),
(2, '女'),
(3, '跨性别女'),
(4, '有跨性别经验的女性'),
(5, '经历过性别转换过程的女性'),
(6, '跨女性化'),
(7, '中心女性'),
(8, '男跨女'),
(9, '半性取向/半性女'),
(10, '姐妹女性'),
(11, '顺性别男'),
(12, '男'),
(13, '性转男'),
(14, '有跨性别经验的男性'),
(15, '经历过性别转换过程的男性'),
(16, '跨男性化'),
(17, '中心男性'),
(18, '女跨男'),
(19, '半性取向/半性男'),
(20, '跨性别男'),
(21, '具有男性精神的跨性别男'),
(22, '跨性别'),
(23, '变性'),
(24, '非二元性别'),
(25, '性别酷儿'),
(26, '无性别'),
(27, '外性别'),
(28, '女性化'),
(29, '男性化'),
(30, '同性恋'),
(31, '男性化(黑人或拉丁裔)'),
(32, '女同+假小子'),
(33, '双性性格'),
(34, '假小子'),
(35, '"gender" outlaw'),
(36, '非常规性别'),
(37, '武装直升机'),
(38, '双性'),
(39, '泛性'),
(40, '性别创造'),
(41, '性别拓展'),
(42, '第三性'),
(43, '中性'),
(44, '全性别'),
(45, '多性别'),
(46, '灰性别'),
(47, '间性别'),
(48, '独行侠'),
(49, '不可描述的性别'),
(50, '双灵'),
(51, '人妖'),
(52, '性转女'),
(53, '性转男');

INSERT INTO "user_sensitive_info" (id, user_id, phone, id_card, address) VALUES
(1, 1, '13800138001', '110105199003071234', '北京市朝阳区'),
(2, 2, '13800138002', '110105198512125678', '上海市浦东新区'),
(3, 3, '13800138003', '110105200005209123', '广州市天河区'),
(4, 4, '13800138004', '110105200108154321', '深圳市南山区'),
(5, 5, '13800138005', '110105199507231234', '杭州市西湖区'),
(6, 6, '13800138006', '110105198004105678', '南京市鼓楼区'),
(7, 7, '13800138007', '110105201001019123', '成都市武侯区'),
(8, 8, '13800138008', '110105199811114321', '武汉市江汉区'),
(9, 9, '13800138009', '110105198802281234', '重庆市渝中区'),
(10, 10, '13800138010', '110105199209155678', '西安市雁塔区');

INSERT INTO "user_order" (id, user_id, order_date, money) VALUES
(1, 11, '2023-01-15 12:30:00', 50.00),
(2, 24, '2023-02-28 15:45:00', 75.25),
(3, 31, '2023-03-20 09:00:00', 100.50),
(4, 45, '2023-04-18 18:20:00', 125.75),
(5, 11, '2023-05-10 10:10:00', 60.00),
(6, 24, '2023-06-05 14:00:00', NULL),
(7, 13, '2023-07-22 20:30:00', 30.25),
(8, 45, NULL, 80.50),
(9, 11, '2023-09-09 09:45:00', 95.75),
(10, 24, '2023-10-03 11:11:00', 120.00),
(11, 15, '2023-11-11 11:11:00', 25.00),
(12, 45, '2023-12-25 00:00:00', 150.25),
(13, 11, '2024-01-01 00:01:00', 200.50),
(14, 24, '2024-02-14 14:14:00', 175.75),
(15, 17, '2024-03-08 08:08:00', 100.00),
(16, 45, '2024-04-30 20:20:00', 90.25),
(17, 11, '2024-05-01 00:00:01', 55.50),
(18, 24, '2024-06-18 18:18:00', 70.75),
(19, 31, '2024-07-07 07:07:00', 45.00),
(20, 45, '2024-08-08 08:08:00', 85.25);

-- 角色数据
INSERT INTO "role" (id, name_cn, code, parent_id) VALUES
(1, 'CEO', '0001', NULL),
(2, 'CTO', '0002', 1),
(3, 'CFO', '0003', 1),
(4, '人力资源总监', '0004', 1),
(5, '市场营销总监', '0005', 0),
(6, '技术总监', '0006', 2),
(7, '软件开发部经理', '0007', 6),
(8, '硬件开发部经理', '0008', 6),
(9, '软件工程师', '0009', 7),
(10, '硬件工程师', '0010', 8);

-- 用户与角色的关联数据
INSERT INTO "user_role" (id, user_id, role_id) VALUES
(1, 1, 1),
(2, 2, 2),
(3, 3, 3),
(4, 4, 4),
(5, 5, 5),
(6, 6, 6),
(7, 7, 7),
(8, 8, 8),
(9, 9, 9),
(10, 10, 10),
(11, 11, 1),
(12, 12, 2),
(13, 13, 3),
(14, 14, 4),
(15, 15, 5),
(16, 16, 6),
(17, 17, 7),
(18, 18, 8),
(19, 19, 9),
(20, 20, 10);

INSERT INTO "menu" VALUES
(1, 'Home', '{"type":"chart-graph","size":24,"strokeWidth":2,"theme":"outline","fill":["#0045f1"]}', true),
(2, 'User', '{"type":"user","size":24,"strokeWidth":2,"theme":"outline","fill":["#0045f1"]}', true),
(3, 'Api', '{"size":24,"strokeWidth":2,"theme":"outline","fill":["#0045f1"],"type":"api","img":false}', false),
(4, 'Role', NULL, false);