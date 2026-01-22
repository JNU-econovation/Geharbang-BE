package guesthouse.guestHousePost.domain.vo;

public enum Status {
    ACTIVE,
    INACTIVE
    ;

    public boolean isClosed(){
        return this == INACTIVE;
    }
}

