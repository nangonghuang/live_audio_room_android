package im.zego.liveaudioroom.callback;

import im.zego.liveaudioroom.emus.ZIMChatRoomErrorCode;

public interface EnterSeatCallback {
    void enterSeat(ZIMChatRoomErrorCode error);
}