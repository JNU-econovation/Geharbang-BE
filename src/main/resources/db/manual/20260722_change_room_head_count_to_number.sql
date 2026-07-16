-- Store room head count as an actual number.
-- Guest house filters still use _1인실, _2인실, _3인이상 as query categories.

SET NAMES utf8mb4;
START TRANSACTION;

ALTER TABLE room
    ADD COLUMN head_count_number INT NULL AFTER head_count;

UPDATE room
SET head_count_number = CASE head_count
    WHEN '_1인실' THEN 1
    WHEN '_2인실' THEN 2
    WHEN '_3인이상' THEN 3
    ELSE 1
END;

ALTER TABLE room
    DROP COLUMN head_count;

ALTER TABLE room
    CHANGE head_count_number head_count INT NOT NULL;

COMMIT;
