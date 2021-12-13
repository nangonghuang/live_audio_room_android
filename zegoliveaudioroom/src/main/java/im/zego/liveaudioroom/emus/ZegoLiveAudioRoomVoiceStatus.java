package im.zego.liveaudioroom.emus;

public enum ZegoLiveAudioRoomVoiceStatus {
    UNUSED(0),
    USED(1),
    LOCKED(2);

    private int value;

    ZegoLiveAudioRoomVoiceStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
