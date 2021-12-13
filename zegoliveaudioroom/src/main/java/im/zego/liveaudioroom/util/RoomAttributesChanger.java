package im.zego.liveaudioroom.util;

import im.zego.liveaudioroom.internal.entity.ZegoLiveAudioRoomInfo;
import im.zego.liveaudioroom.entity.ZegoLiveAudioRoomSeatAttribution;

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

    public Byte[] getZegoLiveAudioRoomSeatAttributionToBytes(ZegoLiveAudioRoomSeatAttribution seat) {

        String string = gsonChanger.getJsonOfZegoLiveAudioRoomSeat(seat);
        return transformer.transformToByteArray(string);
    }

    public ZegoLiveAudioRoomSeatAttribution getBytesToZegoLiveAudioRoomSeatAttribution(Byte[] bytes) {

        String s = transformer.transformToString(bytes);
        return gsonChanger.getZegoLiveAudioRoomSeatAttribution(s);
    }


    public Byte[] getZegoLiveAudioRoomInfoToBytes(ZegoLiveAudioRoomInfo roomInfo) {

        String string = gsonChanger.getJsonOfZegoLiveAudioRoomInfo(roomInfo);
        return transformer.transformToByteArray(string);
    }

    public ZegoLiveAudioRoomInfo getBytesToZZegoLiveAudioRoomInfo(Byte[] bytes) {

        String s = transformer.transformToString(bytes);
        return gsonChanger.getZegoLiveAudioRoomInfo(s);
    }
}