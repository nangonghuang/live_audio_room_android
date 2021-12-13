package im.zego.liveaudioroom.callback;

import im.zego.liveaudioroom.emus.ZIMChatRoomErrorCode;


public interface OnLoginCallback {
    void onConnectionState(ZIMChatRoomErrorCode error);
}