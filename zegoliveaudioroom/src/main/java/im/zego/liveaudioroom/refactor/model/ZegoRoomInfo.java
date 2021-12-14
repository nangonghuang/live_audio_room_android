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
    @SerializedName("num")
    private int seatNum;
    // whether to mute message
    @SerializedName("disable")
    private boolean isTextMessageDisabled;
    // whether to lock seat
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

    @Override
    public String toString() {
        return "ZegoRoomInfo{" +
                "roomID='" + roomID + '\'' +
                ", roomName='" + roomName + '\'' +
                ", hostID='" + hostID + '\'' +
                ", seatCount=" + seatNum +
                ", isMuteMessage=" + isTextMessageDisabled +
                ", isLock=" + isClosed +
                '}';
    }
}