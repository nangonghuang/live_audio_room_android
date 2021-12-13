package im.zego.liveaudioroom.callback;

import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomErrorCode;


public interface JoinRoomCallback {
    void onConnectionState(ZegoLiveAudioRoomErrorCode error);
}