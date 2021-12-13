package im.zego.liveaudioroom.callback;

import im.zego.liveaudioroom.emus.ZIMChatRoomErrorCode;


public interface KickUserToSeatCallback {
    void kickUserToSeat(ZIMChatRoomErrorCode error);
}