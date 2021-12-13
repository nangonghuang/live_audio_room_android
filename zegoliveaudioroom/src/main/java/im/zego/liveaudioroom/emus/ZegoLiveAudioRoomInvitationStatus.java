package im.zego.liveaudioroom.emus;

public enum ZegoLiveAudioRoomInvitationStatus {
    ACCEPT(0),
    REJECT(1);

    private int value;

    ZegoLiveAudioRoomInvitationStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
