package guesthouse.ai;

public enum AiIndexDomain {
    GUESTHOUSE("guesthouses"),
    STAFF_RECRUITMENT("staff-recruitments");

    private final String path;

    AiIndexDomain(String path) {
        this.path = path;
    }

    public String path() {
        return path;
    }
}
