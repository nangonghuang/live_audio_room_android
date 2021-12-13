package im.zego.liveaudioroom.callback;

import im.zego.liveaudioroom.emus.ZIMChatRoomErrorCode;


public interface SendRoomMessageCallback {
    void onSendRoomMessage(ZIMChatRoomErrorCode error);
}