ALTER TABLE review
    ADD COLUMN target_type varchar(50) NOT NULL DEFAULT 'GUEST_HOUSE_POST',
    ADD COLUMN staff_recruitment_id bigint NULL,
    MODIFY guest_house_post_id bigint NULL;

CREATE INDEX idx_review_guest_house_status_user
    ON review (guest_house_post_id, status, user_id);

CREATE INDEX idx_review_staff_recruitment_status_user
    ON review (staff_recruitment_id, status, user_id);
