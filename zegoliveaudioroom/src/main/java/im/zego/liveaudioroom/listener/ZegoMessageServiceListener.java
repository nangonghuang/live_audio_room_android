package im.zego.liveaudioroom.listener;

import im.zego.liveaudioroom.model.ZegoTextMessage;

public interface ZegoMessageServiceListener {

    void onReceiveTextMessage(ZegoTextMessage textMessage, String roomID);
}
