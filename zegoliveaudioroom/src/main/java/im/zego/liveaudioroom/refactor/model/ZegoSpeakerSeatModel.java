package im.zego.liveaudioroom.refactor.model;

/**
 * data structure used to describe a speaker seat status.
 */
public class ZegoSpeakerSeatModel {
    public String userID;
    public int seatIndex;
    public boolean isMicMuted;
    public ZegoSpeakerSeatStatus status;
    public float soundLevel;
    public float network;
}
