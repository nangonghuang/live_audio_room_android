package im.zego.liveaudioroom.emus;

public enum ZIMChatRoomMessageAction {
    TEXT(0),
    GIFT(1),
    INVITATION(2),
    INVITATION_RESPOND(3),
    GIFT_BROADCAST(4);
    private int value;

    ZIMChatRoomMessageAction(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
