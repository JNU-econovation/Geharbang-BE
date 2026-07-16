-- Region reset:
-- 제주시, 서귀포시, 중문, 성산/구좌, 애월/협재, 우도/기타
-- Slash labels are represented with underscores in DB/API enum values.

SET NAMES utf8mb4;
START TRANSACTION;

ALTER TABLE guest_house_post
    MODIFY region ENUM(
        '제주시', '서귀포시', '서부권', '동부권', '도서지역', '중문_대정',
        '중문', '성산_구좌', '애월_협재', '우도_기타'
    ) NULL;

ALTER TABLE staff_recruitment
    MODIFY region ENUM(
        '제주시', '서귀포시', '서부권', '동부권', '도서지역', '중문_대정',
        '중문', '성산_구좌', '애월_협재', '우도_기타'
    ) NULL;

ALTER TABLE application_record
    MODIFY staff_recruitment_region ENUM(
        '제주시', '서귀포시', '서부권', '동부권', '도서지역', '중문_대정',
        '중문', '성산_구좌', '애월_협재', '우도_기타'
    ) NULL;

UPDATE guest_house_post
SET region = CASE region
    WHEN '서부권' THEN '애월_협재'
    WHEN '동부권' THEN '성산_구좌'
    WHEN '도서지역' THEN '우도_기타'
    WHEN '중문_대정' THEN '중문'
    ELSE region
END;

UPDATE staff_recruitment
SET region = CASE region
    WHEN '서부권' THEN '애월_협재'
    WHEN '동부권' THEN '성산_구좌'
    WHEN '도서지역' THEN '우도_기타'
    WHEN '중문_대정' THEN '중문'
    ELSE region
END;

UPDATE application_record
SET staff_recruitment_region = CASE staff_recruitment_region
    WHEN '서부권' THEN '애월_협재'
    WHEN '동부권' THEN '성산_구좌'
    WHEN '도서지역' THEN '우도_기타'
    WHEN '중문_대정' THEN '중문'
    ELSE staff_recruitment_region
END
WHERE staff_recruitment_region IS NOT NULL;

ALTER TABLE guest_house_post
    MODIFY region ENUM('제주시', '서귀포시', '중문', '성산_구좌', '애월_협재', '우도_기타') NULL;

ALTER TABLE staff_recruitment
    MODIFY region ENUM('제주시', '서귀포시', '중문', '성산_구좌', '애월_협재', '우도_기타') NULL;

ALTER TABLE application_record
    MODIFY staff_recruitment_region ENUM('제주시', '서귀포시', '중문', '성산_구좌', '애월_협재', '우도_기타') NULL;

COMMIT;
