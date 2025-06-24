-- 建立資料庫
CREATE DATABASE IF NOT EXISTS pixel_tribe;
USE pixel_tribe;

-- 移除清除既有資料表（依 FK 先子後父）
-- 依相依關係由下而上刪除，避免 Foreign-Key Constraint 衝突
DROP TABLE IF EXISTS NEWS_COM_REPORT;
DROP TABLE IF EXISTS NEWS_LIKE;
DROP TABLE IF EXISTS NEWS_IMAGE;
DROP TABLE IF EXISTS NEW_CONTENT_CLASSIFICATION;
DROP TABLE IF EXISTS NEWS_COMMENTS;
DROP TABLE IF EXISTS NEWS;
DROP TABLE IF EXISTS NEWS_CATEGORY;
DROP TABLE IF EXISTS REPORT_TYPE;
DROP TABLE IF EXISTS MEMBER;


-- 建立 核心資料表：MEMBER（會員基本資料）
CREATE TABLE MEMBER
(
    MEM_NO               INT AUTO_INCREMENT NOT NULL Comment '會員編號(PK)',
    MEM_NAME             VARCHAR(50)        NOT NULL comment '姓名',
    MEM_NICK_NAME        VARCHAR(50)        NOT NULL comment '暱稱',
    MEM_ACCOUNT          VARCHAR(50)        NOT NULL unique Comment '登入帳號',
    MEM_PASSWORD         VARCHAR(50)        NOT NULL Comment '密碼',
    MEM_EMAIL            VARCHAR(100)       NOT NULL unique ,
    MEM_ICON_DATA        LONGBLOB Comment '大頭貼',
    MEM_ADDR             VARCHAR(100)       NOT NULL comment '地址',
    MEM_PHONE            VARCHAR(50)        NOT NULL unique comment '行動電話',
    MEM_BIRTHDAY         DATE               NOT NULL,
    MEM_CREATE           DATETIME                    DEFAULT CURRENT_TIMESTAMP comment '建立時間',
    MEM_UPDATE           DATETIME ON UPDATE CURRENT_TIMESTAMP comment '最後修改時間',
    MEM_STATUS           CHAR(1)        NOT NULL DEFAULT '1' comment '1:正常 2:停權',
    MEM_TOKEN            VARCHAR(50)                 DEFAULT NULL,
    MEM_EMAIL_AUTH       VARCHAR(50)                 DEFAULT NULL comment 'email驗證碼',
    SEND_AUTH_Email_TIME DATETIME                    DEFAULT NULL comment '發送驗證信時間',
    CAN_POST             BOOLEAN            NOT NULL DEFAULT TRUE comment '可否發文',
    CAN_COMMENT          BOOLEAN            NOT NULL DEFAULT TRUE comment '可否留言',
    CAN_USED_PRO         BOOLEAN            NOT NULL DEFAULT FALSE comment '可否二手交易',
    PHONE_AUTHED         BOOLEAN            NOT NULL DEFAULT FALSE comment '手機是否驗證',
    POINT                INT                         DEFAULT 0 comment '會員積分',
    CONSTRAINT MEMBER_PK PRIMARY KEY (MEM_NO)
)comment '會員資料';
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


-- 建立 NEWS_CATEGORY 資料表（含完整欄位）
CREATE TABLE NEWS_CATEGORY
(
    NCAT_NO   INT AUTO_INCREMENT NOT NULL,
    NCAT_NAME VARCHAR(50)        NOT NULL,
    CONSTRAINT NEWS_CATEGORY_PK PRIMARY KEY (NCAT_NO)
) AUTO_INCREMENT = 1 COMMENT ='新聞類別';
-- 新增新聞類別資料 --
INSERT INTO NEWS_CATEGORY (NCAT_NAME)
VALUES ('PC'),
       ('PS5'),
       ('Switch'),
       ('Xbox');


