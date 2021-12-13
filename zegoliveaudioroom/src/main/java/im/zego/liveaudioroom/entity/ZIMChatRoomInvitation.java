package im.zego.liveaudioroom.entity;

import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomInvitationStatus;

import im.zego.zim.entity.ZIMUserInfo;

public class ZegoLiveAudioRoomInvitation {
    String fromUserID;

    public String getFromUserID() {
        return fromUserID;
    }

    public void setFromUserID(String fromUserID) {
        this.fromUserID = fromUserID;
    }
}
