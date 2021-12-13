package im.zego.liveaudioroom.entity;

import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomInvitationStatus;

public class ZegoLiveAudioRoomInvitationRespond {
    String fromUserID;
    ZegoLiveAudioRoomInvitationStatus status;

    public String getFromUserID() {
        return fromUserID;
    }

    public void setFromUserID(String fromUserID) {
        this.fromUserID = fromUserID;
    }

    public ZegoLiveAudioRoomInvitationStatus getStatus() {
        return status;
    }

    public void setStatus(ZegoLiveAudioRoomInvitationStatus status) {
        this.status = status;
    }
}
