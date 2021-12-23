package im.zego.liveaudioroom.model;

/**
 * enum used to describe Speaker seat Status. [ZegoSpeakerSeatStatusUntaken] means available to use,
 * [ZegoSpeakerSeatStatusOccupied] means occupied by other user, [ZegoSpeakerSeatStatusClosed] means the seat is not
 * allowed to use
 */
public enum ZegoSpeakerSeatStatus {

    Untaken(0),

    Occupied(1),

    Closed(2);

    private final int value;

    public int getValue() {
        return value;
    }

    ZegoSpeakerSeatStatus(int value) {
        this.value = value;
    }
}
