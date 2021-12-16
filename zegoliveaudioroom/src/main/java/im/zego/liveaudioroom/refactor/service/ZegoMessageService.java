package im.zego.liveaudioroom.refactor.service;

import im.zego.liveaudioroom.refactor.ZegoRoomManager;
import im.zego.liveaudioroom.refactor.ZegoZIMManager;
import im.zego.liveaudioroom.refactor.callback.ZegoMessageServiceCallback;
import im.zego.liveaudioroom.refactor.callback.ZegoRoomCallback;
import im.zego.liveaudioroom.refactor.model.ZegoCoustomCommand;
import im.zego.liveaudioroom.refactor.model.ZegoTextMessage;
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
    public List<ZegoTextMessage> messageList;

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
        ZegoTextMessage textMessage = new ZegoTextMessage();
        textMessage.message = text;
        String roomID = ZegoRoomManager.getInstance().roomService.roomInfo.getRoomID();
        ZegoZIMManager.getInstance().zim.sendRoomMessage(textMessage, roomID,
            (message, errorInfo) -> {
                if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                    messageList.add(textMessage);
                }
                if (callback != null) {
                    callback.roomCallback(errorInfo.code.value());
                }
            });
    }

    /**
     * send invitation to room user.
     *
     * @param userID   userID
     * @param callback operation result callback
     */
    public void sendInvitation(String userID, ZegoRoomCallback callback) {
        ZegoCoustomCommand command = new ZegoCoustomCommand();
        command.actionType = ZegoCoustomCommand.Gift;
        command.target = Arrays.asList(userID);
        String roomID = ZegoRoomManager.getInstance().roomService.roomInfo.getRoomID();
        ZegoZIMManager.getInstance().zim.sendRoomMessage(command, roomID,
            (message, errorInfo) -> {
                if (callback != null) {
                    callback.roomCallback(errorInfo.code.value());
                }
            });
    }

    public void onReceiveRoomMessage(ZIM zim, ArrayList<ZIMMessage> messageList,
        String fromRoomID) {
        for (ZIMMessage zimMessage : messageList) {
            if (zimMessage.type == ZIMMessageType.CUSTOM) {
                ZegoCoustomCommand command = (ZegoCoustomCommand) zimMessage;
                if (command.actionType == ZegoCoustomCommand.INVITATION) {
                    if (messageServiceCallback != null) {
                        messageServiceCallback.onReceiveCustomCommand(command, fromRoomID);
                    }
                }
            } else if (zimMessage.type == ZIMMessageType.TEXT) {
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
}
