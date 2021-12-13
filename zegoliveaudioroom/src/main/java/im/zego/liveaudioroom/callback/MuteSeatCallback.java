package im.zego.liveaudioroom.callback;

import im.zego.liveaudioroom.emus.ZIMChatRoomErrorCode;


public interface MuteSeatCallback {
    void muteSeat(ZIMChatRoomErrorCode error);
}