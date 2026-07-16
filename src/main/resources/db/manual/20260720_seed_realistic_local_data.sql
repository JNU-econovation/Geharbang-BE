-- Local/demo data reset: 100 guest houses + 100 staff recruitment posts.
-- All names and contact details are synthetic and must not be presented as real businesses.

SET NAMES utf8mb4;
START TRANSACTION;

DELETE FROM question_answer
WHERE application_record_id IN (
    SELECT id FROM application_record
    WHERE staff_recruitment_id IN (SELECT id FROM staff_recruitment)
);
DELETE FROM chat_message
WHERE chat_room_id IN (
    SELECT id FROM chat_room
    WHERE guest_house_post_id IS NOT NULL OR staff_recruitment_id IS NOT NULL
);
DELETE FROM chat_room
WHERE guest_house_post_id IS NOT NULL OR staff_recruitment_id IS NOT NULL;
DELETE FROM application_record
WHERE staff_recruitment_id IN (SELECT id FROM staff_recruitment);
DELETE FROM notification WHERE target_type = 'STAFF_RECRUITMENT';
DELETE FROM wish
WHERE guest_house_post_id IS NOT NULL OR staff_recruitment_id IS NOT NULL;
DELETE FROM review_image
WHERE review_id IN (
    SELECT id FROM review
    WHERE guest_house_post_id IS NOT NULL OR staff_recruitment_id IS NOT NULL
);
DELETE FROM review
WHERE guest_house_post_id IS NOT NULL OR staff_recruitment_id IS NOT NULL;

DELETE FROM weekly_day;
DELETE FROM party_image;
DELETE FROM party;
DELETE FROM room_image;
DELETE FROM room;
DELETE FROM amenity;
DELETE FROM mood;
DELETE FROM guest_house_post_image;
DELETE FROM guest_house_post;

DELETE FROM staff_recruitment_question;
DELETE FROM staff_recruitment_job;
DELETE FROM staff_recruitment_image;
DELETE FROM staff_recruitment;

ALTER TABLE guest_house_post AUTO_INCREMENT = 1;
ALTER TABLE guest_house_post_image AUTO_INCREMENT = 1;
ALTER TABLE amenity AUTO_INCREMENT = 1;
ALTER TABLE room AUTO_INCREMENT = 1;
ALTER TABLE room_image AUTO_INCREMENT = 1;
ALTER TABLE party AUTO_INCREMENT = 1;
ALTER TABLE party_image AUTO_INCREMENT = 1;
ALTER TABLE staff_recruitment AUTO_INCREMENT = 1;
ALTER TABLE staff_recruitment_image AUTO_INCREMENT = 1;
ALTER TABLE staff_recruitment_job AUTO_INCREMENT = 1;
ALTER TABLE staff_recruitment_question AUTO_INCREMENT = 1;

CREATE TEMPORARY TABLE seed_seq (n INT PRIMARY KEY);
INSERT INTO seed_seq
WITH RECURSIVE numbers AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM numbers WHERE n < 100
)
SELECT n FROM numbers;

