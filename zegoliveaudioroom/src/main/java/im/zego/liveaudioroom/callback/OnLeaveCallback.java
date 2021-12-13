package im.zego.liveaudioroom.callback;

import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomErrorCode;


public interface OnLeaveCallback {
    void onConnectionState(ZegoLiveAudioRoomErrorCode error);
}