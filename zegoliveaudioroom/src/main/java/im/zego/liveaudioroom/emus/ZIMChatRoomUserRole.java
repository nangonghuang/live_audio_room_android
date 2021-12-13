package im.zego.liveaudioroom.emus;

public enum ZIMChatRoomUserRole {
    OWNER(0),
    VISITOR(1),
    ;

    private int value;

    ZIMChatRoomUserRole(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