INSERT INTO guest_house_post (
    owner_id, guest_house_name, region, lot_number_address, road_name_address,
    coordinates, introduction, instagram_id, phone_number, web_site,
    reservation_url, owner_message, status
)
SELECT
    5,
    CONCAT(
        ELT(1 + MOD(n - 1, 10), '바람결', '오름달', '귤빛', '파도소리', '돌담길',
            '느린하루', '별이머문', '숨비', '푸른밤', '소소한제주'),
        ' 게스트하우스 ', LPAD(n, 2, '0')
    ),
    ELT(1 + MOD(n - 1, 6), '제주시', '서귀포시', '중문', '성산_구좌', '애월_협재', '우도_기타'),
    CONCAT(
        ELT(1 + MOD(n - 1, 10), '제주시 애월읍 애월리', '제주시 조천읍 함덕리',
            '서귀포시 성산읍 성산리', '서귀포시 안덕면 사계리', '제주시 한림읍 협재리',
            '서귀포시 남원읍 위미리', '제주시 구좌읍 세화리', '서귀포시 표선면 표선리',
            '제주시 우도면 연평리', '서귀포시 대정읍 하모리'),
        ' ', 100 + n
    ),
    CONCAT(
        ELT(1 + MOD(n - 1, 10), '애월해안로', '조함해안로', '성산중앙로', '사계남로',
            '한림로', '태위로', '해맞이해안로', '표선동서로', '우도해안길', '하모중앙로'),
        ' ', 10 + n
    ),
    ST_SRID(POINT(
        126.24 + MOD(n * 37, 70) / 100.0,
        33.22 + MOD(n * 29, 35) / 100.0
    ), 4326),
    CONCAT(
        ELT(1 + MOD(n - 1, 5),
            '제주의 아침과 바다를 천천히 즐길 수 있는 아늑한 숙소입니다. ',
            '돌담과 작은 정원이 어우러진 편안한 제주 스테이입니다. ',
            '혼자 와도 금세 편안해지는 밝고 따뜻한 분위기의 숙소입니다. ',
            '일상에서 잠시 벗어나 조용히 쉬어가기 좋은 공간입니다. ',
            '여행자들이 자연스럽게 이야기를 나눌 수 있는 다정한 숙소입니다. '
        ),
        ELT(1 + MOD(n - 1, 4),
            '도보권에 해변과 카페가 있어 뚜벅이 여행에도 좋습니다.',
            '공용 주방과 넉넉한 라운지를 자유롭게 이용할 수 있습니다.',
            '침구를 매일 관리하며 조용한 밤 시간을 지키고 있습니다.',
            '제주 로컬 여행 정보를 정성껏 안내해 드립니다.'
        )
    ),
    CONCAT('geha_demo_', LPAD(n, 3, '0')),
    CONCAT('010-', LPAD(2000 + n, 4, '0'), '-', LPAD(5000 + n, 4, '0')),
    CONCAT('https://example.com/stay/', n),
    CONCAT('https://example.com/reservations/', n),
    ELT(1 + MOD(n - 1, 4),
        '머무는 동안 제주다운 쉼을 느끼실 수 있도록 정성껏 준비하겠습니다.',
        '깨끗한 침구와 따뜻한 인사로 여행자를 맞이합니다.',
        '늦은 체크인은 미리 연락 주시면 친절히 안내해 드릴게요.',
        '서로를 배려하는 편안한 여행 문화를 함께 만들어 주세요.'
    ),
    IF(MOD(n, 17) = 0, 'INACTIVE', 'ACTIVE')
FROM seed_seq;

INSERT INTO guest_house_post_image (guest_house_post_id, image_url, image_index)
SELECT n,
    CONCAT('https://images.unsplash.com/photo-',
        ELT(1 + MOD(n - 1, 5),
            '1566073771259-6a8506099945',
            '1520250497591-112f2f40a3f4',
            '1445019980597-93fa8acb246c',
            '1564501049412-61c2a3083791',
            '1555854877-bab0e564b8d5'
        ),
        '?auto=format&fit=crop&w=1200&q=80'
    ), 0
FROM seed_seq;

INSERT INTO guest_house_post_image (guest_house_post_id, image_url, image_index)
SELECT n,
    CONCAT('https://images.unsplash.com/photo-',
        ELT(1 + MOD(n - 1, 5),
            '1582719478250-c89cae4dc85b',
            '1566665797739-1674de7a421a',
            '1611892440504-42a792e24d32',
            '1590490360182-c33d57733427',
            '1560185008-b033106af5c3'
        ),
        '?auto=format&fit=crop&w=1200&q=80'
    ), 1
FROM seed_seq;

INSERT INTO mood (guest_house_post_id, value)
SELECT n, ELT(1 + MOD(n - 1, 11), '바닷가', '동물', '자연_숲', '대규모파티', '소규모파티', '조용한', '활발한', '감성_느좋', '파티_X', '솔로', '한달살이')
FROM seed_seq;
INSERT INTO mood (guest_house_post_id, value)
SELECT n, ELT(1 + MOD(n + 4, 11), '바닷가', '동물', '자연_숲', '대규모파티', '소규모파티', '조용한', '활발한', '감성_느좋', '파티_X', '솔로', '한달살이')
FROM seed_seq;

INSERT INTO amenity (guest_house_post_id, value)
SELECT n, '무료 Wi-Fi' FROM seed_seq;
INSERT INTO amenity (guest_house_post_id, value)
SELECT n, ELT(1 + MOD(n - 1, 5), '조식 제공', '세탁기', '공용 주방', '무료 주차', '짐 보관')
FROM seed_seq;
INSERT INTO amenity (guest_house_post_id, value)
SELECT n, ELT(1 + MOD(n + 1, 5), '수건 제공', '헤어드라이어', '개인 사물함', '정수기', '야외 테라스')
FROM seed_seq;

