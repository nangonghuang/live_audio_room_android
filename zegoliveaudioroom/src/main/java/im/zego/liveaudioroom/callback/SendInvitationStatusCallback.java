package im.zego.liveaudioroom.callback;

import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomErrorCode;
import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomInvitationStatus;
import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomVoiceStatus;


public interface SendInvitationStatusCallback {
    void sendInvitationStatus(ZegoLiveAudioRoomErrorCode errorCode);
}