package im.zego.liveaudioroom.callback;

import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomErrorCode;


public interface CreateRoomCallback {
    void onCreateRoomState(ZegoLiveAudioRoomErrorCode error);
}