package im.zego.liveaudioroom.callback;

import im.zego.liveaudioroom.emus.ZIMChatRoomErrorCode;

import java.util.List;


public interface SendGiftMessageCallback {
    void onSendGiftMessage(ZIMChatRoomErrorCode error, List<String> sendFailToUsers);
}