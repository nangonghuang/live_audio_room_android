package im.zego.liveaudioroom.callback;

import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomErrorCode;


public interface SetupRTCModuleCallback {
    void onConnectionState(ZegoLiveAudioRoomErrorCode error);
}