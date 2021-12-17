package im.zego.liveaudioroom.refactor.callback;

import im.zego.liveaudioroom.refactor.model.ZegoCustomCommand;
import im.zego.liveaudioroom.refactor.model.ZegoTextMessage;

public interface ZegoMessageServiceCallback {

    void onReceiveTextMessage(ZegoTextMessage textMessage, String roomID);
}
