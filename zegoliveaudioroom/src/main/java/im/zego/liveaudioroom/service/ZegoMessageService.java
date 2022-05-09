package im.zego.liveaudioroom.service;

import android.util.Log;
import im.zego.liveaudioroom.ZegoRoomManager;
import im.zego.liveaudioroom.ZegoZIMManager;
import im.zego.liveaudioroom.callback.ZegoRoomCallback;
import im.zego.liveaudioroom.listener.ZegoMessageServiceListener;
import im.zego.liveaudioroom.model.ZegoTextMessage;
import im.zego.liveaudioroom.model.ZegoUserInfo;
import im.zego.zim.ZIM;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.entity.ZIMMessageSendConfig;
import im.zego.zim.entity.ZIMTextMessage;
import im.zego.zim.enums.ZIMErrorCode;
import im.zego.zim.enums.ZIMMessageType;
import java.util.ArrayList;
import java.util.List;

/**
 * Class IM message management.
 * <p>Description: This class contains the logics of the IM messages management, such as send or receive messages.</>
 */
public class ZegoMessageService {

    /**
     * The listener related to message updates.
     */
    private ZegoMessageServiceListener messageServiceListener;
    /**
     * The message list.
     */
    private List<ZegoTextMessage> messageList;

    public ZegoMessageService() {
        messageList = new ArrayList<>();
    }

    /**
     * Send IM text message.
     * <p>Description: This method can be used to send IM text message, and all users in the room will receive the
     * message notification.</>
     * <p>Call this method at:  After joining the room</>
     *
     * @param text     refers to the text message content, which is limited to 1kb.
     * @param callback refers to the callback for send text messages.
     */
    public void sendTextMessage(String text, ZegoRoomCallback callback) {
        ZegoUserInfo localUserInfo = ZegoRoomManager
            .getInstance().userService.localUserInfo;
        ZegoTextMessage textMessage = new ZegoTextMessage();
        textMessage.message = text;
        String roomID = ZegoRoomManager.getInstance().roomService.roomInfo.getRoomID();
        ZIMMessageSendConfig config = new ZIMMessageSendConfig();
        ZegoZIMManager.getInstance().zim.sendRoomMessage(textMessage, roomID,config,(message, errorInfo) -> {
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
            if (zimMessage.getType() == ZIMMessageType.TEXT) {
                ZIMTextMessage zimTextMessage = (ZIMTextMessage) zimMessage;
                ZegoTextMessage textMessage = new ZegoTextMessage();
                textMessage.message = zimTextMessage.message;
                textMessage.fromUserID = zimMessage.getSenderUserID();
                Log.d("TAG", "onReceiveRoomMessage() called with: zim = [" + zim + "], messageList = [" + messageList
                    + "], zimMessage.getSenderUserID() = [" + zimMessage.getSenderUserID() + "]");
                if (messageServiceListener != null) {
                    messageServiceListener.onReceiveTextMessage(textMessage);
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
