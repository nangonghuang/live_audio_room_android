package im.zego.liveaudioroom.callback;

import im.zego.liveaudioroom.emus.ZIMChatRoomErrorCode;


public interface MuteAllMessageCallback {
    void onMuteAllMessage(ZIMChatRoomErrorCode error);
}