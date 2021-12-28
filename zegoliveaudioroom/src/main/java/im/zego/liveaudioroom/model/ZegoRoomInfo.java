package im.zego.liveaudioroom.model;

import com.google.gson.annotations.SerializedName;

/**
 * Class room information.
 * <p>Description: This class contain the room status related information.</>
 */
public class ZegoRoomInfo {

    /**
     * Room ID, refers to the the unique identifier of the room, can be used when joining the room.
     */
    @SerializedName("id")
    private String roomID;

    /**
     * Room name, refers to the room title, can be used for display.
     */
    @SerializedName("name")
    private String roomName;

    /**
     * Host ID, refers to the ID of the room creator.
     */
    @SerializedName("hostID")
    private String hostID;

    /**
     * The number of speaker seats.
     */
    @SerializedName("num")
    private int seatNum;

    /**
     * Whether the text chat is disabled in the room.
     */
    @SerializedName("disable")
    private boolean isTextMessageDisabled;

    /**
     * whether the speaker seat is closed.
     */
    @SerializedName("close")
    private boolean isClosed;

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getHostID() {
        return hostID;
    }

    public void setHostID(String hostID) {
        this.hostID = hostID;
    }

    public int getSeatNum() {
        return seatNum;
    }

    public void setSeatNum(int seatNum) {
        this.seatNum = seatNum;
    }

    public boolean isTextMessageDisabled() {
        return isTextMessageDisabled;
    }

    public void setTextMessageDisabled(boolean textMessageDisabled) {
        isTextMessageDisabled = textMessageDisabled;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public void setClosed(boolean closed) {
        isClosed = closed;
    }
}