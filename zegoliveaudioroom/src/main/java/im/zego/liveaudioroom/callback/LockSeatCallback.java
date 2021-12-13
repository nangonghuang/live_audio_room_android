package im.zego.liveaudioroom.callback;

import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomErrorCode;


public interface LockSeatCallback {
    void lockSeat(ZegoLiveAudioRoomErrorCode error);
}