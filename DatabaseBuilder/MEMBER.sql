-- 建立資料庫並設定自動遞增屬性
CREATE DATABASE IF NOT EXISTS teamtest;
USE teamtest;

-- 移除既有 MEMBER 資料表（若存在）
DROP TABLE IF EXISTS MEMBER;

-- 設定自增主鍵起始值與遞增量
SET auto_increment_offset = 1;
SET auto_increment_increment = 1;

-- 建立 MEMBER 資料表（含完整欄位，CAN_USED_PRO 預設為 FALSE）
CREATE TABLE MEMBER
(
    MEM_NO         INT AUTO_INCREMENT NOT NULL,
    MEM_NAME       VARCHAR(50)        NOT NULL,
    MEM_NICK_NAME  VARCHAR(50)        NOT NULL,
    MEM_ACCOUNT    VARCHAR(50)        NOT NULL,
    MEM_PASSWORD   VARCHAR(50)        NOT NULL,
    MEM_EMAIL      VARCHAR(100)       NOT NULL,
    MEM_ICON_DATA  LONGBLOB,
    MEM_ADRS       VARCHAR(100)       NOT NULL,
    MEM_PHONE      VARCHAR(50)        NOT NULL,
    MEM_BIRTHDAY   DATE               NOT NULL,
    MEM_CREATE     DATETIME                    DEFAULT CURRENT_TIMESTAMP,
    MEM_UPDATE     DATETIME ON UPDATE CURRENT_TIMESTAMP,
    MEM_STATUS     CHAR(1)            NOT NULL DEFAULT '0',
    MEM_TOKEN      VARCHAR(50)                 DEFAULT NULL,
    MEM_EMAIL_AUTH VARCHAR(50)                 DEFAULT NULL,
    CAN_POST       BOOLEAN            NOT NULL DEFAULT TRUE,
    CAN_COMMENT    BOOLEAN            NOT NULL DEFAULT TRUE,
    CAN_USED_PRO   BOOLEAN            NOT NULL DEFAULT FALSE,
    AUTHENTICATED  BOOLEAN            NOT NULL DEFAULT TRUE,
    POINT          INT                         DEFAULT 0,
    CONSTRAINT MEMBER_PK PRIMARY KEY (MEM_NO)
);

-- 插入 37 筆會員資料
INSERT INTO MEMBER (MEM_NAME, MEM_NICK_NAME, MEM_ACCOUNT, MEM_PASSWORD, MEM_EMAIL,
                    MEM_ADRS, MEM_PHONE, MEM_BIRTHDAY,
                    MEM_CREATE, MEM_UPDATE, MEM_STATUS, MEM_TOKEN, MEM_EMAIL_AUTH,
                    CAN_POST, CAN_COMMENT, CAN_USED_PRO, AUTHENTICATED, POINT)
VALUES
-- 管理員：MEM_NO = 1
('管理員', '管理員', 'admin', 'AdminPass123', 'admin@teamtest.local', '臺北市中正區行政路1號', '0912345678',
 '1980-01-01', '2024-01-10 08:15:00', '2025-05-20 10:30:00', '0', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 999),

