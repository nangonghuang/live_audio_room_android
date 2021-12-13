package im.zego.liveaudioroom.entity;

import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomSeatEvent;

public class ZIMSpeakerSeatUpdateInfo {
    ZIMSpeakerSeat speakerSeat;
    ZegoLiveAudioRoomSeatEvent event;

    public ZIMSpeakerSeatUpdateInfo(ZIMSpeakerSeat speakerSeat, ZegoLiveAudioRoomSeatEvent event) {
        this.speakerSeat = speakerSeat;
        this.event = event;
    }

    public ZIMSpeakerSeat getSpeakerSeat() {
        return speakerSeat;
    }

    public void setSpeakerSeat(ZIMSpeakerSeat speakerSeat) {
        this.speakerSeat = speakerSeat;
    }

    public ZegoLiveAudioRoomSeatEvent getEvent() {
        return event;
    }

    public void setEvent(ZegoLiveAudioRoomSeatEvent event) {
        this.event = event;
    }
}
