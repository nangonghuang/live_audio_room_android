package im.zego.liveaudioroom.emus;

public enum ZegoLiveAudioRoomUserRole {
    OWNER(0),
    VISITOR(1),
    ;

    private int value;

    ZegoLiveAudioRoomUserRole(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
