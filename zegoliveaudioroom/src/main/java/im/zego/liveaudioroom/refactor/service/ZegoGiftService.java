package im.zego.liveaudioroom.refactor.service;

import im.zego.liveaudioroom.refactor.ZegoRoomManager;
import im.zego.liveaudioroom.refactor.ZegoZIMManager;
import im.zego.liveaudioroom.refactor.callback.ZegoGiftServiceCallback;
import im.zego.liveaudioroom.refactor.callback.ZegoRoomCallback;
import im.zego.liveaudioroom.refactor.model.ZegoCustomCommand;
import im.zego.liveaudioroom.refactor.model.ZegoUserInfo;
import im.zego.zim.ZIM;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.enums.ZIMMessageType;
import java.util.ArrayList;
import java.util.List;

/**
 * room gift send and receive.
 */
public class ZegoGiftService {

    private ZegoGiftServiceCallback giftServiceCallback;

    /**
     * send gift to room users.
     *
     * @param giftID     giftID
     * @param toUserList send gift target
     * @param callback   operation result callback
     */
    public void sendGift(String giftID, List<String> toUserList, ZegoRoomCallback callback) {
        ZegoUserInfo localUserInfo = ZegoRoomManager.getInstance().userService.localUserInfo;
        ZegoCustomCommand command = new ZegoCustomCommand();
        command.actionType = ZegoCustomCommand.Gift;
        command.target = toUserList;
        command.userID = localUserInfo.getUserID();
        command.content.put("giftID", giftID);
        String roomID = ZegoRoomManager.getInstance().roomService.roomInfo.getRoomID();
        ZegoZIMManager.getInstance().zim.sendRoomMessage(command, roomID, (message, errorInfo) -> {
            if (callback != null) {
                callback.roomCallback(errorInfo.code.value());
            }
        });
    }

    public void setGiftServiceCallback(ZegoGiftServiceCallback callback) {
        this.giftServiceCallback = callback;
    }

    public void onReceiveRoomMessage(ZIM zim, ArrayList<ZIMMessage> messageList, String fromRoomID) {
        for (ZIMMessage zimMessage : messageList) {
            if (zimMessage.type == ZIMMessageType.CUSTOM) {
                ZegoCustomCommand command = (ZegoCustomCommand) zimMessage;
                if (command.actionType == ZegoCustomCommand.Gift) {
                    String giftID = command.content.get("giftID");
                    List<String> toUserList = command.target;
                    if (giftServiceCallback != null) {
                        giftServiceCallback.onReceiveGift(giftID, command.userID, toUserList);
                    }
                }
            }
        }
    }
}
