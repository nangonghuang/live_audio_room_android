package im.zego.liveaudioroom.refactor.callback;

import im.zego.liveaudioroom.refactor.model.ZegoCoustomCommand;
import im.zego.liveaudioroom.refactor.model.ZegoTextMessage;

public interface ZegoMessageServiceCallback {

    void onReceiveTextMessage(ZegoTextMessage textMessage, String roomID);

    void onReceiveCustomCommand(ZegoCoustomCommand coustomCommand, String roomID);
}
