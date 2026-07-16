-- Add 기타 to guest house room type.

SET NAMES utf8mb4;
START TRANSACTION;

ALTER TABLE room
    MODIFY type ENUM('남성전용', '여성전용', '기타') NOT NULL;

COMMIT;
