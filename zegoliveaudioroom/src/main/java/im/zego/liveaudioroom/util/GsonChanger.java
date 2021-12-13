package im.zego.liveaudioroom.util;

import com.google.gson.Gson;

import im.zego.liveaudioroom.entity.ZIMChatRoomMessage;
import im.zego.liveaudioroom.entity.ZIMChatRoomSeatAttribution;
import im.zego.liveaudioroom.internal.entity.ZIMChatRoomInfo;

/**
 * 这个工具类是为了将ChatRoom中传递的信息进行json处理
 */
public class GsonChanger {
    private static GsonChanger instance;
    private final Gson gson;

    private GsonChanger() {
        gson = new Gson();
    }

    public static GsonChanger getInstance() {
        if (instance == null) {
            instance = new GsonChanger();
        }
        return instance;
    }

    public ZIMChatRoomMessage getZIMChatRoomMessage(String json) {
        return gson.fromJson(json, ZIMChatRoomMessage.class);
    }

    public String getJsonOfZIMChatRoomMessage(ZIMChatRoomMessage zimChatRoomMessage) {
        return gson.toJson(zimChatRoomMessage);
    }

    public ZIMChatRoomInfo getZIMChatRoomInfo(String json) {
        return gson.fromJson(json, ZIMChatRoomInfo.class);
    }

    public String getJsonOfZIMChatRoomInfo(ZIMChatRoomInfo zimChatRoomInfo) {
        return gson.toJson(zimChatRoomInfo);
    }

    public ZIMChatRoomSeatAttribution getZIMChatRoomSeatAttribution(String json) {
        return gson.fromJson(json, ZIMChatRoomSeatAttribution.class);
    }

    public String getJsonOfZIMChatRoomSeat(ZIMChatRoomSeatAttribution seatAttributes) {
        return gson.toJson(seatAttributes);
    }
}