package im.zego.liveaudioroom.emus;

public enum ZegoLiveAudioRoomState {
    DISCONNECTED(0),
    CONNECTING(1),
    CONNECTED(2),
    RECONNECTING(3);
    private int value;

    ZegoLiveAudioRoomState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public static ZegoLiveAudioRoomState getZegoLiveAudioRoomState(int value) {
        try {
            if (DISCONNECTED.value == value) {
                return DISCONNECTED;
            } else if (CONNECTING.value == value) {
                return CONNECTING;
            } else {
                return CONNECTED.value == value ? CONNECTED : null;
            }
        } catch (Exception var2) {
            throw new RuntimeException("The enumeration cannot be found");
        }
    }
}
