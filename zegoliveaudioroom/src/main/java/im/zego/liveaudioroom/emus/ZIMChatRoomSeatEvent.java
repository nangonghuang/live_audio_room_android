package im.zego.liveaudioroom.emus;

public enum ZegoLiveAudioRoomSeatEvent {
    ENTERED(0),
    LEFT(1),
    LOCKED(2),
    UNLOCKED(3),
    MUTED(4),
    UNMUTED(5);
    private int value;

    ZegoLiveAudioRoomSeatEvent(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
