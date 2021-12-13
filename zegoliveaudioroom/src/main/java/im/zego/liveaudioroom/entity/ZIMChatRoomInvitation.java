package im.zego.liveaudioroom.entity;

import im.zego.liveaudioroom.emus.ZIMChatRoomInvitationStatus;

import im.zego.zim.entity.ZIMUserInfo;

public class ZIMChatRoomInvitation {
    String fromUserID;

    public String getFromUserID() {
        return fromUserID;
    }

    public void setFromUserID(String fromUserID) {
        this.fromUserID = fromUserID;
    }
}
