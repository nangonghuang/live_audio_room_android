package im.zego.liveaudioroom.util;

import com.google.gson.Gson;

import im.zego.liveaudioroom.entity.ZegoLiveAudioRoomMessage;
import im.zego.liveaudioroom.entity.ZegoLiveAudioRoomSeatAttribution;
import im.zego.liveaudioroom.internal.entity.ZegoLiveAudioRoomInfo;

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

    public ZegoLiveAudioRoomMessage getZegoLiveAudioRoomMessage(String json) {
        return gson.fromJson(json, ZegoLiveAudioRoomMessage.class);
    }

    public String getJsonOfZegoLiveAudioRoomMessage(ZegoLiveAudioRoomMessage ZegoLiveAudioRoomMessage) {
        return gson.toJson(ZegoLiveAudioRoomMessage);
    }

    public ZegoLiveAudioRoomInfo getZegoLiveAudioRoomInfo(String json) {
        return gson.fromJson(json, ZegoLiveAudioRoomInfo.class);
    }

    public String getJsonOfZegoLiveAudioRoomInfo(ZegoLiveAudioRoomInfo ZegoLiveAudioRoomInfo) {
        return gson.toJson(ZegoLiveAudioRoomInfo);
    }

    public ZegoLiveAudioRoomSeatAttribution getZegoLiveAudioRoomSeatAttribution(String json) {
        return gson.fromJson(json, ZegoLiveAudioRoomSeatAttribution.class);
    }

    public String getJsonOfZegoLiveAudioRoomSeat(ZegoLiveAudioRoomSeatAttribution seatAttributes) {
        return gson.toJson(seatAttributes);
    }
}