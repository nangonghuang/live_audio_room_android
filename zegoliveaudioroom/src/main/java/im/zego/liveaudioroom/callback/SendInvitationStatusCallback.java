package im.zego.liveaudioroom.callback;

import im.zego.liveaudioroom.emus.ZIMChatRoomErrorCode;
import im.zego.liveaudioroom.emus.ZIMChatRoomInvitationStatus;
import im.zego.liveaudioroom.emus.ZIMChatRoomVoiceStatus;


public interface SendInvitationStatusCallback {
    void sendInvitationStatus(ZIMChatRoomErrorCode errorCode);
}