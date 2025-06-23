-- ---------------------------------------------------------*/
CREATE
DATABASE IF NOT EXISTS teamtest;
USE
teamtest;

-- 1. 清除舊表（若存在）
--   ---------------------------------------------------------*/

DROP TABLE IF EXISTS NEWS_COM_REPORT;


-- 4. 建立 NEWS_COM_REPORT（新聞評論檢舉）
-- ※ 報表類別／原因的參考表（REPORT_TYPE、REPORT_REASON）請依實際需求先行建立
-- ---------------------------------------------------------*/
CREATE TABLE NEWS_COM_REPORT
(
    NEWS_COM_REPORT_NO     INT AUTO_INCREMENT NOT NULL,
    REPORTER               INT      NOT NULL, -- 檢舉者（會員）
    REPORT_TYPE            INT      NOT NULL, -- 檢舉類型（FK：REPORT_TYPE.RPI_NO）
    NEWS_COM_REPORT_STATUS INT      NOT NULL DEFAULT 0, -- 0=未處理 1=已處理
    NCOM_NO                INT      NOT NULL, -- 被檢舉的評論
    CREATE_TIME            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FINISH_TIME            DATETIME,
    CONSTRAINT NEWS_COM_REPORT_PK PRIMARY KEY (NEWS_COM_REPORT_NO),
    CONSTRAINT NCR_FK_MEMBER FOREIGN KEY (REPORTER) REFERENCES MEMBER (MEM_NO),
    CONSTRAINT NCR_FK_COMMENT FOREIGN KEY (NCOM_NO) REFERENCES NEWS_COMMENTS (NCOM_NO),
    CONSTRAINT NCR_FK_RTYPE FOREIGN KEY (REPORT_TYPE) REFERENCES REPORT_TYPE (RPI_NO)
) AUTO_INCREMENT = 1;
-- COMMENT='新聞評論檢舉記錄';

-- 5-3 新增五筆留言檢舉樣例
--     REPORT_TYPE 1: 廣告垃圾   2: 人身攻擊 （僅示範）
INSERT INTO NEWS_COM_REPORT (REPORTER, REPORT_TYPE, NEWS_COM_REPORT_STATUS, NCOM_NO, CREATE_TIME, FINISH_TIME)
VALUES (2, 1, 0, 3, '2025-06-07 12:00:00', NULL),
       (3, 2, 0, 5, '2025-06-07 12:05:00', NULL),
       (4, 1, 1, 7, '2025-06-06 16:00:00', '2025-06-06 18:00:00'),
       (5, 2, 0, 10, '2025-06-06 19:30:00', NULL),
       (6, 1, 1, 12, '2025-06-06 20:00:00', '2025-06-06 21:15:00');