package im.zego.liveaudioroom.service;

import java.util.ArrayList;
import java.util.List;

import im.zego.liveaudioroom.ZegoRoomManager;
import im.zego.liveaudioroom.ZegoZIMManager;
import im.zego.liveaudioroom.callback.ZegoRoomCallback;
import im.zego.liveaudioroom.listener.ZegoMessageServiceListener;
import im.zego.liveaudioroom.model.ZegoTextMessage;
import im.zego.liveaudioroom.model.ZegoUserInfo;
import im.zego.zim.ZIM;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.enums.ZIMErrorCode;
import im.zego.zim.enums.ZIMMessageType;

/**
 * manage room text message.
 */
public class ZegoMessageService {

    private ZegoMessageServiceListener messageServiceListener;
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
                if (messageServiceListener != null) {
                    messageServiceListener.onReceiveTextMessage(textMessage, fromRoomID);
                }
            }
        }
    }

    void reset() {
        messageList.clear();
        messageServiceListener = null;
    }

    public void setListener(ZegoMessageServiceListener listener) {
        this.messageServiceListener = listener;
    }

    public List<ZegoTextMessage> getMessageList() {
        return messageList;
    }
}
