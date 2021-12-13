package im.zego.liveaudioroom.entity;

import static im.zego.liveaudioroom.emus.ZegoLiveAudioRoomVoiceStatus.UNUSED;

import java.util.Objects;

import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomVoiceStatus;

public class ZIMSpeakerSeat {
    ZegoLiveAudioRoomVoiceStatus status;
    ZegoLiveAudioRoomSeatAttribution attribution;
    float soundLevel;

    public ZIMSpeakerSeat() {
        status = UNUSED;
        attribution = new ZegoLiveAudioRoomSeatAttribution();
    }

    public ZegoLiveAudioRoomVoiceStatus getStatus() {
        return status;
    }

    public void setStatus(ZegoLiveAudioRoomVoiceStatus status) {
        this.status = status;
    }

    public ZegoLiveAudioRoomSeatAttribution getAttribution() {
        return attribution;
    }

    public void setAttribution(ZegoLiveAudioRoomSeatAttribution attribution) {
        this.attribution = attribution;
    }

    public float getSoundLevel() {
        return soundLevel;
    }

    public void setSoundLevel(float soundLevel) {
        this.soundLevel = soundLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ZIMSpeakerSeat that = (ZIMSpeakerSeat) o;

        if (Float.compare(that.soundLevel, soundLevel) != 0) return false;
        if (status != that.status) return false;
        return Objects.equals(attribution, that.attribution);
    }

    @Override
    public int hashCode() {
        int result = status != null ? status.hashCode() : 0;
        result = 31 * result + (attribution != null ? attribution.hashCode() : 0);
        result = 31 * result + (soundLevel != +0.0f ? Float.floatToIntBits(soundLevel) : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ZIMSpeakerSeat{" +
                "status=" + status +
                ", attribution=" + attribution +
                '}';
    }
}
