package im.zego.liveaudioroom.entity;

import java.util.Objects;

import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomUserRole;


public class ZegoLiveAudioRoomUser {
    private ZegoLiveAudioRoomUserRole userRole;
    private String userID;
    private String userName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ZegoLiveAudioRoomUser user = (ZegoLiveAudioRoomUser) o;
        return userRole == user.userRole && Objects.equals(userID, user.userID) && Objects.equals(userName, user.userName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userRole, userID, userName);
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

    public ZegoLiveAudioRoomUserRole getUserRole() {
        return userRole;
    }

    public void setUserRole(ZegoLiveAudioRoomUserRole userRole) {
        this.userRole = userRole;
    }

    @Override
    public String toString() {
        return "ZegoLiveAudioRoomUser{" +
                "userRole=" + userRole +
                ", userID='" + userID + '\'' +
                ", userName='" + userName + '\'' +
                '}';
    }
}
