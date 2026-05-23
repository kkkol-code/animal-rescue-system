-- ==================== 城市流浪动物救助站 数据库初始化脚本（SQL Server）====================
-- 执行方式：在 SSMS 中打开此文件，点击"执行"，或使用 sqlcmd：
--   sqlcmd -S localhost -U sa -P your_password -i init.sql

-- 1. 创建数据库
IF DB_ID('animal_rescue') IS NULL
    CREATE DATABASE animal_rescue;
GO

USE animal_rescue;
GO

-- 2. 动物表
IF OBJECT_ID('animals', 'U') IS NULL
    CREATE TABLE animals (
        animal_id   BIGINT IDENTITY(1,1) PRIMARY KEY,
        name        NVARCHAR(50)  NOT NULL,
        species     NVARCHAR(20)  NOT NULL,
        breed       NVARCHAR(50)  NOT NULL,
        gender      NVARCHAR(4)   NOT NULL,
        adopt_status NVARCHAR(20) NOT NULL DEFAULT 'pending',
        entry_date  DATE          NOT NULL
    );
GO

-- 3. 领养人表
IF OBJECT_ID('adopters', 'U') IS NULL
    CREATE TABLE adopters (
        adopter_id      BIGINT IDENTITY(1,1) PRIMARY KEY,
        real_name       NVARCHAR(50)  NOT NULL,
        phone           NVARCHAR(20)  NOT NULL,
        id_card         NVARCHAR(20)  NOT NULL,
        house_condition NVARCHAR(50)  NOT NULL
    );
GO

-- 4. 领养申请表
IF OBJECT_ID('adoption_applications', 'U') IS NULL
    CREATE TABLE adoption_applications (
        application_id BIGINT IDENTITY(1,1) PRIMARY KEY,
        adopter_id     BIGINT        NOT NULL,
        animal_id      BIGINT        NOT NULL,
        apply_date     DATE          NOT NULL,
        status         NVARCHAR(20)  NOT NULL DEFAULT 'pending',
        remark         NVARCHAR(500) NULL
    );
GO

-- 5. 测试数据
INSERT INTO animals (name, species, breed, gender, adopt_status, entry_date) VALUES
(N'大黄', N'狗', N'中华田园犬', N'公', 'pending',  '2026-01-15'),
(N'小花', N'猫', N'中华田园猫', N'母', 'adopted',  '2025-11-20'),
(N'旺财', N'狗', N'金毛',       N'公', 'pending',  '2026-03-08'),
(N'咪咪', N'猫', N'英短',       N'母', 'sick',     '2026-02-14'),
(N'小白', N'狗', N'泰迪',       N'母', 'pending',  '2026-04-01'),
(N'大橘', N'猫', N'中华田园猫', N'公', 'treating', '2026-03-22'),
(N'黑子', N'狗', N'中华田园犬', N'公', 'adopted',  '2025-09-10'),
(N'团子', N'猫', N'美短',       N'母', 'pending',  '2026-05-02'),
(N'阿福', N'狗', N'金毛',       N'公', 'pending',  '2026-05-10'),
(N'雪球', N'猫', N'英短',       N'母', 'adopted',  '2025-12-05'),
(N'虎子', N'狗', N'中华田园犬', N'公', 'pending',  '2026-04-18'),
(N'奶茶', N'猫', N'美短',       N'母', 'sick',     '2026-05-08');
GO

PRINT '数据库初始化完成。';
GO
