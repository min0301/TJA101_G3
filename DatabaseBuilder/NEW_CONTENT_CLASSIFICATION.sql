CREATE
DATABASE IF NOT EXISTS teamtest;
USE
teamtest;

-- 1. 清除舊表（若存在）
-- ---------------------------------------------------------*/
DROP TABLE IF EXISTS NEW_CONTENT_CLASSIFICATION;

-- 3. 建立 NEW_CONTENT_CLASSIFICATION（新聞內容分類）
-- 一篇新聞可屬於多個類別；一個類別可對應多篇新聞
-- --------------------------------------------------------*/
CREATE TABLE NEW_CONTENT_CLASSIFICATION
(
    NCC_NO  INT AUTO_INCREMENT NOT NULL,
    NCAT_NO INT NOT NULL,
    NEWS_NO INT NOT NULL,
    CONSTRAINT NEW_CONTENT_CLASSIFICATION_PK PRIMARY KEY (NCC_NO),
    CONSTRAINT NCC_FK_CATEGORY FOREIGN KEY (NCAT_NO)
        REFERENCES NEWS_CATEGORY (NCAT_NO),
    CONSTRAINT NCC_FK_NEWS FOREIGN KEY (NEWS_NO)
        REFERENCES NEWS (NEWS_NO)
) AUTO_INCREMENT = 1 COMMENT='新聞內容 ↔ 類別對照表';

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