-- 建立檢舉類型表格 --
CREATE TABLE REPORT_TYPE
(
    `RPI_NO`   INT AUTO_INCREMENT NOT NULL COMMENT '檢舉類型編號',
    `RPI_TYPE` VARCHAR(255)       NOT NULL COMMENT '檢舉類型項目',
    Constraint RPI_PK PRIMARY KEY (RPI_NO)
) COMMENT '檢舉類型';
-- 新增檢舉類型資料 -- 
INSERT INTO REPORT_TYPE (`RPI_TYPE`)
VALUES ('與本版主題無關'),
       ('洗版'),
       ('謾罵'),
       ('限制級'),
       ('詐騙、詐欺或不實資訊'),
       ('其他');


-- 建立內容主表：NEWS（新聞）
CREATE TABLE NEWS
(
    NEWS_NO     INT AUTO_INCREMENT NOT NULL comment '新聞編號(PK)',
    NEWS_TIT    VARCHAR(255)       NOT NULL comment '新聞標題',
    NEWS_CON    VARCHAR(9000)      NOT NULL comment '新聞內容',
    NEWS_UPDATE DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '最後更新時間，預設為 NULL',
    NEWS_CRDATE DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '自動填入資料插入時間',
    MEM_NO      INT                NOT NULL DEFAULT 1 COMMENT '預設管理員 1',
    CONSTRAINT NEWS_PK PRIMARY KEY (NEWS_NO),
    CONSTRAINT NEWS_FK_MEMBER FOREIGN KEY (MEM_NO) REFERENCES MEMBER (MEM_NO)
) AUTO_INCREMENT = 1 comment ='新聞主表';
-- 插入 50 筆假資料，其中部分 NEWS_UPDATE 設為 NULL
-- 先插入5筆 
INSERT INTO NEWS (NEWS_TIT, NEWS_CON, NEWS_UPDATE, NEWS_CRDATE)
VALUES ('PS5 獨佔大作《貓咪大亂鬥》宣布延期！原因竟是貓咪演員太難搞？',
        '開發商喵電感應娛樂宣布《貓咪大亂鬥》延期，原因為貓咪演員配合度不穩，導致動作捕捉進度落後。',
        '2025-06-01 10:00:00', '2025-05-30 09:00:00'),

       ('Steam 夏季特賣開跑！錢包君表示：我還沒準備好啊！',
        '數千款遊戲大幅折扣開賣，建議玩家列好清單避免失控消費。',
        '2025-06-02 11:00:00', '2025-06-01 08:30:00'),

       ('獨立遊戲《時空紙牌》登上排行榜前十名！',
        '由三人團隊開發的《時空紙牌》憑藉創新玩法和懷舊像素風格，成功登上 Steam 排行榜前十名。',
        NULL, '2025-06-04 10:00:00'),

       ('遊戲週邊商品瘋搶！限定款爆賣斷貨！',
        '人氣遊戲《星辰幻境》的官方週邊商品一上架即被搶購一空，限定款數分鐘內售罄。',
        '2025-06-05 15:30:00', '2025-06-04 11:00:00'),

       ('知名實況主轉戰 YouTube Gaming！',
        'Twitch 主播「阿宅勇者」轉戰 YouTube Gaming 開新頻道，粉絲留言不斷湧現。',
        '2025-06-05 16:00:00', '2025-06-04 12:00:00');


-- 建立 NEWS_IMAGE：新聞圖片（多對一）
CREATE TABLE NEWS_IMAGE
(
    IMG_NO   INT AUTO_INCREMENT NOT NULL,
    IMG_DATA longblob comment '圖片檔案',
    NEWS_NO  INT                NOT NULL,
    IMG_TYPE VARCHAR(100)       NOT NULL comment 'MIME Type,例如image/png',
    CONSTRAINT NEWS_IMAGE_PK PRIMARY KEY (IMG_NO),
    CONSTRAINT NEWS_IMAGE_FK_NEWS FOREIGN KEY (NEWS_NO) REFERENCES NEWS (NEWS_NO)
) AUTO_INCREMENT = 1 comment '新聞圖片';