INSERT INTO room (
    guest_house_post_id, name, type, head_count, check_in_time, check_out_time, price_per_night
)
SELECT n, '여성 도미토리', '여성전용', '_3인이상', '16:00:00', '11:00:00',
    26000 + MOD(n, 8) * 1000
FROM seed_seq;
INSERT INTO room (
    guest_house_post_id, name, type, head_count, check_in_time, check_out_time, price_per_night
)
SELECT n, '남성 도미토리', '남성전용', '_3인이상', '16:00:00', '11:00:00',
    25000 + MOD(n, 7) * 1000
FROM seed_seq;

INSERT INTO room_image (room_id, image_url, image_index)
SELECT id,
    CONCAT('https://images.unsplash.com/photo-',
        IF(MOD(id, 2) = 0, '1595526114035-0d45ed16cfbf', '1505693416388-ac5ce068fe85'),
        '?auto=format&fit=crop&w=1000&q=80'
    ), 0
FROM room;

INSERT INTO party (
    guest_house_post_id, party_type, start_time, end_time, place, moods,
    is_external_guest_allowed, guest_fee, external_guest_fee, information
)
SELECT n,
    ELT(1 + MOD(n - 1, 4), '포틀럭', '디너_파티', '술파티', '기타'),
    '19:30:00', '21:30:00', '1층 공용 라운지',
    '부담 없이 이야기 나누는 편안한 분위기',
    MOD(n, 3) <> 0, 10000 + MOD(n, 3) * 5000, 20000,
    '참여는 자유이며 당일 오후까지 호스트에게 알려주세요.'
FROM seed_seq
WHERE MOD(n, 4) = 0;

INSERT INTO weekly_day (party_id, value)
SELECT id, IF(MOD(id, 2) = 0, '토', '금') FROM party;

INSERT INTO party_image (party_id, image_url, image_index)
SELECT id,
    'https://images.unsplash.com/photo-1519671482749-fd09be7ccebf?auto=format&fit=crop&w=1000&q=80',
    0
FROM party;

INSERT INTO staff_recruitment (
    owner_id, title, guest_house_name, region, lot_number_address, road_name_address,
    coordinates, start_date, working_period, content, gender, advantages,
    employee_benefits, instagram_id, phone_number, web_site, reservation_url,
    owner_message, view_count, status, created_at, updated_at
)
SELECT
    5,
    CONCAT(
        ELT(1 + MOD(n - 1, 6), '함께 제주를 만들어갈 스텝을 찾아요', '아침을 여는 밝은 스텝 모집',
            '한 달 살며 함께 일할 스텝 구합니다', '여행과 일을 함께할 운영 스텝 모집',
            '제주 바다 가까운 게하 스텝 모집', '다정한 손님맞이 스텝을 기다립니다'),
        ' #', LPAD(n, 3, '0')
    ),
    CONCAT(
        ELT(1 + MOD(n - 1, 10), '바람결', '오름달', '귤빛', '파도소리', '돌담길',
            '느린하루', '별이머문', '숨비', '푸른밤', '소소한제주'),
        ' 게스트하우스 ', LPAD(n, 2, '0')
    ),
    ELT(1 + MOD(n - 1, 6), '제주시', '서귀포시', '중문', '성산_구좌', '애월_협재', '우도_기타'),
    CONCAT(
        ELT(1 + MOD(n - 1, 10), '제주시 애월읍 애월리', '제주시 조천읍 함덕리',
            '서귀포시 성산읍 성산리', '서귀포시 안덕면 사계리', '제주시 한림읍 협재리',
            '서귀포시 남원읍 위미리', '제주시 구좌읍 세화리', '서귀포시 표선면 표선리',
            '제주시 우도면 연평리', '서귀포시 대정읍 하모리'),
        ' ', 100 + n
    ),
    CONCAT(
        ELT(1 + MOD(n - 1, 10), '애월해안로', '조함해안로', '성산중앙로', '사계남로',
            '한림로', '태위로', '해맞이해안로', '표선동서로', '우도해안길', '하모중앙로'),
        ' ', 10 + n
    ),
    ST_SRID(POINT(
        126.24 + MOD(n * 37, 70) / 100.0,
        33.22 + MOD(n * 29, 35) / 100.0
    ), 4326),
    CURRENT_DATE + INTERVAL MOD(n, 45) DAY,
    ELT(1 + MOD(n - 1, 3), '단기', '중기', '장기'),
    CONCAT(
        '체크인 안내와 객실 정리, 공용 공간 관리를 함께 맡아주실 분을 찾습니다. ',
        '하루 평균 근무 시간은 4~5시간이며 업무가 끝난 뒤에는 자유롭게 제주를 즐길 수 있습니다. ',
        ELT(1 + MOD(n - 1, 4),
            '경력보다 책임감과 밝은 소통을 중요하게 생각합니다.',
            '게스트와 자연스럽게 대화하는 것을 좋아하는 분이면 좋습니다.',
            '청결을 꼼꼼히 챙기고 약속 시간을 잘 지키는 분을 기다립니다.',
            '제주 생활을 경험하며 새로운 사람을 만나고 싶은 분께 잘 맞습니다.'
        )
    ),
    ELT(1 + MOD(n - 1, 3), '무관', '여', '남'),
    ELT(1 + MOD(n - 1, 4), '초보 가능, 인수인계 제공', '운전 가능자 우대', '외국어 가능자 우대', '장기 근무자 우대'),
    ELT(1 + MOD(n - 1, 4), '스텝 전용 숙소와 조식 제공', '개인 침대, 세탁, 주방 이용 제공',
        '숙박 및 공용시설 무료 이용', '숙소 제공, 파티 참여 무료'),
    CONCAT('geha_jobs_', LPAD(n, 3, '0')),
    CONCAT('010-', LPAD(3000 + n, 4, '0'), '-', LPAD(6000 + n, 4, '0')),
    CONCAT('https://example.com/jobs/', n),
    CONCAT('https://example.com/apply/', n),
    '궁금한 점은 편하게 문의해 주세요. 서로 배려하며 즐겁게 지낼 분을 기다립니다.',
    MOD(n * 47, 1200),
    IF(MOD(n, 13) = 0, 'INACTIVE', 'ACTIVE'),
    NOW() - INTERVAL MOD(n * 3, 60) DAY,
    NOW() - INTERVAL MOD(n, 10) DAY
