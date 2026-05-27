-- ====================================================================
-- 🐾 城市流浪动物救助站全流程管理系统
--    数据库物理设计与实施脚本 (SQL Server / T-SQL)
-- ====================================================================
-- 执行环境：SQL Server 2016+ / SSMS / sqlcmd
-- 执行方式：在 SSMS 中打开本文件，点击「执行」即可
-- ====================================================================

-- -------------------------------- 0. 建库 --------------------------------
IF DB_ID('animal_rescue') IS NULL
    CREATE DATABASE animal_rescue;
GO
USE animal_rescue;
GO


-- ====================================================================
-- 第一部分：规范化建表与完整性约束 (DDL)
-- ====================================================================

-- -------------------------------- 1.1 管理员表 --------------------------------
-- 说明：存储系统授权用户的登录凭证与基本信息
IF OBJECT_ID('admin_users', 'U') IS NULL
CREATE TABLE admin_users (
    admin_id   BIGINT IDENTITY(1,1) PRIMARY KEY,         -- 管理员编号，自增主键
    username   NVARCHAR(50)  NOT NULL,                    -- 登录用户名
    password   NVARCHAR(100) NOT NULL,                    -- 登录密码（应加密存储）
    real_name  NVARCHAR(50)  NULL,                        -- 真实姓名
    created_at DATETIME      DEFAULT GETDATE()            -- 创建时间，默认当前时间
);
GO

-- -------------------------------- 1.2 动物档案表 --------------------------------
-- 说明：系统核心实体，记录每只入站动物的基本信息与实时状态
IF OBJECT_ID('animals', 'U') IS NULL
CREATE TABLE animals (
    animal_id    BIGINT IDENTITY(1,1) PRIMARY KEY,        -- 动物编号，自增主键
    name         NVARCHAR(50)  NOT NULL,                   -- 动物名称
    species      NVARCHAR(20)  NOT NULL,                   -- 种类：猫 / 狗
    breed        NVARCHAR(50)  NOT NULL,                   -- 品种：中华田园犬、金毛等
    gender       NVARCHAR(4)   NOT NULL,                   -- 性别：公 / 母
    adopt_status NVARCHAR(20)  NOT NULL DEFAULT 'pending', -- 领养状态，默认"待领养"
    entry_date   DATE          NOT NULL,                   -- 入站日期
    vaccination  NVARCHAR(10)  NOT NULL DEFAULT N'未记录', -- 免疫情况（3NF：依赖动物本身而非就诊事件）

    -- 检查约束：限定种类、性别、状态、免疫情况的合法取值
    CONSTRAINT CK_animals_species    CHECK (species    IN (N'猫', N'狗')),
    CONSTRAINT CK_animals_gender     CHECK (gender     IN (N'公', N'母')),
    CONSTRAINT CK_animals_status     CHECK (adopt_status IN ('pending','adopted','sick','treating','applying')),
    CONSTRAINT CK_animals_vaccine    CHECK (vaccination IN (N'已免疫', N'未免疫', N'部分免疫', N'未记录'))
);
GO

-- -------------------------------- 1.3 领养人表 --------------------------------
-- 说明：记录申请领养动物的个人身份信息与居住条件
IF OBJECT_ID('adopters', 'U') IS NULL
CREATE TABLE adopters (
    adopter_id      BIGINT IDENTITY(1,1) PRIMARY KEY,     -- 领养人编号，自增主键
    real_name       NVARCHAR(50)  NOT NULL,                -- 真实姓名
    phone           NVARCHAR(20)  NOT NULL,                -- 联系电话（11位手机号）
    id_card         NVARCHAR(20)  NOT NULL,                -- 身份证号（18位）
    house_condition NVARCHAR(50)  NOT NULL                 -- 住房情况：自有住房/整租/合租/宿舍
);
GO

