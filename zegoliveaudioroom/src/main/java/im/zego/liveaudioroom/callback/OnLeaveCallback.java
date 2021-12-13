package im.zego.liveaudioroom.callback;

import im.zego.liveaudioroom.emus.ZIMChatRoomErrorCode;


public interface OnLeaveCallback {
    void onConnectionState(ZIMChatRoomErrorCode error);
}