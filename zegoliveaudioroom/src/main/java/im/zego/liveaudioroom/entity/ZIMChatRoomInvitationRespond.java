package im.zego.liveaudioroom.entity;

import im.zego.liveaudioroom.emus.ZIMChatRoomInvitationStatus;

public class ZIMChatRoomInvitationRespond {
    String fromUserID;
    ZIMChatRoomInvitationStatus status;

    public String getFromUserID() {
        return fromUserID;
    }

    public void setFromUserID(String fromUserID) {
        this.fromUserID = fromUserID;
    }

    public ZIMChatRoomInvitationStatus getStatus() {
        return status;
    }

    public void setStatus(ZIMChatRoomInvitationStatus status) {
        this.status = status;
    }
}
