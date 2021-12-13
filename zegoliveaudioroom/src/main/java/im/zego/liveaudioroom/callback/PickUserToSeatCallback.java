package im.zego.liveaudioroom.callback;

import im.zego.liveaudioroom.emus.ZIMChatRoomErrorCode;


public interface PickUserToSeatCallback {
    void pickUserToSeat(ZIMChatRoomErrorCode error);
}