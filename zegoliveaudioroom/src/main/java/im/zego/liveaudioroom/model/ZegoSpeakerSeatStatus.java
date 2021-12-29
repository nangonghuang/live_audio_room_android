package im.zego.liveaudioroom.model;

/**
 * Enumeration values of the speaker seat status.
 */
public enum ZegoSpeakerSeatStatus {
    /**
     * The speaker seat is untaken.
     */
    Untaken(0),

    /**
     * The speaker seat is occupied.
     */
    Occupied(1),

    /**
     * The speaker seat is closed.
     */
    Closed(2);

    private final int value;

    public int getValue() {
        return value;
    }

    ZegoSpeakerSeatStatus(int value) {
        this.value = value;
    }
}
