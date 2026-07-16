-- Guest house mood reset:
-- 바닷가, 동물, 자연∙숲, 대규모파티, 소규모파티, 조용한, 활발한,
-- 감성∙느좋, 파티 X, 솔로, 한달살이
-- Dot/space labels are represented with underscores in DB/API enum values.

SET NAMES utf8mb4;
START TRANSACTION;

ALTER TABLE mood
    MODIFY value ENUM(
        '감성', '사교적', '사색', '잔잔한', '조용한', '활발한', '휴식', '힐링',
        '바닷가', '동물', '자연_숲', '대규모파티', '소규모파티',
        '감성_느좋', '파티_X', '솔로', '한달살이'
    ) NULL;

UPDATE mood
SET value = CASE value
    WHEN '사교적' THEN '소규모파티'
    WHEN '힐링' THEN '자연_숲'
    WHEN '사색' THEN '조용한'
    WHEN '잔잔한' THEN '조용한'
    WHEN '감성' THEN '감성_느좋'
    WHEN '휴식' THEN '한달살이'
    ELSE value
END;

CREATE TEMPORARY TABLE mood_dedup AS
SELECT DISTINCT guest_house_post_id, value
FROM mood;

DELETE FROM mood;

INSERT INTO mood (guest_house_post_id, value)
SELECT guest_house_post_id, value
FROM mood_dedup;

DROP TEMPORARY TABLE mood_dedup;

ALTER TABLE mood
    MODIFY value ENUM(
        '바닷가', '동물', '자연_숲', '대규모파티', '소규모파티',
        '조용한', '활발한', '감성_느좋', '파티_X', '솔로', '한달살이'
    ) NULL;

COMMIT;