-- -------------------------------- 1.4 医疗记录表 --------------------------------
-- 说明：以动物为维度，记录其在站期间的全部健康事件（体检、疫苗、手术等）
IF OBJECT_ID('medical_records', 'U') IS NULL
CREATE TABLE medical_records (
    record_id    BIGINT IDENTITY(1,1) PRIMARY KEY,        -- 记录编号，自增主键
    animal_id    BIGINT       NOT NULL,                    -- 关联动物编号（外键）
    check_date   DATE         NOT NULL,                    -- 检查/治疗日期
    type         NVARCHAR(10) NOT NULL,                    -- 记录类型：体检/疫苗/手术/其他
    vet_name     NVARCHAR(50) NOT NULL,                    -- 主治兽医姓名
    weight       DECIMAL(5,2) NULL,                        -- 体重（kg），可为空
    temperature  DECIMAL(4,1) NULL,                        -- 体温（°C），可为空
    diagnosis    NVARCHAR(500) NOT NULL,                   -- 诊断结果/病情描述
    treatment    NVARCHAR(500) NOT NULL,                   -- 治疗方案/用药说明
    created_at   DATETIME     DEFAULT GETDATE(),           -- 记录创建时间

    -- 外键约束：关联 animals 表，删除动物时级联删除其医疗记录
    CONSTRAINT FK_medical_animal FOREIGN KEY (animal_id) REFERENCES animals(animal_id),

    -- 检查约束：限定类型及生理指标合理取值
    CONSTRAINT CK_medical_type   CHECK (type IN (N'体检', N'疫苗', N'手术', N'其他')),
    CONSTRAINT CK_medical_weight CHECK (weight IS NULL OR weight > 0),
    CONSTRAINT CK_medical_temp   CHECK (temperature IS NULL OR (temperature >= 30 AND temperature <= 45))
);
GO

-- -------------------------------- 1.5 领养申请表 --------------------------------
-- 说明：动物与领养人之间的关联实体，记录每一次领养申请的核心信息
IF OBJECT_ID('adoption_applications', 'U') IS NULL
CREATE TABLE adoption_applications (
    application_id BIGINT IDENTITY(1,1) PRIMARY KEY,      -- 申请编号，自增主键
    adopter_id     BIGINT       NOT NULL,                  -- 领养人编号（外键）
    animal_id      BIGINT       NOT NULL,                  -- 动物编号（外键）
    apply_date     DATE         NOT NULL,                  -- 申请日期
    status         NVARCHAR(20) NOT NULL DEFAULT 'pending',-- 审核状态：pending/approved/rejected
    remark         NVARCHAR(500) NULL,                     -- 备注/审核意见

    -- 外键约束
    CONSTRAINT FK_app_adopter FOREIGN KEY (adopter_id) REFERENCES adopters(adopter_id),
    CONSTRAINT FK_app_animal  FOREIGN KEY (animal_id)  REFERENCES animals(animal_id),

    -- 检查约束：限定审核状态的合法取值
    CONSTRAINT CK_app_status CHECK (status IN ('pending', 'approved', 'rejected'))
);
GO


-- ====================================================================
-- 第二部分：物理结构设计 — 索引 (Index)
-- ====================================================================
-- 设计依据：对 WHERE / JOIN / ORDER BY 中的高频字段建立非聚集索引，
--           在加速查询的同时不过度影响 INSERT/UPDATE 性能。

-- 动物表：按领养状态筛选是最常用条件（每天数十次档案查询）
CREATE NONCLUSTERED INDEX IX_animals_adopt_status ON animals(adopt_status);

-- 动物表：按种类筛选为第二常用条件
CREATE NONCLUSTERED INDEX IX_animals_species ON animals(species);

-- 医疗记录表：按动物 ID 查询其全部医疗历史（最核心的关联查询）
CREATE NONCLUSTERED INDEX IX_medical_animal_id ON medical_records(animal_id);

-- 医疗记录表：按检查日期降序排列（"最新的 N 条记录"场景）
CREATE NONCLUSTERED INDEX IX_medical_check_date ON medical_records(check_date DESC);

-- 领养申请表：按动物 + 领养人联合查询（"某人是否已申请某动物"去重判断）
CREATE NONCLUSTERED INDEX IX_app_animal_adopter ON adoption_applications(animal_id, adopter_id);

-- 领养申请表：按申请时间降序（"最近的申请记录"场景）
CREATE NONCLUSTERED INDEX IX_app_apply_date ON adoption_applications(apply_date DESC);

-- 领养人表：按身份证号查询（提交领养时查重 —— 最高频的精确查找）
CREATE NONCLUSTERED INDEX IX_adopters_id_card ON adopters(id_card);
GO


-- ====================================================================
-- 第三部分：高级实施 — 视图 (View)
-- ====================================================================

-- 3.1 综合动物信息视图 -------------------------------------------------------
-- 业务说明：将动物的基本信息、最新一条医疗记录整合为一张"全局状态卡"，
--           减少前端多次请求的复杂度。
CREATE OR ALTER VIEW v_animal_comprehensive_info AS
SELECT
    -- 动物基本信息
    a.animal_id,
    a.name          AS animal_name,
    a.species,
    a.breed,
    a.gender,
    a.adopt_status,
    a.entry_date,
    a.vaccination,                                      -- 3NF：免疫情况直接从 animals 读取

    -- 最新医疗记录
    latest.check_date   AS last_check_date,
    latest.type         AS last_medical_type,
    latest.diagnosis    AS last_diagnosis,
    latest.treatment    AS last_treatment,
    latest.vet_name     AS last_vet_name,

    -- 状态中文显示
    CASE
        WHEN a.adopt_status = 'pending'  THEN N'待领养'
        WHEN a.adopt_status = 'adopted'  THEN N'已领养'
        WHEN a.adopt_status = 'sick'     THEN N'生病中'
        WHEN a.adopt_status = 'treating' THEN N'治疗中'
        WHEN a.adopt_status = 'applying' THEN N'申请中'
        ELSE N'未知'
    END AS status_display

