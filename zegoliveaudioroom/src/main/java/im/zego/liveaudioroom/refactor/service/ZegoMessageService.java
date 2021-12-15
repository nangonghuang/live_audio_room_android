package im.zego.liveaudioroom.refactor.service;

import im.zego.liveaudioroom.refactor.callback.ZegoMessageServiceCallback;
import im.zego.liveaudioroom.refactor.callback.ZegoRoomCallback;
import im.zego.liveaudioroom.refactor.model.ZegoTextMessage;
import java.util.List;

/**
 * manage room text message.
 */
public class ZegoMessageService {

    private ZegoMessageServiceCallback messageServiceCallback;
    private List<ZegoTextMessage> messageList;

    public void sendTextMessage(String message, ZegoRoomCallback callback) {

    }

    public void sendInvitation(String userID, ZegoRoomCallback callback) {

    }
}
