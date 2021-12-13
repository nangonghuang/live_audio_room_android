package im.zego.liveaudioroom.callback;

import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomErrorCode;


public interface PickUserToSeatCallback {
    void pickUserToSeat(ZegoLiveAudioRoomErrorCode error);
}