FROM animals a
OUTER APPLY (
    SELECT TOP 1 *
    FROM medical_records
    WHERE animal_id = a.animal_id
    ORDER BY check_date DESC
) latest;
GO

-- 3.2 领养详情视图 -----------------------------------------------------------
-- 业务说明：三表 JOIN 整合完整领养记录，供审核页面和管理报表使用
CREATE OR ALTER VIEW v_adoption_detail AS
SELECT
    app.application_id,
    app.apply_date,
    app.status      AS app_status,
    app.remark,
    a.animal_id,
    a.name          AS animal_name,
    a.species       AS animal_species,
    a.breed         AS animal_breed,
    a.gender        AS animal_gender,
    ad.adopter_id,
    ad.real_name    AS adopter_name,
    ad.phone        AS adopter_phone,
    ad.id_card      AS adopter_id_card,
    ad.house_condition
FROM adoption_applications app
INNER JOIN animals   a  ON app.animal_id  = a.animal_id
INNER JOIN adopters  ad ON app.adopter_id = ad.adopter_id;
GO


-- ====================================================================
-- 第四部分：高级实施 — 触发器 (Trigger)
-- ====================================================================

-- 4.1 领养审核通过后自动更新动物状态 ------------------------------------------
-- 业务说明：当领养申请被审核员审批通过（status 更新为 'approved'），
--           触发器自动将对应动物的 adopt_status 设为 'adopted'，
--           避免应用层遗漏更新导致数据不一致。
CREATE OR ALTER TRIGGER trg_after_adoption_approved
ON adoption_applications
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    -- 仅处理新增的"已通过"记录
    UPDATE animals
    SET adopt_status = 'adopted'
    FROM animals a
    INNER JOIN inserted i ON a.animal_id = i.animal_id
    WHERE i.status = 'approved';
END;
GO

-- 4.2 删除动物前级联清理关联数据 ----------------------------------------------
-- 业务说明：使用 INSTEAD OF 触发器，在真正执行 DELETE 前，
--           先删除该动物在 adoption_applications 和 medical_records 中的关联记录，
--           防止外键约束报错或产生孤儿数据。
CREATE OR ALTER TRIGGER trg_before_animal_delete
ON animals
INSTEAD OF DELETE
AS
BEGIN
    SET NOCOUNT ON;

    DELETE FROM adoption_applications WHERE animal_id IN (SELECT animal_id FROM deleted);
    DELETE FROM medical_records        WHERE animal_id IN (SELECT animal_id FROM deleted);
    DELETE FROM animals               WHERE animal_id IN (SELECT animal_id FROM deleted);
END;
GO


-- ====================================================================
-- 第五部分：高级实施 — 存储过程 (Stored Procedure)
-- ====================================================================

-- 5.1 月度运营统计报表 -------------------------------------------------------
-- 业务说明：输入年份和月份，输出该月的四项核心运营指标。
--          可用于 Dashboard 的月度趋势图和周期性汇报。
CREATE OR ALTER PROCEDURE sp_generate_monthly_statistics
    @Year  INT,           -- 统计年份，如 2026
    @Month INT            -- 统计月份，如 05
AS
BEGIN
    SET NOCOUNT ON;

    -- 计算统计时间区间
    DECLARE @StartDate DATE = DATEFROMPARTS(@Year, @Month, 1);
    DECLARE @EndDate   DATE = EOMONTH(@StartDate);

    -- 1. 本月新增收容动物数
    DECLARE @NewAnimals INT;
    SELECT @NewAnimals = COUNT(*)
    FROM animals
    WHERE entry_date BETWEEN @StartDate AND @EndDate;

    -- 2. 本月新增医疗记录数
    DECLARE @MedicalRecords INT;
    SELECT @MedicalRecords = COUNT(*)
    FROM medical_records
    WHERE check_date BETWEEN @StartDate AND @EndDate;

    -- 3. 本月康复动物数（从治疗状态恢复到 healthy 或 pending，且无近期异常记录）
    DECLARE @CuredAnimals INT;
    SELECT @CuredAnimals = COUNT(DISTINCT animal_id)
    FROM medical_records
    WHERE check_date BETWEEN @StartDate AND @EndDate
      AND type = N'体检'
      AND diagnosis LIKE N'%康复%';

    -- 4. 本月成功领养数
    DECLARE @AdoptedAnimals INT;
    SELECT @AdoptedAnimals = COUNT(*)
    FROM adoption_applications
    WHERE apply_date BETWEEN @StartDate AND @EndDate
      AND status = 'approved';

    -- 输出统计结果
    SELECT
        @Year            AS 统计年份,
        @Month           AS 统计月份,
        @NewAnimals      AS 新增收容数,
        @MedicalRecords  AS 医疗记录数,
        @CuredAnimals    AS 治愈数,
        @AdoptedAnimals  AS 成功领养数;
