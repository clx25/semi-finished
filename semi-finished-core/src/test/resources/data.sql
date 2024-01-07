DROP TABLE IF EXISTS "users";
DROP TABLE IF EXISTS "user_order";
DROP TABLE IF EXISTS "gender";
DROP TABLE IF EXISTS "user_order";
DROP TABLE IF EXISTS "role";
DROP TABLE IF EXISTS "user_role";

CREATE TABLE IF NOT EXISTS "users"(
    id INT  PRIMARY KEY,
    name VARCHAR(50),
    gender INT
);



CREATE TABLE IF NOT EXISTS "user_order"(
       id INT  PRIMARY KEY,
       user_id INT,
       order_date DATETIME,
       money DECIMAL(10,2)
);


CREATE TABLE IF NOT EXISTS "gender"(
     id INT  PRIMARY KEY,
     name VARCHAR(50)
);



-- 角色表
CREATE TABLE  IF NOT EXISTS "role" (
      id INT PRIMARY KEY,
      name_cn VARCHAR(50) NOT NULL,
      code VARCHAR(10) NOT NULL,
      parent_id INT
);


-- 角色与用户的关联表
CREATE TABLE IF NOT EXISTS "user_role" (
       id INT PRIMARY KEY,
       user_id INT NOT NULL,
       role_id INT NOT NULL
);


INSERT INTO users (id, name, gender) VALUES (1, 'Alice', 23);
INSERT INTO users (id, name, gender) VALUES (2, 'Bob', 30);
INSERT INTO users (id, name, gender) VALUES (3, 'Charlie', 15);
INSERT INTO users (id, name, gender) VALUES (4, 'David', 14);
INSERT INTO users (id, name, gender) VALUES (5, 'Emma', 24);
INSERT INTO users (id, name, gender) VALUES (6, 'Frank', 40);
INSERT INTO users (id, name, gender) VALUES (7, 'Grace', 8);
INSERT INTO users (id, name, gender) VALUES (8, 'Henry', 28);
INSERT INTO users (id, name, gender) VALUES (9, 'Ivy', 45);
INSERT INTO users (id, name, gender) VALUES (10, 'Jack', 19);
INSERT INTO users (id, name, gender) VALUES (11, 'Kate', 24);
INSERT INTO users (id, name, gender) VALUES (12, 'Liam', 14);
INSERT INTO users (id, name, gender) VALUES (13, 'Mia', 27);
INSERT INTO users (id, name, gender) VALUES (14, 'Noah', 25);
INSERT INTO users (id, name, gender) VALUES (15, 'Olivia', 14);
INSERT INTO users (id, name, gender) VALUES (16, 'Peter', 37);
INSERT INTO users (id, name, gender) VALUES (17, 'Quinn', 8);
INSERT INTO users (id, name, gender) VALUES (18, 'Ryan', 47);
INSERT INTO users (id, name, gender) VALUES (19, 'Sophia', 24);
INSERT INTO users (id, name, gender) VALUES (20, 'Thomas', 51);
INSERT INTO users (id, name, gender) VALUES (21, 'Uma', 10);
INSERT INTO users (id, name, gender) VALUES (22, 'Victor', 14);
INSERT INTO users (id, name, gender) VALUES (23, 'Wendy', 36);
INSERT INTO users (id, name, gender) VALUES (24, 'Xavier', 17);
INSERT INTO users (id, name, gender) VALUES (25, 'Yara', 26);
INSERT INTO users (id, name, gender) VALUES (26, 'Zane', 14);
INSERT INTO users (id, name, gender) VALUES (27, 'Amy', 33);
INSERT INTO users (id, name, gender) VALUES (28, 'Ben', 16);
INSERT INTO users (id, name, gender) VALUES (29, 'Cathy', 31);
INSERT INTO users (id, name, gender) VALUES (30, 'Daniel', 14);
INSERT INTO users (id, name, gender) VALUES (31, 'Eva', 11);
INSERT INTO users (id, name, gender) VALUES (32, 'Felix', 24);
INSERT INTO users (id, name, gender) VALUES (33, 'Gina', 9);
INSERT INTO users (id, name, gender) VALUES (34, 'Harry', 42);
INSERT INTO users (id, name, gender) VALUES (35, 'Isabella', 21);
INSERT INTO users (id, name, gender) VALUES (36, 'Jason', 46);
INSERT INTO users (id, name, gender) VALUES (37, 'Karen', 13);
INSERT INTO users (id, name, gender) VALUES (38, 'Leo', 8);
INSERT INTO users (id, name, gender) VALUES (39, 'Megan', 43);
INSERT INTO users (id, name, gender) VALUES (40, 'Nathan', 7);
INSERT INTO users (id, name, gender) VALUES (41, 'Oliver', 50);
INSERT INTO users (id, name, gender) VALUES (42, 'Penny', 27);
INSERT INTO users (id, name, gender) VALUES (43, 'Quincy', 44);
INSERT INTO users (id, name, gender) VALUES (44, 'Rachel', 6);
INSERT INTO users (id, name, gender) VALUES (45, 'Samuel', 8);
INSERT INTO users (id, name, gender) VALUES (46, 'Tina', 34);
INSERT INTO users (id, name, gender) VALUES (47, 'Usher', 5);
INSERT INTO users (id, name, gender) VALUES (48, 'Vera', 48);
INSERT INTO users (id, name, gender) VALUES (49, 'William', 3);
INSERT INTO users (id, name, gender) VALUES (50, 'Xena', 40);


