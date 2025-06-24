-- MEMBER Table Test Data
INSERT INTO MEMBER (MEM_NAME, MEM_NICK_NAME, MEM_ACCOUNT, MEM_PASSWORD, MEM_EMAIL,
                    MEM_ADRS, MEM_PHONE, MEM_BIRTHDAY)
VALUES ( '妙花種子', '種子種子', 'SeedSeed', '123456789abc'
       , 'seed@TJA.com.tw', '迷霧森林', '0988888888', '1955-04-09');
select *
from MEMBER
where MEM_PHONE = 0988888888

