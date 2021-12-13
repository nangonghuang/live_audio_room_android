package im.zego.liveaudioroom.entity;

import im.zego.liveaudioroom.emus.ZIMChatRoomSeatEvent;

public class ZIMSpeakerSeatUpdateInfo {
    ZIMSpeakerSeat speakerSeat;
    ZIMChatRoomSeatEvent event;

    public ZIMSpeakerSeatUpdateInfo(ZIMSpeakerSeat speakerSeat, ZIMChatRoomSeatEvent event) {
        this.speakerSeat = speakerSeat;
        this.event = event;
    }

    public ZIMSpeakerSeat getSpeakerSeat() {
        return speakerSeat;
    }

    public void setSpeakerSeat(ZIMSpeakerSeat speakerSeat) {
        this.speakerSeat = speakerSeat;
    }

    public ZIMChatRoomSeatEvent getEvent() {
        return event;
    }

    public void setEvent(ZIMChatRoomSeatEvent event) {
        this.event = event;
    }
}
