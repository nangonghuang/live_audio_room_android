package im.zego.liveaudioroom.emus;

public enum ZIMChatRoomSeatEvent {
    ENTERED(0),
    LEFT(1),
    LOCKED(2),
    UNLOCKED(3),
    MUTED(4),
    UNMUTED(5);
    private int value;

    ZIMChatRoomSeatEvent(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
