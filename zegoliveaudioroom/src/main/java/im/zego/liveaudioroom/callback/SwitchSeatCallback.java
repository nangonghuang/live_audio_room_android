package im.zego.liveaudioroom.callback;

import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomErrorCode;


public interface SwitchSeatCallback {
    void switchSeat(ZegoLiveAudioRoomErrorCode error);
}