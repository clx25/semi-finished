-- 创建表
CREATE TABLE IF NOT EXISTS users
(
    id
    INT
    PRIMARY
    KEY,
    name
    VARCHAR
(
    50
)
    );

-- 插入数据
INSERT INTO users (id, name)
VALUES (1, 'Alice');
INSERT INTO users (id, name)
VALUES (2, 'Bob');
