-- Combined Pixel_Tribe Schema & Seed Data
-- Generated on 2025-06-25

-- ===== START OF MEMBER_SysAndNEWS_Sys.sql =====
-- 建立資料庫
CREATE
DATABASE IF NOT EXISTS pixel_tribe;
USE pixel_tribe;

SET
FOREIGN_KEY_CHECKS = 0;

-- 移除清除既有資料表（依 FK 先子後父）
-- 依相依關係由下而上刪除，避免 Foreign-Key Constraint 衝突
DROP TABLE IF EXISTS PRODUCT;
DROP TABLE IF EXISTS MALL_TAG;
DROP TABLE IF EXISTS COUPON;
DROP TABLE IF EXISTS COUPON_WALLET;
DROP TABLE IF EXISTS `ORDER`;
DROP TABLE IF EXISTS ORDER_ITEM;
DROP TABLE IF EXISTS PRODUCT_IMAGE;
DROP TABLE IF EXISTS FAVORITE_PRODUCT;
DROP TABLE IF EXISTS PRO_SERIAL_NUMBERS;
-- 刪除 討論區聊天室訊息檢舉 表格 --
DROP TABLE IF EXISTS FORUM_CHAT_REPORT;
-- 刪除 聊天室訊息 表格 --
DROP TABLE IF EXISTS FORUM_CHAT_MESSAGE;
-- 刪除 文章圖片 表格 --
DROP TABLE IF EXISTS FORUM_IMAGE;
-- 刪除 文章留言踩讚 表格 --
DROP TABLE IF EXISTS FORUM_MES_LIKE;
-- 刪除 文章留言檢舉 表格
DROP TABLE IF EXISTS ARTICLE_COM_REPORT;
-- 刪除 文章留言 表格 --
DROP TABLE IF EXISTS FORUM_MES;
-- 刪除 文章檢舉 表格 --
DROP TABLE IF EXISTS ARTICLE_REPORT;
-- 刪除 文章收藏 表格 --
DROP TABLE IF EXISTS POST_COLLECT;
-- 刪除 文章踩讚 表格 --
DROP TABLE IF EXISTS POST_LIKE;
-- 刪除 文章踩讚 表格 --
DROP TABLE IF EXISTS FORUM_LIKE;
-- 刪除 討論區文章 表格
DROP TABLE IF EXISTS FORUM_POST;
-- 刪除 文章類別標籤 表格 --
DROP TABLE IF EXISTS FORUM_TAG;
-- 刪除 討論區收藏 表格 --
DROP TABLE IF EXISTS FORUM_COLLECT;
-- 刪除 討論區 表格 --
DROP TABLE IF EXISTS FORUM;
-- 刪除 討論區類別 表格 --
DROP TABLE IF EXISTS FORUM_CATEGORY;
-- 刪除 檢舉類型表格 表格 --
DROP TABLE IF EXISTS REPORT_TYPE;
DROP TABLE IF EXISTS NEWS_COM_REPORT;
DROP TABLE IF EXISTS NEWS_LIKE;
DROP TABLE IF EXISTS NEWS_IMAGE;
DROP TABLE IF EXISTS NEWS_CONTENT_CLASSIFICATION;
DROP TABLE IF EXISTS NEWS_COMMENTS;
DROP TABLE IF EXISTS NEWS;
DROP TABLE IF EXISTS NEWS_CATEGORY;
DROP TABLE IF EXISTS REPORT_TYPE;
DROP TABLE IF EXISTS PRIVATE_MESSAGE;
DROP TABLE IF EXISTS PRIVATE_CHATROOM;
DROP TABLE IF EXISTS MEMBER_LOGIN_LOG;
DROP TABLE IF EXISTS FRIEND_LIST;
DROP TABLE IF EXISTS ADMINISTRATOR;
DROP TABLE IF EXISTS LOG_FAIL_REASON;
DROP TABLE IF EXISTS MEMBER;


-- 建立 核心資料表：MEMBER（會員基本資料）
CREATE TABLE MEMBER
(
    MEM_NO               INT AUTO_INCREMENT NOT NULL Comment '會員編號(PK)',
    MEM_NAME             VARCHAR(50)        NOT NULL comment '姓名',
    MEM_NICK_NAME        VARCHAR(50)        NOT NULL comment '暱稱',
    MEM_ACCOUNT          VARCHAR(50)        NOT NULL unique Comment '登入帳號',
    MEM_PASSWORD         VARCHAR(60)        NOT NULL Comment '密碼/Bcrypt',
    MEM_EMAIL            VARCHAR(100)       NOT NULL unique,
    MEM_ICON_DATA        LONGBLOB Comment '大頭貼',
    MEM_ADDR             VARCHAR(100)       NOT NULL comment '地址',
    MEM_PHONE            VARCHAR(50)        NOT NULL unique comment '行動電話',
    MEM_BIRTHDAY         DATE               NOT NULL,
    MEM_CREATE           DATETIME                    DEFAULT CURRENT_TIMESTAMP comment '建立時間',
    MEM_UPDATE           DATETIME ON UPDATE CURRENT_TIMESTAMP comment '最後修改時間',
    MEM_STATUS           CHAR(1)            NOT NULL DEFAULT '1' comment '1:正常 2:停權',
    MEM_TOKEN            VARCHAR(50)                 DEFAULT NULL comment 'Token',
    MEM_EMAIL_AUTH       VARCHAR(50)                 DEFAULT NULL comment 'email驗證碼',
    SEND_AUTH_Email_TIME DATETIME                    DEFAULT NULL comment '發送驗證信時間',
    CAN_POST             BOOLEAN            NOT NULL DEFAULT TRUE comment '可否發文',
    CAN_COMMENT          BOOLEAN            NOT NULL DEFAULT TRUE comment '可否留言',
    CAN_USED_PRO         BOOLEAN            NOT NULL DEFAULT FALSE comment '可否二手交易',
    PHONE_AUTHED         BOOLEAN            NOT NULL DEFAULT FALSE comment '手機是否驗證',
    AUTH_PROVIDER        VARCHAR(20) COMMENT '登入來源：LOCAL / GOOGLE / FACEBOOK…',
    PROVIDER_UID         VARCHAR(100) COMMENT 'Provider 回傳之唯一 ID',
    ROLE                 VARCHAR(20)        NOT NULL DEFAULT 'ROLE_USER' COMMENT 'Spring Security 角色',
    POINT                INT                         DEFAULT 0 comment '會員積分',
    CONSTRAINT MEMBER_PK PRIMARY KEY (MEM_NO)
) comment '會員資料';
-- 插入 37 筆會員資料
INSERT INTO MEMBER (MEM_NAME, MEM_NICK_NAME, MEM_ACCOUNT, MEM_PASSWORD, MEM_EMAIL,
                    MEM_ADDR, MEM_PHONE, MEM_BIRTHDAY,
                    MEM_CREATE, MEM_UPDATE, MEM_STATUS, MEM_TOKEN, MEM_EMAIL_AUTH,
                    CAN_POST, CAN_COMMENT, CAN_USED_PRO, PHONE_AUTHED, POINT)
VALUES
-- 管理員：MEM_NO = 1
('管理員', '管理員', 'admin', 'AdminPass123', 'admin@teamtest.local', '臺北市中正區行政路1號', '0912345678',
 '1980-01-01', '2024-01-10 08:15:00', '2025-05-20 10:30:00', '1', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 999),