FROM seed_seq;

INSERT INTO staff_recruitment_image (
    staff_recruitment_id, type, image_url, image_index, created_at, updated_at
)
SELECT n, '대표이미지',
    CONCAT('https://images.unsplash.com/photo-',
        ELT(1 + MOD(n - 1, 5),
            '1566073771259-6a8506099945',
            '1520250497591-112f2f40a3f4',
            '1445019980597-93fa8acb246c',
            '1564501049412-61c2a3083791',
            '1555854877-bab0e564b8d5'
        ),
        '?auto=format&fit=crop&w=1200&q=80'
    ),
    0, NOW(), NOW()
FROM seed_seq;

INSERT INTO staff_recruitment_job (
    staff_recruitment_id, name, start_time, end_time, job, standard,
    work_days, rest_days, work_schedule_type, created_at, updated_at
)
SELECT n,
    ELT(1 + MOD(n - 1, 4), '오전 객실 관리', '게스트 응대 및 체크인', '공용 공간 관리', '운영 보조'),
    ELT(1 + MOD(n - 1, 3), '09:00:00', '10:00:00', '15:00:00'),
    ELT(1 + MOD(n - 1, 3), '13:00:00', '14:00:00', '19:00:00'),
    ELT(1 + MOD(n - 1, 4),
        '침구 교체, 객실과 욕실 정리', '체크인 안내와 게스트 문의 응대',
        '라운지 및 주방 정리, 소모품 점검', '예약 확인과 숙소 운영 전반 보조'),
    IF(MOD(n, 2) = 0, '로테이션', '_7일_기준'),
    5, 2, '주5일', NOW(), NOW()
FROM seed_seq;

INSERT INTO staff_recruitment_question (
    staff_recruitment_id, content, created_at, updated_at
)
SELECT n, '지원 가능한 시작일과 희망 근무 기간을 알려주세요.', NOW(), NOW()
FROM seed_seq;
INSERT INTO staff_recruitment_question (
    staff_recruitment_id, content, created_at, updated_at
)
SELECT n, '공동생활 또는 서비스 업무 경험이 있다면 간단히 소개해 주세요.', NOW(), NOW()
FROM seed_seq;

DROP TEMPORARY TABLE seed_seq;
COMMIT;