INSERT INTO gender (id, name) VALUES (1, '顺性别女');
INSERT INTO gender (id, name) VALUES (2, '女');
INSERT INTO gender (id, name) VALUES (3, '跨性别女');
INSERT INTO gender (id, name) VALUES (4, '有跨性别经验的女性');
INSERT INTO gender (id, name) VALUES (5, '经历过性别转换过程的女性');
INSERT INTO gender (id, name) VALUES (6, '跨女性化');
INSERT INTO gender (id, name) VALUES (7, '中心女性');
INSERT INTO gender (id, name) VALUES (8, '男跨女');
INSERT INTO gender (id, name) VALUES (9, '半性取向/半性女');
INSERT INTO gender (id, name) VALUES (10, '姐妹女性');
INSERT INTO gender (id, name) VALUES (11, '顺性别男');
INSERT INTO gender (id, name) VALUES (12, '男');
INSERT INTO gender (id, name) VALUES (13, '性转男');
INSERT INTO gender (id, name) VALUES (14, '有跨性别经验的男性');
INSERT INTO gender (id, name) VALUES (15, '经历过性别转换过程的男性');
INSERT INTO gender (id, name) VALUES (16, '跨男性化');
INSERT INTO gender (id, name) VALUES (17, '中心男性');
INSERT INTO gender (id, name) VALUES (18, '女跨男');
INSERT INTO gender (id, name) VALUES (19, '半性取向/半性男');
INSERT INTO gender (id, name) VALUES (20, '跨性别男');
INSERT INTO gender (id, name) VALUES (21, '具有男性精神的跨性别男');
INSERT INTO gender (id, name) VALUES (22, '跨性别');
INSERT INTO gender (id, name) VALUES (23, '变性');
INSERT INTO gender (id, name) VALUES (24, '非二元性别');
INSERT INTO gender (id, name) VALUES (25, '性别酷儿');
INSERT INTO gender (id, name) VALUES (26, '无性别');
INSERT INTO gender (id, name) VALUES (27, '外性别');
INSERT INTO gender (id, name) VALUES (28, '女性化');
INSERT INTO gender (id, name) VALUES (29, '男性化');
INSERT INTO gender (id, name) VALUES (30, '同性恋');
INSERT INTO gender (id, name) VALUES (31, '男性化(黑人或拉丁裔)');
INSERT INTO gender (id, name) VALUES (32, '女同+假小子');
INSERT INTO gender (id, name) VALUES (33, '双性性格');
INSERT INTO gender (id, name) VALUES (34, '假小子');
INSERT INTO gender (id, name) VALUES (35, 'Gender outlaw');
INSERT INTO gender (id, name) VALUES (36, '非常规性别');
INSERT INTO gender (id, name) VALUES (37, '武装直升机');
INSERT INTO gender (id, name) VALUES (38, '双性');
INSERT INTO gender (id, name) VALUES (39, '泛性');
INSERT INTO gender (id, name) VALUES (40, '性别创造');
INSERT INTO gender (id, name) VALUES (41, '性别拓展');
INSERT INTO gender (id, name) VALUES (42, '第三性');
INSERT INTO gender (id, name) VALUES (43, '中性');
INSERT INTO gender (id, name) VALUES (44, '全性别');
INSERT INTO gender (id, name) VALUES (45, '多性别');
INSERT INTO gender (id, name) VALUES (46, '灰性别');
INSERT INTO gender (id, name) VALUES (47, '间性别');
INSERT INTO gender (id, name) VALUES (48, '独行侠');
INSERT INTO gender (id, name) VALUES (49, '不可描述的性别');
INSERT INTO gender (id, name) VALUES (50, '双灵');
INSERT INTO gender (id, name) VALUES (51, '人妖');
INSERT INTO gender (id, name) VALUES (52, '性转女');
INSERT INTO gender (id, name) VALUES (53, '性转男');


