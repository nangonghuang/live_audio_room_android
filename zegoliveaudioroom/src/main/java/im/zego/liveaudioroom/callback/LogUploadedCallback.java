package im.zego.liveaudioroom.callback;

import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomErrorCode;

public interface LogUploadedCallback {
    void onLogUploaded(ZegoLiveAudioRoomErrorCode errorCode);
}