('王小明', '幻影疾風', 'user_one', 'PaSsWoRd123', 'a1b2c3d@TJA101.com.tw', '屏東縣崁頂鄉永安路87號', '0922815837',
 '1990-05-15', '2024-02-05 09:20:00', '2024-12-15 11:45:00', '0', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('陳麗華', '冰雪薔薇', 'test_account', 'SecurePwd!', 'e4f5g6h@TJA101.com.tw', '高雄市路竹區倫理街53號', '0924206351',
 '1988-11-22', '2024-03-12 14:50:00', '2025-01-10 16:00:00', '0', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('林志偉', '烈焰戰神', 'demo_user', 'MySecret99', 'i7j8k9l@TJA101.com.tw', '臺北市大同區民族西路36號', '0939489171',
 '1995-03-01', '2024-04-18 10:05:00', '2025-02-20 09:30:00', '0', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('張雅婷', '月影潛行者', 'info_test', 'Strong@Pass', 'm0n1o2p@TJA101.com.tw', '臺北市中山區松江路76號', '0910347356',
 '1992-07-10', '2024-05-22 17:30:00', '2025-03-15 14:10:00', '0', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('李文傑', '聖光使者', 'another_user', 'Complex#123', 'q3r4s5t@TJA101.com.tw', '彰化縣大村鄉學府路56號', '0933984252',
 '2000-01-25', '2024-06-30 08:00:00', '2025-04-25 12:20:00', '0', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('黃美玲', '暗夜精靈', 'test_sample', 'SimplePass', 'u6v7w8x@TJA101.com.tw', '桃園市平鎮區廣豐街福壽四巷14號3樓',
 '0935633441', '1985-09-08', '2024-07-10 13:15:00', '2025-05-01 15:55:00', '0', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('劉俊豪', '疾風之狼', 'data_user', 'HardToGuess', 'y9z0a1b@TJA101.com.tw', '屏東縣萬丹鄉吉林路30號', '0922914555',
 '1997-04-03', '2024-08-05 11:25:00', '2025-05-10 10:40:00', '0', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('蔡淑芬', '星辰旅人', 'trial_account', 'PasswordTest', 'c2d3e4f@TJA101.com.tw', '桃園市龜山區文明一街76號',
 '0988439659', '1993-12-18', '2024-09-12 09:45:00', '2025-05-15 13:05:00', '0', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('楊宗翰', '蒼穹之鷹', 'account_test', 'MySecureKey', 'g5h6i7j@TJA101.com.tw', '苗栗縣大湖鄉信義路50號', '0987839119',
 '1989-06-30', '2024-10-01 14:00:00', '2025-06-01 09:10:00', '0', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('吳怡君', '碧海遊龍', 'tester_user', 'JustATest', 'k8l9m0n@TJA101.com.tw', '臺南市關廟區中山路２段77號', '0926380187',
 '1996-02-14', '2024-11-20 16:30:00', '2025-06-02 11:45:00', '0', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('蕭雅文', '紫電狂刀', 'alpha_user', 'AnotherPwd!23', 'o1p2q3r@TJA101.com.tw', '嘉義縣新港鄉古民街22號2樓',
 '0989121836', '1991-08-20', '2024-12-15 10:20:00', '2025-06-03 10:00:00', '0', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('徐國樑', '幽夢旅人', 'beta_tester', 'SuperSecret#01', 's4t5u6v@TJA101.com.tw', '臺中市大里區東明路52號4樓',
 '0986597364', '1987-05-03', '2025-01-05 09:15:00', '2025-06-04 12:30:00', '0', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('曾靜宜', '赤炎魔導', 'gamma_test', 'CodeWord456', 'w7x8y9z@TJA101.com.tw', '臺中市清水區鎮新南路54號8樓',
 '0953237113', '1998-11-09', '2025-01-20 11:05:00', '2025-06-04 14:50:00', '0', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('潘志明', '寒冰箭手', 'delta_info', 'TopSecretKey', 'b0c1d2e@TJA101.com.tw', '臺中市太平區振德街11號', '0971652133',
 '1994-01-17', '2025-02-10 15:40:00', '2025-06-04 16:20:00', '0', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('賴惠君', '森林遊俠', 'epsilon_user', 'NewPassword12', 'f3g4h5i@TJA101.com.tw', '臺東縣臺東市中華路１段11號',
 '0970214685', '1999-07-28', '2025-03-01 12:30:00', '2025-06-04 18:10:00', '0', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('羅建宏', '暮色吟遊者', 'zeta_sample', 'TestThisPass', 'j6k7l8m@TJA101.com.tw', '臺南市南區利南街94號', '0937847416',
 '1986-03-12', '2025-03-15 17:55:00', '2025-06-04 19:45:00', '0', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('簡佩珊', '雷霆之錘', 'eta_data', 'CheckMyPwd', 'n9o0p1q@TJA101.com.tw', '高雄市前鎮區修文街18號之13', '0934782721',
 '1992-09-25', '2025-04-05 08:00:00', '2025-06-04 20:30:00', '0', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('鍾俊彥', '幻夜舞者', 'theta_account', 'TryThisOne', 'r2s3t4u@TJA101.com.tw', '高雄市茄萣區白砂路89號之13',
 '0955328726', '2001-04-06', '2025-04-20 10:25:00', '2025-06-04 21:15:00', '0', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('杜芳瑜', '聖潔之羽', 'iota_test', 'FinalPwdCheck', 'v5w6x7y@TJA101.com.tw', '新竹縣竹北市界址15號', '0987630010',
 '1983-12-01', '2025-05-01 14:15:00', '2025-06-04 22:00:00', '0', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('洪偉誠', '影舞者', 'kappa_user', 'LastTestAccount', 'z8a9b0c@TJA101.com.tw', '臺南市安定區大同34號', '0935176482',
 '1995-06-19', '2025-05-10 16:45:00', '2025-06-04 22:45:00', '0', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('林志明', '狂暴之熊', 'user001', 'SecureUser1', 'd1e2f3g@TJA101.com.tw', '高雄市楠梓區大學二十三街20號', '0986632252',
 '1993-03-08', '2025-05-15 09:00:00', '2025-06-04 23:30:00', '0', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('王淑芬', '迷霧行者', 'account002', 'TestPass2', 'h4i5j6k@TJA101.com.tw', '臺中市潭子區頭家路36號9樓', '0914291484',
 '1988-11-15', '2025-05-20 11:10:00', '2025-06-04 23:50:00', '0', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('張俊傑', '熾天使', 'demoUser003', 'MyPwd3', 'l7m8n9o@TJA101.com.tw', '臺南市關廟區花園三街14號', '0911138891',
 '1997-07-22', '2025-05-25 13:25:00', '2025-06-05 00:10:00', '0', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('李雅婷', '冰霜女王', 'infoTest004', 'Simple123', 'p0q1r2s@TJA101.com.tw', '桃園市中壢區新建市場6號', '0968712238',
 '1990-01-05', '2025-05-30 15:30:00', '2025-06-05 00:30:00', '0', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('陳文雄', '迅捷獵豹', 'anotherUser005', 'Strong!Pass', 't3u4v5w@TJA101.com.tw', '高雄市小港區翠亨南路72號',
 '0938529405', '1995-09-12', '2025-06-01 10:45:00', '2025-06-05 00:50:00', '0', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('黃美玲', '落日餘暉', 'testSample006', 'CheckPwd4', 'x6y7z8a@TJA101.com.tw', '桃園市中壢區民溪二路90號之11',
 '0956364473', '1986-05-28', '2025-06-02 12:00:00', '2025-06-05 01:10:00', '0', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('劉俊豪', '奔雷手', 'dataUser007', 'DataUser5', 'b9c0d1e@TJA101.com.tw', '新北市中和區秀朗路３段37號4樓', '0954750566',
 '1999-04-01', '2025-06-03 14:15:00', '2025-06-05 01:30:00', '0', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('蔡怡君', '魅影刺客', 'trialAcc008', 'TrialAcc6', 'f2g3h4i@TJA101.com.tw', '桃園市中壢區中正路４段86號', '0910418815',
 '1991-12-10', '2025-06-04 08:20:00', '2025-06-05 01:50:00', '0', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('楊宗翰', '光明祭司', 'accountTest009', 'AccountTest7', 'j5k6l7m@TJA101.com.tw', '高雄市路竹區大社路58號之1',
 '0937667217', '1984-08-17', '2025-06-04 10:35:00', '2025-06-05 02:10:00', '0', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('吳佩珊', '暗影之刃', 'testerUser010', 'Tester8', 'n8o9p0q@TJA101.com.tw', '南投縣埔里鎮樹人二街58號9樓', '0935031782',
 '1998-02-25', '2025-06-04 12:45:00', '2025-06-05 02:30:00', '0', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('蕭國樑', '怒火 Berserker', 'alphaUser011', 'AlphaPwd9', 'r1s2t3u@TJA101.com.tw', '臺中市新社區興中街31號11樓',
 '0955674702', '1992-06-03', '2025-06-04 14:55:00', '2025-06-05 02:50:00', '0', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('徐靜宜', '沉靜之語', 'betaTester012', 'BetaTest10', 'v4w5x6y@TJA101.com.tw', '臺南市東山區龍鳳一街32號', '0924948942',
 '1989-10-11', '2025-06-04 16:05:00', '2025-06-05 03:10:00', '0', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('曾志明', '烈陽之弓', 'gammaTest013', 'GammaInfo11', 'z7a8b9c@TJA101.com.tw', '新北市鶯歌區宏德司法新村90號6樓之19',
 '0919629699', '1996-01-18', '2025-06-04 17:20:00', '2025-06-05 03:30:00', '0', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('潘惠君', '冰晶法師', 'deltaInfo014', 'DeltaUser12', 'd0e1f2g@TJA101.com.tw', '桃園市蘆竹區溪洲68號', '0989684649',
 '1987-09-24', '2025-06-04 18:35:00', '2025-06-05 03:50:00', '0', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('賴建宏', '荊棘之盾', 'epsilonUser015', 'EpsilonAcc13', 'h3i4j5k@TJA101.com.tw', '新北市瑞芳區洞頂路87號之11',
 '0937887702', '2000-05-01', '2025-06-04 19:45:00', '2025-06-05 04:10:00', '0', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0),
('羅雅文', '星夜呢喃', 'zetaSample016', 'ZetaSample14', 'l6m7n8o@TJA101.com.tw', '臺中市清水區臨海路95號之7',
 '0913802841', '1994-11-07', '2025-06-04 20:55:00', '2025-06-05 04:30:00', '0', NULL, NULL, TRUE, TRUE, FALSE, TRUE, 0);

-- 測試預設欄位是否正常
INSERT INTO MEMBER (MEM_NAME, MEM_NICK_NAME, MEM_ACCOUNT, MEM_PASSWORD, MEM_EMAIL,
                    MEM_ADRS, MEM_PHONE, MEM_BIRTHDAY)
VALUES ( '妙花種子', '種子種子', 'SeedSeed', '123456789abc'
       , 'seed@TJA.com.tw', '迷霧森林', '0988888888', '1955-04-09');
select *
from MEMBER
where MEM_PHONE = 0988888888
   