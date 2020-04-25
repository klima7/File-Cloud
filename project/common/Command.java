package project.common;

public enum Command {
    LOGIN(1),
    LOGOUT(2),
    LOGIN_SUCCESS(3),
    SEND_FILE(4),
    DELETE_FILE(5),
    CHECK_FILE(6),
    NEED_FILE(7),
    USER_ACTIVE(8),
    USER_INACTIVE(9),
    SEND_TO_USER(10),
    SERVER_DOWN(11);

    private int number;

    Command(int number) {
        this.number = number;
    }

    public int asInt() {
        return this.number;
    }
}
