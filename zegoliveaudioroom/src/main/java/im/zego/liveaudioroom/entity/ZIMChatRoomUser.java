package im.zego.liveaudioroom.entity;

import java.util.Objects;

import im.zego.liveaudioroom.emus.ZIMChatRoomUserRole;


public class ZIMChatRoomUser {
    private ZIMChatRoomUserRole userRole;
    private String userID;
    private String userName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ZIMChatRoomUser user = (ZIMChatRoomUser) o;
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

    public ZIMChatRoomUserRole getUserRole() {
        return userRole;
    }

    public void setUserRole(ZIMChatRoomUserRole userRole) {
        this.userRole = userRole;
    }

    @Override
    public String toString() {
        return "ZIMChatRoomUser{" +
                "userRole=" + userRole +
                ", userID='" + userID + '\'' +
                ", userName='" + userName + '\'' +
                '}';
    }
}
