package im.zego.liveaudioroom.emus;

public enum ZIMChatRoomInvitationStatus {
    ACCEPT(0),
    REJECT(1);

    private int value;

    ZIMChatRoomInvitationStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
