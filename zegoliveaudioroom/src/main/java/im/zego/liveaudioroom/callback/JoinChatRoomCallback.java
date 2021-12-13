package im.zego.liveaudioroom.callback;

import im.zego.liveaudioroom.emus.ZIMChatRoomErrorCode;


public interface JoinChatRoomCallback {
    void onConnectionState(ZIMChatRoomErrorCode error);
}