('王小明', '幻影疾風', 'user_one', 'PaSsWoRd123', 'a1b2c3d@TJA101.com.tw', '屏東縣崁頂鄉永安路87號', '0922815837',
 '1990-05-15', '2024-02-05 09:20:00', '2024-12-15 11:45:00', '1', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('陳麗華', '冰雪薔薇', 'test_account', 'SecurePwd!', 'e4f5g6h@TJA101.com.tw', '高雄市路竹區倫理街53號', '0924206351',
 '1988-11-22', '2024-03-12 14:50:00', '2025-01-10 16:00:00', '1', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('林志偉', '烈焰戰神', 'demo_user', 'MySecret99', 'i7j8k9l@TJA101.com.tw', '臺北市大同區民族西路36號', '0939489171',
 '1995-03-01', '2024-04-18 10:05:00', '2025-02-20 09:30:00', '1', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('張雅婷', '月影潛行者', 'info_test', 'Strong@Pass', 'm0n1o2p@TJA101.com.tw', '臺北市中山區松江路76號', '0910347356',
 '1992-07-10', '2024-05-22 17:30:00', '2025-03-15 14:10:00', '1', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('李文傑', '聖光使者', 'another_user', 'Complex#123', 'q3r4s5t@TJA101.com.tw', '彰化縣大村鄉學府路56號', '0933984252',
 '2000-01-25', '2024-06-30 08:00:00', '2025-04-25 12:20:00', '1', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('黃美玲', '暗夜精靈', 'test_sample', 'SimplePass', 'u6v7w8x@TJA101.com.tw', '桃園市平鎮區廣豐街福壽四巷14號3樓',
 '0935633441', '1985-09-08', '2024-07-10 13:15:00', '2025-05-01 15:55:00', '1', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('劉俊豪', '疾風之狼', 'data_user', 'HardToGuess', 'y9z0a1b@TJA101.com.tw', '屏東縣萬丹鄉吉林路30號', '0922914555',
 '1997-04-03', '2024-08-05 11:25:00', '2025-05-10 10:40:00', '1', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('蔡淑芬', '星辰旅人', 'trial_account', 'PasswordTest', 'c2d3e4f@TJA101.com.tw', '桃園市龜山區文明一街76號',
 '0988439659', '1993-12-18', '2024-09-12 09:45:00', '2025-05-15 13:05:00', '1', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('楊宗翰', '蒼穹之鷹', 'account_test', 'MySecureKey', 'g5h6i7j@TJA101.com.tw', '苗栗縣大湖鄉信義路50號', '0987839119',
 '1989-06-30', '2024-10-01 14:00:00', '2025-06-01 09:10:00', '1', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('吳怡君', '碧海遊龍', 'tester_user', 'JustATest', 'k8l9m0n@TJA101.com.tw', '臺南市關廟區中山路２段77號', '0926380187',
 '1996-02-14', '2024-11-20 16:30:00', '2025-06-02 11:45:00', '1', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('蕭雅文', '紫電狂刀', 'alpha_user', 'AnotherPwd!23', 'o1p2q3r@TJA101.com.tw', '嘉義縣新港鄉古民街22號2樓',
 '0989121836', '1991-08-20', '2024-12-15 10:20:00', '2025-06-03 10:00:00', '1', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('徐國樑', '幽夢旅人', 'beta_tester', 'SuperSecret#01', 's4t5u6v@TJA101.com.tw', '臺中市大里區東明路52號4樓',
 '0986597364', '1987-05-03', '2025-01-05 09:15:00', '2025-06-04 12:30:00', '1', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('曾靜宜', '赤炎魔導', 'gamma_test', 'CodeWord456', 'w7x8y9z@TJA101.com.tw', '臺中市清水區鎮新南路54號8樓',
 '0953237113', '1998-11-09', '2025-01-20 11:05:00', '2025-06-04 14:50:00', '1', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('潘志明', '寒冰箭手', 'delta_info', 'TopSecretKey', 'b0c1d2e@TJA101.com.tw', '臺中市太平區振德街11號', '0971652133',
 '1994-01-17', '2025-02-10 15:40:00', '2025-06-04 16:20:00', '1', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('賴惠君', '森林遊俠', 'epsilon_user', 'NewPassword12', 'f3g4h5i@TJA101.com.tw', '臺東縣臺東市中華路１段11號',
 '0970214685', '1999-07-28', '2025-03-01 12:30:00', '2025-06-04 18:10:00', '1', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('羅建宏', '暮色吟遊者', 'zeta_sample', 'TestThisPass', 'j6k7l8m@TJA101.com.tw', '臺南市南區利南街94號', '0937847416',
 '1986-03-12', '2025-03-15 17:55:00', '2025-06-04 19:45:00', '1', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('簡佩珊', '雷霆之錘', 'eta_data', 'CheckMyPwd', 'n9o0p1q@TJA101.com.tw', '高雄市前鎮區修文街18號之13', '0934782721',
 '1992-09-25', '2025-04-05 08:00:00', '2025-06-04 20:30:00', '1', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('鍾俊彥', '幻夜舞者', 'theta_account', 'TryThisOne', 'r2s3t4u@TJA101.com.tw', '高雄市茄萣區白砂路89號之13',
 '0955328726', '2001-04-06', '2025-04-20 10:25:00', '2025-06-04 21:15:00', '1', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('杜芳瑜', '聖潔之羽', 'iota_test', 'FinalPwdCheck', 'v5w6x7y@TJA101.com.tw', '新竹縣竹北市界址15號', '0987630010',
 '1983-12-01', '2025-05-01 14:15:00', '2025-06-04 22:00:00', '1', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('洪偉誠', '影舞者', 'kappa_user', 'LastTestAccount', 'z8a9b0c@TJA101.com.tw', '臺南市安定區大同34號', '0935176482',
 '1995-06-19', '2025-05-10 16:45:00', '2025-06-04 22:45:00', '1', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('林志明', '狂暴之熊', 'user001', 'SecureUser1', 'd1e2f3g@TJA101.com.tw', '高雄市楠梓區大學二十三街20號', '0986632252',
 '1993-03-08', '2025-05-15 09:00:00', '2025-06-04 23:30:00', '1', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('王淑芬', '迷霧行者', 'account002', 'TestPass2', 'h4i5j6k@TJA101.com.tw', '臺中市潭子區頭家路36號9樓', '0914291484',
 '1988-11-15', '2025-05-20 11:10:00', '2025-06-04 23:50:00', '1', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('張俊傑', '熾天使', 'demoUser003', 'MyPwd3', 'l7m8n9o@TJA101.com.tw', '臺南市關廟區花園三街14號', '0911138891',
 '1997-07-22', '2025-05-25 13:25:00', '2025-06-05 00:10:00', '1', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('李雅婷', '冰霜女王', 'infoTest004', 'Simple123', 'p0q1r2s@TJA101.com.tw', '桃園市中壢區新建市場6號', '0968712238',
 '1990-01-05', '2025-05-30 15:30:00', '2025-06-05 00:30:00', '1', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('陳文雄', '迅捷獵豹', 'anotherUser005', 'Strong!Pass', 't3u4v5w@TJA101.com.tw', '高雄市小港區翠亨南路72號',
 '0938529405', '1995-09-12', '2025-06-01 10:45:00', '2025-06-05 00:50:00', '1', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('黃美玲', '落日餘暉', 'testSample006', 'CheckPwd4', 'x6y7z8a@TJA101.com.tw', '桃園市中壢區民溪二路90號之11',
 '0956364473', '1986-05-28', '2025-06-02 12:00:00', '2025-06-05 01:10:00', '1', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('劉俊豪', '奔雷手', 'dataUser007', 'DataUser5', 'b9c0d1e@TJA101.com.tw', '新北市中和區秀朗路３段37號4樓', '0954750566',
 '1999-04-01', '2025-06-03 14:15:00', '2025-06-05 01:30:00', '1', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('蔡怡君', '魅影刺客', 'trialAcc008', 'TrialAcc6', 'f2g3h4i@TJA101.com.tw', '桃園市中壢區中正路４段86號', '0910418815',
 '1991-12-10', '2025-06-04 08:20:00', '2025-06-05 01:50:00', '1', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('楊宗翰', '光明祭司', 'accountTest009', 'AccountTest7', 'j5k6l7m@TJA101.com.tw', '高雄市路竹區大社路58號之1',
 '0937667217', '1984-08-17', '2025-06-04 10:35:00', '2025-06-05 02:10:00', '1', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('吳佩珊', '暗影之刃', 'testerUser010', 'Tester8', 'n8o9p0q@TJA101.com.tw', '南投縣埔里鎮樹人二街58號9樓', '0935031782',
 '1998-02-25', '2025-06-04 12:45:00', '2025-06-05 02:30:00', '1', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('蕭國樑', '怒火 Berserker', 'alphaUser011', 'AlphaPwd9', 'r1s2t3u@TJA101.com.tw', '臺中市新社區興中街31號11樓',
 '0955674702', '1992-06-03', '2025-06-04 14:55:00', '2025-06-05 02:50:00', '1', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('徐靜宜', '沉靜之語', 'betaTester012', 'BetaTest10', 'v4w5x6y@TJA101.com.tw', '臺南市東山區龍鳳一街32號', '0924948942',
 '1989-10-11', '2025-06-04 16:05:00', '2025-06-05 03:10:00', '1', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('曾志明', '烈陽之弓', 'gammaTest013', 'GammaInfo11', 'z7a8b9c@TJA101.com.tw', '新北市鶯歌區宏德司法新村90號6樓之19',
 '0919629699', '1996-01-18', '2025-06-04 17:20:00', '2025-06-05 03:30:00', '1', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('潘惠君', '冰晶法師', 'deltaInfo014', 'DeltaUser12', 'd0e1f2g@TJA101.com.tw', '桃園市蘆竹區溪洲68號', '0989684649',
 '1987-09-24', '2025-06-04 18:35:00', '2025-06-05 03:50:00', '1', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('賴建宏', '荊棘之盾', 'epsilonUser015', 'EpsilonAcc13', 'h3i4j5k@TJA101.com.tw', '新北市瑞芳區洞頂路87號之11',
 '0937887702', '2000-05-01', '2025-06-04 19:45:00', '2025-06-05 04:10:00', '1', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('羅雅文', '星夜呢喃', 'zetaSample016', 'ZetaSample14', 'l6m7n8o@TJA101.com.tw', '臺中市清水區臨海路95號之7',
 '0913802841', '1994-11-07', '2025-06-04 20:55:00', '2025-06-05 04:30:00', '1', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0);

CREATE TABLE LOG_FAIL_REASON
(
    LOG_FAIL_REASON_NO INT AUTO_INCREMENT NOT NULL COMMENT '失敗原因編號',
    LOG_FAIL_TYPE      VARCHAR(50)        NOT NULL COMMENT '失敗原因種類',
    PRIMARY KEY (LOG_FAIL_REASON_NO)
) COMMENT ='登入失敗原因';

INSERT INTO LOG_FAIL_REASON(LOG_FAIL_TYPE)
VALUES ('帳號密碼錯誤'),
       ('停權');

CREATE TABLE ADMINISTRATOR
(
    ADM_NO       INT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '管理員編號',
    ADM_ACCOUNT  VARCHAR(50)                    NOT NULL COMMENT '管理員帳號',
    ADM_NAME     VARCHAR(50)                    NOT NULL COMMENT '管理員名稱',
    ADM_PASSWORD VARCHAR(60)                    NOT NULL COMMENT '管理員密碼/Bcrypt',
    CREATE_TIME  DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    ROLE         VARCHAR(20) DEFAULT 'ROLE_ADMIN',
    ADM_PROFILE  LONGBLOB COMMENT '管理員頭像'
) COMMENT '管理員';

-- 填入假資料
INSERT INTO ADMINISTRATOR (ADM_ACCOUNT, ADM_NAME, ADM_PASSWORD)
VALUES ('chienyu749@gmail.com', '黃健祐', '123456'),
       ('akueiyh@gmail.com', '許政逵', '123456'),
       ('a84052294@gmail.com', '高于雯', '123456'),
       ('joanna.yu0820@gmail.com', '林薰妤', '123456'),
       ('xiang10220619@gmail.com', '林玉祥', '123456'),
       ('jimmy00tw@gmail.com', '魏君珉', '123456');

CREATE TABLE FRIEND_LIST
(
    FRILIST_NO     INT PRIMARY KEY AUTO_INCREMENT COMMENT '好友關係編號',
    MEMBER_1       INT         NOT NULL COMMENT '會員1編號',
    MEMBER_2       INT         NOT NULL COMMENT '會員2編號',
    FRILIST_STATUS VARCHAR(20) NOT NULL COMMENT '狀態',
    SEND_TIME      DATETIME COMMENT '申請時間',
    RESPOND_TIME   DATETIME COMMENT '回應時間',

-- 外來鍵設定
    FOREIGN KEY (MEMBER_1) REFERENCES MEMBER (MEM_NO),
    FOREIGN KEY (MEMBER_2) REFERENCES MEMBER (MEM_NO)
) COMMENT '好友關係';

INSERT INTO FRIEND_LIST (MEMBER_1, MEMBER_2, FRILIST_STATUS, SEND_TIME, RESPOND_TIME)
VALUES (22, 34, '待同意', '2024-03-08 01:30:56', Null),
       (11, 23, '待同意', '2023-02-26 13:25:53', Null),
       (14, 6, '不同意', '2025-03-19 06:51:22', '2025-03-22 06:51:22'),
       (23, 20, '待同意', '2023-06-19 10:55:07', NULL),
       (4, 17, '待同意', '2025-04-05 06:23:30', NULL),
       (24, 1, '待同意', '2025-02-22 01:18:52', NULL),
       (9, 19, '不同意', '2022-09-11 09:29:27', '2022-09-19 09:29:27'),
       (33, 37, '待同意', '2022-10-27 08:12:39', NULL),
       (22, 23, '不同意', '2022-02-09 23:44:11', '2022-02-11 23:44:11'),
       (9, 16, '不同意', '2022-11-21 18:55:58', '2022-11-24 18:55:58'),
       (8, 30, '同意', '2024-05-13 06:51:24', '2024-05-14 06:51:24'),
       (6, 5, '同意', '2025-05-12 19:24:48', '2025-05-13 19:24:48'),
       (23, 5, '同意', '2025-01-12 10:35:32', '2025-01-12 10:35:32'),
       (1, 25, '同意', '2024-01-04 21:57:03', '2024-01-12 21:57:03'),
       (5, 30, '不同意', '2023-09-03 06:59:11', '2023-09-05 06:59:11'),
       (34, 6, '同意', '2023-12-05 06:50:06', '2023-12-11 06:50:06'),
       (5, 26, '不同意', '2022-09-10 12:33:37', '2022-09-18 12:33:37'),
       (18, 7, '同意', '2023-03-23 04:59:10', '2023-04-01 04:59:10'),
       (26, 16, '同意', '2022-02-08 21:49:14', '2022-02-12 21:49:14'),
       (32, 35, '同意', '2023-10-29 05:57:45', '2023-10-30 05:57:45'),
       (20, 30, '待同意', '2024-08-26 10:36:07', NULL),
       (35, 12, '同意', '2022-03-27 19:00:25', '2022-04-05 19:00:25'),
       (15, 4, '待同意', '2025-01-17 07:42:31', NULL),
       (12, 32, '不同意', '2022-08-26 20:47:56', '2022-08-29 20:47:56'),
       (21, 28, '待同意', '2023-10-12 01:47:48', NULL),
       (27, 24, '同意', '2024-05-03 21:23:44', '2024-05-10 21:23:44'),
       (5, 10, '同意', '2024-11-09 16:18:32', '2024-11-13 16:18:32'),
       (18, 15, '待同意', '2022-02-12 04:53:06', NULL),
       (20, 18, '同意', '2024-01-21 10:24:03', '2024-01-29 10:24:03'),
       (8, 24, '待同意', '2023-07-31 14:29:09', NULL),
       (24, 8, '待同意', '2022-03-10 15:18:58', NULL),
       (32, 31, '同意', '2022-07-12 21:55:54', '2022-07-18 21:55:54'),
       (17, 12, '不同意', '2024-02-07 12:05:06', '2024-02-07 12:05:06'),
       (30, 10, '待同意', '2024-08-12 15:30:09', NULL),
       (36, 10, '同意', '2024-03-07 22:37:23', '2024-03-09 22:37:23'),
       (11, 6, '不同意', '2025-02-06 22:06:28', '2025-02-06 22:06:28'),
       (8, 36, '待同意', '2024-11-25 08:18:16', NULL),
       (18, 15, '同意', '2022-02-03 07:11:48', '2022-02-03 07:11:48'),
       (27, 8, '不同意', '2024-12-09 07:06:10', '2024-12-17 07:06:10'),
       (32, 21, '待同意', '2022-04-05 15:11:48', NULL),
       (20, 27, '同意', '2022-05-10 01:25:57', '2022-05-12 01:25:57'),
       (15, 18, '不同意', '2024-03-06 01:37:44', '2024-03-08 01:37:44'),
       (12, 5, '待同意', '2024-01-24 23:56:39', NULL),
       (6, 36, '同意', '2022-01-14 22:17:01', '2022-01-23 22:17:01'),
       (22, 32, '待同意', '2024-02-11 06:35:30', NULL),
       (11, 18, '同意', '2025-03-29 06:13:24', '2025-04-02 06:13:24'),
       (6, 13, '同意', '2024-10-31 21:06:13', '2024-11-01 21:06:13'),
       (5, 4, '待同意', '2022-05-28 04:40:33', NULL),
       (27, 30, '同意', '2022-06-10 05:17:50', '2022-06-11 05:17:50'),
       (19, 32, '待同意', '2025-04-11 13:07:02', NULL);

CREATE TABLE MEMBER_LOGIN_LOG
(
    LOG_NO             INT AUTO_INCREMENT NOT NULL COMMENT '紀錄編號',
    MEM_NO             INT                NOT NULL COMMENT '會員編號',
    LOG_TIME           DATETIME           NOT NULL COMMENT '登入時間',
    LOG_IP             VARCHAR(50)        NOT NULL COMMENT '登入IP地址',
    LOG_BROWSER        VARCHAR(50)        NOT NULL COMMENT '登入瀏覽器',
    LOG_EQUIP          VARCHAR(50)        NOT NULL COMMENT '登入裝置',
    LOG_SUCCESS        BOOLEAN            NOT NULL COMMENT '是否成功',
    LOG_FAIL_REASON_NO INT COMMENT '失敗原因編號',
    PRIMARY KEY (LOG_NO),
    FOREIGN KEY (MEM_NO) REFERENCES MEMBER (MEM_NO),
    FOREIGN KEY (LOG_FAIL_REASON_NO) REFERENCES LOG_FAIL_REASON (LOG_FAIL_REASON_NO)
) COMMENT ='會員登入紀錄';

INSERT INTO MEMBER_LOGIN_LOG(MEM_NO, LOG_TIME, LOG_IP, LOG_BROWSER, LOG_EQUIP, LOG_SUCCESS, LOG_FAIL_REASON_NO)
VALUES (6, '2024-08-31 20:59:01', '192.168.103.114', 'Edge', 'Windows PC', 1, null),
       (9, '2025-02-21 09:26:01', '192.168.140.204', 'Opera', 'iPhone', 1, null),
       (25, '2025-03-04 08:17:01', '192.168.23.41', 'Firefox', 'Tablet', 0, 1),
       (14, '2024-07-13 09:23:01', '192.168.211.180', 'Firefox', 'Tablet', 1, null),
       (34, '2025-01-28 12:26:01', '192.168.67.90', 'Chrome', 'Android Phone', 1, null),
       (12, '2024-10-04 07:40:01', '192.168.211.139', 'Chrome', 'iPhone', 1, null),
       (30, '2024-09-22 03:41:01', '192.168.194.42', 'Firefox', 'MacBook', 1, null),
       (11, '2025-03-19 23:49:01', '192.168.186.68', 'Firefox', 'Windows PC', 1, null),
       (32, '2024-06-07 05:20:01', '192.168.43.192', 'Safari', 'MacBook', 0, 1),
       (28, '2024-09-16 10:48:01', '192.168.43.192', 'Chrome', 'iPhone', 1, null),
       (1, '2024-07-02 08:09:01', '192.168.13.15', 'Firefox', 'MacBook', 0, 2),
       (23, '2025-04-02 23:35:01', '192.168.215.43', 'Edge', 'MacBook', 1, null),
       (19, '2024-08-19 07:11:01', '192.168.230.10', 'Opera', 'Tablet', 1, null),
       (12, '2025-05-15 14:11:01', '192.168.131.86', 'Firefox', 'MacBook', 1, null),
       (6, '2025-04-23 20:21:01', '192.168.192.123', 'Opera', 'Tablet', 1, null),
       (16, '2025-05-07 18:31:01', '192.168.140.204', 'Safari', 'Android Phone', 0, 1),
       (26, '2024-06-09 04:41:01', '192.168.24.166', 'Opera', 'Tablet', 0, 2),
       (1, '2024-08-27 11:52:01', '192.168.64.79', 'Safari', 'Tablet', 0, 2),
       (2, '2025-04-26 18:46:01', '192.168.53.171', 'Edge', 'Tablet', 1, null),
       (3, '2024-09-01 15:37:01', '192.168.45.227', 'Safari', 'Android Phone', 1, null),
       (5, '2025-03-16 18:20:01', '192.168.224.117', 'Chrome', 'Windows PC', 0, 2),
       (4, '2025-04-08 17:44:01', '192.168.67.90', 'Safari', 'MacBook', 0, 2),
       (28, '2024-07-09 13:59:01', '192.168.23.41', 'Opera', 'Android Phone', 0, 2),
       (28, '2025-04-11 13:07:01', '192.168.126.37', 'Chrome', 'iPhone', 1, null),
       (13, '2024-06-26 18:02:01', '192.168.40.42', 'Edge', 'MacBook', 1, null),
       (19, '2024-11-13 21:40:01', '192.168.53.171', 'Edge', 'iPhone', 1, null),
       (21, '2024-07-27 15:37:01', '192.168.237.201', 'Safari', 'Tablet', 0, 1),
       (19, '2025-02-27 16:45:01', '192.168.211.139', 'Firefox', 'MacBook', 1, null),
       (8, '2024-10-22 01:27:01', '192.168.191.118', 'Firefox', 'MacBook', 1, null),
       (1, '2025-04-15 14:34:01', '192.168.59.109', 'Opera', 'iPhone', 0, 1),
       (22, '2024-08-20 12:18:01', '192.168.218.246', 'Safari', 'iPhone', 1, null),
       (20, '2024-12-07 19:51:01', '192.168.211.12', 'Firefox', 'Tablet', 1, null),
       (27, '2025-02-19 08:10:01', '192.168.145.42', 'Safari', 'Windows PC', 1, null),
       (5, '2024-10-25 19:30:01', '192.168.215.43', 'Safari', 'Windows PC', 1, null),
       (33, '2025-03-19 03:48:01', '192.168.98.156', 'Edge', 'iPhone', 1, null),
       (16, '2024-11-17 06:33:01', '192.168.140.204', 'Firefox', 'Android Phone', 1, null),
       (11, '2025-01-09 11:54:01', '192.168.218.246', 'Chrome', 'iPhone', 0, 2),
       (8, '2025-01-12 15:48:01', '192.168.39.204', 'Opera', 'Tablet', 1, null),
       (33, '2025-03-22 23:37:01', '192.168.65.13', 'Safari', 'MacBook', 1, null),
       (21, '2025-04-25 22:01:01', '192.168.67.90', 'Safari', 'Windows PC', 0, 2),
       (1, '2025-04-06 12:57:01', '192.168.154.215', 'Safari', 'Tablet', 1, null),
       (29, '2025-01-27 10:23:01', '192.168.98.156', 'Chrome', 'Android Phone', 0, 2),
       (8, '2024-09-03 05:22:01', '192.168.52.42', 'Edge', 'Windows PC', 1, null),
       (17, '2025-01-01 17:53:01', '192.168.246.111', 'Safari', 'iPhone', 0, 2),
       (25, '2025-03-03 09:08:01', '192.168.243.173', 'Safari', 'Android Phone', 1, null),
       (29, '2024-06-16 04:59:01', '192.168.84.164', 'Edge', 'Windows PC', 0, 2),
       (4, '2024-07-01 05:26:01', '192.168.68.100', 'Opera', 'Android Phone', 0, 1),
       (3, '2024-12-29 17:03:01', '192.168.11.43', 'Firefox', 'Tablet', 0, 1),
       (15, '2024-09-10 14:49:01', '192.168.192.123', 'Edge', 'Android Phone', 0, 2),
       (2, '2025-05-14 10:24:01', '192.168.194.42', 'Firefox', 'Android Phone', 1, null);

CREATE TABLE PRIVATE_CHATROOM
(
    PRIROOM_NO  INT AUTO_INCREMENT NOT NULL COMMENT '聊天室編號',
    MEMBER1     INT                NOT NULL COMMENT '會員1編號',
    MEMBER2     INT                NOT NULL COMMENT '會員2編號',
    CREATE_TIME DATETIME COMMENT '創建時間',
    PRI_STATUS  CHAR(1) COMMENT '聊天室狀態(開啟:1,關閉:2)',
    PRIMARY KEY (PRIROOM_NO),
    FOREIGN KEY (MEMBER1) REFERENCES MEMBER (MEM_NO),
    FOREIGN KEY (MEMBER2) REFERENCES MEMBER (MEM_NO)
) COMMENT ='私人聊天室';

INSERT INTO PRIVATE_CHATROOM (MEMBER1, MEMBER2, CREATE_TIME, PRI_STATUS)
VALUES (23, 20, '2024-07-15 14:23:00', '1'),
       (8, 12, '2024-09-01 09:45:00', '2'),
       (11, 14, '2024-11-10 17:30:00', '1'),
       (34, 30, '2024-06-22 21:15:00', '1'),
       (30, 9, '2025-01-03 12:00:00', '2'),
       (25, 36, '2024-08-12 10:05:00', '1'),
       (14, 24, '2024-10-18 13:45:00', '1'),
       (12, 36, '2024-12-05 08:30:00', '2'),
       (4, 21, '2024-07-28 20:00:00', '1'),
       (36, 8, '2024-06-30 18:20:00', '1'),
       (18, 8, '2024-11-25 16:10:00', '2'),
       (21, 33, '2024-09-15 07:55:00', '1'),
       (25, 19, '2024-10-30 11:40:00', '1'),
       (18, 24, '2024-08-03 22:10:00', '2'),
       (17, 16, '2024-12-12 15:50:00', '1'),
       (32, 29, '2025-02-09 19:00:00', '1'),
       (4, 20, '2024-07-20 06:35:00', '2'),
       (12, 13, '2024-09-25 10:25:00', '1'),
       (17, 9, '2025-01-14 23:10:00', '2'),
       (15, 3, '2024-11-01 14:30:00', '1'),
       (8, 24, '2024-06-18 16:45:00', '1'),
       (30, 14, '2024-08-20 13:15:00', '2'),
       (15, 18, '2024-10-05 17:00:00', '1'),
       (16, 14, '2024-07-02 08:20:00', '1'),
       (32, 12, '2024-09-12 12:10:00', '2'),
       (27, 24, '2024-12-20 11:25:00', '1'),
       (7, 35, '2024-06-10 09:50:00', '2'),
       (11, 5, '2025-01-28 10:15:00', '1'),
       (24, 18, '2024-08-07 19:40:00', '1'),
       (20, 4, '2024-09-17 22:30:00', '2'),
       (8, 20, '2024-07-25 14:00:00', '1'),
       (19, 6, '2024-10-09 18:05:00', '1'),
       (24, 21, '2024-11-30 07:45:00', '2'),
       (32, 7, '2024-06-14 15:10:00', '1'),
       (8, 30, '2024-08-29 16:25:00', '1'),
       (28, 6, '2024-09-08 20:15:00', '2'),
       (22, 14, '2024-12-01 21:00:00', '1'),
       (8, 27, '2024-06-24 10:50:00', '1'),
       (10, 28, '2024-07-12 13:35:00', '2'),
       (36, 15, '2024-08-18 11:45:00', '1'),
       (10, 16, '2025-01-06 12:20:00', '2'),
       (8, 35, '2024-09-28 08:10:00', '1'),
       (32, 30, '2024-10-14 14:55:00', '1'),
       (28, 37, '2024-07-08 17:50:00', '2'),
       (1, 5, '2024-11-20 06:40:00', '1'),
       (15, 20, '2024-06-05 23:30:00', '1'),
       (4, 7, '2024-08-25 15:00:00', '2'),
       (12, 3, '2024-09-22 16:20:00', '1'),
       (13, 8, '2024-10-21 19:35:00', '1'),
       (33, 9, '2024-12-18 09:00:00', '2');

CREATE TABLE PRIVATE_MESSAGE
(
    PRIVATE_MES_NO INT PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '聊天室訊息編號',
    PRIROOM_NO     INT COMMENT '聊天室編號',
    SENDER_NO      INT COMMENT '發送人',
    CONTENT        VARCHAR(5000) COMMENT '訊息內容',
    SEND_AT        DATETIME COMMENT '發送時間',
    IS_DEL         BOOLEAN COMMENT '是否刪除訊息',
    FOREIGN KEY (PRIROOM_NO) REFERENCES PRIVATE_CHATROOM (PRIROOM_NO),
    FOREIGN KEY (SENDER_NO) REFERENCES MEMBER (MEM_NO)
) COMMENT ='私人訊息';

INSERT INTO PRIVATE_MESSAGE(PRIVATE_MES_NO, PRIROOM_NO, SENDER_NO, CONTENT, SEND_AT, IS_DEL)
VALUES (1, 5, 16, '你的簡報很棒！', '2025-05-14 06:32:41', 1),
       (2, 3, 3, '謝謝你幫我那件事', '2025-05-25 09:05:41', 1),
       (3, 8, 36, '我等等要出門了', '2025-05-27 09:36:41', 0),
       (4, 2, 18, '你的簡報很棒！', '2025-05-13 15:15:41', 0),
       (5, 5, 17, '收到你的訊息了', '2025-05-14 13:28:41', 1),
       (6, 1, 31, '剛剛開會提到你', '2025-05-20 15:32:41', 1),
       (7, 1, 11, '收到你的訊息了', '2025-05-15 19:09:41', 0),
       (8, 8, 33, '這週末有空嗎？', '2025-05-29 12:26:41', 0),
       (9, 3, 6, '我剛剛看到你的訊息了。', '2025-06-04 11:49:41', 0),
       (10, 7, 13, '這部電影很好看推薦給你', '2025-05-22 08:35:41', 0),
       (11, 5, 28, '週五有空我們聚一聚吧', '2025-05-19 05:20:41', 0),
       (12, 6, 1, '我剛剛看到你的訊息了。', '2025-05-31 06:25:41', 0),
       (13, 10, 11, '小心今天會下雨記得帶傘', '2025-05-29 10:49:41', 1),
       (14, 1, 31, '你的簡報很棒！', '2025-05-31 05:05:41', 0),
       (15, 4, 34, '可以借我一下筆記嗎？', '2025-05-10 10:08:41', 0),
       (16, 10, 30, '這週超級忙', '2025-05-21 10:44:41', 0),
       (17, 3, 20, '明天見！', '2025-05-18 02:50:41', 0),
       (18, 1, 22, '可以借我一下筆記嗎？', '2025-05-31 01:34:41', 0),
       (19, 7, 2, '我剛剛看到你的訊息了。', '2025-05-17 13:07:41', 0),
       (20, 7, 18, '明天見！', '2025-05-11 10:41:41', 1),
       (21, 9, 15, '剛剛開會提到你', '2025-05-12 02:16:41', 0),
       (22, 5, 20, '你覺得這份報告怎麼樣？', '2025-05-26 23:01:41', 0),
       (23, 3, 18, '要不要一起去吃飯？', '2025-05-17 20:17:41', 0),
       (24, 2, 16, '你今天心情如何？', '2025-05-24 21:54:41', 0),
       (25, 6, 36, '我剛剛看到你的訊息了。', '2025-05-15 15:08:41', 1),
       (26, 9, 16, '這週末有空嗎？', '2025-05-12 21:18:41', 0),
       (27, 8, 17, '晚點回你', '2025-06-07 16:07:41', 1),
       (28, 9, 24, '你今天心情如何？', '2025-05-10 05:26:41', 1),
       (29, 9, 28, '晚點回你', '2025-05-20 02:20:41', 1),
       (30, 5, 12, '我剛剛看到你的訊息了。', '2025-05-25 12:02:41', 0);


-- NEWS System
-- 建立 NEWS_CATEGORY 資料表（含完整欄位）
CREATE TABLE NEWS_CATEGORY
(
    NCAT_NO   INT AUTO_INCREMENT NOT NULL comment '新聞類別編號',
    NCAT_NAME VARCHAR(50)        NOT NULL comment '類別名稱',
    CONSTRAINT NEWS_CATEGORY_PK PRIMARY KEY (NCAT_NO)
) AUTO_INCREMENT = 1 COMMENT ='新聞類別';
-- 新增新聞類別資料 --
INSERT INTO NEWS_CATEGORY (NCAT_NAME)
VALUES ('PC'),
       ('PS5'),
       ('Switch'),
       ('Xbox');


-- 建立檢舉類型表格 --
-- CREATE TABLE REPORT_TYPE
-- (
--     `RPI_NO`   INT AUTO_INCREMENT NOT NULL COMMENT '檢舉類型編號',
--     `RPI_TYPE` VARCHAR(255)       NOT NULL COMMENT '檢舉類型項目',
--     Constraint RPI_PK PRIMARY KEY (RPI_NO)
-- ) COMMENT '檢舉類型';
-- -- 新增檢舉類型資料 --
-- INSERT INTO REPORT_TYPE (`RPI_TYPE`)
-- VALUES ('與本版主題無關'),
--        ('洗版'),
--        ('謾罵'),
--        ('限制級'),
--        ('詐騙、詐欺或不實資訊'),
--        ('其他');


-- 建立內容主表：NEWS（新聞）
CREATE TABLE NEWS
(
    NEWS_NO     INT AUTO_INCREMENT NOT NULL comment '新聞編號(PK)',
    NEWS_TIT    VARCHAR(255)       NOT NULL comment '新聞標題',
    NEWS_CON    VARCHAR(9000)      NOT NULL comment '新聞內容',
    NEWS_UPDATE DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '最後更新時間，預設為 NULL',
    NEWS_CRDATE DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '自動填入資料插入時間',
    IS_SHOWED   BOOLEAN            NOT NULL DEFAULT true COMMENT 'true:顯示 false:不顯示',
    MEM_NO      INT                NOT NULL DEFAULT 1 COMMENT '預設管理員 1',
    ADMIN_NO    INT                NOT NULL DEFAULT 1 comment '給Adiminstor 預設為1',
    CONSTRAINT NEWS_PK PRIMARY KEY (NEWS_NO),
    CONSTRAINT NEWS_FK_MEMBER FOREIGN KEY (MEM_NO) REFERENCES MEMBER (MEM_NO),
    CONSTRAINT NEWS_FK_ADMINISTOR FOREIGN KEY (ADMIN_NO) REFERENCES ADMINISTRATOR (ADM_NO)
) AUTO_INCREMENT = 1 comment ='新聞主表';
-- 插入 50 筆假資料，其中部分 NEWS_UPDATE 設為 NULL
-- 先插入5筆
INSERT INTO NEWS (NEWS_TIT, NEWS_CON, NEWS_UPDATE, NEWS_CRDATE)
VALUES ('PS5 獨佔大作《貓咪大亂鬥》宣布延期！原因竟是貓咪演員太難搞？',
        '開門見山，喵電感應娛樂今日正式說明，《貓咪大亂鬥》原訂六月底推出但不得不延後。團隊坦言，雖然早已為貓咪角色量身打造高階動作捕捉流程，但實際執行時才發現「喵星人心情陰晴不定」，導致多數鏡頭無法一次完成。開發者被迫反覆重拍、調整骨架權重與動畫曲線，排程瞬間向後滑動數週。目前官方正與專業動物訓練師合作，並加派 AI 動補後製人力，希望確保遊戲品質無慮後再公布確切上市時間。團隊強調：「我們絕不釋出半成品，請玩家放心等待，也感謝各位耐心支持！」',
        '2025-06-01 10:00:00', '2025-05-30 09:00:00'),

       ('Steam 夏季特賣開跑！錢包君表示：我還沒準備好啊！',
        'Steam 夏季特賣今日凌晨全面上線，數千款作品祭出 10% 至 90% 不等折扣，活動將為期兩週。Valve 指出，本次特賣特別設定多段「驚喜閃促」時段，價格可能再度下探，呼籲玩家先把心儀遊戲加入願望清單並開啟通知，以免錯失最低點。同時，平台也推出「購物節理財提示」小工具，協助使用者設定每日預算上限，避免一時衝動造成帳單壓力。開發者也同步鼓勵玩家趁折扣體驗獨立佳作，「支持創作者、理性消費」成為今年特賣主軸。',
        '2025-06-02 11:00:00', '2025-06-01 08:30:00'),

       ('獨立遊戲《時空紙牌》登上排行榜前十名！',
        '僅由三人小組開發的《時空紙牌》在上架第二週即闖入 Steam 全球暢銷榜 Top 10，刷新台灣獨立遊戲紀錄。遊戲結合復古像素畫面與卡牌穿梭關卡機制，玩家需在有限回合內操控紙牌，改變角色所處年代以解謎闖關。團隊受訪時表示，開發期間面臨資金緊縮，但透過社群回饋靈活調整難度曲線，終於在正式版獲得海外實況主關注，帶動爆炸式口碑。未來計畫推出免費 DLC，新增「古文明篇」與排行榜爬分模式，並考慮移植 Switch 平台，讓更多玩家體驗「時間‧策略‧懷舊」的魅力。',
        NULL, '2025-06-04 10:00:00'),

       ('遊戲週邊商品瘋搶！限定款爆賣斷貨！',
        '《星辰幻境》官方在凌晨零點開放的周邊預購活動僅三分鐘即全數售罄，尤其限定款發光水晶吊飾與角色 PVC 模型最受歡迎，網站瞬間湧入超過二十萬名粉絲，伺服器一度當機。營運團隊隨即公告將追加生產並改善流量分散機制，同時推出「第二波預購排隊碼」以確保公平性。官方提醒粉絲留意詐騙連結，切勿透過未經授權渠道購買高價炒作品。品牌負責人表示，後續將考慮採取「預定量產」模式降低炒賣空間，也會在製程與品質控管上再加碼，確保每一份商品都能安全送達玩家手中。',
        '2025-06-05 15:30:00', '2025-06-04 11:00:00'),

       ('知名實況主轉戰 YouTube Gaming！',
        'Twitch 百萬粉「阿宅勇者」昨晚在直播尾聲突然宣佈，將於下週起全職移師 YouTube Gaming，並同步開設 Shorts、VOD 與會員專屬內容。原因除了合約到期，也因新平台提供更完整長短片整合以及 AI 字幕優化，能讓海外觀眾更容易追看。阿宅勇者承諾現有訂閱者福利不打折，並預留一週轉場期協助粉絲完成帳號遷移。對此，YouTube Gaming 亞太區負責人亦在社群發短片表達歡迎。「跨平台經營是趨勢，不變的是我們陪伴粉絲的初心。」阿宅勇者最後鼓勵觀眾持續支持內容創作者，共同打造更活躍的遊戲社群。',
        '2025-06-05 16:00:00', '2025-06-04 12:00:00');


-- 建立 NEWS_IMAGE：新聞圖片（多對一）
CREATE TABLE NEWS_IMAGE
(
    IMG_NO   INT AUTO_INCREMENT NOT NULL,
    IMG_URL  VARCHAR(1000) comment '圖片路徑',
    NEWS_NO  INT                NOT NULL,
    IMG_TYPE VARCHAR(100) comment 'MIME Type,例如image/png',
    CONSTRAINT NEWS_IMAGE_PK PRIMARY KEY (IMG_NO),
    CONSTRAINT NEWS_IMAGE_FK_NEWS FOREIGN KEY (NEWS_NO) REFERENCES NEWS (NEWS_NO)
) AUTO_INCREMENT = 1 comment '新聞圖片';


-- 建立 NEWS_COMMENTS：新聞留言
CREATE TABLE NEWS_COMMENTS
(
    NCOM_NO       INT AUTO_INCREMENT NOT NULL,
    NCOM_CON      VARCHAR(4000)      NOT NULL comment '留言內容',
    NCOM_CRE      DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '自動填入插入當下時間,留言時間',
    NCOM_STATUS   CHAR(1)            NOT NULL DEFAULT '1' COMMENT '1:正常 2:違規，預設為正常',
    NEWS_NO       INT                NOT NULL,
    MEM_NO        INT                NOT NULL,
    NCOM_LIKE_LC  INT                NOT NULL DEFAULT 0 comment '按讚',
    NCOM_LIKE_DLC INT                NOT NULL DEFAULT 0 comment '倒讚',
    CONSTRAINT NEWS_COMMENTS_PK PRIMARY KEY (NCOM_NO),
    CONSTRAINT NEWS_COMMENTS_FK_NEWS FOREIGN KEY (NEWS_NO) REFERENCES NEWS (NEWS_NO),
    CONSTRAINT NEWS_COMMENTS_FK_MEMBER FOREIGN KEY (MEM_NO) REFERENCES MEMBER (MEM_NO)
) AUTO_INCREMENT = 1 comment '新聞留言';
-- 插入前5則新聞的留言
INSERT INTO NEWS_COMMENTS (NCOM_CON, NCOM_CRE, NCOM_STATUS, NEWS_NO, MEM_NO, NCOM_LIKE_LC, NCOM_LIKE_DLC)
VALUES
    -- 新聞 1：PS5《貓咪大亂鬥》延期
    ('貓咪本來就只做牠想做的事，開發團隊辛苦了！', '2025-05-30 10:26:00', '1', 1, 2, 3, 0),
    ('動捕遇到貓皇超級爆笑，期待更精緻的最終成品！', '2025-05-30 10:39:00', '1', 1, 3, 5, 1),
    ('延期無妨，只要品質到位，一定全力支持。', '2025-05-30 09:33:00', '1', 1, 4, 4, 5),
    ('拜託附贈罐罐 DLC 補償玩家吧！', '2025-05-30 09:18:00', '1', 1, 5, 5, 0),
    -- 新聞 2：Steam 夏季特賣
    ('夏特賣一到，願望清單瞬間膨脹！', '2025-06-01 09:50:00', '1', 2, 6, 5, 0),
    ('錢包表示：為什麼受傷的總是我……', '2025-06-01 08:38:00', '1', 2, 7, 2, 1),
    ('提醒各位先看評價再下手，理性消費！', '2025-06-01 09:04:00', '1', 2, 8, 5, 4),
    ('剛買完又特價，真的會心碎。', '2025-06-01 08:38:00', '1', 2, 9, 5, 1),
    ('Valve 每年都精準收割，太可怕了！', '2025-06-01 10:06:00', '1', 2, 10, 5, 5),
    -- 新聞 3：獨立遊戲《時空紙牌》
    ('三人團隊闖進前十，真心佩服！', '2025-06-04 11:14:00', '1', 3, 11, 5, 1),
    ('像素風＋時間卡牌機制超有創意，已加入收藏。', '2025-06-04 11:02:00', '1', 3, 12, 5, 2),
    ('希望之後推出中文介面，方便更多玩家。', '2025-06-04 11:48:00', '1', 3, 13, 0, 1),
    ('已入手！支持獨立開發者加油！', '2025-06-04 11:34:00', '1', 3, 14, 5, 2),
    -- 新聞 4：遊戲週邊限定款斷貨
    ('限定款根本秒殺，機器人又贏了。', '2025-06-04 11:40:00', '1', 4, 15, 4, 1),
    ('求官方加開預購，不要讓黃牛得逞！', '2025-06-04 12:42:00', '1', 4, 16, 5, 0),
    ('錢早準備好卻只看到已售完，心碎。', '2025-06-04 11:42:00', '1', 4, 17, 5, 2),
    ('官方商店崩潰，我刷新半小時才進去。', '2025-06-04 13:11:00', '1', 4, 18, 1, 1),
    ('希望下一波能有更多庫存，讓大家都買得到。', '2025-06-04 12:59:00', '1', 4, 19, 5, 0),
    -- 新聞 5：知名實況主轉戰 YouTube Gaming
    ('恭喜主播升級平台，期待新內容！', '2025-06-04 12:23:00', '1', 5, 20, 5, 3),
    ('YouTube 畫質穩定，對觀眾來說是好事。', '2025-06-04 12:35:00', '1', 5, 21, 5, 2),
    ('訂閱已加一，全力支持！', '2025-06-04 13:47:00', '1', 5, 22, 5, 1),
    ('綠色傘起飛，Twitch 加油！', '2025-06-04 13:05:00', '1', 5, 23, 7, 0);


-- 建立 NEWS_LIKE：留言按讚 / 倒讚
CREATE TABLE NEWS_LIKE
(
    NLIKE_NO     INT AUTO_INCREMENT NOT NULL,
    NLIKE_STATUS CHAR(1)            NOT NULL DEFAULT '1' COMMENT '1:正常 2:讚 3:倒讚 ',
    MEM_NO       INT                NOT NULL comment '操作人',
    NLIKE_UPDATE DATETIME           NOT NULL default current_timestamp ON UPDATE CURRENT_TIMESTAMP comment '最後操作時間',
    NLIKE_CRDATE DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '自動填入插入當下時間,建立時間',
    NCOM_NO      INT                NOT NULL comment '對應留言',
    CONSTRAINT NEWS_LIKE_PK PRIMARY KEY (NLIKE_NO),
    CONSTRAINT NEWS_LIKE_FK_MEMBER FOREIGN KEY (MEM_NO) REFERENCES MEMBER (MEM_NO),
    CONSTRAINT NEWS_LIKE_FK_COMMENT FOREIGN KEY (NCOM_NO) REFERENCES NEWS_COMMENTS (NCOM_NO)
) AUTO_INCREMENT = 1 comment '留言讚/倒讚';
-- 先插入前5篇新聞中的留言 讚倒讚
INSERT INTO NEWS_LIKE (NLIKE_STATUS, MEM_NO, NLIKE_UPDATE, NLIKE_CRDATE, NCOM_NO)
VALUES ('2', 2, '2025-05-30 10:27:00', '2025-05-30 10:27:00', 1),
       ('2', 3, '2025-05-30 10:27:00', '2025-05-30 10:27:00', 1),
       ('2', 4, '2025-05-30 10:27:00', '2025-05-30 10:27:00', 1),
       ('2', 5, '2025-05-30 10:40:00', '2025-05-30 10:40:00', 2),
       ('2', 6, '2025-05-30 10:40:00', '2025-05-30 10:40:00', 2),
       ('2', 7, '2025-05-30 10:40:00', '2025-05-30 10:40:00', 2),
       ('2', 8, '2025-05-30 10:40:00', '2025-05-30 10:40:00', 2),
       ('2', 9, '2025-05-30 10:40:00', '2025-05-30 10:40:00', 2),
       ('3', 2, '2025-05-30 10:41:00', '2025-05-30 10:41:00', 2),
       ('2', 10, '2025-05-30 09:34:00', '2025-05-30 09:34:00', 3),
       ('2', 11, '2025-05-30 09:34:00', '2025-05-30 09:34:00', 3),
       ('2', 12, '2025-05-30 09:34:00', '2025-05-30 09:34:00', 3),
       ('2', 13, '2025-05-30 09:34:00', '2025-05-30 09:34:00', 3),
       ('3', 3, '2025-05-30 09:35:00', '2025-05-30 09:35:00', 3),
       ('3', 4, '2025-05-30 09:35:00', '2025-05-30 09:35:00', 3),
       ('3', 5, '2025-05-30 09:35:00', '2025-05-30 09:35:00', 3),
       ('3', 6, '2025-05-30 09:35:00', '2025-05-30 09:35:00', 3),
       ('3', 7, '2025-05-30 09:35:00', '2025-05-30 09:35:00', 3),
       ('2', 14, '2025-05-30 09:19:00', '2025-05-30 09:19:00', 4),
       ('2', 15, '2025-05-30 09:19:00', '2025-05-30 09:19:00', 4),
       ('2', 16, '2025-05-30 09:19:00', '2025-05-30 09:19:00', 4),
       ('2', 17, '2025-05-30 09:19:00', '2025-05-30 09:19:00', 4),
       ('2', 18, '2025-05-30 09:19:00', '2025-05-30 09:19:00', 4),
       ('2', 19, '2025-06-01 09:51:00', '2025-06-01 09:51:00', 5),
       ('2', 20, '2025-06-01 09:51:00', '2025-06-01 09:51:00', 5),
       ('2', 21, '2025-06-01 09:51:00', '2025-06-01 09:51:00', 5),
       ('2', 22, '2025-06-01 09:51:00', '2025-06-01 09:51:00', 5),
       ('2', 23, '2025-06-01 09:51:00', '2025-06-01 09:51:00', 5),
       ('2', 24, '2025-06-01 08:39:00', '2025-06-01 08:39:00', 6),
       ('2', 25, '2025-06-01 08:39:00', '2025-06-01 08:39:00', 6),
       ('3', 8, '2025-06-01 08:40:00', '2025-06-01 08:40:00', 6),
       ('2', 26, '2025-06-01 09:05:00', '2025-06-01 09:05:00', 7),
       ('2', 27, '2025-06-01 09:05:00', '2025-06-01 09:05:00', 7),
       ('2', 28, '2025-06-01 09:05:00', '2025-06-01 09:05:00', 7),
       ('2', 29, '2025-06-01 09:05:00', '2025-06-01 09:05:00', 7),
       ('2', 30, '2025-06-01 09:05:00', '2025-06-01 09:05:00', 7),
       ('3', 9, '2025-06-01 09:06:00', '2025-06-01 09:06:00', 7),
       ('3', 10, '2025-06-01 09:06:00', '2025-06-01 09:06:00', 7),
       ('3', 11, '2025-06-01 09:06:00', '2025-06-01 09:06:00', 7),
       ('3', 12, '2025-06-01 09:06:00', '2025-06-01 09:06:00', 7),
       ('2', 31, '2025-06-01 08:39:00', '2025-06-01 08:39:00', 8),
       ('2', 32, '2025-06-01 08:39:00', '2025-06-01 08:39:00', 8),
       ('2', 33, '2025-06-01 08:39:00', '2025-06-01 08:39:00', 8),
       ('2', 34, '2025-06-01 08:39:00', '2025-06-01 08:39:00', 8),
       ('2', 35, '2025-06-01 08:39:00', '2025-06-01 08:39:00', 8),
       ('3', 13, '2025-06-01 08:40:00', '2025-06-01 08:40:00', 8),
       ('2', 36, '2025-06-01 10:07:00', '2025-06-01 10:07:00', 9),
       ('2', 37, '2025-06-01 10:07:00', '2025-06-01 10:07:00', 9),
       ('2', 2, '2025-06-01 10:07:00', '2025-06-01 10:07:00', 9),
       ('2', 3, '2025-06-01 10:07:00', '2025-06-01 10:07:00', 9),
       ('2', 4, '2025-06-01 10:07:00', '2025-06-01 10:07:00', 9),
       ('3', 14, '2025-06-01 10:08:00', '2025-06-01 10:08:00', 9),
       ('3', 15, '2025-06-01 10:08:00', '2025-06-01 10:08:00', 9),
       ('3', 16, '2025-06-01 10:08:00', '2025-06-01 10:08:00', 9),
       ('3', 17, '2025-06-01 10:08:00', '2025-06-01 10:08:00', 9),
       ('3', 18, '2025-06-01 10:08:00', '2025-06-01 10:08:00', 9),
       ('2', 5, '2025-06-04 11:15:00', '2025-06-04 11:15:00', 10),
       ('2', 6, '2025-06-04 11:15:00', '2025-06-04 11:15:00', 10),
       ('2', 7, '2025-06-04 11:15:00', '2025-06-04 11:15:00', 10),
       ('2', 8, '2025-06-04 11:15:00', '2025-06-04 11:15:00', 10),
       ('2', 9, '2025-06-04 11:15:00', '2025-06-04 11:15:00', 10),
       ('3', 19, '2025-06-04 11:16:00', '2025-06-04 11:16:00', 10),
       ('2', 10, '2025-06-04 11:03:00', '2025-06-04 11:03:00', 11),
       ('2', 11, '2025-06-04 11:03:00', '2025-06-04 11:03:00', 11),
       ('2', 12, '2025-06-04 11:03:00', '2025-06-04 11:03:00', 11),
       ('2', 13, '2025-06-04 11:03:00', '2025-06-04 11:03:00', 11),
       ('2', 14, '2025-06-04 11:03:00', '2025-06-04 11:03:00', 11),
       ('3', 20, '2025-06-04 11:04:00', '2025-06-04 11:04:00', 11),
       ('3', 21, '2025-06-04 11:04:00', '2025-06-04 11:04:00', 11),
       ('3', 22, '2025-06-04 11:50:00', '2025-06-04 11:50:00', 12),
       ('2', 15, '2025-06-04 11:35:00', '2025-06-04 11:35:00', 13),
       ('2', 16, '2025-06-04 11:35:00', '2025-06-04 11:35:00', 13),
       ('2', 17, '2025-06-04 11:35:00', '2025-06-04 11:35:00', 13),
       ('2', 18, '2025-06-04 11:35:00', '2025-06-04 11:35:00', 13),
       ('2', 19, '2025-06-04 11:35:00', '2025-06-04 11:35:00', 13),
       ('3', 23, '2025-06-04 11:36:00', '2025-06-04 11:36:00', 13),
       ('3', 24, '2025-06-04 11:36:00', '2025-06-04 11:36:00', 13),
       ('2', 20, '2025-06-04 11:41:00', '2025-06-04 11:41:00', 14),
       ('2', 21, '2025-06-04 11:41:00', '2025-06-04 11:41:00', 14),
       ('2', 22, '2025-06-04 11:41:00', '2025-06-04 11:41:00', 14),
       ('2', 23, '2025-06-04 11:41:00', '2025-06-04 11:41:00', 14),
       ('3', 25, '2025-06-04 11:42:00', '2025-06-04 11:42:00', 14),
       ('2', 24, '2025-06-04 12:43:00', '2025-06-04 12:43:00', 15),
       ('2', 25, '2025-06-04 12:43:00', '2025-06-04 12:43:00', 15),
       ('2', 26, '2025-06-04 12:43:00', '2025-06-04 12:43:00', 15),
       ('2', 27, '2025-06-04 12:43:00', '2025-06-04 12:43:00', 15),
       ('2', 28, '2025-06-04 12:43:00', '2025-06-04 12:43:00', 15),
       ('2', 29, '2025-06-04 11:43:00', '2025-06-04 11:43:00', 16),
       ('2', 30, '2025-06-04 11:43:00', '2025-06-04 11:43:00', 16),
       ('2', 31, '2025-06-04 11:43:00', '2025-06-04 11:43:00', 16),
       ('2', 32, '2025-06-04 11:43:00', '2025-06-04 11:43:00', 16),
       ('2', 33, '2025-06-04 11:43:00', '2025-06-04 11:43:00', 16),
       ('3', 26, '2025-06-04 11:44:00', '2025-06-04 11:44:00', 16),
       ('3', 27, '2025-06-04 11:44:00', '2025-06-04 11:44:00', 16),
       ('2', 34, '2025-06-04 13:12:00', '2025-06-04 13:12:00', 17),
       ('3', 28, '2025-06-04 13:13:00', '2025-06-04 13:13:00', 17),
       ('2', 35, '2025-06-04 13:00:00', '2025-06-04 13:00:00', 18),
       ('2', 36, '2025-06-04 13:00:00', '2025-06-04 13:00:00', 18),
       ('2', 37, '2025-06-04 13:00:00', '2025-06-04 13:00:00', 18),
       ('2', 2, '2025-06-04 13:00:00', '2025-06-04 13:00:00', 18),
       ('2', 3, '2025-06-04 13:00:00', '2025-06-04 13:00:00', 18),
       ('2', 4, '2025-06-04 12:24:00', '2025-06-04 12:24:00', 19),
       ('2', 5, '2025-06-04 12:24:00', '2025-06-04 12:24:00', 19),
       ('2', 6, '2025-06-04 12:24:00', '2025-06-04 12:24:00', 19),
       ('2', 7, '2025-06-04 12:24:00', '2025-06-04 12:24:00', 19),
       ('2', 8, '2025-06-04 12:24:00', '2025-06-04 12:24:00', 19),
       ('3', 29, '2025-06-04 12:25:00', '2025-06-04 12:25:00', 19),
       ('3', 30, '2025-06-04 12:25:00', '2025-06-04 12:25:00', 19),
       ('3', 31, '2025-06-04 12:25:00', '2025-06-04 12:25:00', 19),
       ('2', 9, '2025-06-04 12:36:00', '2025-06-04 12:36:00', 20),
       ('2', 10, '2025-06-04 12:36:00', '2025-06-04 12:36:00', 20),
       ('2', 11, '2025-06-04 12:36:00', '2025-06-04 12:36:00', 20),
       ('2', 12, '2025-06-04 12:36:00', '2025-06-04 12:36:00', 20),
       ('2', 13, '2025-06-04 12:36:00', '2025-06-04 12:36:00', 20),
       ('3', 32, '2025-06-04 12:37:00', '2025-06-04 12:37:00', 20),
       ('3', 33, '2025-06-04 12:37:00', '2025-06-04 12:37:00', 20),
       ('2', 14, '2025-06-04 13:48:00', '2025-06-04 13:48:00', 21),
       ('2', 15, '2025-06-04 13:48:00', '2025-06-04 13:48:00', 21),
       ('2', 16, '2025-06-04 13:48:00', '2025-06-04 13:48:00', 21),
       ('2', 17, '2025-06-04 13:48:00', '2025-06-04 13:48:00', 21),
       ('2', 18, '2025-06-04 13:48:00', '2025-06-04 13:48:00', 21),
       ('3', 34, '2025-06-04 13:49:00', '2025-06-04 13:49:00', 21),
       ('2', 19, '2025-06-04 13:06:00', '2025-06-04 13:06:00', 22),
       ('2', 20, '2025-06-04 13:06:00', '2025-06-04 13:06:00', 22),
       ('2', 21, '2025-06-04 13:06:00', '2025-06-04 13:06:00', 22),
       ('2', 22, '2025-06-04 13:06:00', '2025-06-04 13:06:00', 22),
       ('2', 23, '2025-06-04 13:06:00', '2025-06-04 13:06:00', 22),
       ('2', 24, '2025-06-04 13:06:00', '2025-06-04 13:06:00', 22),
       ('2', 25, '2025-06-04 13:06:00', '2025-06-04 13:06:00', 22);


-- 建立 NEWS_CONTENT_CLASSIFICATION：新聞 ↔ 類別
CREATE TABLE NEWS_CONTENT_CLASSIFICATION
(
    NCC_NO  INT AUTO_INCREMENT NOT NULL,
    NCAT_NO INT                NOT NULL,
    NEWS_NO INT                NOT NULL,
    CONSTRAINT NEWS_CONTENT_CLASSIFICATION_PK PRIMARY KEY (NCC_NO),
    CONSTRAINT NCC_FK_CATEGORY FOREIGN KEY (NCAT_NO)
        REFERENCES NEWS_CATEGORY (NCAT_NO),
    CONSTRAINT NCC_FK_NEWS FOREIGN KEY (NEWS_NO)
        REFERENCES NEWS (NEWS_NO)
) AUTO_INCREMENT = 1 COMMENT ='新聞內容分類對照';
-- 5-2 將前五篇新聞分別歸類
-- (假設 NEWS_NO 1~5 已於 NEWS 表存在)
INSERT INTO NEWS_CONTENT_CLASSIFICATION (NCAT_NO, NEWS_NO)
VALUES (1, 1),
       (2, 2),
       (3, 3),
       (4, 4),
       (1, 5),
       (2, 5),
       (4, 5);


-- 建立 NEWS_COM_REPORT（新聞評論檢舉）
CREATE TABLE NEWS_COM_REPORT
(
    NEWS_COM_REPORT_NO     INT AUTO_INCREMENT NOT NULL,
    REPORTER               INT                NOT NULL comment '檢舉者 MEM_NO',                 -- 檢舉者（會員）
    REPORT_TYPE            INT                NOT NULL comment '檢舉類型 RPI_NO',               -- 檢舉類型（FK：REPORT_TYPE.RPI_NO）
    NEWS_COM_REPORT_STATUS CHAR(1)            NOT NULL DEFAULT '0' comment '0:未處理 1:已處理', -- 未處理 已處理
    NCOM_NO                INT                NOT NULL comment '被檢舉留言',                    -- 被檢舉的評論
    CREATE_TIME            DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP comment '創造新聞評論檢舉時間',
    FINISH_TIME            DATETIME comment '處理完成時間',
    CONSTRAINT NEWS_COM_REPORT_PK PRIMARY KEY (NEWS_COM_REPORT_NO),
    CONSTRAINT NCR_FK_MEMBER FOREIGN KEY (REPORTER) REFERENCES MEMBER (MEM_NO),
    CONSTRAINT NCR_FK_COMMENT FOREIGN KEY (NCOM_NO) REFERENCES NEWS_COMMENTS (NCOM_NO),
    CONSTRAINT NCR_FK_RTYPE FOREIGN KEY (REPORT_TYPE) REFERENCES REPORT_TYPE (RPI_NO)
) AUTO_INCREMENT = 1 COMMENT ='新聞評論檢舉記錄';
-- 新增五筆留言檢舉樣例
-- REPORT_TYPE 1: 廣告垃圾   2: 人身攻擊 （僅示範）
INSERT INTO NEWS_COM_REPORT (REPORTER, REPORT_TYPE, NEWS_COM_REPORT_STATUS, NCOM_NO, CREATE_TIME, FINISH_TIME)
VALUES (2, 1, '0', 3, '2025-06-07 12:00:00', NULL),
       (3, 2, '0', 5, '2025-06-07 12:05:00', NULL),
       (4, 1, '1', 7, '2025-06-06 16:00:00', '2025-06-06 18:00:00'),
       (5, 2, '0', 10, '2025-06-06 19:30:00', NULL),
       (6, 1, '1', 12, '2025-06-06 20:00:00', '2025-06-06 21:15:00');

-- ===== END OF MEMBER_SysAndNEWS_Sys.sql =====

-- ===== START OF 討論區.sql =====
-- 目前有哪些資料庫 --
-- SHOW DATABASES;

-- 建立 Pixel_Tribe 資料庫 --
-- CREATE DATABASE IF NOT EXISTS pixel_tribe;

-- 使用 Pixel_Tribe --
-- USE Pixel_Tribe;

-- 確認選擇的資料庫 --
-- SELECT DATABASE();

-- 設定自增主鍵起始值與遞增量
SET
auto_increment_offset = 1;
SET
auto_increment_increment = 1;


-- 建立 討論區類別 表格 --
CREATE TABLE FORUM_CATEGORY
(
    `CAT_NO`   INT AUTO_INCREMENT PRIMARY KEY COMMENT '討論區類別編號',
    `CAT_NAME` VARCHAR(30) NOT NULL COMMENT '類別名稱',
    `CAT_DES`  VARCHAR(255) COMMENT '類別描述',
    `CAT_DATE` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '創建時間'
) COMMENT '討論區類別';

-- 新增 討論區類別 資料 --
INSERT INTO FORUM_CATEGORY(`CAT_NAME`, `CAT_DES`, `CAT_DATE`)
VALUES ('角色扮演', '玩家扮演虛擬角色,體驗故事情節與成長', '2024-06-25 08:30:00'),
       ('動作', '強調操作技巧與反應速度，以戰鬥或解謎為主', '2024-07-12 18:00:00'),
       ('射擊', '以槍械或其他遠程武器進行攻擊的遊戲', '2024-09-03 13:55:00'),
       ('競速', '駕駛交通工具或角色進行速度比拼的遊戲', '2024-10-21 14:30:00'),
       ('冒險', '以探索未知世界、解謎、揭示劇情為核心', '2024-11-28 16:20:00'),
       ('益智', '考驗邏輯思維、解決問題能力的遊戲', '2025-01-05 9:15:00'),
       ('運動', '模擬現實或虛擬運動賽事的遊戲', '2025-02-19 17:10:00'),
       ('線上休閒', '適合輕鬆遊玩，通常具有社交元素的線上遊戲', '2025-03-01 11:45:00'),
       ('其他', '不屬於以上分類的遊戲類型', '2025-04-10 10:00:00');


-- 建立 討論區 表格 --
CREATE TABLE FORUM
(
    `FOR_NO`      INT AUTO_INCREMENT PRIMARY KEY COMMENT '討論區編號',
    `FOR_NAME`    VARCHAR(30) NOT NULL COMMENT '討論區名稱',
    `CAT_NO`      INT COMMENT '類別編號',
    `FOR_DES`     VARCHAR(255) COMMENT '討論區描述',
    `FOR_DATE`    DATETIME             DEFAULT CURRENT_TIMESTAMP COMMENT '創建時間',
    `FOR_UPDATE`  DATETIME             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    `FOR_STATUS`  CHAR(1)     NOT NULL DEFAULT '0' COMMENT '討論區狀態',
    `FOR_IMG_URL` VARCHAR(255) COMMENT '討論區圖片URL',
    FOREIGN KEY (`CAT_NO`)
        REFERENCES FORUM_CATEGORY (`CAT_NO`)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) COMMENT '討論區';

-- 新增 討論區 資料 --
INSERT INTO FORUM (`FOR_NAME`, `CAT_NO`, `FOR_DES`, `FOR_DATE`, `FOR_UPDATE`, `FOR_IMG_URL`)
VALUES ('原神', '1', '全球知名的開放世界動作RPG，強調元素反應戰鬥與豐富探索。', '2024-06-05 9:00:00',
        '2024-06-05 9:05:15', '/images/forum_img/01.jpg'),
       ('崩壞：星穹鐵道', '1', '米哈遊旗下回合制策略RPG，以星際冒險與精緻角色設計著稱。', '2024-06-07 10:15:30',
        '2024-06-07 10:20:45', '/images/forum_img/02.jpg'),
       ('艾爾登法環', '1', '魂系開放世界RPG，以高難度、深奧世界觀及自由探索聞名，TGA年度遊戲。', '2024-06-10 11:30:00',
        '2024-06-10 11:35:10', '/images/forum_img/03.jpg'),
       ('最終幻想VII 重生', '1', '經forum典日系RPG《FFVII》重製版第二部，以壯闊劇情與創新戰鬥系統吸引玩家。',
        '2024-06-12 13:45:20',
        '2024-06-12 13:50:30', '/images/forum_img/04.jpg'),
       ('聖獸之王', '1', '結合戰術RPG與回合制戰鬥，畫風精美，策略深度高。', '2024-06-15 14:00:10',
        '2024-06-15 14:05:25', '/images/forum_img/05.jpg'),
       ('戰神：諸神黃昏', '2', '史詩級動作冒險，延續北歐神話故事，強調父子情感與殘酷戰鬥。', '2024-06-17 15:10:05',
        '2024-06-17 15:15:15', '/images/forum_img/06.jpg'),
       ('師父', '2', '獨特武術動作遊戲，每次死亡會讓你變老，考驗玩家技巧與成長。', '2024-06-20 16:20:00',
        '2024-06-20 16:25:30', '/images/forum_img/07.jpg'),
       ('惡魔獵人3', '2', '系列前傳，奠定DMC戰鬥系統基礎，風格狂放，高難度挑戰性。', '2024-06-22 17:30:40',
        '2024-06-22 17:35:55', '/images/forum_img/08.jpg'),
       ('黑暗靈魂系列', '2', '開創「魂系」高難度動作RPG先河，以沉浸式世界與獨特敘事吸引玩家。', '2024-06-25 18:40:15',
        '2024-06-25 18:45:20', '/images/forum_img/09.jpg'),
       ('決勝時刻系列', '3', '全球最受歡迎的射擊遊戲系列，以電影般戰役和快節奏多人對戰聞名。', '2024-06-27 9:00:00',
        '2024-06-27 9:05:10', '/images/forum_img/10.jpg'),
       ('特戰英豪', '3', 'Riot Games推出的戰術射擊遊戲，結合英雄技能與經典槍戰玩法。', '2024-06-30 10:05:00',
        '2024-06-30 10:10:25', '/images/forum_img/11.jpg'),
       ('Apex 英雄', '3', '大逃殺射擊遊戲，強調英雄技能搭配與小隊合作，節奏快速。', '2024-07-02 11:15:10',
        '2024-07-02 11:20:30', '/images/forum_img/12.jpg'),
       ('PUBG：絕地求生', '3', '大逃殺類型遊戲的始祖，玩家在大型地圖上互相競爭生存到最後。', '2024-07-05 12:25:00',
        '2024-07-05 12:30:15', '/images/forum_img/13.jpg'),
       ('絕對武力：全球攻勢 (CS2)', '3', '經典競技FPS，以團隊合作、經濟系統與精準射擊為核心。', '2024-07-07 13:35:05',
        '2024-07-07 13:40:20', '/images/forum_img/14.jpg'),
       ('戰地風雲系列', '3', '以大規模戰場、載具戰鬥和可破壞場景為特色的軍事FPS。', '2024-07-10 14:45:00',
        '2024-07-10 14:50:05', '/images/forum_img/15.jpg'),
       ('極限競速 地平線5', '4', '開放世界競速遊戲，擁有廣闊地圖、海量車輛與精美畫面。', '2024-07-12 15:55:10',
        '2024-07-12 16:00:20', '/images/forum_img/16.jpg'),
       ('跑跑卡丁車Rush+', '4', '經典休閒競速遊戲手遊版，卡通風格與趣味道具戰。', '2024-07-15 16:05:00',
        '2024-07-15 16:10:15', '/images/forum_img/17.jpg'),
       ('狂野飆車系列', '4', '手機平台知名競速遊戲，畫面華麗，操作爽快。', '2024-07-17 17:15:15',
        '2024-07-17 17:20:30', '/images/forum_img/18.jpg'),
       ('瑪利歐賽車系列', '4', '老少皆宜的休閒競速遊戲，以趣味道具與角色技能大亂鬥。', '2024-07-20 18:25:00',
        '2024-07-20 18:30:05', '/images/forum_img/19.jpg'),
       ('GT賽車系列', '4', '主打擬真駕駛體驗的賽車模擬遊戲。', '2024-07-22 9:35:10',
        '2024-07-22 9:40:25', '/images/forum_img/20.jpg'),
       ('薩爾達傳說', '5', '開放世界冒險遊戲顛峰，極高自由度、創造性與豐富解謎。', '2024-07-25 10:45:00',
        '2024-07-25 10:50:15', '/images/forum_img/21.jpg'),
       ('柏德之門3', '5', '劇情豐富、選擇多樣的CRPG，兼具強大探索與冒險元素，TGA年度遊戲。', '2024-07-27 11:55:05',
        '2024-07-27 12:00:20', '/images/forum_img/22.jpg'),
       ('霍格華茲傳承', '5', '哈利波特世界觀開放世界冒險，玩家可體驗魔法學校生活。', '2024-07-30 13:05:00',
        '2024-07-30 13:10:05', '/images/forum_img/23.jpg'),
       ('俠盜獵車手V', '5', '極具影響力的開放世界動作冒險，可體驗三主角視角下的犯罪人生。', '2024-08-01 14:15:10',
        '2024-08-01 14:20:25', '/images/forum_img/24.jpg'),
       ('Minecraft', '5', '高自由度沙盒遊戲，玩家可自由創造、探索、生存，發揮無限創意。', '2024-08-04 15:25:00',
        '2024-08-04 15:30:15', '/images/forum_img/25.jpg'),
       ('動物森友會系列', '6', '休閒經營與收集的益智遊戲，與可愛動物一同打造夢幻營地。', '2024-08-06 16:35:05',
        '2024-08-06 16:40:20', '/images/forum_img/26.jpg'),
       ('夢幻家園', '6', '三消與裝潢結合的益智遊戲，透過解謎修復並設計豪宅。', '2024-08-09 17:45:00',
        '2024-08-09 17:50:05', '/images/forum_img/27.jpg'),
       ('俄羅斯方塊', '6', '永恆的經典益智遊戲，考驗空間邏輯與反應速度。', '2024-08-11 18:55:10',
        '2024-08-11 19:00:25', '/images/forum_img/28.jpg'),
       ('EA Sports FC™ (原FIFA系列)', '7', '最受歡迎的足球遊戲，提供擬真比賽體驗與豐富球隊模式。', '2024-08-14 9:05:00',
        '2024-08-14 9:10:15', '/images/forum_img/29.jpg'),
       ('NBA 2K系列', '7', '最受歡迎的籃球遊戲，擬真球員數據、動作與生涯模式。', '2024-08-16 10:15:05',
        '2024-08-16 10:20:20', '/images/forum_img/30.jpg'),
       ('MLB The Show系列', '7', '官方美國職棒大聯盟遊戲，提供擬真棒球體驗。', '2024-08-19 11:25:00',
        '2024-08-19 11:30:05', '/images/forum_img/31.jpg'),
       ('EA Sports UFC系列', '7', '終極格鬥冠軍賽官方遊戲，提供寫實格鬥體驗。', '2024-08-21 12:35:10',
        '2024-08-21 12:40:25', '/images/forum_img/32.jpg'),
       ('勁舞團', '7', '音樂節奏類型的線上休閒運動遊戲，曾風靡一時。', '2024-08-24 13:45:00',
        '2024-08-24 13:50:15', '/images/forum_img/33.jpg'),
       ('糖豆人', '8', '大亂鬥闖關派對遊戲，玩家扮演可愛豆人闖關競技。', '2024-08-26 14:55:05',
        '2024-08-26 15:00:20', '/images/forum_img/34.jpg'),
       ('Among Us', '8', '推理社交遊戲，玩家需找出船員中的冒充者。', '2024-08-29 16:05:00',
        '2024-08-29 16:10:05', '/images/forum_img/35.jpg'),
       ('傳說對決', '8', '熱門手機MOBA遊戲，提供快節奏的5v5對戰。', '2024-08-31 17:15:10',
        '2024-08-31 17:20:25', '/images/forum_img/36.jpg'),
       ('英雄聯盟', '8', '經典MOBA遊戲，全球電競重要項目，玩家操控英雄進行戰術對戰。', '2024-09-03 18:25:00',
        '2024-09-03 18:30:15', '/images/forum_img/37.jpg'),
       ('楓之谷', '8', '經典橫版卷軸MMORPG，以可愛畫風與豐富內容吸引玩家。', '2024-09-05 9:35:05',
        '2024-09-05 9:40:20', '/images/forum_img/38.jpg'),
       ('魔物獵人系列', '9', '大型動作狩獵遊戲，玩家狩獵巨型魔物，製作裝備。', '2024-09-13 13:05:00',
        '2024-09-13 13:10:15', '/images/forum_img/39.jpg');


-- 建立 文章類別標籤 表格 --
CREATE TABLE FORUM_TAG
(
    `FTAG_NO`       INT         NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT '文章類別標籤編號',
    `FTAG_NAME`     VARCHAR(30) NOT NULL COMMENT '類別標籤名稱',
    `FTAG_SPEC`     VARCHAR(255) COMMENT '類別標籤說明',
    `DEFAULT_IMAGE` LONGBLOB comment '文章標籤預設圖片'
) COMMENT '文章類別標籤';

-- 新增 文章類別標籤 資料 --
INSERT INTO FORUM_TAG(`FTAG_NAME`, `FTAG_SPEC`)
VALUES ('趣味', '分享奇聞趣事或搞笑瞬間');
INSERT INTO FORUM_TAG(`FTAG_NAME`, `FTAG_SPEC`)
VALUES ('問題', '用於提出疑問或尋求解答');
INSERT INTO FORUM_TAG(`FTAG_NAME`, `FTAG_SPEC`)
VALUES ('情報', '分享或提供相關資訊和消息');
INSERT INTO FORUM_TAG(`FTAG_NAME`, `FTAG_SPEC`)
VALUES ('心得', '分享個人經驗、感悟或學習體會');
INSERT INTO FORUM_TAG(`FTAG_NAME`, `FTAG_SPEC`)
VALUES ('討論', '針對特定主題進行深入探討和辯論');
INSERT INTO FORUM_TAG(`FTAG_NAME`, `FTAG_SPEC`)
VALUES ('攻略', '提供遊戲、任務或其他方面的詳細指導和策略');
INSERT INTO FORUM_TAG(`FTAG_NAME`, `FTAG_SPEC`)
VALUES ('密技', '分享不為人知的技巧、竅門或隱藏功能');
INSERT INTO FORUM_TAG(`FTAG_NAME`, `FTAG_SPEC`)
VALUES ('閒聊', '輕鬆、非正式的交流，聊日常或趣事');
INSERT INTO FORUM_TAG(`FTAG_NAME`, `FTAG_SPEC`)
VALUES ('其他', '不屬於上述任何類別的內容');
INSERT INTO FORUM_TAG(`FTAG_NAME`, `FTAG_SPEC`)
VALUES ('招募', '發布招聘信息或尋找合作夥伴');
INSERT INTO FORUM_TAG(`FTAG_NAME`, `FTAG_SPEC`)
VALUES ('官方訊息', '由官方發布的重要通知、公告或聲明');


-- 建立 文章 表格--
create table FORUM_POST
(
    `POST_NO`         INT AUTO_INCREMENT NOT NULL PRIMARY KEY COMMENT '文章編號',
    `FOR_NO`          INT COMMENT '討論區編號',
    `MEM_NO`          INT COMMENT '會員編號',
    `FTAG_NO`         INT COMMENT '類別編號',
    `POST_TITLE`      VARCHAR(50) COMMENT '文章標題',
    `POST_CON`        VARCHAR(5000) NOT NULL COMMENT '文章內容',
    `POST_CRDATE`     DATETIME               DEFAULT CURRENT_TIMESTAMP COMMENT '創建時間',
    `POST_UPDATE`     DATETIME               DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    `POST_PIN`        CHAR(1)       NOT NULL DEFAULT '0' COMMENT '置頂狀態',
    `POST_STATUS`     CHAR(1)       NOT NULL DEFAULT '0' COMMENT '文章狀態(違規)',
    `MES_NUMBERS`     INT                    DEFAULT '0' COMMENT '留言篇數',
    `POST_LIKE_COUNT` INT                    DEFAULT '0' COMMENT '讚總數',
    `POST_LIKE_DLC`   INT                    DEFAULT '0' COMMENT '倒讚總數',
    `POSTIMAGE_URL` VARCHAR(255) DEFAULT NULL comment '文章預設封面圖片',
    FOREIGN KEY (`MEM_NO`) REFERENCES MEMBER (`MEM_NO`) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (`FTAG_NO`) REFERENCES FORUM_TAG (`FTAG_NO`) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (`FOR_NO`) REFERENCES FORUM (`FOR_NO`) ON DELETE SET NULL ON UPDATE CASCADE
) COMMENT '文章';

-- 新增 文章 資料 --
INSERT INTO FORUM_POST (`FOR_NO`, `MEM_NO`, `FTAG_NO`, `POST_TITLE`, `POST_CON`, `POST_CRDATE`, `POST_UPDATE`,
                        `MES_NUMBERS`, `POST_LIKE_COUNT`, `POST_LIKE_DLC`, `POSTIMAGE_URL`)
VALUES ( '1', '12', '6'
       , '楓丹探索度100%不迷路！全水神瞳與奇妙能量礦點位整理,親愛的旅行者們，大家好！'
       , '這次為大家帶來楓丹探索的終極攻略！是不是覺得楓丹的水下世界美到想沉溺，但又常常迷失方向呢？別擔心，我整理了一份超級詳細的楓丹全水神瞳點位圖，每個位置都附帶了路線建議和注意事項。

此外，針對許多人卡關的「奇妙能量礦」，我也標註了所有可能刷新的地點，讓你輕鬆收集材料，再也不用大海撈針。如果你也跟我一樣有探索度強迫症，這篇絕對能幫上大忙！

記得搭配你的「尋寶羅盤」使用，效果會更好喔！有任何疑問或發現我漏掉的點位，歡迎在下方留言交流。祝大家都能順利探索楓丹，拿到那顆珍貴的名片！'
       , '2024-06-05 15:23:26', '2024-06-05 18:45:12', '0', '0', '0', 'images/forumposttag_img/06.jpg'),
       ( '3', '37', '4'
       , '打完蒙格後的空虛感... 褪色者們還有什麼推薦？'
       , '最近終於單刷過了「鮮血君王蒙格」，雖然過程煎熬，但在打敗他後卻湧上了一股莫名的空虛感... 感覺整個遊戲最讓我上頭的挑戰告一段落了。

雖然還有一些支線和隱藏 Boss 沒打，但主線告一段落後真的有點不知道該做什麼了。各位褪色者們，除了二周目，還有什麼其他好玩的玩法或挑戰，能重新找回初見時那種刺激感呢？

我目前是用信仰流，裝備也大致成型了。是不是該試試看純法師或近戰 Build？或者有什麼特別的 Boss 值得反覆挑戰？歡迎大家分享自己的「後艾爾登法環時期」都在玩什麼，求推薦，救救我的空虛啊！'
       , '2024-06-06 10:00:00', '2024-06-06 15:30:00', '0', '0', '0', 'images/forumposttag_img/04.jpg'),
       ( '11', '3', '10'
       , '穩定鑽石局雙排夥伴！求一位主控/輔助，不玻璃心！'
       , '各位瓦羅蘭特戰友們，大家好！

我是主玩 Astra 和 Sova 的玩家，目前在鑽石二。想找一位穩定雙排的夥伴，希望你是主控角或輔助角玩家，能補齊隊伍陣容。

我們可以在週末晚上和平日晚上一起排位，目標衝擊不朽。希望你具備以下特質：

不玻璃心：輸贏是兵家常事，能互相鼓勵、共同進步最重要。
有麥克風：溝通順暢是贏遊戲的關鍵。
觀念佳：懂得地圖控制、戰術執行。
心態積極：即使逆風也能保持冷靜。
如果你也想找一位穩定且能互相配合的夥伴，請直接留言你的遊戲 ID 和主玩角色，或私訊我。期待與你一起上分！'
       , '2024-06-07 8:30:00', '2024-06-08 10:00:00', '0', '0', '0', 'images/forumposttag_img/10.jpg'),
       ( '26', '28', '8'
       , '你們島上的「明星」是哪隻小動物？快來曬曬！'
       , '嘿！動森的各位島主們！

你們島上是不是也有那麼一隻特別受寵、特別有戲的小動物呢？牠可能表情包特別多，可能每天都在做些奇怪的事情，或是常常說出一些爆笑的台詞，讓你對牠愛不釋手。

在我島上，絕對是我的元老級居民——「茶茶丸」！那隻無時無刻都在健身的猩猩，有時候會對著空氣深蹲，有時候又在廣場上跳奇怪的舞，每天都帶給我無限歡樂。我還專門為牠蓋了一個「健身房」。

快來分享你島上的「明星小動物」是誰吧！可以附上牠的搞笑截圖或影片，讓大家一起感受牠的魅力！'
       , '2024-06-08 14:15:00', '2024-06-08 19:00:00', '0', '0', '0', 'images/forumposttag_img/08.jpg'),
       ( '29', '17', '2'
       , 'UT模式市場崩盤？為什麼我高價買的卡一直跌？'
       , '各位 FC 大大們，救命啊！

我最近在 Ultimate Team 模式花大錢買了幾張高評分球員卡，想說組一套最強陣容，結果這幾天市場整個大崩盤，我買的那些卡價格一直跌，跌到我心都在淌血了。

請問這是正常的市場波動嗎？還是官方最近有什麼活動或禮包導致大量高評分卡流入市場？我是不是該趕快賣掉止損，還是再等等看會不會回升？

有沒有什麼預測市場趨勢的技巧？感覺我每次都買在最高點，賣在最低點... 真的好沮喪啊！求各位大神指點迷津。'
       , '2024-06-09 11:00:00', '2024-06-10 9:00:00', '0', '0', '0', 'images/forumposttag_img/02.jpg'),
       ( '38', '23', '11'
       , 'V265 版本更新預告：全新職業「魂之繼承者」登場！'
       , '冒險者們，準備好迎接新的挑戰了嗎？

我們預計將於台灣時間 6 月 12 日（三）上午 9 點至下午 3 點進行 V265 版本更新維護。本次更新重點內容如下：

全新職業「魂之繼承者」：來自艾斯佩拉的新英雄，擁有獨特的靈魂契約能力，操控神秘力量殲滅敵人！詳細技能介紹將於官網同步公開。
「阿爾卡娜」地區全面重製：新的地圖設計、怪物配置與任務線，提供更流暢的冒險體驗。
「燃燒世界」活動重啟：角色等級衝刺的最佳時機，享受經驗加成與專屬獎勵！
系統優化與 Bug 修正：提升遊戲穩定性，修復已知問題。
請各位玩家務必在維護前妥善保管個人遊戲資料，並提前登出遊戲。感謝各位冒險者的支持，期待與「魂之繼承者」在楓之谷世界相見！'
       , '2024-06-10 9:30:00', '2024-06-10 14:00:00', '0', '0', '0', 'images/forumposttag_img/11.jpg'),
       ( '22', '6', '5'
       , '關於善惡選擇對劇情的影響：大家怎麼看？'
       , '各位費倫大陸的冒險者們好！

我在玩《柏德之門3》時，發現遊戲中許多善惡選項的影響遠比我想像的要深遠。有時候一個看似不起眼的選擇，可能在幾十個小時後才展現出它的蝴蝶效應，甚至導致某個重要角色走向完全不同的結局。

例如，在第一章救不救林地，或是處理「奪心魔蝌蚪」的方式，都直接影響了我後續的劇情走向和隊友關係。我個人是偏向走善良路線，但有時候又很想嘗試一下邪惡或中立的選項，看看劇情會怎麼跑。

大家在玩的時候，會刻意去嘗試不同的善惡選擇嗎？還是會順著自己的心意去玩？有沒有哪個選擇讓你印象特別深刻，覺得非常震撼或意想不到的？歡迎大家分享自己的「道德困境」或有趣的劇情分支！'
       , '2024-06-11 16:00:00', '2024-06-12 11:00:00', '0', '0', '0', 'images/forumposttag_img/05.jpg'),
       ( '16', '22', '7'
       , '賺錢神速！教你如何快速刷超級抽獎券！'
       , '各位地平線車手們，想快速賺錢改裝你的愛車嗎？這次我來分享一個目前超有效率的「刷超級抽獎券」密技！

這個方法主要是利用特定車輛的車輛專精。步驟如下：

購買車輛： 前往拍賣場或商店購買 「Willis MB Jeep」 (俗稱吉普車，價格很便宜)。
升級專精： 買到車後，進入「車輛專精」頁面，只點亮其中一個點：
「技能點」：如果你有很多技能點，直接點亮吉普車專精樹上能給你「超級抽獎券」的那個點。
重複操作： 拿到超級抽獎券後，就可以把這輛吉普車再掛到拍賣場賣掉 (雖然賣不了多少錢，但重點是抽獎券)。然後再買新的吉普車，重複以上步驟。
這個方法只需要少量的 CR 就能不斷刷出超級抽獎券，運氣好抽到千萬級跑車或大量現金就賺翻了！比你辛辛苦苦跑幾十場比賽來得快。快去試試看吧！'
       , '2024-06-12 7:00:00', '2024-06-12 12:30:00', '0', '0', '0', 'images/forumposttag_img/07.jpg'),
       ( '34', '30', '1'
       , '新賽季地圖吐槽大會！哪個關卡讓你又愛又恨？'
       , '各位圓滾滾的糖豆們，新賽季開始也一陣子了，新的地圖大家玩得開心嗎？

我個人是覺得這次有些新地圖很有創意，但有些真的玩到讓人想把鍵盤砸了 (哭)。例如那個「XXX」地圖，每次都卡在那個跳板上，簡直是我的惡夢！但「YYY」這張則設計得很有趣，常常能看到各種神操作和搞笑瞬間。

不知道大家對新賽季的地圖有什麼看法？哪個地圖讓你玩得超火大，哪個又是你的最愛，覺得根本是為你量身打造的？快來分享你的心得，吐槽也好，讚美也好，一起聊聊這次更新的關卡設計吧！'
       , '2024-06-13 13:45:00', '2024-06-14 8:00:00', '0', '0', '0', 'images/forumposttag_img/01.jpg'),
       ( '36', '1', '2'
       , 'SXX 賽季輔助角選擇障礙：求解排位上分推薦！'
       , '各位傳說戰友們，SXX 賽季開始後，我主玩輔助位，但總覺得不知道該選哪個輔助角上分比較穩。

目前版本感覺刺客和戰士都很強勢，打野和凱薩路的壓力都很大。我本來很常玩克里希和塔拉，但感覺在團戰中保護後排的能力有點不足。

大家覺得現在的版本，哪些輔助是比較推薦拿來排位上分的？是功能型輔助 (例如：提米、夸克)，還是坦度高、能開團的輔助 (例如：薩尼、牛魔王) 比較吃香？有沒有什麼特別的輔助搭配能提供給隊友更多幫助？

求各位輔助大神們指點迷津，或分享一下你們在排位中的輔助選擇策略！謝謝！'
       , '2024-06-14 10:30:00', '2024-06-14 16:00:00', '0', '0', '0', 'images/forumposttag_img/02.jpg'),
       ( '10', '33', '3'
       , '下一代 COD 消息洩露！回歸現代戰爭背景？'
       , '重磅消息！最近網路上瘋傳關於下一代《決勝時刻》的情報，雖然尚未經過官方證實，但多個可靠消息來源都指出，新作很可能會再次回歸現代戰爭題材，讓許多老玩家為之振奮！

根據洩露的情報，新遊戲的代號是「Cerberus」，背景設定在 21 世紀初期，將會深入探討多國特種部隊的秘密行動。地圖設計據稱將會更注重垂直作戰和多路徑設計，希望能解決目前遊戲中一些地圖過於平坦的問題。

雖然目前這些都只是傳聞，但如果消息屬實，那對於喜歡真實戰爭氛圍和戰術射擊的玩家來說，無疑是一大福音。大家對此有什麼期待或看法呢？希望這次的情報不是空穴來風啊！'
       , '2024-06-15 9:00:00', '2024-06-15 13:00:00', '0', '0', '0', 'images/forumposttag_img/03.jpg'),
       ( '25', '15', '10'
       , '招募建築大神！生存服缺地標建築師！'
       , '各位《我的世界》建築師們，看過來！

我們是一個和諧穩定的小型生存伺服器，目前成員大約 10 幾人，大家各自發展，偶爾會一起完成大型項目。最近我們正在籌劃將出生點附近區域打造成一個宏偉的主題城市，但現有的建築師人手不足，急需新的血輪加入！

如果你對建築有熱情，不論是中世紀城堡、現代都市、科幻基地或是奇幻風格，只要你有創意、有耐心，歡迎加入我們！我們提供充足的資源和自由的創作空間。

如果你有作品集或以往的建築經驗更好，請在下方留言你的遊戲 ID，附上你的建築風格或作品截圖，我們會盡快聯繫你！一起在《我的世界》中建造屬於我們的史詩級作品吧！'
       , '2024-06-16 12:00:00', '2024-06-17 9:00:00', '0', '0', '0', 'images/forumposttag_img/10.jpg'),
       ( '21', '6', '4'
       , '我用究極手蓋的「空中堡壘」！海拉魯霸主就是我！'
       , '自從有了「究極手」和「餘料建造」這兩項神級能力，我發現《王國之淚》的玩法真的無限可能！最近我花了好幾個小時，在空島上蓋了一座超級巨大的「空中移動堡壘」，簡直是把海拉魯的敵人玩弄於股掌之間！

這座堡壘結合了多個飛行器和大量扇風機，還加裝了火焰噴射器和雷射砲塔，基本上就是一個移動的死亡堡壘。我甚至還在上面建了個小平台，可以讓林克在上面休息喝藥水，感覺自己就是海拉魯的霸主！

雖然操作起來有點卡，材料也消耗得很快，但看到敵人被我從天而降的火力壓制，那種成就感真的無法言喻！大家在《王國之淚》裡都用究極手蓋過什麼奇特的載具或建築呢？快來分享你的創意作品，讓我開開眼界！'
       , '2024-06-17 15:00:00', '2024-06-17 20:00:00', '0', '0', '0', 'images/forumposttag_img/04.jpg'),
       ( '37', '9', '5'
       , '新裝備「裂隙制造者」對法師生態的影響？'
       , '各位召喚師們，新版本更新後，法師裝備「裂隙制造者」的改動引起了不小的討論。它現在提供了更好的生命值、法術強度和全能吸血，雖然被動觸發條件有所調整，但許多人都認為這件裝備會大幅改變法師的出裝選擇。

有觀點認為，這將讓一些持續輸出型法師 (如：斯溫、卡莎碧雅) 更具生存能力，甚至能讓一些法坦類的英雄崛起。但也有人擔心，吸血效果會讓他們變得過於難以擊殺，影響遊戲平衡。

大家覺得「裂隙制造者」的改動，對目前的法師生態會有什麼樣的影響？它會成為哪些英雄的核心裝備？或是會讓遊戲節奏變得更加拖沓嗎？歡迎大家發表自己的看法，一起分析這個新版本帶來的變化！'
       , '2024-06-18 10:00:00', '2024-06-18 10:00:00', '0', '0', '0', 'images/forumposttag_img/05.jpg'),
       ( '3', '26', '6'
       , '出血流究極攻略！讓你輕鬆砍爆交界地 Boss！'
       , '褪色者們，還在為難纏的 Boss 苦惱嗎？今天我將分享當前版本最穩定、輸出最高的打法之一：出血流 Build 究極攻略！

本攻略將詳細介紹如何從開局就組建出血流，包含：

武器選擇與取得方式：推薦「蒙格溫的聖矛」和「雙頭刀」等出血武器，以及它們的獲取路線。
禱告/法術與戰灰推薦：「黃金樹立誓」和「鮮血斬擊」等必備搭配。
護符與靈藥配置：如何最大化出血傷害。
能力值加點建議：初期、中期、後期分配策略。
實戰 Boss 應用技巧：針對常見 Boss 的出血觸發時機與應對策略。
即使是手殘黨也能輕鬆駕馭，體驗一刀入魂的快感！學會這套，保證你成為交界地的出血之王！有任何問題或建議，歡迎在下方留言討論。'
       , '2024-06-19 8:00:00', '2024-06-19 13:00:00', '0', '0', '0', 'images/forumposttag_img/06.jpg'),
       ( '17', '8', '2'
       , '我的手機玩起來好燙！求解最佳化設定？'
       , '各位跑跑玩家們，你們的手機玩《跑跑卡丁車Rush+》會很燙嗎？

我最近只要玩個兩三場，手機背面就開始發燙，有時候還會感覺到掉幀，影響手感。我的手機型號是 XXXX (可自行填寫)，理論上配置應該夠跑才對啊。

請問大家有遇到類似的問題嗎？有沒有什麼推薦的遊戲內設定或手機系統設定能讓遊戲跑得更順、減少發熱？例如：調低畫質、關閉高幀率、或是開啟省電模式等等。

真的很想流暢地享受競速的樂趣，希望有經驗的玩家能分享你們的優化心得，感激不盡！'
       , '2024-06-20 11:30:00', '2024-06-21 10:00:00', '0', '0', '0', 'images/forumposttag_img/02.jpg'),
       ( '22', '4', '8'
       , '你的隊伍配置會「輪替」嗎？我已經換過 N 次隊友了！'
       , '玩《柏德之門3》玩到現在，我發現一個很有趣的現象：我好像無法固定我的隊伍配置！

常常是玩到某個劇情段落，覺得某個隊友跟當前任務特別搭，就把他拉進隊伍；或是為了看隊友間的特殊對話和反應，也會刻意帶上不同組合。結果就是，我的營地常常門庭若市，每個隊友的裝備都得更新，超忙！

我個人最愛的是影心和蓋爾的嘴砲組合，每次聽到他們鬥嘴都超好笑。但有時候為了劇情需要，又會把養雞妹或卡菈克帶上。

不知道大家是不是也跟我一樣，隊伍配置會一直「輪替」呢？還是你們會堅持用一套固定的隊伍打天下？有沒有哪些隊友的組合是你特別喜歡，或是覺得效果特別好的？一起來分享你的「隊友輪替經」吧！'
       , '2024-06-21 9:00:00', '2024-06-21 15:00:00', '0', '0', '0', 'images/forumposttag_img/08.jpg'),
       ( '25', '31', '10'
       , '尋找生存夥伴！想一起從零開始蓋個「天空之城」！'
       , 'Minecraft 玩家們安安！

我是個喜歡長期經營生存伺服器的玩家，目前有個新開的獨立伺服器，希望能找到幾位志同道合的夥伴，一起從零開始打造一個令人驚嘆的**「天空之城」**！

我們的目標是：

在地表建立一個安全的生存據點。
收集大量資源，利用飛行工具或傳送點逐步向天空發展。
在空中建造宏偉的浮空島嶼和建築群，連結成一個巨大的「天空之城」。
一起挑戰 Boss、探索未知區域，並分享彼此的建築創意。
我們希望你：有耐心、有創意、對生存和建築有熱情、願意團隊合作。不要求你是建築大師，只要有心就能加入！

如果你也對這個宏偉的計畫感興趣，請在下方留言，附上你的 Discord ID 或遊戲 ID，我們會聯繫你！一起打造屬於我們的 Minecraft 奇蹟吧！'
       , '2024-06-22 14:00:00', '2024-06-22 18:00:00', '0', '0', '0', 'images/forumposttag_img/10.jpg'),
       ( '4', '19', '4'
       , '破關心得：這不是我認識的結局，但卻是新的可能？'
       , '玩到《最終幻想VII 重生》結局，心裡真的是百感交集。雖然我一直知道這款遊戲會有改動，但實際看到劇情走向與原作如此大的差異時，內心還是受到了衝擊。

特別是那個最終戰和艾莉絲的命運，真的讓許多老玩家包括我在內，既震驚又難以接受。但換個角度想，Square Enix 似乎正試圖開闢一個全新的故事線，給予這些經典角色不同的可能性。

我承認這份結局確實挑戰了我的情懷，但同時也為未來劇情發展留下了巨大的懸念和想像空間。不論你喜歡或不喜歡這個結局，它都成功地引發了玩家們熱烈的討論。各位玩到結局的夥伴們，你們是如何看待這次的改動呢？'
       , '2024-06-23 10:00:00', '2024-06-24 11:00:00', '0', '0', '0', 'images/forumposttag_img/04.jpg'),
       ( '11', '9', '11'
       , '重要公告：伺服器維護與遊戲更新說明 (6/15)'
       , '各位特戰英豪的玩家們，大家好！

為了提供更穩定的遊戲體驗與更新內容，我們預計將於台灣時間 6 月 15 日（星期六）上午 03:00 至上午 08:00 進行伺服器例行維護。維護期間，遊戲將暫時無法登入。

本次更新的重點包括：

全新特務「極影」：來自未知維度的新控場特務，擁有獨特的煙霧與位移技能，將為戰場帶來更多變數。
地圖「冰盒」平衡性調整：針對 A 點和 B 點的進攻與防守路徑進行優化，提升遊戲體驗。
武器平衡性調整：針對暴徒和幻象的彈道分散進行微調。
錯誤修復：修正部分特務技能的視覺錯誤與遊戲卡頓問題。
請各位玩家提前登出遊戲，以免造成不必要的損失。感謝各位對《特戰英豪》的支持與理解！'
       , '2024-06-24 7:00:00', '2024-06-24 12:00:00', '0', '0', '0', 'images/forumposttag_img/11.jpg'),
       ( '39', '7', '7'
       , '【魔物獵人：世界】冰原 DLC 密技：如何高效刷「歷戰古龍」素材？'
       , '各位獵人們，還在為湊齊歷戰古龍的稀有素材而苦惱嗎？今天我來分享一個在《魔物獵人：世界 冰原》中高效刷**「歷戰古龍」**素材的密技！

這個方法主要利用了**「聚魔之地」**的特定機制。步驟如下：

提升區域等級： 首先，你需要將你的聚魔之地中，目標歷戰古龍出現的區域等級提升到最高 (例如：龍結晶地帶或永霜凍土)。
軟化肉質與破部位： 在狩獵過程中，盡量使用「飛翔爪」軟化魔物肉質，並集中攻擊部位破壞，這能增加「調查」和「特殊痕跡」的掉落機率。
引導特定魔物： 當有你需要的歷戰古龍在場時，盡量透過「引導」功能將牠吸引到有利地形進行狩獵，例如有落石或能觸發環境陷阱的地方。
撿取痕跡： 最重要的環節！即便你無法完成狩獵，每次撿取牠掉落的痕跡和軟化肉質後的掉落物，都能增加該魔物的「調查」進度條。當進度條滿了，就會生成該魔物的「調查任務」，這些任務是獲取歷戰古龍素材的主要來源！
這套方法可以讓你即使手殘無法單刷古龍，也能透過撿痕跡的方式累積調查任務，再組隊去挑戰，大大提高效率！快去試試看吧！'
       , '2024-06-25 13:00:00', '2024-06-26 9:00:00', '0', '0', '0', 'images/forumposttag_img/07.jpg'),
       ( '26', '25', '5'
       , '關於「狸克」與「西施惠」：你們對他們有什麼愛恨情仇？'
       , '各位島主們，我們島上的兩位靈魂人物：狸克和西施惠，在你們心中究竟是怎樣的存在呢？

狸克：這位看似憨厚可愛的貍貓，是不是讓你又愛又恨？從房貸到建設費，他幾乎掌握了你島上所有的金錢流動。但不可否認，他也是你島嶼發展的最大推手。你們對他的印象是什麼？是個黑心資本家，還是個勤勞的島嶼規劃師？

西施惠：這位總是笑臉迎人的秘書，每天向你報告島嶼狀況，有時候還會唱歌。她總是那麼溫柔體貼，但偶爾她給出的「島嶼評價」卻讓你摸不著頭緒。你們喜歡她的存在嗎？會不會覺得她有些過於「完美」？

歡迎大家分享你與這兩位動森「公務員」的愛恨情仇，或是任何有趣的互動故事！'
       , '2024-06-26 16:00:00', '2024-06-26 21:00:00', '0', '0', '0', 'images/forumposttag_img/05.jpg'),
       ( '21', '2', '7'
       , '神級密技！無限刷「大師劍」耐久度！'
       , '各位海拉魯的勇者們，你的大師劍又沒電了嗎？別擔心！今天我來分享一個在《薩爾達傳說：曠野之息》中可以無限刷「大師劍」耐久度的神級密技！讓你隨時都有趁手的大師劍可以用！

這個方法主要是利用遊戲中特定機制來重置大師劍的耐久，步驟如下：

引爆大師劍耐久： 首先，你需要把大師劍用光耐久，讓它進入「冷卻狀態」。
前往「樹林驛站」： 傳送到海拉魯地圖東部的「樹林驛站」附近。
進入「試煉之塔」： 在驛站旁邊，你會看到一個「試煉之塔」的入口。
保存與重載： 在進入塔之前，先進行一次手動存檔。然後進入塔內，馬上再載入剛剛的手動存檔。
檢查大師劍： 退出塔後，你就會發現你的大師劍已經恢復滿耐久了！
這個密技可以讓你省去等待大師劍充電的時間，隨時都能在戰鬥中使用這把神劍！快去試試看，成為永不熄滅的大師劍使用者吧！'
       , '2024-06-27 9:00:00', '2024-06-28 10:00:00', '0', '0', '0', 'images/forumposttag_img/07.jpg'),
       ( '24', '11', '8'
       , '線上模式大家都在幹嘛？我只是個賣車的富翁...'
       , '各位洛聖都的老哥們，玩《GTA V》線上模式這麼多年，你們現在都在幹嘛？

我從最一開始做搶劫任務賺錢，然後買了豪宅、買了各種超跑、遊艇、私人飛機，也體驗了各種生意：地堡、夜總會、摩托幫會所... 現在感覺遊戲裡能買的都買了，能賺的錢也夠多了，每天上線真的有點不知道要做什麼。

有時候就開著我的噴射背包在天上飛來飛去，或是開著超跑在街上亂逛，偶爾跟朋友玩玩小任務。感覺自己就是個「無所事事的富翁」，不知道這遊戲還能給我帶來什麼新的刺激。

大家在線上模式有沒有什麼新的目標或有趣的玩法？或是你們現在上線都在做什麼？求分享一些能讓我重燃熱情的遊戲內容！'
       , '2024-06-28 11:00:00', '2024-06-28 16:00:00', '0', '0', '0', 'images/forumposttag_img/08.jpg'),
       ( '33', '36', '10'
       , '尋找舞團成員！想組一個固定練功、出賽的實力派舞團！'
       , '各位勁舞團的舞者們，好久不見！

我是個熱愛勁舞團的老玩家，最近回來發現少了當初一起練功、一起出賽的夥伴，有點寂寞。因此，我決定號召一群有實力、有熱情的舞者，共同組建一個固定練功、以出賽為目標的實力派舞團！

我們希望團員具備以下特質：

一定的技術水平：不要求是大神，但至少能穩定過高難度歌曲。
積極的練習態度：願意花時間提升技術，一起研究舞步。
良好的溝通能力：能夠配合團隊，接受建議。
活潑開朗、不玻璃心：輸贏乃兵家常事，互相鼓勵最重要！
如果你也厭倦了單打獨鬥，想找一群志同道合的夥伴一起進步，歡迎在下方留言你的遊戲 ID、擅長模式/歌曲難度，並簡述你的遊戲經歷。期待與你們一起在舞池中閃耀！'
       , '2024-06-29 8:00:00', '2024-06-29 13:00:00', '0', '0', '0', 'images/forumposttag_img/10.jpg'),
       ( '30', '2', '6'
       , '【NBA 2K24】MyCAREER 神射手養成攻略：徽章、投籃選擇與訓練指南！'
       , '各位籃球迷們，想在《NBA 2K24》的 MyCAREER 模式中成為一名百步穿楊的神射手嗎？這篇攻略將為你詳細拆解神射手的養成之路！

天賦與身體數據：
身高/臂展/體重：推薦能兼顧投籃出手速度與防守干擾的黃金比例。
能力值加點優先順序：外線投籃、中距離投籃、罰球等核心數據的分配策略。
核心徽章推薦：
投籃類徽章：綠色機器、急停跳投、狙擊手等必備徽章的升級優先順序。
組織/防守輔助徽章：如何搭配能讓神射手更有生存空間。
投籃包與跳投動作： 選擇適合你手感的投籃動作，以及如何透過訓練找到你的「綠色時機」。
訓練模式與戰術應用：
球隊訓練：如何有效提升能力與徽章進度。
比賽中的投籃選擇：空位投籃、擋拆後的投籃、運球後跳投的時機把握。
學會這些，你就能在 MyCAREER 中成為一個讓對手膽寒的三分射手！有任何問題，歡迎在下方留言交流！'
       , '2024-06-30 15:00:00', '2024-07-01 12:00:00', '0', '0', '0', 'images/forumposttag_img/06.jpg'),
       ( '5', '29', '5'
       , '你們最愛的兵種組合是什麼？求推薦最強搭配！'
       , '各位指揮官們，玩《聖獸之王》這麼久，大家有沒有發現哪個兵種組合特別好用、或是特別有趣？我個人目前在嘗試「天使+弓手+雙盾」的組合，感覺輸出和防禦兼備，但遇到一些特殊敵人時還是會卡關。

這遊戲的兵種搭配真的變化多端，每個角色、每個兵種都有獨特的技能和弱點，排列組合起來簡直是學問！

有沒有哪個兵種組合讓你們印象深刻，或是覺得在推圖、競技場特別強勢？例如：專打魔法單位的、高輸出秒殺流的、或是怎麼都打不死的磨隊？大家快來分享你們的「夢幻部隊」配置，以及背後的戰術考量吧！'
       , '2024-07-01 10:00:00', '2024-07-01 14:00:00', '0', '0', '0', 'images/forumposttag_img/05.jpg'),
       ( '23', '5', '4'
       , '我的學院選擇心得：葛萊芬多真的最香嗎？'
       , '各位霍格華茲的同學們，你們當初選擇哪個學院？玩到現在，你們對自己的學院選擇後悔過嗎？

我當初是毫不猶豫地選擇了葛萊芬多，因為覺得主角就是該有勇氣和冒險精神。玩下來的確體驗到了許多熱血的劇情，但後來跟朋友聊，才發現每個學院的支線任務和休息室都有獨特的風格。

聽說史萊哲林有比較多黑暗魔法的彩蛋？雷文克勞的謎題也很有趣？赫夫帕夫則有溫馨的社群感？

我個人是覺得葛萊芬多的確很「王道」，但或許錯過了其他學院的獨特魅力。不知道大家有沒有體驗過其他學院的劇情？你們覺得哪個學院的體驗最棒、最有代入感？歡迎大家分享自己的學院選擇心得，以及有沒有「如果重來一次，我會選擇…」的想法！'
       , '2024-07-02 12:00:00', '2024-07-03 9:00:00', '0', '0', '0', 'images/forumposttag_img/04.jpg'),
       ( '31', '8', '6'
       , '【MLB The Show 24】新手入門攻略：Diamond Dynasty 模式快速上手指南！'
       , '各位棒球迷們，剛踏入《MLB The Show 24》的 Diamond Dynasty (鑽石王朝) 模式是不是有點手足無措？別擔心！這篇攻略將為你提供快速上手的基本指南！

了解基本玩法： DD 模式的核心是收集球員卡片，組建你的夢幻隊伍，並在各種模式中對戰。
初期卡片獲取：
時刻挑戰 (Moments)： 完成這些小型挑戰能快速獲得球員卡和獎勵。
程式 (Programs)： 透過完成特定任務和挑戰，累積進度並解鎖更強大的球員卡。
征服 (Conquest)： 類似大富翁的戰略模式，初期能獲得大量新手卡包和獎勵。
隊伍組建技巧：
優先補強投手陣容： 先發和後援投手的強度對勝負影響很大。
注重打者能力： 選擇具備力量、接觸、速度等不同打擊屬性的球員。
解鎖隊伍核心： 許多強大的球員卡需要透過收集特定系列的卡片來解鎖。
練習模式與手感： 在自訂打擊/投球練習中，熟悉不同球員的投打動畫，找到最適合自己的設定。
掌握這些基本原則，你就能在 Diamond Dynasty 模式中更快地建立起一支具有競爭力的隊伍，享受收集與對戰的樂趣！有任何問題，歡迎在下方留言討論！'
       , '2024-07-03 7:00:00', '2024-07-03 12:00:00', '0', '0', '0', 'images/forumposttag_img/06.jpg'),
       ( '35', '23', '8'
       , '那些年，你遇過最奇葩/好笑的船員或內鬼行為？'
       , '各位太空人/內鬼們，玩《Among Us》這麼久，有沒有遇過什麼讓你哭笑不得、或是超級奇葩的遊戲行為？

我上次當內鬼，結果一開始就隊友自爆，然後我傻傻地去修電線，被船員誤認為是好人，最後還幫我投票贏了，真的是笑死！還有遇過船員一直跟著我，結果被我騙進黑屋直接刀掉的，超有成就感！

大家在遊戲中有沒有什麼特別好笑、特別荒謬，或是超級天才的內鬼/船員操作？快來分享你的「奇葩遊戲經歷」，讓大家一起笑一笑，回味一下這款遊戲的魅力！'
       , '2024-07-04 14:00:00', '2024-07-05 11:00:00', '0', '0', '0', 'images/forumposttag_img/08.jpg'),
       ( '4', '16', '11'
       , '重磅！第二波 DLC「金碟遊樂園：盛夏慶典」即將登場！'
       , '各位英雄們，準備好迎接盛夏了嗎？我們很高興地宣布，《最終幻想VII 重生》的第二波 DLC 「金碟遊樂園：盛夏慶典」 即將在台灣時間 2025 年 7 月 25 日 正式上線！

本次 DLC 將帶領玩家回到人聲鼎沸的金碟遊樂園，體驗全新的夏日慶典內容：

新增限定小 挑戰「水上摩托車競速大賽」、「魔晶石對戰進化版」等令人腎上腺素飆升的夏日專屬小遊戲。
全新角色服裝： 為克勞德、蒂法、艾莉絲、巴雷特、赤紅十三等角色設計的夏日限定泳裝與慶典服飾，讓你在金碟遊樂園成為最閃亮的焦點。
支線劇情「金碟之戀」： 體驗一段圍繞著金碟遊樂園的浪漫夏日戀曲，解鎖意想不到的獎勵與劇情彩蛋。
挑戰模式更新： 新增多個高難度競技場挑戰，考驗你的戰鬥技巧與團隊配合。
請各位玩家確保遊戲已更新至最新版本。準備好在金碟遊樂園與你的夥伴們共度一個難忘的盛夏吧！'
       , '2024-07-05 9:00:00', '2024-07-05 14:00:00', '0', '0', '0', 'images/forumposttag_img/11.jpg'),
       ( '12', '13', '10'
       , '穩定白金/鑽石三排！尋求一位主打擊位或突破手！'
       , '各位 Apex 傳奇們，我跟朋友穩定雙排，目前在白金一/鑽石四之間徘徊，急需一位強力的第三位隊友來衝擊更高段位！

我們的主力英雄是**「尋血犬」和「惡靈」**，希望你能補足隊伍的輸出或突破能力。我們尋找的夥伴條件如下：

專精打擊位或突破手： 例如：惡靈、班加羅爾、動力小子、瓦爾基里等，能穩定輸出並製造擊殺。
槍法穩定且有團隊意識： 不單打獨鬥，能配合隊友進行戰術執行。
有麥克風且積極溝通： 能在遊戲中即時報點、討論戰術。
心態良好，不玻璃心： 輸贏很正常，能從失誤中學習並共同進步。
如果你也是一位想上分的 Apex 玩家，歡迎在下方留言你的遊戲 ID、主玩英雄，並附上你的遊玩時段。期待與你一起在競技場上叱吒風雲！'
       , '2024-07-06 11:00:00', '2024-07-07 8:00:00', '0', '0', '0', 'images/forumposttag_img/10.jpg'),
       ( '17', '27', '1'
       , '你的第一台「傳說車」是什麼？難忘的入手故事！'
       , '各位跑跑玩家們，你們還記得自己入手**第一台「傳說車」**是什麼時候、哪一台嗎？那種興奮和激動的心情，是不是到現在還記憶猶新？

我記得我的第一台傳說車是**「棉花糖」**！當時為了抽到它，存了好久的電池，每天都虔誠地拜拜，最後真的在一次十連抽中看到它的身影，簡直開心到飛起來！從此之後，我的競速成績也大大提升，感覺自己瞬間變成「車神」！

雖然現在遊戲出了很多更強的車，但棉花糖在我心中永遠有特殊地位。大家快來分享你的第一台傳說車是什麼，以及你與它之間有什麼難忘的入手故事或使用心得吧！'
       , '2024-07-07 16:00:00', '2024-07-07 20:00:00', '0', '0', '0', 'images/forumposttag_img/01.jpg'),
       ( '22', '5', '7'
       , '超實用！教你如何無限刷金錢與經驗值！'
       , '各位冒險者們，想在《柏德之門3》中體驗當個「財大氣粗」的富翁，或是快速升級碾壓敵人嗎？今天我來分享一個無限刷金錢與經驗值的超實用密技！

這個密技主要利用了交易機制和一些特定物品的特性，步驟如下：

找到「販賣機」： 在遊戲中找到任何一個商人。
準備「物品」： 準備一些可以拆分或合成的物品，例如「箭矢」、「藥水」、「投擲物」等。
無限交易大法：
將你準備的物品放入交易欄位。
在交易介面中，透過「拆分」或「合成」該物品，使其數量不斷增加或減少，但每次操作都會產生微小的金錢盈餘或經驗值。
重複此操作，你就能無限獲取金錢和經驗值！
這個密技雖然有點像是在鑽遊戲漏洞，但對於想快速體驗不同 Build 或只是想輕鬆遊玩的玩家來說，絕對是個福音。快去試試看，讓你的隊伍一夜暴富吧！'
       , '2024-07-08 10:00:00', '2024-07-09 13:00:00', '0', '0', '0', 'images/forumposttag_img/07.jpg'),
       ( '10', '20', '5'
       , '手把與鍵鼠的永恆之爭！到底誰更適合？'
       , '各位《決勝時刻》的玩家們，這真的是一個永恆的爭論：到底是用手把 (控制器) 玩，還是用鍵盤滑鼠 (鍵鼠) 玩更強？

我個人是鍵鼠玩家，習慣了精準的瞄準和快速的轉身，感覺在遠距離對槍和反應速度上有優勢。但每次遇到一些手把玩家，他們的鎖定輔助好像都特別黏，近距離對槍常常被反殺，真的會讓人質疑人生！

社群裡關於這個話題的討論從來沒停過，有人說手把有「鎖定輔助」，有人說鍵鼠的「自由度」更高。你們覺得在《決勝時刻》的環境下，哪個輸入方式更有優勢？或者有什麼特別的打法，能讓其中一方的優勢被放大？歡迎大家理性討論，分享你們的心得！'
       , '2024-07-09 8:00:00', '2024-07-09 12:00:00', '0', '0', '0', 'images/forumposttag_img/05.jpg'),
       ( '16', '19', '4'
       , '墨西哥地圖最美拍照點分享！你的「車圖」在哪拍？'
       , '各位《極限競速 地平線5》的車手和攝影師們，你們是不是也像我一樣，除了跑車和改裝，也很喜歡在墨西哥廣闊的地圖上尋找最美的風景來拍下你的「車圖」？

這遊戲的畫面真的美到不行，從陽光普照的沙漠、茂密的熱帶雨林、到歷史悠久的古蹟和熱鬧的城市，每個地方都能拍出大片感。

我個人最喜歡在瓜納華托的彩色小鎮和火山頂的日落時分拍照，那裡的燈光和氛圍真的絕美！每次拍完都忍不住發朋友圈炫耀。

大家有沒有什麼私藏的**「墨西哥最美拍照點」**？或是你拍過哪些讓你特別滿意的「車圖」？快來分享你的神級攝影作品，一起感受《地平線5》的極致美學吧！'
       , '2024-07-10 13:00:00', '2024-07-11 10:00:00', '0', '0', '0', 'images/forumposttag_img/04.jpg'),
       ( '37', '10', '6'
       , 'SXX 賽季 AD 位上分指南：下路對線、團戰站位與英雄選擇！'
       , '各位召喚師們，新賽季開始後，下路 AD 位又有了新的變化！如果你想在 SXX 賽季穩穩上分，這篇 AD 位攻略將為你提供全面的指導！

對線期處理：
壓制與控線： 如何根據不同輔助搭配，選擇激進或保守的對線策略。
補刀與換血： 最大化經濟收益，同時消耗對手。
防範打野 Gank： 眼位佈置與意識判斷。
團戰站位與輸出：
安全輸出位置： 如何在團戰中尋找輸出空間，避免被對方切入。
目標選擇： 優先擊殺威脅最高的敵人，還是穩定輸出前排？
技能釋放時機： 大招和保命技能的把握。
主流 AD 英雄選擇與搭配：
推薦當前版本強勢的 AD 英雄，如：凱莎、吉茵珂絲、燼，並分析其優勢與適用陣容。
如何搭配不同類型的輔助，發揮最大戰力。
掌握這些核心要點，你就能在下路打出優勢，並在團戰中扮演關鍵的輸出角色，帶領隊伍走向勝利！有任何問題或更多心得，歡迎在下方留言交流！'
       , '2024-07-11 7:00:00', '2024-07-11 11:00:00', '0', '0', '0', 'images/forumposttag_img/06.jpg'),
       ( '27', '34', '2'
       , '卡關求助！XX 關怎麼都過不去？有沒有通關技巧？'
       , '各位《夢幻家園》的大神們，救命啊！我被第 XXXX 關 (請自行填寫關卡數字) 卡死了，已經好幾天了，星星都快用完了，怎麼玩都過不去！

這一關的目標是要收集 XX 個方塊，但障礙物特別多，而且步數非常有限。我試過優先消除炸彈，也試過集中火力合成道具，但總是差那麼一點點。

有沒有已經通關這關的大佬，可以分享一下你們的通關思路或技巧？有沒有什麼道具合成的順序或消除特定方塊的策略？我真的快被這關逼瘋了，急需支援啊！'
       , '2024-07-12 15:00:00', '2024-07-13 12:00:00', '0', '0', '0', 'images/forumposttag_img/02.jpg'),
       ( '3', '18', '11'
       , '緊急維護公告：伺服器異常與 Bug 修正 (6/4)'
       , '各位褪色者們，非常抱歉打擾各位在交界地的冒險！

我們已偵測到部分伺服器出現異常狀況，導致連線不穩及部分玩家遊戲崩潰問題。為了確保遊戲體驗，我們將於台灣時間 6 月 4 日 (星期三) 下午 2:00 至 下午 5:00 進行緊急伺服器維護。

本次維護主要針對以下問題進行修正：

伺服器連線穩定性優化： 解決部分地區玩家連線品質不佳的問題。
Boss 戰特定 Bug： 修復「熔爐騎士」和「腐敗樹靈」在特定情況下可能導致遊戲崩潰的錯誤。
物品複製漏洞： 針對近期發現的物品複製漏洞進行修復。
維護期間，遊戲將無法登入。請各位玩家務必提前登出，以免造成資料遺失。對於此次緊急維護造成的不便，我們深感抱歉。感謝各位褪色者的理解與支持！'
       , '2024-07-13 9:00:00', '2024-07-13 14:00:00', '0', '0', '0', 'images/forumposttag_img/11.jpg'),
       ( '25', '35', '6'
       , '新手生存攻略：從砍樹到地獄堡壘！'
       , '剛進入《Minecraft》世界的新手們，是不是對這個方塊世界感到既興奮又迷茫？別擔心！這篇攻略將帶你從零開始，一步步掌握生存的訣竅，最終挑戰地獄堡壘！

第一天生存要點：
砍樹與合成工作台： 獲取木材，製作基礎工具。
挖掘與庇護所： 找到煤炭，建造簡陋的防禦工事，度過第一個夜晚。
食物來源： 狩獵動物或種植作物，解決飢餓問題。
中期發展：
礦物收集： 探索洞穴，獲取鐵、黃金、鑽石等稀有資源。
農場與牧場： 建立穩定食物來源，獲取皮革、羊毛等材料。
附魔與釀造： 提升裝備強度與製作藥水。
地獄探險：
建造下界傳送門： 準備好黑曜石和打火石，進入地獄。
尋找地獄堡壘： 掌握地獄生物的特性，收集重要資源如烈焰棒。
從基礎生存到挑戰地獄，一步步帶你成為《Minecraft》的生存大師！祝各位冒險者在方塊世界中玩得愉快！'
       , '2024-07-14 11:00:00', '2024-07-15 9:00:00', '0', '0', '0', 'images/forumposttag_img/06.jpg'),
       ( '21', '14', '8'
       , '海拉魯最美的風景在哪？你的「拍照景點」大公開！'
       , '各位林克們，在《薩爾達傳說：王國之淚》廣闊的海拉魯大陸上，你們有沒有發現哪些特別讓你驚豔的**「拍照景點」**？

這遊戲的風景真的太美了！從壯麗的空島、神秘的地下世界、到熟悉的地面海拉魯，每個角落都充滿驚喜。我最喜歡在拉聶爾濕地高塔頂端看日出，那種視野真的讓人心曠神怡；還有格魯德沙漠的星空，美到讓我想一直駐足欣賞。

大家有沒有什麼私藏的絕美拍照點，或是你在哪裡拍過最喜歡的遊戲截圖？快來分享你的「海拉魯美景清單」，讓大家一起感受這個世界的魅力吧！'
       , '2024-07-15 14:00:00', '2024-07-15 19:00:00', '0', '0', '0', 'images/forumposttag_img/08.jpg'),
       ( '26', '30', '10'
       , '尋找「大頭菜」交易夥伴！一起賺取鈴錢財富！'
       , '各位島主們，你們還在為大頭菜價格波動而煩惱嗎？現在是時候加入我們，一起穩定賺取鈴錢財富了！

我們正在尋找一群活躍的**「大頭菜交易夥伴」**，目標是建立一個互助互惠的社群，在彼此島嶼上進行大頭菜買賣，避開低價區，追逐高價！

我們提供：

即時大頭菜價格資訊： 社群內會即時分享島上大頭菜價格，方便大家掌握商機。
穩定且開放的島嶼： 在價格高時，會開放島嶼供夥伴來交易，確保交易順暢。
友好互助的氛圍： 彼此分享島嶼資源、交流佈置心得。
如果你也想告別單打獨鬥的大頭菜炒股生涯，歡迎加入我們！請在下方留言你的遊戲 ID 和 Switch FC，我們會私訊你加入方式。一起讓鈴錢滿到爆倉吧！'
       , '2024-07-16 10:00:00', '2024-07-17 11:00:00', '0', '0', '0', 'images/forumposttag_img/10.jpg'),
       ( '22', '27', '11'
       , '感謝百萬玩家！Patch 7 重磅更新預告！'
       , '親愛的冒險者們，感謝你們將《柏德之門3》的冒險帶入費倫大陸！我們非常興奮地宣布，遊戲銷量已突破數百萬份！為了回饋廣大玩家的支持，我們即將推出規模最大的更新檔 Patch 7！

本次 Patch 7 預計於台灣時間 2025 年 8 月 15 日 上線，主要更新內容包括：

全新可玩種族與職業子類： 解鎖「亞龍人」種族以及多個全新職業子類，為你的角色扮演提供更多選擇。
夥伴劇情擴展： 深入探索更多夥伴的個人故事線，新增多個浪漫選項與互動橋段。
終局內容擴充： 針對遊戲後期內容進行優化與擴充，加入新的高難度挑戰與隱藏 Boss。
效能優化與 Bug 修正： 大幅提升遊戲運行效能，並修正超過上百個已知錯誤。
我們將持續致力於提供最棒的遊戲體驗。感謝你們的耐心與支持，期待與你們在更新後的費倫大陸相見！'
       , '2024-07-17 8:00:00', '2024-07-17 13:00:00', '0', '0', '0', 'images/forumposttag_img/11.jpg'),
       ( '17', '17', '4'
       , '我的競速生涯！從「甩尾菜鳥」到「彎道戰神」的心得分享！'
       , '各位跑跑卡丁車的車手們，還記得你們剛開始玩這款遊戲時的青澀模樣嗎？我從一個連甩尾都會撞牆的「菜鳥」，到現在能在彎道中與高手一較高下的「戰神」，真的很有感觸！

一開始我只會傻傻地加速，連最基本的拖飄都搞不懂。但後來透過看高手教學影片、反覆練習特定賽道、甚至挑戰自己不擅長的彎道，才慢慢摸索出適合自己的跑法。那種從失誤連連到完美過彎的成就感，真的無可替代！

現在，我雖然稱不上頂尖高手，但至少能在排位賽中穩定上分。這不僅是技術的提升，更是對耐心和毅力的考驗。大家在跑跑生涯中有沒有什麼特別的「轉折點」或心得，讓你從新手變成老司機的？快來分享你的飆車故事吧！'
       , '2024-07-18 12:00:00', '2024-07-19 10:00:00', '0', '0', '0', 'images/forumposttag_img/04.jpg'),
       ( '13', '32', '5'
       , '新版本「戰術裝備」對遊戲平衡的影響？你支持還是反對？'
       , '各位雞友們，新版本推出的「戰術裝備」系統，在社群中引起了廣泛討論。從能快速修復載具的「維修套件」、到能偵測敵人的「無人機」，這些裝備的加入無疑改變了遊戲的策略深度。

有人認為，這些戰術裝備增加了遊戲的變數和可玩性，讓戰局更加豐富有趣；但也有不少玩家抱怨，部分裝備的強度過高，導致遊戲平衡被破壞，甚至讓一些純粹的槍法對決變得不再那麼重要。

你們覺得「戰術裝備」的引入對《PUBG》的遊戲體驗是好是壞？你支持這個系統，還是希望官方能進行調整，甚至移除部分裝備？歡迎大家分享自己的看法，一起探討這個熱門話題！'
       , '2024-07-19 9:00:00', '2024-07-19 14:00:00', '0', '0', '0', 'images/forumposttag_img/05.jpg'),
       ( '30', '21', '8'
       , '生涯模式最難忘的「對手」！誰讓你又愛又恨？'
       , '各位《NBA 2K》生涯模式的玩家們，在你的 MyCAREER 之路中，有沒有哪個「對手」讓你印象特別深刻？

可能是選秀時和你競爭的宿敵，可能是每次對位都把你守到發瘋的鐵血防守者，也可能是你不斷想超越的聯盟巨星。那個對手可能讓你又愛又恨，但同時也激勵你不斷進步。

我記得我的生涯模式裡，有一個每次都會被電腦操控得像神一樣的「XXX」(可自行填寫球員名稱)，每次遇到他，我都必須拿出百分之兩百的實力才能險勝。打敗他後的成就感，比贏下任何總冠軍都還滿足！

大家有沒有這種讓你難忘的「生涯模式對手」？快來分享你們的故事，一起回味那些在球場上的「愛恨情仇」吧！'
       , '2024-07-20 15:00:00', '2024-07-21 11:00:00', '0', '0', '0', 'images/forumposttag_img/08.jpg'),
       ( '39', '18', '8'
       , '狩獵的藝術：《魔物獵人》系列為何如此引人入勝？'
       , '自初代發售以來，《魔物獵人》系列便以其獨特的狩獵體驗征服了無數玩家。它不僅僅是一款動作遊戲，更是一款需要玩家耐心觀察、策略思考、團隊合作的「狩獵模擬器」。從武器的選擇、裝備的搭配，到對魔物習性的研究和狩獵路線的規劃，每一個細節都充滿了深度。而成功狩獵到強大魔物時的成就感，更是其他遊戲難以比擬的。《魔物獵人》的魅力，就在於這份硬核卻又充滿樂趣的狩獵過程。'
       , '2024-07-21 8:00:00', '2024-07-21 11:00:00', '7', '7', '1', 'images/forumposttag_img/08.jpg'),
       ( '39', '27', '6'
       , '新手獵人必看：《魔物獵人：世界》入門指南'
       , '對於剛踏入《魔物獵人》世界的玩家來說，龐大的系統和各式各樣的魔物可能會讓人感到不知所措。《魔物獵人：世界》作為系列在次世代主機上的首次亮相，在畫面和操作上都進行了大幅優化，更加入了許多方便新手的設計。本指南將從基礎操作、武器選擇、裝備製作等方面，幫助新手獵人快速上手，享受狩獵的樂趣。'
       , '2024-07-22 8:00:00', '2024-07-23 8:00:00', '3', '5', '2', 'images/forumposttag_img/06.jpg'),
       ( '39', '15', '8'
       , '回味經典：《魔物獵人：攜帶版2nd G》的那些年'
       , '對於許多老獵人來說，《魔物獵人：攜帶版2nd G》（MHP2G）絕對是系列中最具代表性的一作。豐富的任務、各具特色的魔物、以及與朋友聯機狩獵的熱情回憶，都讓這款遊戲在玩家心中留下了深刻的印記。即使在多年後的今天，MHP2G依然是一款值得回味的經典之作。'
       , '2024-07-23 8:00:00', '2024-07-19 14:00:00', '5', '3', '1', 'images/forumposttag_img/08.jpg'),
       ( '39', '12', '6'
       , '武器大師之路：《魔物獵人》系列武器全解析'
       , '《魔物獵人》系列擁有豐富多樣的武器種類，從攻守兼備的片手劍，到威力驚人的大劍，再到靈活多變的雙劍和弓箭，每一種武器都有其獨特的特性和操作技巧。選擇一把適合自己的武器，是成為一名優秀獵人的第一步。本文將對系列中主要的武器進行詳細解析，幫助玩家找到最適合自己的狩獵夥伴。'
       , '2024-07-24 8:00:00', '2024-07-24 8:00:00', '5', '4', '1', 'images/forumposttag_img/06.jpg'),
       ( '39', '33', '8'
       , '生態的奧秘：《魔物獵人》系列魔物生態考察'
       , '《魔物獵人》系列的世界不僅僅是狩獵場，更是一個充滿生機的生態系統。每一種魔物都有其獨特的棲息地、行為模式和捕食習慣。深入了解魔物的生態，不僅能幫助獵人更好地進行狩獵，更能讓玩家感受到這個世界的真實與魅力。'
       , '2024-07-25 8:00:00', '2024-07-25 8:00:00', '6', '3', '1', 'images/forumposttag_img/08.jpg'),
       ( '39', '23', '8'
       , '從《世界》到《崛起》：《魔物獵人》系列的進化與創新'
       , '《魔物獵人：世界》和《魔物獵人：崛起》是系列近年來的兩部重要作品。前者以其無縫地圖和細膩的畫面表現，將狩獵體驗提升到了一個新的高度；後者則加入了翔蟲等全新的移動和戰鬥機制，帶來了更加快速和靈活的狩獵體驗。本文將對這兩部作品的進化與創新進行比較分析。'
       , '2024-07-26 8:00:00', '2024-07-26 8:00:00', '6', '4', '1', 'images/forumposttag_img/08.jpg'),
       ( '39', '15', '4'
       , '多人狩獵的樂趣：《魔物獵人》系列聯機體驗分享'
       , '與朋友一起組隊狩獵，是《魔物獵人》系列的核心樂趣之一。通過互相配合、共同挑戰強大的魔物，玩家們不僅能體驗到團隊合作的魅力，更能建立深厚的友誼。本文將分享一些多人狩獵的經驗和技巧，幫助玩家更好地享受聯機狩獵的樂趣。'
       , '2024-07-27 8:00:00', '2024-07-27 8:00:00', '7', '5', '1', 'images/forumposttag_img/04.jpg'),
       ( '39', '6', '6'
       , '裝備控的福音：《魔物獵人》系列裝備製作與搭配指南'
       , '在《魔物獵人》的世界裡，一套好的裝備是獵人狩獵成功的關鍵。通過收集素材、打造和強化裝備，獵人可以提升自己的攻擊力、防禦力以及各種技能。本文將詳細介紹裝備的製作流程、屬性以及技能搭配的技巧，幫助玩家打造出最適合自己的狩獵裝備。'
       , '2024-07-28 8:00:00', '2024-07-28 8:00:00', '7', '4', '1', 'images/forumposttag_img/06.jpg'),
       ( '39', '2', '6'
       , '挑戰極限：《魔物獵人》系列高難度魔物攻略'
       , '《魔物獵人》系列中總有一些讓獵人們聞風喪膽的強大魔物。它們不僅擁有驚人的攻擊力和血量，更具備複雜的攻擊模式和特殊的技能。挑戰這些高難度魔物，是證明獵人實力的最好方式。本文將分享一些挑戰高難度魔物的攻略和心得。'
       , '2024-07-29 8:00:00', '2024-07-29 8:00:00', '7', '4', '1', 'images/forumposttag_img/06.jpg'),
       ( '39', '17', '8'
       , '《魔物獵人》系列未來展望：我們還期待什麼？'
       , '從最初的簡陋畫面到如今的精美呈現，《魔物獵人》系列一直在不斷進化和創新。在《魔物獵人：崛起》獲得成功之後，玩家們也開始期待系列的下一部作品將會帶來哪些新的驚喜。本文將對《魔物獵人》系列的未來進行展望，並探討玩家們可能期待的新要素和方向。'
       , '2024-07-30 8:00:00', '2024-08-02 8:00:00', '8', '4', '1', 'images/forumposttag_img/08.jpg'),
       ( '39', '2', '1'
       , '大家今天午餐吃什麼'
       , '想不到午餐要吃什麼，拜託大家給個建議'
       , '2024-07-31 8:00:00', '2024-07-31 8:00:00', '6', '0', '8', 'images/forumposttag_img/01.jpg');


-- 建立 檢舉類型 表格 --
CREATE TABLE REPORT_TYPE
(
    `RPI_NO`   INT AUTO_INCREMENT NOT NULL PRIMARY KEY COMMENT '檢舉類型編號',
    `RPI_TYPE` VARCHAR(255)       NOT NULL COMMENT '檢舉類型項目'
) COMMENT '檢舉類型';

-- 新增 檢舉類型 資料 --
INSERT INTO REPORT_TYPE (`RPI_TYPE`)
VALUES ('與本版主題無關'),
       ('洗版'),
       ('謾罵'),
       ('限制級'),
       ('詐騙、詐欺或不實資訊'),
       ('其他');


-- 建立 文章檢舉 表格 --
CREATE TABLE ARTICLE_REPORT
(
    `NO`             INT AUTO_INCREMENT NOT NULL PRIMARY KEY COMMENT '檢舉編號',
    `REPORTER`       INT COMMENT '檢舉者編號',
    `RPI_NO`         INT COMMENT '檢舉類型編號',
    `POST_NO`        INT COMMENT '文章編號',
    `ART_REP_STATUS` CHAR(1)NOT NULL DEFAULT '0' COMMENT '處理狀態',
    `CREATE_TIME`    DATETIME                    DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    `FINISH_TIME`    DATETIME,
    FOREIGN KEY (`REPORTER`) REFERENCES MEMBER (`MEM_NO`) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (`RPI_NO`) REFERENCES REPORT_TYPE (`RPI_NO`) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (`POST_NO`) REFERENCES FORUM_POST (`POST_NO`) ON DELETE SET NULL ON UPDATE CASCADE
) COMMENT '文章檢舉';

-- 新增 文章檢舉 資料 --
INSERT INTO ARTICLE_REPORT (`REPORTER`, `RPI_NO`, `POST_NO`)
VALUES (22, 1, 57);


-- 建立 文章留言 表格 --
CREATE TABLE FORUM_MES
(
    `MES_NO`       INT auto_increment primary KEY COMMENT '文章留言編號',
    `POST_NO`      INT COMMENT '文章編號',
    `MEM_NO`       INT COMMENT '會員編號',
    `MES_CON`      VARCHAR(500) COMMENT '留言內容',
    `MES_CRDATE`   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '創建時間',
    `MES_UPDATA`   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    `MES_STATUS`   CHAR(1)  DEFAULT '0',
    `MES_LIKE_LC`  INT      DEFAULT 0,
    `MES_LIKE_DLC` INT      DEFAULT 0,
    FOREIGN KEY (`POST_NO`) REFERENCES FORUM_POST (`POST_NO`) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (`MEM_NO`) REFERENCES MEMBER (`MEM_NO`) ON DELETE SET NULL ON UPDATE CASCADE
) COMMENT '文章留言';

-- 新增 文章留言 資料 --
INSERT INTO `FORUM_MES` (`POST_NO`, `MEM_NO`, `MES_CON`, `MES_CRDATE`, `MES_UPDATA`, `MES_LIKE_LC`, `MES_LIKE_DLC`)
VALUES ('47', '15', '真的！每次成功狩獵大型魔物都超有成就感！那種團隊合作的感覺也很棒。', '2025-06-21 8:20:00',
        '2025-06-21 8:20:00', '0', '0'),
       ('47', '1', '說到心坎裡了！我就是喜歡這種需要研究和準備的硬核感。', '2025-06-21 8:20:00', '2025-06-21 8:20:00',
        '0', '0'),
       ('47', '30', '武器和裝備系統真的太豐富了，光是配裝就能玩很久。', '2025-06-21 8:20:00', '2025-06-21 8:20:00', '0',
        '0'),
       ('47', '9', '完全同意！這不是無腦砍怪的遊戲，策略性很重要。', '2025-06-21 8:20:00', '2025-06-21 8:20:00', '0', '0'),
       ('47', '20', '從PSP時代就開始玩了，這種狩獵的醍醐味一直沒變。', '2025-06-21 8:20:00', '2025-06-21 8:20:00', '0',
        '0'),
       ('47', '31', '這篇指南太實用了！剛入坑真的很多東西看不懂。', '2025-06-21 8:20:00', '2025-06-21 8:20:00', '0', '0'),
       ('47', '25', '感謝分享！武器選擇真的好糾結，每種都想試試看。', '2025-06-21 8:20:00', '2025-06-21 8:20:00', '0',
        '0'),
       ('48', '8', '對新手很友善，很多基礎知識都有提到。', '2025-06-22 8:20:00', '2025-06-22 8:20:00', '0', '0'),
       ('48', '5', '看完這篇感覺比較有方向了，準備開始我的獵人生涯！', '2025-06-22 8:20:00', '2025-06-22 8:20:00', '0',
        '0'),
       ('48', '22', '《世界》的畫面真的讚，玩起來很沉浸。', '2025-06-22 8:20:00', '2025-06-22 8:20:00', '0', '0'),
       ('49', '32', 'MHP2G絕對是神作！當年跟朋友一起聯機的回憶太美好。', '2025-06-22 8:20:00', '2025-06-22 8:20:00', '0',
        '0'),
       ('49', '29', '我也是從這代開始入坑的，永遠的經典！', '2025-06-22 8:20:00', '2025-06-22 8:20:00', '0', '0'),
       ('49', '18', '貓飯系統也是一大特色，每次出發前都要好好選一下。', '2025-06-22 8:20:00', '2025-06-22 8:20:00', '0',
        '0'),
       ('49', '5', '雖然畫面現在看來比較樸素，但遊戲性真的沒話說。', '2025-06-22 8:20:00', '2025-06-22 8:20:00', '0',
        '0'),
       ('49', '32', '懷念當時一起刷轟龍、金獅子的時光！', '2025-06-22 8:20:00', '2025-06-22 8:20:00', '0', '0'),
       ('50', '6', '武器介紹很詳細！讓我對各種武器的特性有了更深的了解。', '2025-06-23 8:15:00', '2025-06-23 8:15:00',
        '0', '0'),
       ('50', '13', '我還是不知道該選哪把武器...感覺每把都很有趣。', '2025-06-23 8:15:00', '2025-06-23 8:15:00', '0',
        '0'),
       ('50', '3', '有沒有推薦新手入門的武器啊？', '2025-06-23 8:15:00', '2025-06-23 8:15:00', '0', '0'),
       ('50', '36', '果然每種武器都有其獨特的魅力，要成為武器大師還有一段路要走。', '2025-06-23 8:15:00',
        '2025-06-23 8:15:00', '0', '0'),
       ('50', '28', '希望之後能有更進階的武器操作技巧分享。', '2025-06-23 8:15:00', '2025-06-23 8:15:00', '0', '0'),
       ('51', '11', '原來魔物之間還有這樣的互動關係，長知識了！', '2025-06-24 8:01:00', '2025-06-24 8:01:00', '0', '0'),
       ('51', '2', '觀察魔物的行為模式真的很有趣，狩獵的時候也更有策略。', '2025-06-24 8:01:00', '2025-06-24 8:01:00',
        '0', '0'),
       ('51', '8', '這個世界的生態做得真的很用心，感覺魔物都是活生生的。', '2025-06-24 8:01:00', '2025-06-24 8:01:00',
        '0', '0'),
       ('51', '33', '希望之後的系列作能有更多關於魔物生態的描寫。', '2025-06-24 8:01:00', '2025-06-24 8:01:00', '0',
        '0'),
       ('51', '10', '了解生態後，狩獵的沉浸感更強了。', '2025-06-24 8:01:00', '2025-06-24 8:01:00', '0', '0'),
       ('52', '4', '《世界》的無縫地圖真的回不去了，《崛起》的翔蟲也很讚！', '2025-06-25 8:20:00', '2025-06-25 8:20:00', '0',
        '0'),
       ('52', '36', '兩款遊戲各有千秋，我都很喜歡。', '2025-06-25 8:20:00', '2025-06-25 8:20:00', '0', '0'),
       ('52', '27', '感覺《世界》比較注重探索，《崛起》更偏向動作性。', '2025-06-25 8:20:00', '2025-06-25 8:20:00', '0', '0'),
       ('52', '19', '期待未來能有結合兩者優點的續作。', '2025-06-25 8:20:00', '2025-06-25 8:20:00', '0', '0'),
       ('52', '14', '畫面和操作都一直在進步，這個系列真的越來越好。', '2025-06-25 8:20:00', '2025-06-25 8:20:00', '0',
        '0'),
       ('53', '1', '跟朋友一起狩獵真的超歡樂！各種神救援和失誤都很好笑。', '2025-06-26 8:10:00', '2025-06-26 8:10:00',
        '0', '0'),
       ('53', '35', '聯機的時候溝通很重要，才能更有效地合作。', '2025-06-26 8:10:00', '2025-06-26 8:10:00', '0', '0'),
       ('53', '16', '有沒有推薦的組隊配置啊？', '2025-06-26 8:10:00', '2025-06-26 8:10:00', '0', '0'),
       ('53', '3', '希望之後能有更多方便組隊的功能。', '2025-06-26 8:10:00', '2025-06-26 8:10:00', '0', '0'),
       ('53', '12', '一起打贏強大魔物的成就感是單人遊玩無法比擬的。', '2025-06-26 8:10:00', '2025-06-26 8:10:00', '0',
        '0'),
       ('54', '23', '配裝真的是一門大學問，要考慮技能和屬性。', '2025-06-27 8:01:00', '2025-06-27 8:01:00', '0', '0'),
       ('54', '28', '這篇指南對於裝備製作的流程講解得很清楚。', '2025-06-27 8:01:00', '2025-06-27 8:01:00', '0', '0'),
       ('54', '2', '有沒有推薦的初期裝備啊？', '2025-06-27 8:01:00', '2025-06-27 8:01:00', '0', '0'),
       ('54', '34', '為了做出心儀的裝備，刷素材也是一種樂趣（？）', '2025-06-27 8:01:00', '2025-06-27 8:01:00', '0', '0'),
       ('54', '12', '希望之後能有更多關於畢業裝的分享。', '2025-06-27 8:01:00', '2025-06-27 8:01:00', '0', '0'),
       ('55', '21', '看到這些魔物的名字就腿軟了...', '2025-06-28 8:05:00', '2025-06-28 8:05:00', '2', '0'),
       ('55', '10', '挑戰高難度魔物真的需要不斷練習和研究。', '2025-06-28 8:05:00', '2025-06-28 8:05:00', '1', '0'),
       ('55', '9', '求大佬分享打XX魔物的技巧！', '2025-06-28 8:05:00', '2025-06-28 8:05:00', '3', '0'),
       ('55', '29', '成功討伐古龍的瞬間真的會覺得自己變強了。', '2025-06-28 8:05:00', '2025-06-28 8:05:00', '4', '0'),
       ('55', '21', '這些魔物真的是獵人的終極考驗。', '2025-06-28 8:05:00', '2025-06-28 8:05:00', '3', '0'),
       ('56', '26', '希望下一作能有更多新的魔物和地圖！', '2025-06-29 8:20:00', '2025-06-29 8:20:00', '5', '0'),
       ('56', '37', '我希望武器系統能有更多的創新。', '2025-06-29 8:20:00', '2025-06-29 8:20:00', '4', '0'),
       ('56', '17', '如果能有更豐富的劇情模式就好了。', '2025-06-29 8:20:00', '2025-06-29 8:20:00', '4', '0'),
       ('56', '24', '期待次世代主機上的《魔物獵人》能帶來更震撼的體驗。', '2025-06-29 8:20:00', '2025-06-29 8:20:00', '2',
        '0'),
       ('56', '22', '這個系列永遠玩不膩，期待未來的發展！', '2025-06-29 8:20:00', '2025-06-29 8:20:00', '2', '0'),
       ('56', '30', '太神啦！這隻魔物我每次都打得七零八落，你的技巧真的值得學習！', '2025-06-29 8:20:00',
        '2025-06-29 8:20:00', '1', '0'),
       ('55', '25', '感謝分享！這個打法我還沒試過，下次狩獵這隻魔物的時候我會嘗試看看的。', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '4', '0'),
       ('56', '17', '哇，這套配裝思路很棒耶！沒想到可以這樣搭配，學到了一課！', '2025-06-30 8:30:00', '2025-06-30 8:30:00',
        '3', '0'),
       ('53', '24', '恭喜你成功狩獵！看著你對魔物的理解和熱情，我都想再回去玩了。', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '3', '0'),
       ('52', '6', '每次看到這種精彩的狩獵影片，都覺得魔物獵人真的是一款很棒的遊戲！', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '1', '0'),
       ('55', '35', '滅盡龍真的是每個新手的畢業考官，被牠的動作虐到不行！', '2025-06-30 8:30:00', '2025-06-30 8:30:00',
        '1', '0'),
       ('54', '7', '打黑龍一定要有耐心，而且對攻擊時機的掌握非常重要，千萬不能貪刀。', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '1', '0'),
       ('51', '14', '這隻魔物確實很煩人，每次都被牠的吼叫搞得手忙腳亂。', '2025-06-30 8:30:00', '2025-06-30 8:30:00', '2',
        '0'),
       ('54', '11', '有沒有推薦打這隻魔物的必備技能？每次都被牠的招式打到貓車。', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '1', '0'),
       ('53', '19', '成功單刷這隻魔物真的很有成就感，恭喜你！', '2025-06-30 8:30:00', '2025-06-30 8:30:00', '1', '0'),
       ('57', '16', '檢舉！請問這篇文章的魔物屬性是什麼？是會掉落什麼料理素材嗎？（認真）', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('57', '7', '檢舉！是不是把魔物獵人討論區當成美食獵人版了？', '2025-06-30 8:30:00', '2025-06-30 8:30:00', '0',
        '0'),
       ('57', '33', '檢舉！樓主你是不是把獵人營地裡的『用餐區』當成現實的廚房了？這裡是打魔物的地方啦！',
        '2025-06-30 8:30:00', '2025-06-30 8:30:00', '0', '0'),
       ('57', '37', '檢舉！這篇文章讓我產生了嚴重的飢餓debuff，導致我狩獵動力大幅下降，你負責！', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('57', '13', '檢舉！如果這篇文章沒有附上料理圖片，那根本就是違規中的違規！', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('57', '20', '檢舉！當這裡是你個版？', '2025-06-30 8:30:00', '2025-06-30 8:30:00', '0', '0'),
       ('56', '37', '這款遊戲根本是垃圾，玩這種東西的都是白癡。開發商也只是想坑錢的廢物。', '2025-06-30 8:35:00',
        '2025-06-30 8:35:00', '0', '6'),
       ('1', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('1', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('1', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('1', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('1', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('1', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('2', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('2', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('2', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('2', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('2', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('2', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('2', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('2', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('3', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('3', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('3', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('3', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('3', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('3', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('3', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('3', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('4', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('4', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('4', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('4', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('4', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('4', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('4', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('4', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('4', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('4', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('5', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('5', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('5', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('5', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('5', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('5', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('5', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('6', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('6', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('6', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('6', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('6', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('6', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('6', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('6', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('6', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0'),
       ('6', '1', '展示用留言', '2025-06-30 8:30:00',
        '2025-06-30 8:30:00', '0', '0');


-- 建立 討論區圖片 表格 --
CREATE TABLE FORUM_IMAGE
(
    `IMG_NO`   INT AUTO_INCREMENT PRIMARY KEY COMMENT '圖片編號',
    `IMG_DATA` LONGBLOB COMMENT '圖片資料',
    `POST_NO`  INT COMMENT '文章編號',
    `IMG_TYPE` VARCHAR(100) COMMENT '圖片類型',
    FOREIGN KEY (`POST_NO`) REFERENCES FORUM_POST (`POST_NO`) ON DELETE SET NULL ON UPDATE CASCADE
) COMMENT '文章圖片';


-- 建立 討論區收藏 表格 --
CREATE TABLE FORUM_COLLECT
(
    `FCOLL_NO`       INT AUTO_INCREMENT PRIMARY KEY COMMENT '討論區收藏編號',
    `MEM_NO`         INT COMMENT '會員編號',
    `FOR_NO`         INT COMMENT '討論區編號',
    `COLLECT_STATUS` VARCHAR(50) COMMENT '討論區收藏狀態',
    `FCOLL_UPDATE`   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    FOREIGN KEY (`MEM_NO`) REFERENCES MEMBER (`MEM_NO`) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (`FOR_NO`) REFERENCES FORUM (`FOR_NO`) ON DELETE SET NULL ON UPDATE CASCADE
) COMMENT '討論區收藏';

-- 新增 討論區收藏 資料 --
INSERT INTO FORUM_COLLECT (`MEM_NO`, `FOR_NO`, `COLLECT_STATUS`)
VALUES (2, 39, 'COLLECT'),
       (3, 23, 'COLLECT'),
       (4, 15, 'COLLECT'),
       (6, 39, 'COLLECT');


-- 建立 文章收藏 表格 --
CREATE TABLE POST_COLLECT
(
    `PCOLL_NO`     INT AUTO_INCREMENT PRIMARY KEY COMMENT '文章收藏編號',
    `MEM_NO`       INT COMMENT '會員編號',
    `POST_NO`      INT COMMENT '文章編號',
    `POST_COLLECT_STATUS` VARCHAR(50) COMMENT '文章收藏狀態',
    `PCOLL_UPDATE` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    FOREIGN KEY (`MEM_NO`) REFERENCES MEMBER (`MEM_NO`) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (`POST_NO`) REFERENCES FORUM_POST (`POST_NO`) ON DELETE SET NULL ON UPDATE CASCADE
) COMMENT '文章收藏';

-- 新增 文章收藏 資料  --
INSERT INTO `POST_COLLECT`(`MEM_NO`, `POST_NO`,`POST_COLLECT_STATUS`)
VALUES (2, 47,'COLLECT'),
       (3, 47,'COLLECT'),
       (2, 48,'COLLECT'),
       (3, 49,'COLLECT'),
       (6, 49,'COLLECT'),
       (7, 53,'COLLECT'),
       (11, 51,'COLLECT'),
       (21, 33,'COLLECT'),
       (4, 47,'COLLECT'),
       (13, 53,'COLLECT'),
       (23, 49,'COLLECT'),
       (33, 48,'COLLECT'),
       (23, 46,'COLLECT'),
       (11, 55,'COLLECT'),
       (2, 44,'COLLECT'),
       (5, 49,'COLLECT'),
       (5, 47,'COLLECT'),
       (8, 47,'COLLECT');


-- 建立 文章留言檢舉 表格 --
CREATE TABLE `ARTICLE_COM_REPORT`
(
    `NO`                 INT AUTO_INCREMENT PRIMARY KEY COMMENT '檢舉編號',
    `REPORTER`           INT COMMENT '檢舉者編號',
    `RPI_NO`             INT COMMENT '檢舉類型編號',
    `MES_NO`             INT COMMENT '留言編號',
    `ART_COM_REP_STATUS` CHAR(1) NOT NULL DEFAULT '0' COMMENT '處理狀態',
    `CREATE_TIME`        DATETIME         DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    `FINISH_TIME`        DATETIME COMMENT '處理完成時間',
    FOREIGN KEY (`REPORTER`) REFERENCES MEMBER (`MEM_NO`) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (`RPI_NO`) REFERENCES REPORT_TYPE (`RPI_NO`) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (`MES_NO`) REFERENCES FORUM_MES (`MES_NO`) ON DELETE SET NULL ON UPDATE CASCADE
) COMMENT '文章留言檢舉';

-- 新增 文章留言檢舉 資料 --
INSERT INTO `ARTICLE_COM_REPORT` (`REPORTER`, `RPI_NO`, `MES_NO`)
VALUES (2, 3, 67),
       (9, 3, 67);

-- 新增 文章踩讚 表格 --
CREATE TABLE `POST_LIKE`
(
    `PLIKE_NO`     INT AUTO_INCREMENT PRIMARY KEY COMMENT '討論區喜愛編號',
    `POST_NO`      INT COMMENT '文章編號',
    `MEM_NO`       INT COMMENT '會員編號',
    `PLIKE_UPDATE` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '更新時間',
    `PLIKE_STATUS` VARCHAR(50) COMMENT '文章喜愛狀態',
    `PLIKE_CRDATE` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '創建時間',
    FOREIGN KEY (`POST_NO`) REFERENCES FORUM_POST (`POST_NO`) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (`MEM_NO`) REFERENCES MEMBER (`MEM_NO`) ON DELETE SET NULL ON UPDATE CASCADE
) COMMENT '文章踩讚';

-- 新增 文章踩讚 資料 --
INSERT INTO `POST_LIKE` (`POST_NO`, `MEM_NO`, `PLIKE_STATUS`)
VALUES (47, 24, 'LIKE'),
       (47, 11, 'LIKE'),
       (47, 35, 'LIKE'),
       (47, 1, 'LIKE'),
       (47, 19, 'LIKE'),
       (47, 7, 'DISLIKE'),
       (47, 28, 'LIKE'),
       (47, 33, 'LIKE'),
       (48, 15, 'LIKE'),
       (48, 2, 'DISLIKE'),
       (48, 9, 'LIKE'),
       (48, 21, 'LIKE'),
       (48, 3, 'LIKE'),
       (48, 30, 'DISLIKE'),
       (48, 18, 'LIKE'),
       (49, 36, 'LIKE'),
       (49, 13, 'LIKE'),
       (49, 5, 'DISLIKE'),
       (49, 22, 'LIKE'),
       (50, 1, 'LIKE'),
       (50, 10, 'LIKE'),
       (50, 27, 'DISLIKE'),
       (50, 8, 'LIKE'),
       (50, 32, 'LIKE'),
       (51, 16, 'LIKE'),
       (51, 20, 'LIKE'),
       (51, 29, 'DISLIKE'),
       (51, 25, 'LIKE'),
       (52, 14, 'LIKE'),
       (52, 4, 'LIKE'),
       (52, 34, 'LIKE'),
       (52, 17, 'DISLIKE'),
       (52, 6, 'LIKE'),
       (53, 26, 'LIKE'),
       (53, 31, 'LIKE'),
       (53, 23, 'LIKE'),
       (53, 12, 'DISLIKE'),
       (53, 7, 'LIKE'),
       (53, 35, 'LIKE'),
       (54, 19,'LIKE'),
       (54, 2, 'LIKE'),
       (54, 37, 'DISLIKE'),
       (54, 24, 'LIKE'),
       (54, 11, 'LIKE'),
       (55, 21, 'LIKE'),
       (55, 15, 'LIKE'),
       (55, 33, 'LIKE'),
       (55, 5, 'LIKE'),
       (55, 28, 'DISLIKE'),
       (56, 1, 'LIKE'),
       (56, 9, 'LIKE'),
       (56, 30, 'LIKE'),
       (56, 22, 'LIKE'),
       (56, 8, 'DISLIKE'),
       (57, 13, 'DISLIKE'),
       (57, 18, 'DISLIKE'),
       (57, 4, 'DISLIKE'),
       (57, 10, 'DISLIKE'),
       (57, 36, 'DISLIKE'),
       (57, 17, 'DISLIKE'),
       (57, 27, 'DISLIKE'),
       (57, 20, 'DISLIKE');


-- 新增 文章留言踩讚 表格 --
CREATE TABLE `FORUM_MES_LIKE`
(
    `MLIKE_NO`      INT AUTO_INCREMENT PRIMARY KEY COMMENT '討論區喜愛編號',
    `MES_NO`        INT COMMENT '留言編號',
    `MEM_NO`        INT COMMENT '會員編號',
    `FMLIKE_UPDATE` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '更新時間',
    `FMLIKE_STATUS` VARCHAR(50) COMMENT '文章留言喜愛狀態',
    `FMLIKE_CRDATE` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '創建時間',
    FOREIGN KEY (`MEM_NO`) REFERENCES MEMBER (`MEM_NO`) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (`MES_NO`) REFERENCES FORUM_MES (`MES_NO`) ON DELETE SET NULL ON UPDATE CASCADE
) COMMENT '文章留言採讚';

--  新增 文章留言踩讚 資料 --
INSERT INTO FORUM_MES_LIKE (MES_NO, MEM_NO, FMLIKE_STATUS)
VALUES (67, 2, 'DISLIKE'),
       (67, 3, 'DISLIKE'),
       (67, 4, 'DISLIKE'),
       (67, 5, 'DISLIKE'),
       (67, 6, 'DISLIKE'),
       (67, 7, 'DISLIKE'),
       (45, 8, 'LIKE'),
       (46, 9, 'LIKE'),
       (47, 10, 'LIKE'),
       (48, 11, 'LIKE'),
       (48, 12, 'LIKE'),
       (47, 13, 'LIKE'),
       (46, 14, 'LIKE'),
       (44, 15, 'LIKE'),
       (45, 16, 'LIKE'),
       (44, 17, 'LIKE'),
       (44, 18, 'LIKE'),
       (43, 19, 'LIKE'),
       (43, 20, 'LIKE'),
       (43, 21, 'LIKE'),
       (42, 22, 'LIKE'),
       (41, 23, 'LIKE'),
       (41, 24, 'LIKE'),
       (49, 25, 'LIKE'),
       (50, 26, 'LIKE'),
       (50, 27, 'LIKE'),
       (51, 28, 'LIKE'),
       (52, 29, 'LIKE'),
       (52, 30, 'LIKE'),
       (52, 1, 'LIKE'),
       (52, 2, 'LIKE'),
       (53, 3, 'LIKE'),
       (53, 4, 'LIKE'),
       (53, 5, 'LIKE'),
       (44, 6, 'LIKE'),
       (45, 7, 'LIKE'),
       (47, 8, 'LIKE'),
       (46, 9, 'LIKE'),
       (46, 10, 'LIKE'),
       (46, 11, 'LIKE'),
       (47, 12, 'LIKE'),
       (48, 13, 'LIKE'),
       (48, 14, 'LIKE'),
       (49, 15, 'LIKE'),
       (54, 2, 'LIKE'),
       (54, 5, 'LIKE'),
       (54, 8, 'LIKE'),
       (55, 11, 'LIKE'),
       (56, 14, 'LIKE'),
       (57, 15, 'LIKE'),
       (58, 17, 'LIKE'),
       (58, 23, 'LIKE'),
       (59, 22, 'LIKE'),
       (60, 26, 'LIKE');


-- 新增 聊天室訊息 表格 --
CREATE TABLE `FORUM_CHAT_MESSAGE`
(
    `CMES_NO`     INT AUTO_INCREMENT PRIMARY KEY COMMENT '聊天室訊息編號',
    `MEM_NO`      INT COMMENT '會員編號',
    `FOR_NO`      INT COMMENT '討論區編號',
    `CMES_AT`     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '發送時間',
    `CMES_TEXT`   VARCHAR(5000) COMMENT '訊息內容',
    `CMES_DEL`    CHAR(1)  DEFAULT '0' COMMENT '訊息狀態',
    `CMES_STATUS` CHAR(1)  DEFAULT '0' COMMENT '違規處理狀態',
    FOREIGN KEY (`MEM_NO`) REFERENCES MEMBER (`MEM_NO`) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (`FOR_NO`) REFERENCES FORUM (`FOR_NO`) ON DELETE SET NULL ON UPDATE CASCADE
) COMMENT '聊天室訊息';

-- 建立 討論區聊天室訊息檢舉 表格 --
CREATE TABLE `FORUM_CHAT_REPORT`
(
    NO                    INT PRIMARY KEY COMMENT '檢舉編號',
    `REPORTER`            INT COMMENT '檢舉者編號',
    `REPORT_TYPE`         INT COMMENT '檢舉類型編號',
    `FOR_CHAT_REP_STATUS` CHAR(1) NOT NULL DEFAULT '0' COMMENT '處理狀態',
    `CMES_NO`             INT COMMENT '聊天室訊息編號',
    `CREATE_TIME`         DATETIME         DEFAULT CURRENT_TIMESTAMP COMMENT '創建時間',
    `FINISH_TIME`         DATETIME COMMENT '處理完成時間',
    FOREIGN KEY (`REPORTER`) REFERENCES MEMBER (`MEM_NO`) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (`REPORT_TYPE`) REFERENCES REPORT_TYPE (`RPI_NO`) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (`CMES_NO`) REFERENCES FORUM_CHAT_MESSAGE (`CMES_NO`) ON DELETE SET NULL ON UPDATE CASCADE
) COMMENT '討論區聊天室訊息檢舉';
-- ===== END OF 討論區.sql =====

-- ===== START OF product.sql =====


CREATE TABLE MALL_TAG
(
    MALL_TAG_NO   INT PRIMARY KEY AUTO_INCREMENT COMMENT '商城標籤編號',
    MALL_TAG_NAME VARCHAR(25) NOT NULL COMMENT '商城標籤名稱'
) COMMENT '商城標籤';

CREATE TABLE PRODUCT
(
    PRO_NO           INT PRIMARY KEY AUTO_INCREMENT COMMENT '產品編號',
    PRO_NAME         VARCHAR(30) NOT NULL COMMENT '產品名稱',
    PRO_PRICE        INT         NOT NULL COMMENT '產品價格',
    PRO_STATUS       VARCHAR(10) NOT NULL COMMENT '產品狀態',
    PRO_VERSION      VARCHAR(30) NOT NULL COMMENT '產品發行版本',
    PRO_DATE         DATE COMMENT '產品發行日期',
    PRO_COVER        LONGBLOB COMMENT '產品封面',
    PRO_DETAILS      VARCHAR(500) COMMENT '產品簡介',
    PRO_INCLUDE      VARCHAR(255) COMMENT '產品內含',
    MALL_TAG_NO      INT         NOT NULL COMMENT '商城標籤編號',
    PRO_ISMARKET     CHAR        NOT NULL COMMENT '產品上下架',
    PRO_TOTAL_COMMIT INT COMMENT '評論總數',
    PRO_TOTAL_STAR   DECIMAL(1, 1) COMMENT '總星數',

    CONSTRAINT FK_PRODUCT_MALL_TAG_MALL_TAG_NO
        FOREIGN KEY (MALL_TAG_NO) REFERENCES MALL_TAG (MALL_TAG_NO)
) COMMENT '產品';

INSERT INTO MALL_TAG(MALL_TAG_NAME)
VALUES ('Steam'),
       ('PS5'),
       ('Switch'),
       ('Xbox');

INSERT INTO PRODUCT (PRO_NAME, PRO_PRICE, PRO_STATUS, PRO_VERSION, PRO_DATE,
                     PRO_DETAILS, PRO_INCLUDE, MALL_TAG_NO, PRO_ISMARKET, PRO_TOTAL_COMMIT, PRO_TOTAL_STAR)
VALUES ('光與影：33 號遠征隊', 1490, '已發售', '一般版', '2025-04-24',
        '《光與影：33 號遠征隊》是一款具備獨特的即時機制的創新回合制 RPG ，玩家將在這受法國美好年代所啟發的奇幻世界中探索，與和強大無比的敵人展開激戰。遊戲採用 Unreal Engine 5 研發、以遠征隊 33 作為故事主角，描述在那個時代有個女畫師「繪母」，當她每次醒來並在巨石上畫上被詛咒的數字時，那個年齡的每個人都將灰飛煙滅。年復一年、日復一日的恐懼不斷壓迫著人們，明天她將醒來並畫上「33」，為了阻止更多人死去，遠征隊 33 為了拯救世界將出發討伐，帶領人們脫離死亡的恐懼。',
        '遊戲序號', 2, 0, 0, 0),
       ('印第安納瓊斯：古老之圈', 1990, '已發售', '一般版', '2024-12-09',
        '《印第安納瓊斯：古老之圈》由曾經手《德軍總部》系列的工作室 MachineGames 與 Lucasfilm Games 共同開發。背景設定在《法櫃奇兵》與《聖戰奇兵》之間，玩家將扮演傳奇考古學家印第安納‧瓊斯，踏上環遊世界的史詩級冒險，走訪梵蒂岡神聖的廳堂、埃及乾燥的旱漠、素可泰鬱鬱蔥蔥的沉沒廟宇以及喜馬拉雅冰天雪地的山峰。駕馭隨身佩帶的長鞭，運用智慧揭露驚天動地的陰謀，阻止邪惡勢力尋覓古代力量的祕密。',
        '遊戲序號', 2, 0, 0, 0),
       ('鬼滅之刃 火之神血風譚 2', 1790, '預購中', '一般版', '2025-08-01',
        '《鬼滅之刃 火之神血風譚 2》是 2021 年推出之對戰動作遊戲《鬼滅之刃 火之神血風譚》的續篇作品。以電視動畫《鬼滅之刃》為基礎，接續前作內容加以延伸。單人遊玩模式部分承接自前作《鬼滅之刃 火之神血風譚》，能讓玩家親自體驗主角 —— 竈門炭治郎經歷的故事。收錄電視動畫《鬼滅之刃》的「遊郭篇」、「刀匠村篇」、「柱訓練篇」的內容。而對戰模式則以在「刀匠村篇」與上弦之鬼展開激烈戰鬥的兩名柱「時透無一郎」和「甘露寺蜜璃」為首，新增了許多可使用角色。鬼殺隊最高位階劍士「柱」的 9 名成員也都作為可操控角色登場。',
        '遊戲序號', 2, 1, 0, 0),
       ('艾爾登法環：黑夜君臨', 1190, '已發售', '一般版', '2025-05-30',
        '《艾爾登法環 黑夜君臨》是暢銷黑暗奇幻動作角色扮演遊戲《艾爾登法環》系列的衍生作品，承襲原作的世界觀設定，並採用全新多人協力生存動作玩法。最多 3 名玩家將操作各具特色的角色，攜手面對新的威脅。體驗探索迷宮、角色成長、與強敵之間緊張無比的戰鬥等 RPG 的核心樂趣，感受快節奏的刺激遊戲體驗。玩家將在有別於本篇的舞台「寧姆韋德」展開冒險，扮演從各地被徵召而來的戰士「渡夜者」，目的是與其他人一同在廣闊的場景中度過 3 天，迎戰每天夜晚來襲的頭目，討伐在第 3 天夜晚現身的 8 個巨大威脅',
        '遊戲序號', 1, 0, 0, 0),
       ('艾爾登法環：黑夜君臨', 1690, '已發售', '豪華版', '2025-05-30',
        '《艾爾登法環 黑夜君臨》是暢銷黑暗奇幻動作角色扮演遊戲《艾爾登法環》系列的衍生作品，承襲原作的世界觀設定，並採用全新多人協力生存動作玩法。最多 3 名玩家將操作各具特色的角色，攜手面對新的威脅。體驗探索迷宮、角色成長、與強敵之間緊張無比的戰鬥等 RPG 的核心樂趣，感受快節奏的刺激遊戲體驗。玩家將在有別於本篇的舞台「寧姆韋德」展開冒險，扮演從各地被徵召而來的戰士「渡夜者」，目的是與其他人一同在廣闊的場景中度過 3 天，迎戰每天夜晚來襲的頭目，討伐在第 3 天夜晚現身的 8 個巨大威脅',
        '遊戲序號+追加內容（預定於 2025 年內推出）+數位美術書+數位迷你原聲帶', 1, 0, 0, 0),
       ('雙點博物館', 999, '已發售', '一般版', '2025-03-05',
        '由《雙點醫院》和《雙點校園》的創作者打造的《雙點博物館》是一款創新經營模擬遊戲，玩家可透過極具吸引力的策展及規劃提升訪客各方面的參觀體驗，親手打造自己夢想中的博物館。 身為菜鳥館長的你被交付了營運博物館的重責大任。但這間博物館的一切都非比尋常......？你必須派遣專家前去收集展品，或是讓參觀訪客「體驗」你引以為傲的館藏。策劃館內導覽，打造眾所公認的最棒博物館吧。',
        '遊戲序號', 1, 0, 0, 0),
       ('人中之龍 8 外傳 夏威夷海盜', 1390, '已發售', '一般版', '2025-02-20',
        '失去記憶的真島吾朗。航向汪洋大海，開啟了全新的傳說篇章......  《人中之龍 8 外傳 夏威夷海盜》以真島吾朗為主角，描繪《人中之龍 8》故事結束後世界的全新作品。本次的舞台是島，以及......大海。駕駛海盜船「吾朗丸」航向大海，盡情冒險。戰鬥系統上除了極具真島特有速度感的「狂犬」風格以外，還有能夠靈活運用彎刀等豐富的海盜工具的全新戰鬥風格「海盜」。玩家能根據戰況和喜好運用兩種戰鬥風格，將敵人打得落花流水吧。',
        '遊戲序號', 1, 0, 0, 0),
       ('即刻離職', 318, '已發售', '一般版', '2024-04-03',
        '《即刻離職》是一款以職場為主題、加入 Roguelite 元素的橫向捲軸動作遊戲，玩家扮演的普通上班族，某天一如往常地前往公司上班，已經被生活壓力壓得喘不過氣的你，剛踏入公司便被同事指控要離職？！早已失去理智、無法溝通的同事們開始失控並攻擊玩家，你必須一邊擊倒他們，一邊找到最終 BOSS，打爆黑心公司，離職重獲自由。 在遊戲過程中，身為主角的玩家需要突破人事部、財務部、業務部等層層關卡，每個單位有各自的「部門風情」，需要玩家謹慎思考，搭配各種技能來應對各式各樣的「同事」。此外遊戲中還有更多充滿特色及挑戰的辦公室，等玩家前來探索。',
        '遊戲序號', 1, 0, 0, 0),
       ('戰鼓啪打碰 1+2 REPLAY', 990, '預購中', '一般版', '2025-07-10',
        '以 4 個太鼓敲打出節奏下達指示並展開冒險的動作遊戲《戰鼓啪打碰》系列，將以套組的形式推出收錄系列原點之頭兩款作品《戰鼓啪打碰》、《戰鼓啪打碰 2》的合輯《戰鼓啪打碰 1+2 REPLAY》。玩家將化身為「PATAPON」的「神明大人」，透過「4 個太鼓」敲打出節奏，向既神秘又可愛，像眼球般的生物下達指示，引領著 PATAPON 大軍前往「世界的盡頭」。而仔細觀察 PATAPON 就會發現他們之間各有不同，有著各式各樣的類型（職業），各自的功能與戰鬥方式也都有差異，玩家須可以組織一個能發揮各自特色的部隊，攻克各種關卡。',
        '遊戲序號', 3, 1, 0, 0),
       ('歧路旅人 + 歧路旅人 II 合輯', 1790, '已發售', '一般版', '2025-04-02',
        '《歧路旅人》的故事舞台是奧魯斯特拉大陸，主角是八位不同的旅人，你就要扮演其中一人，在這個世界上自由展開旅程。生出地點和旅途目的，以及特技都不一樣的八人－你到底會體驗到什麼樣的旅程呢？ 《歧路旅人 2》《歧路旅人》系列最新 RPG 作品，玩家將在名為「索里斯帝亞」的全新大地與新的八位旅人在嶄新的時代。本作依舊保有能透過職業和技能的搭配，自由培育角色的系統，以及藉由破防 & 增幅進行戰鬥的爽快鬥智，當然也預計加入了全新系統。',
        '遊戲序號', 3, 0, 0, 0),
       ('瑪利歐 & 路易吉 RPG 兄弟齊航！', 1590, '已發售', '一般版', '2024-11-07',
        '本次瑪利歐和路易吉將探訪漂浮在汪洋大海上的各個島嶼，展開全新的冒險。故事的舞台是和蘑菇王國位在不同世界的「柯連庫塔魯大地」。瑪利歐和路易吉必須齊心協力，解決旅途中阻礙他們的各種困難。過程中將會遭遇各式各樣的人物，其中有些會協助瑪利歐們，也有些個性獨特的人物會以敵人身分阻擋在前。除此之外，也有大家所熟悉的蘑菇王國公主以及烏龜族大魔王等瑪利歐系列人物會現身。',
        '遊戲序號', 3, 0, 0, 0),
       ('桃太郎電鐵 ~昭和 平成 令和 也是定番！~', 1390, '已發售', '一般版', '2024-12-12',
        '《桃太郎電鐵 ~昭和 平成 令和 也是定番！~》是 1988 年在任天堂紅白機上問世的老牌大富翁遊戲《桃太郎電鐵》系列邁入新年號「令和」的首款作品。同樣由系列之父佐久間晃親自領軍製作，承襲經營鐵路公司營利、成為日本第一大老闆的鐵道經營大富翁玩法。同時，本作還具備包括更多樣化可購置的「新物產」，能提供協助玩家的「歷史英雄」，帶給玩家噩夢的各式各樣「窮神」......等的各具魅力的特色要素。',
        '遊戲序號', 3, 0, 0, 0),
       ('薩爾達傳說 智慧的再現', 1690, '已發售', '一般版', '2024-09-26',
        '由薩爾達公主擔任主角的《薩爾達傳說》系列最新作品《薩爾達傳說 智慧的再現》，劇情描述廣大的海拉魯各地出現了不明裂縫，並且引發將人與物體吞沒的「神隱」現象。劍士林克也因遭到神隱而失去蹤跡。倖存的薩爾達公主遇見了妖精「托力」，並從他那裡得到不可思議的魔杖「托力之杖」。為了拯救人民與林克，薩爾達公主與托力攜手踏上了海拉魯巡迴之旅。',
        '遊戲序號', 3, 0, 0, 0),
       ('劫薪日 3', 1190, '已發售', '一般版', '2023-09-22',
        '令黑白兩道聞風喪膽的傳奇團隊「Payday」回來了。曾經的風波又新生威脅，令金盆洗手的他們重操舊業。為解決新麻煩，Payday 離開了他們曾經發跡的華盛頓特區前往紐約。新環境將帶來新挑戰，他們決定在此大幹一場。策劃一場完美犯罪，計劃、努力、運氣，缺一不可。在《劫薪日 3》要正面進攻還是潛蹤隱跡；要單打獨鬥還是成幫結隊；要釋放人質還是作為籌碼 —— 都由玩家決定。無盡的選擇就是你手中的王牌，每個選項都會大幅改變遊戲的玩法和走向。',
        '遊戲序號', 4, 0, 0, 0),
       ('樂高星際大戰：天行者傳奇', 1590, '已發售', '一般版', '2022-04-05',
        '這款全新獨特的樂高遊戲中，體驗全九部《星際大戰》傳奇電影。體驗充滿樂趣的冒險、異想天開的幽默，以及完全沉浸在《樂高星際大戰》銀河中的自由。 想要扮演絕地？西斯？還是反抗軍、賞金獵人,或是機器人？《樂高星際大戰：天行者傳奇》將為你從全銀河帶來上百個可玩角色。無論是陸地還是宇宙，數不清的車輛與飛船將供你差遣。搭乘千年鷹號以光速前進，或是駕駛 T-47 空中飛行器在抵抗勢力的 X 翼間穿梭，與鈦戰機一決高低......終極的《樂高星際大戰》體驗，盡在於此。',
        '遊戲序號', 4, 0, 0, 0),
       ('漫威午夜之⼦', 610, '已發售', '加強版', '2022-12-02',
        '這漫威午夜之⼦》以漫威於 90 年代連載的漫畫《午夜之⼦的崛起》為基礎。玩家可在這款由《XCOM》創作團隊一手打造的戰術角色扮演遊戲，體驗有如角色扮演遊戲般的豐富故事、角色關係、自訂選項與進展，以及全新卡牌制戰術遊戲的戰術策略與戰鬥機制。 故事描述玩家將扮演漫威宇宙⾸位可⾃訂的原創英雄「獵⼈」，從長達數世紀的沉睡中甦醒，領導⿊暗英雄組成的「午夜之⼦」攜⼿復仇者聯盟、X 戰警、離家童盟等英雄團隊反抗⿊暗勢⼒，搶在⿊暗神書完成前聯⼿擊敗惡魔之⺟「莉莉絲」和上古之神桑恩的冥界大軍。',
        '遊戲序號+高級英雄外觀 5 款', 4, 0, 0, 0),
       ('宣誓', 1791, '已發售', '豪華版', '2025-02-18',
        '歡迎來到盎然之地，這座充滿冒險與危險的神秘島嶼。《宣誓》是由 Obsidian Entertainment 所推出的第一人稱奇幻動作 RPG，以奇幻世界艾歐拉為舞台，您將扮演來自遙遠國度聚鹿王國的特使，奉命前往調查瘟疫遍布整個盎然之地的謠言。這座島嶼充滿神秘與秘密、危險與冒險、選擇與結果，以及未開發的荒野。您將發現自己與盎然之地的關係，以及能夠摧毀一切的古老秘密。您是否能夠威脅要將一切撕裂殆盡的力量之手，拯救這個未知的邊陲荒野及您自身的靈魂？',
        '遊戲序號+頂級造型套件+數位美術設定集+原聲帶', 4, 0, 0, 0),
       ('沉默之丘 f', 1990, '預購中', '豪華版', '2025-09-25',
        '《沉默之丘》系列的全新作品，以 1960 年代的日本為背景，玩家將扮演來自「戎之丘」的高中生深水雛子，在她的故鄉逐漸被迷霧籠罩、轉變為詭譎惡夢般空間的過程中，雛子必須做出一連串關鍵抉擇，這些選擇將塑造她最終的命運...... 她會選擇優雅與美麗，還是滑入瘋狂與恐懼？她是註定要被這場瘋狂吞沒，還是能面對內心恐懼、尋得逃脫之路？',
        '遊戲序號+白色水手服+“牡丹” 御守+道具禮包', 2, 1, 0, 0),
       ('沉默之丘 f', 2290, '預購中', '豪華版', '2025-09-25',
        '《沉默之丘》系列的全新作品，以 1960 年代的日本為背景，玩家將扮演來自「戎之丘」的高中生深水雛子，在她的故鄉逐漸被迷霧籠罩、轉變為詭譎惡夢般空間的過程中，雛子必須做出一連串關鍵抉擇，這些選擇將塑造她最終的命運...... 她會選擇優雅與美麗，還是滑入瘋狂與恐懼？她是註定要被這場瘋狂吞沒，還是能面對內心恐懼、尋得逃脫之路？',
        '遊戲序號+白色水手服+“牡丹” 御守+道具禮包', 1, 1, 0, 0),
       ('數碼寶貝物語 時空異客', 1790, '預購中', '一般版', '2025-10-02',
        '《數碼寶貝物語》系列最新作，本作以「跨越時空的羈絆故事」為主題，「過去與未來」、「人類世界與異世界」、「人類與數碼寶貝」—— 兩種截然不同的要素，交織成羈絆、糾葛的故事。玩家可以在追查世界毀滅謎團的過程中，一面享受橫跨人類世界與異世界的冒險，以及蒐集培育種類豐富的數碼寶貝。而當玩家挺身面對困難時，玩家與數碼寶貝的羈絆將會受到考驗。遊戲中有兩個世界：人類世界與數碼寶貝居住的異世界「數碼世界 伊利亞斯」。玩家能在故事中穿梭不同世界，體驗活用各個世界特性的機制與委託。冒險中玩家可以將數碼寶貝收納為夥伴，透過進化、退化、自訂，培育出自己專屬的數碼寶貝。',
        '遊戲序號', 2, 1, 0, 0);


-- ===== END OF product.sql =====

-- ===== START OF coupon.sql =====
-- 建立資料庫並設定自動遞增屬性
-- CREATE DATABASE IF NOT EXISTS pixel_tribe;
-- USE pixel_tribe;

-- 刪除既有 優惠券 資料表


-- 設定自增主鍵起始值與遞增量
-- SET auto_increment_offset = 1;
-- SET auto_increment_increment = 1;

-- 建立 優惠券夾COUPON 資料表
CREATE TABLE COUPON
(
    COU_NO          INT AUTO_INCREMENT PRIMARY KEY NOT NULL COMMENT '優惠券代碼',
    COU_NAME        VARCHAR(25) COMMENT '優惠券名稱',
    COU_DISCOUNT    INT COMMENT '折扣值',
    COU_STATUS      CHAR(1) DEFAULT '0' COMMENT '狀態',
    COU_USE_START   DATETIME COMMENT '開始使用時間',
    COU_USE_END     DATETIME COMMENT '結束使用時間',
    COU_TOTAL       INT COMMENT '發放數量',
    COU_USED        INT     DEFAULT 0 COMMENT '已使用數量',
    COU_CODE        VARCHAR(30) UNIQUE COMMENT '優惠券編碼',
    COU_DTR_START   DATETIME COMMENT '發放開始時間',
    COU_DTR_END     DATETIME COMMENT '發放結束時間',
    COU_RECEIVE_NUM INT     DEFAULT 0 COMMENT '優惠券已被領用數量'
);

-- -- 插入 2筆 優惠券COUPON 資料
INSERT INTO COUPON (COU_NAME, COU_DISCOUNT, COU_STATUS, COU_USE_START, COU_USE_END, COU_TOTAL, COU_USED, COU_CODE,
                    COU_DTR_START, COU_DTR_END, COU_RECEIVE_NUM)
    VALUE ('論壇歡迎券', '100', '1', '2025-06-06 0:00:00', '2025-12-31 23:59:59', '50', '0', 'WELCOME2025',
           '2025-06-01 0:00:00', '2025-06-30 23:59:59', '10'),
    ('滿千折百優惠券', '100', '1', '2025-06-06 0:00:00', '2025-09-30 23:59:59', '20', '3', 'SAVE100NOW',
     '2025-06-01 0:00:00', '2025-08-31 23:59:59', 5);
-- = END OF coupon.sql =====

-- ===== START OF coupon_wallet.sql =====
-- 建立資料庫並設定自動遞增屬性
-- CREATE DATABASE IF NOT EXISTS pixel_tribe;
-- USE pixel_tribe;

-- 刪除既有 優惠券夾COUPON_WALLET 資料表


-- 設定自增主鍵起始值與遞增量
-- SET auto_increment_offset = 1;
-- SET auto_increment_increment = 1;

-- 建立 優惠券夾COUPON_WALLET 資料表
CREATE TABLE COUPON_WALLET
(
    COUPON_WALLET_NO    INT AUTO_INCREMENT PRIMARY KEY NOT NULL COMMENT '優惠票夾代碼',
    COU_NO              INT COMMENT '優惠券代碼',
    MEM_NO              INT COMMENT '會員編號',
    COUPON_RELEASE_TIME DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '發放時間',
    COU_WALLET_USED     CHAR COMMENT '優惠票夾使用狀態',

    FOREIGN KEY (MEM_NO) REFERENCES MEMBER (MEM_NO)
);


-- -- 插入 15筆 優惠券夾COUPON_WALLET 資料
INSERT INTO COUPON_WALLET (COU_NO, MEM_NO, COU_WALLET_USED)
VALUES ('1', '15', '1'),
       ('2', '22', '0'),
       ('1', '8', '1'),
       ('2', '3', '0'),
       ('1', '12', '1'),
       ('2', '8', '0'),
       ('1', '6', '1'),
       ('2', '29', '0'),
       ('2', '15', '1'),
       ('2', '37', '1'),
       ('1', '31', '0'),
       ('2', '4', '0'),
       ('1', '11', '0'),
       ('2', '9', '0'),
       ('1', '23', '0');
-- ===== END OF coupon_wallet.sql =====

-- ===== START OF order.sql =====
-- 建立資料庫並設定自動遞增屬性
-- CREATE DATABASE IF NOT EXISTS pixel_tribe;
-- USE pixel_tribe;

-- 刪除既有 訂單ORDER 資料表


-- 建立 訂單ORDER 資料表



CREATE TABLE `ORDER` (
                         ORDER_NO INT PRIMARY KEY NOT NULL COMMENT'訂單編號(格式:YYYYMM0001)',
                         MEM_NO INT NOT NULL COMMENT'會員編號',
                         COUPON_WALLET_NO INT DEFAULT NULL COMMENT'優惠票夾代碼',
                         ORDER_DATETIME DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT'訂購時間',
                         ORDER_STATUS VARCHAR(20) COMMENT'訂單狀態',
                         ORDER_TOTAL INT COMMENT'訂單總額',
                         POINT_USED INT COMMENT'使用積分',
                         FOREIGN KEY (MEM_NO) REFERENCES MEMBER(MEM_NO),
                         FOREIGN KEY (COUPON_WALLET_NO) REFERENCES COUPON_WALLET(COUPON_WALLET_NO)
);

-- 重新啟用外鍵檢查
SET FOREIGN_KEY_CHECKS = 1;

-- 對應的觸發器 ORDER_NO 自動產生的設定
DELIMITER $$
CREATE TRIGGER generate_order_no

    BEFORE INSERT ON `ORDER`

    FOR EACH ROW
BEGIN
    DECLARE next_seq INT;
    DECLARE current_month VARCHAR(6);

    -- 取得目前年月格式 YYYYMM
    SET current_month = DATE_FORMAT(NOW(), '%Y%m');

    -- 查詢目前月份的最大序號(流水號碼)
    SELECT COALESCE(MAX(CAST(RIGHT(ORDER_NO, 4) AS UNSIGNED)), 0) + 1
    INTO next_seq
    FROM `ORDER`
    WHERE LEFT(ORDER_NO, 6) = current_month;

    -- 生成新的ORDER_NO (格式: YYYYMM0001)
    SET NEW.ORDER_NO = CONCAT(current_month, LPAD(next_seq, 4, '0'));
END$$
    DELIMITER ;

-- 停用外鍵檢查（插入測試資料時）
SET FOREIGN_KEY_CHECKS = 0;

-- 插入 9筆 訂單ORDER 資料（會自動產生 ORDER_NO）

    INSERT INTO `ORDER` (MEM_NO, COUPON_WALLET_NO, ORDER_STATUS, ORDER_TOTAL, POINT_USED)
    VALUES (15, 1, '已完成', 1399, 0),
           (23, NULL, '處理中', 1990, 0),
           (8, 3, '已完成', 1090, 0),
           (12, 5, '已完成', 1090, 0),
           (23, NULL, '處理中', 1690, 0),
           (6, 7, '已完成', 899, 0),
           (29, NULL, '已出貨', 1390, 0),
           (15, 9, '已完成', 218, 0),
           (37, 10, '處理中', 1690, 0);

-- 插入對應 ORDER_ITEM 測試資料的新訂單（手動指定 ORDER_NO）
    INSERT INTO `ORDER` (ORDER_NO, MEM_NO, COUPON_WALLET_NO, ORDER_DATETIME, ORDER_STATUS, ORDER_TOTAL, POINT_USED)
    VALUES
        (2025070010, 15, NULL, '2025-07-07 16:31:16', '已完成', 2397, 0),
        (2025070011, 20, NULL, '2025-07-08 10:15:30', '已完成', 1990, 0),
        (2025070012, 25, 3, '2025-07-09 14:22:45', '處理中', 2144, 0),
        (2025070013, 29, NULL, '2025-07-10 09:30:20', '已出貨', 3380, 0),
        (2025070014, 33, 5, '2025-07-11 11:45:10', '已完成', 3180, 0),
        (2025070015, 28, NULL, '2025-07-12 15:20:35', '處理中', 3497, 0),
        (2025070016, 29, 7, '2025-07-13 16:31:16', '已出貨', 1390, 0);



    -- ===== END OF order.sql =====

-- ===== START OF oreder_item.sql =====
-- 建立資料庫並設定自動遞增屬性
-- CREATE DATABASE IF NOT EXISTS pixel_tribe;
-- USE pixel_tribe;

-- 刪除既有 訂單明細ORDER_ITEM 資料表


-- 建立 訂單明細ORDER_ITEM 資料表


    CREATE TABLE ORDER_ITEM (
                                ORDER_ITEM_NO INT AUTO_INCREMENT PRIMARY KEY NOT NULL COMMENT '訂單明細編號',
                                ORDER_NO INT COMMENT'訂單編號',
                                PRO_NO INT COMMENT'產品編號',
                                ORDER_AMOUNT INT COMMENT'訂購數量',
                                PRO_PRICE INT COMMENT'產品價格',
                                PRO_NAME VARCHAR(30) COMMENT'產品名稱',
                                PRODUCT_COMMENT VARCHAR(255) COMMENT'產品評論內容',
                                PRODUCT_COMMENT_CRDATE DATETIME COMMENT'評論時間',
                                PRO_STAR INT COMMENT'產品評價',
                                PRO_COM_STATUS CHAR(1) DEFAULT '0' COMMENT'產品評論狀態',
                                DATA_SOURCE CHAR(1) DEFAULT 'R' COMMENT '資料來源：R=真實訂單, T=測試資料',

                                FOREIGN KEY (ORDER_NO) REFERENCES `ORDER`(ORDER_NO),
                                FOREIGN KEY (PRO_NO) REFERENCES PRODUCT(PRO_NO)
    );

-- 插入原有的測試資料（標記為 'T'）
    INSERT INTO ORDER_ITEM (ORDER_NO, PRO_NO, ORDER_AMOUNT, PRO_PRICE, PRO_NAME, PRODUCT_COMMENT, PRODUCT_COMMENT_CRDATE, PRO_STAR, PRO_COM_STATUS, DATA_SOURCE)
    VALUES
        (2025070001, 1, 1, 1499, '光與影：33 號遠征隊', NULL, NULL, NULL, '1', 'T'),
        (2025070002, 2, 1, 1990, '印第安納瓊斯：古老之圈', NULL, NULL, NULL, '1', 'T'),
        (2025070003, 4, 1, 1190, '艾爾登法環：黑夜君臨', NULL, NULL, NULL, '1', 'T'),
        (2025070004, 4, 1, 1190, '艾爾登法環：黑夜君臨', NULL, NULL, NULL, '1', 'T'),
        (2025070005, 5, 1, 1690, '艾爾登法環：黑夜君臨', NULL, NULL, NULL, '1', 'T'),
        (2025070006, 6, 1, 999, '雙點博物館', NULL, NULL, NULL, '1', 'T'),
        (2025070007, 7, 1, 1390, '人中之龍 8 外傳 夏威夷海盜', NULL, NULL, NULL, '1', 'T'),
        (2025070008, 8, 1, 318, '即刻離職', NULL, NULL, NULL, '1', 'T'),
        (2025070009, 10, 1, 1790, '歧路旅人 + 歧路旅人 II 合輯', NULL, NULL, NULL, '1', 'T');

-- 插入與前端對應的測試資料（符合業務邏輯：一個訂單多個商品）
    INSERT INTO ORDER_ITEM (ORDER_NO, PRO_NO, ORDER_AMOUNT, PRO_PRICE, PRO_NAME, PRODUCT_COMMENT, PRODUCT_COMMENT_CRDATE, PRO_STAR, PRO_COM_STATUS, DATA_SOURCE)
    VALUES

-- 訂單 2025070010：購買 2 個光與影 + 1 個雙點博物館
(2025070010, 1, 2, 699, '光與影：33 號遠征隊', NULL, NULL, NULL, '0', 'T'),
(2025070010, 6, 1, 999, '雙點博物館', NULL, NULL, NULL, '0', 'T'),

-- 訂單 2025070011：購買 1 個印第安納瓊斯
(2025070011, 2, 1, 1990, '印第安納瓊斯：古老之圈', NULL, NULL, NULL, '0', 'T'),

-- 訂單 2025070012：購買 1 個艾爾登法環 + 3 個即刻離職
(2025070012, 4, 1, 1190, '艾爾登法環：黑夜君臨', NULL, NULL, NULL, '0', 'T'),
(2025070012, 8, 3, 318, '即刻離職', NULL, NULL, NULL, '0', 'T'),

-- 訂單 2025070013：購買 2 個艾爾登法環
(2025070013, 5, 2, 1690, '艾爾登法環：黑夜君臨', NULL, NULL, NULL, '0', 'T'),

-- 訂單 2025070014：購買 1 個人中之龍 + 1 個歧路旅人（已評論）
(2025070014, 7, 1, 1390, '人中之龍 8 外傳 夏威夷海盜', '劇情很棒！', '2025-07-15 14:30:00', 5, '1', 'T'),
(2025070014, 10, 1, 1790, '歧路旅人 + 歧路旅人 II 合輯', '畫面很美', '2025-07-15 15:20:00', 4, '1', 'T'),

-- 訂單 2025070015：購買 1 個光與影 + 2 個雙點博物館（部分評論）
(2025070015, 1, 1, 1499, '光與影：33 號遠征隊', '還不錯玩', '2025-07-16 10:15:00', 4, '1', 'T'),
(2025070015, 6, 2, 999, '雙點博物館', NULL, NULL, NULL, '0', 'T'),

-- 訂單 2025070016：購買 1 個人中之龍
(2025070016, 7, 1, 1390, '人中之龍 8 外傳 夏威夷海盜', NULL, NULL, NULL, '0', 'T');

    -- 重新啟用外鍵檢查
--
-- ===== END OF oreder_item.sql =====

-- ===== START OF product_image.sql =====
-- 建立資料庫並設定自動遞增屬性
-- CREATE DATABASE IF NOT EXISTS pixel_tribe;
-- USE pixel_tribe;

-- 刪除既有 產品圖片PRODUCT_IMAGE 資料表


-- 設定自增主鍵起始值與遞增量
-- SET auto_increment_offset = 1;
-- SET auto_increment_increment = 1;

-- 建立 產品圖片PRODUCT_IMAGE 資料表
    CREATE TABLE PRODUCT_IMAGE
    (
        PRO_IMG_NO   INT AUTO_INCREMENT PRIMARY KEY NOT NULL COMMENT '產品圖片編號',
        PRO_NO       INT                            NOT NULL COMMENT '產品編號',
        PRO_IMG_DATA LONGBLOB COMMENT '圖片資料',
        PRO_IMG_TYPE VARCHAR(100) COMMENT '產品圖片類型',

        CONSTRAINT FK_PRODUCT_IMAGE_PRODUCT_PRO_NO
            FOREIGN KEY (PRO_NO) REFERENCES PRODUCT (PRO_NO)
    );
    -- ===== END OF product_image.sql =====

-- ===== START OF favorite_product.sql =====
-- 建立資料庫並設定自動遞增屬性
-- CREATE DATABASE IF NOT EXISTS pixel_tribe;
-- USE pixel_tribe;

-- 刪除既有 最愛產品 FAVORITE_PRODUCT 資料表


-- 設定自增主鍵起始值與遞增量
-- SET auto_increment_offset = 1;
-- SET auto_increment_increment = 1;

    CREATE TABLE FAVORITE_PRODUCT
    (
        FAV_PRO_NO INT AUTO_INCREMENT PRIMARY KEY NOT NULL COMMENT '最愛產品編號',
        MEM_NO     INT                            NOT NULL COMMENT '會員編號',
        PRO_NO     INT                            NOT NULL COMMENT '產品編號',

        FOREIGN KEY (MEM_NO) REFERENCES MEMBER (MEM_NO),
        FOREIGN KEY (PRO_NO) REFERENCES PRODUCT (PRO_NO)
    );

-- -- 插入 產品序號 FAVORITE_PRODUCT 資料
    INSERT INTO FAVORITE_PRODUCT (MEM_NO, PRO_NO)
        VALUE (20, 1),
    (33, 3),
    (6, 5),
    (5, 7),
    (3, 9),
    (17, 12),
    (15, 15),
    (34, 18),
    (18, 2),
    (24, 6),
    (11, 11),
    (31, 14),
    (3, 8),
    (13, 13),
    (25, 16);
    -- ===== END OF favorite_product.sql =====

-- ===== START OF pro_serial_numbers.sql =====
-- 建立資料庫並設定自動遞增屬性
-- CREATE DATABASE IF NOT EXISTS pixel_tribe;
-- USE pixel_tribe;

-- 刪除既有 產品序號 PRO_SERIAL_NUMBERS 資料表


-- 設定自增主鍵起始值與遞增量
-- SET auto_increment_offset = 1;
-- SET auto_increment_increment = 1;

-- 建立 產品序號 PRO_SERIAL_NUMBERS 資料表
    CREATE TABLE PRO_SERIAL_NUMBERS
    (
        PRODUCT_SN_NO INT AUTO_INCREMENT PRIMARY KEY NOT NULL COMMENT '產品序號流水號',
        PRODUCT_SN    VARCHAR(100) COMMENT '產品序號',
        ORDER_ITEM_NO INT COMMENT '訂單明細編號',
        PRO_NO        INT COMMENT '產品編號',

        FOREIGN KEY (ORDER_ITEM_NO) REFERENCES ORDER_ITEM (ORDER_ITEM_NO),
        FOREIGN KEY (PRO_NO) REFERENCES PRODUCT (PRO_NO)
    );


-- -- 插入 產品序號 PRO_SERIAL_NUMBERS 資料
    INSERT INTO PRO_SERIAL_NUMBERS (PRODUCT_SN, ORDER_ITEM_NO, PRO_NO)
        VALUE ('N284X-42RP4-0J9KS', 1, 1),
    ('F81F0-G1VP0-L0B96', NULL, 1),
    ('SGH46-30U5H-S89PJ', NULL, 1),
    ('34GL1-72L86-B66YJ', NULL, 1),
    ('ZWKEB-FKZKD-5B335', NULL, 1),
    ('58C46-866ED-8IU73', NULL, 1),
    ('91X34-J2I88-O5X80', NULL, 1),
    ('Y964U-M4B42-1I3AT', NULL, 1),
    ('RC3QF-MOS1J-8297Q', NULL, 1),
    ('4E28V-8XGBB-7EC21', NULL, 1),
    ('6159V-5EKFU-SJ29F', 2, 2),
    ('0I799-M3ZEF-2A01A', NULL, 2),
    ('BI7I9-N468U-Z06M8', NULL, 2),
    ('2Z075-J0SO3-AQS59', NULL, 2),
    ('99J9V-8GD7J-TUGSO', NULL, 2),
    ('758T8-L3FG9-OGD13', NULL, 2),
    ('95VBH-690XD-9K8Z8', NULL, 2),
    ('C87B4-80U0W-IMWI7', NULL, 2),
    ('6UEGC-MS40X-S8FT9', NULL, 2),
    ('X356T-PJVOP-J91CQ', NULL, 2),
    ('WL0438-K673-QF0442', 3, 4),
    ('MP7027-A847-LK2046', 4, 4),
    ('JC0152-Z706-QP0594', NULL, 4),
    ('NW5103-B881-MA7203', NULL, 4),
    ('DO6036-P933-RW0636', NULL, 4),
    ('AD4922-R513-WV5522', NULL, 4),
    ('YB6782-H917-CR5360', NULL, 4),
    ('RP5997-T591-GX9174', NULL, 4),
    ('LT5474-A166-PJ6682', NULL, 4),
    ('VS2997-I007-TA2961', NULL, 4),
    ('WM6543-S154-KB2694', 5, 5),
    ('PN8308-E617-QR2213', NULL, 5),
    ('DC1630-W469-WL3730', NULL, 5),
    ('QJ6483-C003-DO1995', NULL, 5),
    ('NW7212-H778-WZ6419', NULL, 5),
    ('KG6754-J023-VX8857', NULL, 5),
    ('PC2892-R098-ZY1943', NULL, 5),
    ('HP1173-L329-IE7265', NULL, 5),
    ('TR3931-U962-VJ2218', NULL, 5),
    ('BF1311-V880-PU5956', NULL, 5),
    ('HJ2625-B516-AY8960', 6, 6),
    ('OV8478-V824-SU8713', NULL, 6),
    ('TY8675-L175-ZN5288', NULL, 6),
    ('PP4319-X118-UP0813', NULL, 6),
    ('WA2107-V577-HV4546', NULL, 6),
    ('YL6888-Y119-ZC1397', NULL, 6),
    ('YP2340-D694-ZD6163', NULL, 6),
    ('IW4100-I212-YD7384', NULL, 6),
    ('FE6549-X221-DD9791', NULL, 6),
    ('JP0109-X682-PH8771', NULL, 6),
    ('0100DC61E98A335F', 7, 7),
    ('0100C85A8AA0E2C3', NULL, 7),
    ('010090BF9F2E5E4A', NULL, 7),
    ('0100FE9374BCAD14', NULL, 7),
    ('01005D4A67BFC57E', NULL, 7),
    ('0100307722DBAE19', NULL, 7),
    ('0100C1F045C9C810', NULL, 7),
    ('01007290DED7230E', NULL, 7),
    ('010039D08DAAC82E', NULL, 7),
    ('01008E45EA294B0F', NULL, 7),
    ('6765U-1Y14S-S89Z0', 8, 8),
    ('LX49V-0VDOP-UKFLG', NULL, 8),
    ('5U871-RZ9LW-F7FNV', NULL, 8),
    ('BC9ND-3071Q-A0Y1T', NULL, 8),
    ('C2XTZ-N3246-9P507', NULL, 8),
    ('SU889-KRG7G-PE5V5', NULL, 8),
    ('S70E2-J7BDV-C5KOF', NULL, 8),
    ('3G8A8-L0L66-FF3U2', NULL, 8),
    ('8E2MR-6UU02-7Y6OM', NULL, 8),
    ('0F4W7-29NZX-818MJ', NULL, 8),
    ('NPAX-18366_38-W25955307', 9, 10),
    ('NPYX-07811_14-L72766112', NULL, 10),
    ('NPUU-35083_11-E62679559', NULL, 10),
    ('NPUO-76035_39-S50812581', NULL, 10),
    ('NPSA-19228_38-F26028574', NULL, 10),
    ('NPOB-38815_52-Z57426927', NULL, 10),
    ('NPFT-38181_77-H19348940', NULL, 10),
    ('NPSI-63428_57-J78921127', NULL, 10),
    ('NPRJ-64964_57-P45035797', NULL, 10),
    ('NPRX-02491_41-D95451721', NULL, 10),
    ('0100B38D2FCB776B', NULL, 11),
    ('0100B3D47857F3BF', NULL, 11),
    ('0100896E9C63A7FE', NULL, 11),
    ('0100AE1B6D9931CF', NULL, 11),
    ('0100226C7541CD09', NULL, 11),
    ('01005C2A2050E3BC', NULL, 11),
    ('01001D333FD9D810', NULL, 11),
    ('01009690926FC83F', NULL, 11),
    ('0100C729C5343DBF', NULL, 11),
    ('0100CE0E8DF6AA9A', NULL, 11),
    ('AP1496-S025-DS6465', NULL, 12),
    ('RV6623-L336-BO4792', NULL, 12),
    ('SU6626-M684-KX2943', NULL, 12),
    ('KP1834-F315-AD2140', NULL, 12),
    ('NM6334-R759-RV5865', NULL, 12),
    ('QL0868-C988-YO8920', NULL, 12),
    ('DM7799-W514-JC8420', NULL, 12),
    ('KE9799-W069-YY0597', NULL, 12),
    ('ZQ0032-E826-QH6249', NULL, 12),
    ('VR6799-U870-ZY4889', NULL, 12),
    ('01005DD41270C7B2', NULL, 13),
    ('0100D548577508E2', NULL, 13),
    ('0100D21CAA8BD6C2', NULL, 13),
    ('0100880B11430CDE', NULL, 13),
    ('0100986F955E40A0', NULL, 13),
    ('01004F898C8B2401', NULL, 13),
    ('0100378AB12ED69C', NULL, 13),
    ('010041EF1C89C0FB', NULL, 13),
    ('010033692862ADB2', NULL, 13),
    ('0100184BFCA43333', NULL, 13),
    ('0100A7F6A547E436', NULL, 14),
    ('01005F94D1BCA937', NULL, 14),
    ('01005934562D2B0B', NULL, 14),
    ('01001342DAA14542', NULL, 14),
    ('0100C17DE7F2BF0E', NULL, 14),
    ('0100F87FF29B765A', NULL, 14),
    ('010041F83B51B90E', NULL, 14),
    ('010095E6DFA7D945', NULL, 14),
    ('0100974959091D2D', NULL, 14),
    ('0100EE92FB9E68AB', NULL, 14),
    ('KFMIY-T886X-O9HOY-MJSQ7-YPQ15', NULL, 15),
    ('Z22QH-J1DMI-T04WA-MB4C3-GHR4J', NULL, 15),
    ('P8Q83-QEQ00-MBKKL-0C4Z4-PGLD3', NULL, 15),
    ('KEK3N-1WQBI-NS8W2-KVM5H-4ONY0', NULL, 15),
    ('WT4UQ-K14W8-2W62H-OC3JN-8Y1HW', NULL, 15),
    ('24JFC-WRLP2-BFFZO-VS76Q-1UCVE', NULL, 15),
    ('A6OK8-147UF-R453T-HE6QH-7SDGA', NULL, 15),
    ('11686-P6B2J-YAB9D-1N53C-4AGNG', NULL, 15),
    ('OGBB7-AOP64-4AXIQ-ZZT0Q-IFOPV', NULL, 15),
    ('8SDQR-HAAGV-BU1HV-N58IU-QK6HG', NULL, 15),
    ('FM0JV-N741H-3JOJL-WB7H6-2COLY', NULL, 16),
    ('2F472-BJGPU-2F2LD-I441O-WKELH', NULL, 16),
    ('CSQ8O-11J6V-542LM-KNV2Y-INC6G', NULL, 16),
    ('1DCIG-VZQBR-7DDNQ-SHGZ1-R97OZ', NULL, 16),
    ('PB6FQ-I2MTG-2N7LD-YKMBU-QFVJR', NULL, 16),
    ('IFNDM-AOUFT-84TRF-UUCWL-IBENV', NULL, 16),
    ('FSTND-ZAJU2-55ADJ-1O8FK-PF5J9', NULL, 16),
    ('FPFWP-AI2WY-19XU3-MR9Z1-Z2MWR', NULL, 16),
    ('J2J94-20ZMH-8ZOYQ-HV5IT-Q6Y7A', NULL, 16),
    ('SDNVM-5N73L-L9Z4W-9ER3W-PVPGK', NULL, 16),
    ('0Y2YA-3IUFH-19584', NULL, 17),
    ('N0091-0EGF2-34N07', NULL, 17),
    ('NC8BQ-4WRTU-QP12M', NULL, 17),
    ('G0ON4-1W0GM-14C91', NULL, 17),
    ('ZP6R7-63J41-K3997', NULL, 17),
    ('34DIL-8C170-R318K', NULL, 17),
    ('9O1DQ-R9A3W-N3FB7', NULL, 17),
    ('47NIZ-0109J-5256R', NULL, 17),
    ('M0X0C-WY275-8CMQ0', NULL, 17),
    ('0GP8B-HMD0I-1N4JB', NULL, 17);

-- ===== END OF pro_serial_numbers.sql =====
    SET FOREIGN_KEY_CHECKS = 1;