package im.zego.liveaudioroom.callback;

import im.zego.liveaudioroom.emus.ZIMChatRoomErrorCode;


public interface SwitchSeatCallback {
    void switchSeat(ZIMChatRoomErrorCode error);
}