package im.zego.liveaudioroom.model;

import com.google.gson.annotations.SerializedName;

/**
 * data structure used to describe a speaker seat status.
 */
public class ZegoSpeakerSeatModel {

    @SerializedName("id")
    public String userID;

    @SerializedName("index")
    public int seatIndex;

    @SerializedName("mic")
    public boolean mic;

    public ZegoSpeakerSeatStatus status;

    public transient float soundLevel;
    public transient ZegoNetWorkQuality network = ZegoNetWorkQuality.Good;

}
