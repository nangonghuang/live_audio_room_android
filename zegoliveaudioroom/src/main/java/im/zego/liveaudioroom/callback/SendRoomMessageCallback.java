package im.zego.liveaudioroom.callback;

import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomErrorCode;


public interface SendRoomMessageCallback {
    void onSendRoomMessage(ZegoLiveAudioRoomErrorCode error);
}