END;
GO


-- ====================================================================
-- 第六部分：初始数据
-- ====================================================================

-- 6.1 默认管理员（密码 123456，生产环境应加密存储）
IF NOT EXISTS (SELECT 1 FROM admin_users WHERE username = 'admin')
    INSERT INTO admin_users (username, password, real_name)
    VALUES ('admin', '123456', N'系统管理员');

-- 6.2 示例动物数据（品种分布覆盖中华田园犬/猫、金毛、泰迪、英短、美短）
IF NOT EXISTS (SELECT 1 FROM animals)
    INSERT INTO animals (name, species, breed, gender, adopt_status, entry_date, vaccination) VALUES
    (N'大黄', N'狗', N'中华田园犬', N'公', 'pending',  '2026-01-15', N'已免疫'),
    (N'小花', N'猫', N'中华田园猫', N'母', 'adopted',  '2025-11-20', N'未记录'),
    (N'旺财', N'狗', N'金毛',       N'公', 'pending',  '2026-03-08', N'未记录'),
    (N'咪咪', N'猫', N'英短',       N'母', 'sick',     '2026-02-14', N'未免疫'),
    (N'小白', N'狗', N'泰迪',       N'母', 'pending',  '2026-04-01', N'未记录'),
    (N'大橘', N'猫', N'中华田园猫', N'公', 'treating', '2026-03-22', N'未记录'),
    (N'黑子', N'狗', N'中华田园犬', N'公', 'adopted',  '2025-09-10', N'未记录'),
    (N'团子', N'猫', N'美短',       N'母', 'pending',  '2026-05-02', N'未记录'),
    (N'阿福', N'狗', N'金毛',       N'公', 'pending',  '2026-05-10', N'未记录'),
    (N'雪球', N'猫', N'英短',       N'母', 'adopted',  '2025-12-05', N'未记录'),
    (N'虎子', N'狗', N'中华田园犬', N'公', 'pending',  '2026-04-18', N'未记录'),
    (N'奶茶', N'猫', N'美短',       N'母', 'sick',     '2026-05-08', N'未免疫');

-- 6.3 示例医疗记录
IF NOT EXISTS (SELECT 1 FROM medical_records)
    INSERT INTO medical_records (animal_id, check_date, type, vet_name, weight, temperature, diagnosis, treatment) VALUES
    -- 大黄（animal_id=1）的三次记录
    (1,  '2026-01-15', N'体检', N'李医生', 12.5, 38.2, N'体表轻微寄生虫，整体健康',       N'体外驱虫处理，观察3天'),
    (1,  '2026-02-20', N'疫苗', N'王医生', 13.2, 38.5, N'健康状况良好，适合接种',         N'接种狂犬疫苗+四联疫苗第一针'),
    (1,  '2026-04-10', N'体检', N'李医生', 14.0, 38.3, N'右后腿轻微跛行，疑似运动扭伤',   N'限制活动一周，口服消炎药'),
    -- 咪咪（animal_id=4）的两次记录
    (4,  '2026-02-14', N'体检', N'李医生', 4.2,  39.1, N'呼吸道感染，伴有咳嗽、打喷嚏',   N'抗生素治疗7天，隔离观察'),
    (4,  '2026-02-22', N'手术', N'张医生', 3.9,  38.7, N'绝育手术，术后恢复中',           N'术后消炎3天，佩戴伊丽莎白圈'),
    -- 奶茶（animal_id=12）的记录
    (12, '2026-05-08', N'体检', N'王医生', 3.5,  38.4, N'皮肤真菌感染（猫癣），背部脱毛', N'外用抗真菌药膏，每周药浴');
GO


-- ====================================================================
-- 验证
-- ====================================================================
PRINT '========================================';
PRINT '  数据库实施完毕！';
PRINT '  表: 5 | 视图: 2 | 触发器: 2 | 存储过程: 1';
PRINT '  索引: 7 个 | 测试数据: 管理员1 + 动物12 + 医疗6';
PRINT '========================================';
GO
