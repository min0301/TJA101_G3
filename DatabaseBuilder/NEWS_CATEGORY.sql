/*-----------------------------------------------------------
確保在 teamtest 資料庫下操作
-----------------------------------------------------------*/
CREATE
DATABASE IF NOT EXISTS teamtest;
USE
teamtest;

/*-----------------------------------------------------------
1. 清除舊表（若存在）
- 最後刪除 NEWS_CATEGORY
-----------------------------------------------------------*/
DROP TABLE IF EXISTS NEWS_CATEGORY;

/*-----------------------------------------------------------
2. 建立 NEWS_CATEGORY（新聞類別）
-----------------------------------------------------------*/
CREATE TABLE NEWS_CATEGORY
(
    NCAT_NO   INT AUTO_INCREMENT NOT NULL,
    NCAT_NAME VARCHAR(50) NOT NULL,
    CONSTRAINT NEWS_CATEGORY_PK PRIMARY KEY (NCAT_NO)
) AUTO_INCREMENT = 1 COMMENT='新聞類別';


INSERT INTO NEWS_CATEGORY (NCAT_NAME)
VALUES ('PC'),
       ('PS5'),
       ('Switch'),
       ('Xbox');
