package guesthouse.certificate.domain.vo;

public enum Status {
    검토_대기,
    승인_완료,
    거부됨
    ;

    public Boolean isInReview() {
        return this == 검토_대기;
    }
}
