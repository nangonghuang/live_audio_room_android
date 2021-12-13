package im.zego.liveaudioroom.callback;

import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomErrorCode;

import java.util.List;


public interface SendGiftMessageCallback {
    void onSendGiftMessage(ZegoLiveAudioRoomErrorCode error, List<String> sendFailToUsers);
}