-- 建立 NEWS_COMMENTS：新聞留言
CREATE TABLE NEWS_COMMENTS
(
    NCOM_NO       INT AUTO_INCREMENT NOT NULL,
    NCOM_CON      VARCHAR(4000)      NOT NULL comment '留言內容',
    NCOM_CRE      DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '自動填入插入當下時間,留言時間',
    NCOM_STATUS   CHAR(1)        NOT NULL DEFAULT '1' COMMENT '1:正常 2:違規，預設為正常',
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
    NLIKE_STATUS CHAR(1)        NOT NULL DEFAULT '1' COMMENT '1:正常 2:讚 3:倒讚 ',
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


-- 建立 NEW_CONTENT_CLASSIFICATION：新聞 ↔ 類別
CREATE TABLE NEW_CONTENT_CLASSIFICATION
(
    NCC_NO  INT AUTO_INCREMENT NOT NULL,
    NCAT_NO INT                NOT NULL,
    NEWS_NO INT                NOT NULL,
    CONSTRAINT NEW_CONTENT_CLASSIFICATION_PK PRIMARY KEY (NCC_NO),
    CONSTRAINT NCC_FK_CATEGORY FOREIGN KEY (NCAT_NO)
        REFERENCES NEWS_CATEGORY (NCAT_NO),
    CONSTRAINT NCC_FK_NEWS FOREIGN KEY (NEWS_NO)
        REFERENCES NEWS (NEWS_NO)
) AUTO_INCREMENT = 1 COMMENT ='新聞內容分類對照';
-- 5-2 將前五篇新聞分別歸類
-- (假設 NEWS_NO 1~5 已於 NEWS 表存在)
INSERT INTO NEW_CONTENT_CLASSIFICATION (NCAT_NO, NEWS_NO)
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
    NEWS_COM_REPORT_NO     INT AUTO_INCREMENT NOT NULL ,
    REPORTER               INT                NOT NULL comment '檢舉者 MEM_NO',                                           -- 檢舉者（會員）
    REPORT_TYPE            INT                NOT NULL comment '檢舉類型 RPI_NO',                                           -- 檢舉類型（FK：REPORT_TYPE.RPI_NO）
    NEWS_COM_REPORT_STATUS CHAR(50)        NOT NULL DEFAULT '0' comment '0:未處理 1:已處理', -- 未處理 已處理
    NCOM_NO                INT                NOT NULL comment '被檢舉留言',                                           -- 被檢舉的評論
    CREATE_TIME            DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FINISH_TIME            DATETIME comment '處理完成時間',
    CONSTRAINT NEWS_COM_REPORT_PK PRIMARY KEY (NEWS_COM_REPORT_NO),
    CONSTRAINT NCR_FK_MEMBER FOREIGN KEY (REPORTER) REFERENCES MEMBER (MEM_NO),
    CONSTRAINT NCR_FK_COMMENT FOREIGN KEY (NCOM_NO) REFERENCES NEWS_COMMENTS (NCOM_NO),
    CONSTRAINT NCR_FK_RTYPE FOREIGN KEY (REPORT_TYPE) REFERENCES REPORT_TYPE (RPI_NO)
) AUTO_INCREMENT = 1 COMMENT='新聞評論檢舉記錄';
-- 新增五筆留言檢舉樣例
-- REPORT_TYPE 1: 廣告垃圾   2: 人身攻擊 （僅示範）
INSERT INTO NEWS_COM_REPORT (REPORTER, REPORT_TYPE, NEWS_COM_REPORT_STATUS, NCOM_NO, CREATE_TIME, FINISH_TIME)
VALUES (2, 1, '0', 3, '2025-06-07 12:00:00', NULL),
       (3, 2, '0', 5, '2025-06-07 12:05:00', NULL),
       (4, 1, '1', 7, '2025-06-06 16:00:00', '2025-06-06 18:00:00'),
       (5, 2, '0', 10, '2025-06-06 19:30:00', NULL),
       (6, 1, '1', 12, '2025-06-06 20:00:00', '2025-06-06 21:15:00');
