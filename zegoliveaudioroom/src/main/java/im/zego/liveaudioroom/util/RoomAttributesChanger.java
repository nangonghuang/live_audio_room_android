package im.zego.liveaudioroom.util;

import im.zego.liveaudioroom.internal.entity.ZIMChatRoomInfo;
import im.zego.liveaudioroom.entity.ZIMChatRoomSeatAttribution;

public class RoomAttributesChanger {

    private static RoomAttributesChanger instance;
    GsonChanger gsonChanger;
    ByteAndStringTransformer transformer;

    private RoomAttributesChanger() {
        gsonChanger = GsonChanger.getInstance();
        transformer = ByteAndStringTransformer.getInstance();
    }

    public static RoomAttributesChanger getInstance() {
        if (instance == null) {
            instance = new RoomAttributesChanger();
        }
        return instance;
    }

    public Byte[] getZIMChatRoomSeatAttributionToBytes(ZIMChatRoomSeatAttribution seat) {

        String string = gsonChanger.getJsonOfZIMChatRoomSeat(seat);
        return transformer.transformToByteArray(string);
    }

    public ZIMChatRoomSeatAttribution getBytesToZIMChatRoomSeatAttribution(Byte[] bytes) {

        String s = transformer.transformToString(bytes);
        return gsonChanger.getZIMChatRoomSeatAttribution(s);
    }


    public Byte[] getZIMChatRoomInfoToBytes(ZIMChatRoomInfo roomInfo) {

        String string = gsonChanger.getJsonOfZIMChatRoomInfo(roomInfo);
        return transformer.transformToByteArray(string);
    }

    public ZIMChatRoomInfo getBytesToZZIMChatRoomInfo(Byte[] bytes) {

        String s = transformer.transformToString(bytes);
        return gsonChanger.getZIMChatRoomInfo(s);
    }
}