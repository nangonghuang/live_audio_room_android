package im.zego.liveaudioroom.helper;

import com.google.gson.Gson;

import java.util.HashMap;

import im.zego.liveaudioroom.constants.ZegoRoomConstants;
import im.zego.liveaudioroom.model.ZegoRoomInfo;
import im.zego.zim.entity.ZIMRoomAttributesSetConfig;

/**
 * Created by rocket_wang on 2021/12/14.
 */
public class ZegoRoomAttributesHelper {
    public static HashMap<String, String> getRoomConfigByTextMessage(boolean isDisabled, ZegoRoomInfo roomInfo) {
        HashMap<String, String> roomConfig = new HashMap<>();
        roomInfo.setTextMessageDisabled(isDisabled);
        roomConfig.put(ZegoRoomConstants.KEY_ROOM_INFO, new Gson().toJson(roomInfo));
        return roomConfig;
    }

    public static HashMap<String, String> getRoomConfigByCloseSeat(boolean isClosed, ZegoRoomInfo roomInfo) {
        HashMap<String, String> roomConfig = new HashMap<>();
        roomInfo.setClosed(isClosed);
        roomConfig.put(ZegoRoomConstants.KEY_ROOM_INFO, new Gson().toJson(roomInfo));
        return roomConfig;
    }

    public static ZIMRoomAttributesSetConfig getAttributesSetConfig() {
        ZIMRoomAttributesSetConfig setConfig = new ZIMRoomAttributesSetConfig();
        setConfig.isForce = true;
        setConfig.isDeleteAfterOwnerLeft = true;
        setConfig.isUpdateOwner = false;
        return setConfig;
    }
}