package im.zego.liveaudioroom.callback;

import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomErrorCode;


public interface KickUserToSeatCallback {
    void kickUserToSeat(ZegoLiveAudioRoomErrorCode error);
}