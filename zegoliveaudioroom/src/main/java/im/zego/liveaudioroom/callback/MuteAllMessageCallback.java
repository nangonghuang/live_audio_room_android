package im.zego.liveaudioroom.callback;

import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomErrorCode;


public interface MuteAllMessageCallback {
    void onMuteAllMessage(ZegoLiveAudioRoomErrorCode error);
}