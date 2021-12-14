package im.zego.liveaudioroom.refactor.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by rocket_wang on 2021/12/14.
 */
public class ZegoRoomInfo {
    // room ID
    @SerializedName("id")
    private String roomID;
    // room name
    @SerializedName("name")
    private String roomName;
    // host user ID
    @SerializedName("hostID")
    private String hostID;
    // speaker seat count
    @SerializedName("count")
    private int seatCount;
    // whether to mute message
    @SerializedName("mute")
    private boolean isMuteMessage;
    // whether to lock seat
    @SerializedName("lock")
    private boolean isLock;

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

    public int getSeatCount() {
        return seatCount;
    }

    public void setSeatCount(int seatCount) {
        this.seatCount = seatCount;
    }

    public boolean isMuteMessage() {
        return isMuteMessage;
    }

    public void setMuteMessage(boolean muteMessage) {
        isMuteMessage = muteMessage;
    }

    public boolean isLock() {
        return isLock;
    }

    public void setLock(boolean lock) {
        isLock = lock;
    }

    @Override
    public String toString() {
        return "ZegoRoomInfo{" +
                "roomID='" + roomID + '\'' +
                ", roomName='" + roomName + '\'' +
                ", hostID='" + hostID + '\'' +
                ", seatCount=" + seatCount +
                ", isMuteMessage=" + isMuteMessage +
                ", isLock=" + isLock +
                '}';
    }
}