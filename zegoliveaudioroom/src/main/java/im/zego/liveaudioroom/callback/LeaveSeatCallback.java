package im.zego.liveaudioroom.callback;

import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomErrorCode;


public interface LeaveSeatCallback {
    void leaveSeat(ZegoLiveAudioRoomErrorCode error);
}