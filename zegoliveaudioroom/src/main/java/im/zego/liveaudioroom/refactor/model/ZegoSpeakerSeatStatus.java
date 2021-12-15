package im.zego.liveaudioroom.refactor.model;

/**
 * enum used to describe Speaker seat Status. [ZegoSpeakerSeatStatusUntaken] means available to use,
 * [ZegoSpeakerSeatStatusOccupied] means occupied by other user, [ZegoSpeakerSeatStatusClosed] means
 * the seat is not allowed to use
 */
public enum ZegoSpeakerSeatStatus {
    Untaken,
    Occupied,
    Closed
}
