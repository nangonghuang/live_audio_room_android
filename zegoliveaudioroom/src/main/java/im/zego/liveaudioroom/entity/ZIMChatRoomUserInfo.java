package im.zego.liveaudioroom.entity;

import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomUserRole;

import java.util.Objects;

public class ZegoLiveAudioRoomUserInfo {
    private String userID;
    private String userName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ZegoLiveAudioRoomUserInfo user = (ZegoLiveAudioRoomUserInfo) o;
        return Objects.equals(userID, user.userID) && Objects.equals(userName, user.userName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userID, userName);
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
