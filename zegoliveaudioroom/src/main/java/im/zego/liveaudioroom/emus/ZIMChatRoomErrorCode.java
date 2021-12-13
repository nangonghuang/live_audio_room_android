package im.zego.liveaudioroom.emus;

public enum ZIMChatRoomErrorCode {
    SUCCESS(0),
    ERROR(1),
    ROOM_EXISTED(1001),
    ROOM_NOT_FOUND(1002),
    SEAT_EXISTED(2001),
    SET_SEAT_INFO_FAILED(2002),
    ALREADY_IN_SEAT(2003),
    NO_PERMISSION(2004),
    NOT_IN_SEAT(2005);
    private int value;

    ZIMChatRoomErrorCode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
