package im.zego.liveaudioroom.refactor.model;

import com.google.gson.annotations.SerializedName;

/**
 * enum used to describe Speaker seat Status. [ZegoSpeakerSeatStatusUntaken] means available to use,
 * [ZegoSpeakerSeatStatusOccupied] means occupied by other user, [ZegoSpeakerSeatStatusClosed] means
 * the seat is not allowed to use
 */
public enum ZegoSpeakerSeatStatus {

    @SerializedName("0")
    Untaken(0),

    @SerializedName("1")
    Occupied(1),

    @SerializedName("2")
    Closed(2);

    private final int value;

    public int getValue() {
        return value;
    }

    ZegoSpeakerSeatStatus(int value) {
        this.value = value;
    }
}
