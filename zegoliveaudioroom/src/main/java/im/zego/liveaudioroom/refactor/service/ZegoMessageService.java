package im.zego.liveaudioroom.refactor.service;

import im.zego.liveaudioroom.refactor.ZegoRoomManager;
import im.zego.liveaudioroom.refactor.ZegoZIMManager;
import im.zego.liveaudioroom.refactor.callback.ZegoMessageServiceCallback;
import im.zego.liveaudioroom.refactor.callback.ZegoRoomCallback;
import im.zego.liveaudioroom.refactor.model.ZegoCustomCommand;
import im.zego.liveaudioroom.refactor.model.ZegoTextMessage;
import im.zego.liveaudioroom.refactor.model.ZegoUserInfo;
import im.zego.zim.ZIM;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.enums.ZIMErrorCode;
import im.zego.zim.enums.ZIMMessageType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * manage room text message.
 */
public class ZegoMessageService {

    private ZegoMessageServiceCallback messageServiceCallback;
    private List<ZegoTextMessage> messageList;

    public ZegoMessageService() {
        messageList = new ArrayList<>();
    }

    /**
     * send text message to room.
     *
     * @param text     message text
     * @param callback operation result callback
     */
    public void sendTextMessage(String text, ZegoRoomCallback callback) {
        ZegoUserInfo localUserInfo = ZegoRoomManager
            .getInstance().userService.localUserInfo;
        ZegoTextMessage textMessage = new ZegoTextMessage();
        textMessage.message = text;
        textMessage.userID = localUserInfo.getUserID();
        String roomID = ZegoRoomManager.getInstance().roomService.roomInfo.getRoomID();
        ZegoZIMManager.getInstance().zim.sendRoomMessage(textMessage, roomID, (message, errorInfo) -> {
            if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                messageList.add(textMessage);
            }
            if (callback != null) {
                callback.roomCallback(errorInfo.code.value());
            }
        });
    }

    public void onReceiveRoomMessage(ZIM zim, ArrayList<ZIMMessage> messageList, String fromRoomID) {
        for (ZIMMessage zimMessage : messageList) {
            if (zimMessage.type == ZIMMessageType.TEXT) {
                ZegoTextMessage textMessage = (ZegoTextMessage) zimMessage;
                messageList.add(textMessage);
                if (messageServiceCallback != null) {
                    messageServiceCallback.onReceiveTextMessage(textMessage, fromRoomID);
                }
            }
        }
    }

    void reset() {
        messageList.clear();
        messageServiceCallback = null;
    }

    public void setMessageServiceCallback(ZegoMessageServiceCallback callback) {
        this.messageServiceCallback = callback;
    }

    public List<ZegoTextMessage> getMessageList() {
        return messageList;
    }
}
