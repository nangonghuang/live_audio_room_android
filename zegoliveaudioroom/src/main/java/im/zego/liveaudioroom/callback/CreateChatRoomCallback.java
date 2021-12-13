package im.zego.liveaudioroom.callback;

import im.zego.liveaudioroom.emus.ZIMChatRoomErrorCode;


public interface CreateChatRoomCallback {
    void onCreateRoomState(ZIMChatRoomErrorCode error);
}