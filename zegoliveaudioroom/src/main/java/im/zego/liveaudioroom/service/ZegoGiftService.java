package im.zego.liveaudioroom.service;

import com.google.gson.Gson;
import im.zego.liveaudioroom.ZegoRoomManager;
import im.zego.liveaudioroom.ZegoZIMManager;
import im.zego.liveaudioroom.callback.ZegoRoomCallback;
import im.zego.liveaudioroom.listener.ZegoGiftServiceListener;
import im.zego.liveaudioroom.model.ZegoCustomCommand;
import im.zego.liveaudioroom.model.ZegoUserInfo;
import im.zego.zim.ZIM;
import im.zego.zim.entity.ZIMCustomMessage;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.enums.ZIMMessageType;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Class gift management.
 * <p>Description: This class contains the logics of send and receive gifts.</>
 */
public class ZegoGiftService {

    /**
     * The listener related to gift updates.
     */
    private ZegoGiftServiceListener giftServiceListener;

    /**
     * Send virtual gift.
     * <p>Description: This method can be used to send a virtual gift, all room users will receive a notification. You
     * can determine whether you are the gift recipient by the toUserList parameter.</>
     * <p>Call this method at:  After joining the room</>
     *
     * @param giftID     refers to the gift type.
     * @param toUserList refers to the gift recipient.
     * @param callback   refers to the callback for send a virtual gift.
     */
    public void sendGift(String giftID, List<String> toUserList, ZegoRoomCallback callback) {
        ZegoUserInfo localUserInfo = ZegoRoomManager.getInstance().userService.localUserInfo;
        ZegoCustomCommand command = new ZegoCustomCommand();
        command.actionType = ZegoCustomCommand.Gift;
        command.target = toUserList;
        command.userID = localUserInfo.getUserID();
        command.content.put("giftID", giftID);
        String string = new Gson().toJson(command);
        command.message = string.getBytes(StandardCharsets.UTF_8);
        String roomID = ZegoRoomManager.getInstance().roomService.roomInfo.getRoomID();
        ZegoZIMManager.getInstance().zim.sendRoomMessage(command, roomID, (message, errorInfo) -> {
            if (callback != null) {
                callback.roomCallback(errorInfo.code.value());
            }
        });
    }

    public void setListener(ZegoGiftServiceListener listener) {
        this.giftServiceListener = listener;
    }

    public void onReceiveRoomMessage(ZIM zim, ArrayList<ZIMMessage> messageList, String fromRoomID) {
        for (ZIMMessage zimMessage : messageList) {
            if (zimMessage.type == ZIMMessageType.CUSTOM) {
                ZIMCustomMessage zimCustomMessage = (ZIMCustomMessage) zimMessage;
                String json = new String(zimCustomMessage.message);
                ZegoCustomCommand command = new Gson().fromJson(json, ZegoCustomCommand.class);
                if (command.actionType == ZegoCustomCommand.Gift) {
                    String giftID = command.content.get("giftID");
                    List<String> toUserList = command.target;
                    if (giftServiceListener != null) {
                        giftServiceListener.onReceiveGift(giftID, zimCustomMessage.userID, toUserList);
                    }
                }
            }
        }
    }

    public void reset() {
        giftServiceListener = null;
    }
}
