package im.zego.liveaudioroom.service;

import android.util.Log;
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
 * room gift send and receive.
 */
public class ZegoGiftService {

    private ZegoGiftServiceListener giftServiceListener;

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
