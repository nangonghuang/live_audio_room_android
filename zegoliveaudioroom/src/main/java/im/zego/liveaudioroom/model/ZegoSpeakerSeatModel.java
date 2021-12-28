package im.zego.liveaudioroom.model;

import com.google.gson.annotations.SerializedName;

/**
 * Class speaker seat status information.
 * <p>Description: This class contains the speaker seat status information.</>
 */
public class ZegoSpeakerSeatModel {

    /**
     * User ID, null indicates the current speaker seat is available/untaken.
     */
    @SerializedName("id")
    public String userID;

    /**
     * The seat index.
     */
    @SerializedName("index")
    public int seatIndex;

    /**
     * The speaker seat mic status.
     */
    @SerializedName("mic")
    public boolean mic;

    /**
     * The speaker seat status, it is untaken by default.
     */
    public ZegoSpeakerSeatStatus status;

    /**
     * Volume value, a local record attribute, used for displaying the sound level.
     */
    public transient float soundLevel;

    /**
     * status, a local record attributes. It is calculated based on stream quality, can be used for displaying the network status.
     */
    public transient ZegoNetWorkQuality network = ZegoNetWorkQuality.Good;

    @Override
    public String toString() {
        return "ZegoSpeakerSeatModel{" +
            "userID='" + userID + '\'' +
            ", seatIndex=" + seatIndex +
            ", mic=" + mic +
            ", status=" + status +
            '}';
    }
}
