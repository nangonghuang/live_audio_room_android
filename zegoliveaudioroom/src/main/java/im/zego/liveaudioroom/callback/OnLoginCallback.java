package im.zego.liveaudioroom.callback;

import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomErrorCode;


public interface OnLoginCallback {
    void onConnectionState(ZegoLiveAudioRoomErrorCode error);
}