package im.zego.liveaudioroom.callback;

import im.zego.liveaudioroom.emus.ZIMChatRoomErrorCode;


public interface LeaveSeatCallback {
    void leaveSeat(ZIMChatRoomErrorCode error);
}