INSERT INTO user_order (id, user_id, order_date, money) VALUES (1, 11, '2023-01-15 12:30:00', 50.00);
INSERT INTO user_order (id, user_id, order_date, money) VALUES (2, 24, '2023-02-28 15:45:00', 75.25);
INSERT INTO user_order (id, user_id, order_date, money) VALUES (3, 31, '2023-03-20 09:00:00', 100.50);
INSERT INTO user_order (id, user_id, order_date, money) VALUES (4, 45, '2023-04-18 18:20:00', 125.75);
INSERT INTO user_order (id, user_id, order_date, money) VALUES (5, 11, '2023-05-10 10:10:00', 60.00);
INSERT INTO user_order (id, user_id, order_date, money) VALUES (6, 24, '2023-06-05 14:00:00', null);
INSERT INTO user_order (id, user_id, order_date, money) VALUES (7, 13, '2023-07-22 20:30:00', 30.25);
INSERT INTO user_order (id, user_id, order_date, money) VALUES (8, 45, '2023-08-17 16:40:00', 80.50);
INSERT INTO user_order (id, user_id, order_date, money) VALUES (9, 11, '2023-09-09 09:45:00', 95.75);
INSERT INTO user_order (id, user_id, order_date, money) VALUES (10, 24, '2023-10-03 11:11:00', 120.00);
INSERT INTO user_order (id, user_id, order_date, money) VALUES (11, 15, '2023-11-11 11:11:00', 25.00);
INSERT INTO user_order (id, user_id, order_date, money) VALUES (12, 45, '2023-12-25 00:00:00', 150.25);
INSERT INTO user_order (id, user_id, order_date, money) VALUES (13, 11, '2024-01-01 00:01:00', 200.50);
INSERT INTO user_order (id, user_id, order_date, money) VALUES (14, 24, '2024-02-14 14:14:00', 175.75);
INSERT INTO user_order (id, user_id, order_date, money) VALUES (15, 17, '2024-03-08 08:08:00', 100.00);
INSERT INTO user_order (id, user_id, order_date, money) VALUES (16, 45, '2024-04-30 20:20:00', 90.25);
INSERT INTO user_order (id, user_id, order_date, money) VALUES (17, 11, '2024-05-01 00:00:01', 55.50);
INSERT INTO user_order (id, user_id, order_date, money) VALUES (18, 24, '2024-06-18 18:18:00', 70.75);
INSERT INTO user_order (id, user_id, order_date, money) VALUES (19, 31, '2024-07-07 07:07:00', 45.00);
INSERT INTO user_order (id, user_id, order_date, money) VALUES (20, 45, '2024-08-08 08:08:00', 85.25);


-- 角色数据
INSERT INTO role (id, name_cn, code, parent_id) VALUES (1, 'CEO', '0001', NULL);
INSERT INTO role (id, name_cn, code, parent_id) VALUES (2, 'CTO', '0002', 1);
INSERT INTO role (id, name_cn, code, parent_id) VALUES (3, 'CFO', '0003', 1);
INSERT INTO role (id, name_cn, code, parent_id) VALUES (4, '人力资源总监', '0004', 1);
INSERT INTO role (id, name_cn, code, parent_id) VALUES (5, '市场营销总监', '0005', 1);
INSERT INTO role (id, name_cn, code, parent_id) VALUES (6, '技术总监', '0006', 2);
INSERT INTO role (id, name_cn, code, parent_id) VALUES (7, '软件开发部经理', '0007', 6);
INSERT INTO role (id, name_cn, code, parent_id) VALUES (8, '硬件开发部经理', '0008', 6);
INSERT INTO role (id, name_cn, code, parent_id) VALUES (9, '软件工程师', '0009', 7);
INSERT INTO role (id, name_cn, code, parent_id) VALUES (10, '硬件工程师', '0010', 8);


-- 用户与角色的关联数据
INSERT INTO user_role (id, user_id, role_id) VALUES (1, 1, 1);
INSERT INTO user_role (id, user_id, role_id) VALUES (2, 2, 2);
INSERT INTO user_role (id, user_id, role_id) VALUES (3, 3, 3);
INSERT INTO user_role (id, user_id, role_id) VALUES (4, 4, 4);
INSERT INTO user_role (id, user_id, role_id) VALUES (5, 5, 5);
INSERT INTO user_role (id, user_id, role_id) VALUES (6, 6, 6);
INSERT INTO user_role (id, user_id, role_id) VALUES (7, 7, 7);
INSERT INTO user_role (id, user_id, role_id) VALUES (8, 8, 8);
INSERT INTO user_role (id, user_id, role_id) VALUES (9, 9, 9);
INSERT INTO user_role (id, user_id, role_id) VALUES (10, 10, 10);
INSERT INTO user_role (id, user_id, role_id) VALUES (11, 11, 1);
INSERT INTO user_role (id, user_id, role_id) VALUES (12, 12, 2);
INSERT INTO user_role (id, user_id, role_id) VALUES (13, 13, 3);
INSERT INTO user_role (id, user_id, role_id) VALUES (14, 14, 4);
INSERT INTO user_role (id, user_id, role_id) VALUES (15, 15, 5);
INSERT INTO user_role (id, user_id, role_id) VALUES (16, 16, 6);
INSERT INTO user_role (id, user_id, role_id) VALUES (17, 17, 7);
INSERT INTO user_role (id, user_id, role_id) VALUES (18, 18, 8);
INSERT INTO user_role (id, user_id, role_id) VALUES (19, 19, 9);
INSERT INTO user_role (id, user_id, role_id) VALUES (20, 20, 10);