package im.zego.liveaudioroom.emus;

public enum ZegoLiveAudioRoomEvent {
    SUCCESS(0),
    NETWORK_INTERRUPTED(1),
    NETWORK_DISCONNECTED(2),
    ROOM_NOT_EXIST(3),
    ACTIVE_CREATE(4),
    CREATE_FAILED(5),
    ACTIVE_ENTER(6),
    ENTER_FAILED(7),
    KICKED_OUT(8);
    private int value;

    ZegoLiveAudioRoomEvent(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public static ZegoLiveAudioRoomEvent getZegoLiveAudioRoomEvent(int value) {
        try {
            if (SUCCESS.value == value) {
                return SUCCESS;
            } else if (NETWORK_INTERRUPTED.value == value) {
                return NETWORK_INTERRUPTED;
            } else if (NETWORK_DISCONNECTED.value == value) {
                return NETWORK_DISCONNECTED;
            } else if (ROOM_NOT_EXIST.value == value) {
                return ROOM_NOT_EXIST;
            } else if (ACTIVE_CREATE.value == value) {
                return ACTIVE_CREATE;
            } else if (CREATE_FAILED.value == value) {
                return CREATE_FAILED;
            } else if (ACTIVE_ENTER.value == value) {
                return ACTIVE_ENTER;
            } else if (ENTER_FAILED.value == value) {
                return ENTER_FAILED;
            } else {
                return KICKED_OUT.value == value ? KICKED_OUT : null;
            }
        } catch (Exception var2) {
            throw new RuntimeException("The enumeration cannot be found");
        }
    }
}
