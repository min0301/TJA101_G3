CREATE
DATABASE IF NOT EXISTS teamtest;
use
teamtest;

-- 刪除 檢舉類型表格 表格 --  
DROP TABLE IF EXISTS REPORT_TYPE;

-- 建立檢舉類型表格 -- 
CREATE TABLE REPORT_TYPE
(
    `RPI_NO`   INT AUTO_INCREMENT NOT NULL PRIMARY KEY COMMENT '檢舉類型編號',
    `RPI_TYPE` VARCHAR(255) NOT NULL COMMENT '檢舉類型項目'
)COMMENT '檢舉類型';

-- 新增檢舉類型資料 -- 
INSERT INTO REPORT_TYPE (`RPI_TYPE`)
VALUES ('與本版主題無關'),
       ('洗版'),
       ('謾罵'),
       ('限制級'),
       ('詐騙、詐欺或不實資訊'),
       ('其他');