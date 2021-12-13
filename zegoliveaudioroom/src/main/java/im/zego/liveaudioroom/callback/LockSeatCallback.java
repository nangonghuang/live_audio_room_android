package im.zego.liveaudioroom.callback;

import im.zego.liveaudioroom.emus.ZIMChatRoomErrorCode;


public interface LockSeatCallback {
    void lockSeat(ZIMChatRoomErrorCode error);
}