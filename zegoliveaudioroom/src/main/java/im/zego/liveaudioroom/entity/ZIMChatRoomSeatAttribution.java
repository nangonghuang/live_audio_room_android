package im.zego.liveaudioroom.entity;

import java.util.Objects;

public class ZegoLiveAudioRoomSeatAttribution {

    String user_id = "";
    int index;
    boolean is_muted;
    boolean is_locked;
    String extras;

    public ZegoLiveAudioRoomSeatAttribution() {
        this.user_id = "";
        this.index = -1;
        this.is_muted = false;
        extras = "";
    }

    public ZegoLiveAudioRoomSeatAttribution(String user_id, int index) {
        this.user_id = user_id;
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public boolean isIs_muted() {
        return is_muted;
    }

    public void setIs_muted(boolean is_muted) {
        this.is_muted = is_muted;
    }

    public boolean isIs_locked() {
        return is_locked;
    }

    public void setIs_locked(boolean is_locked) {
        this.is_locked = is_locked;
    }

    public String getExtras() {
        return extras;
    }

    public void setExtras(String extras) {
        this.extras = extras;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ZegoLiveAudioRoomSeatAttribution that = (ZegoLiveAudioRoomSeatAttribution) o;
        return index == that.index && is_muted == that.is_muted && is_locked == that.is_locked && Objects.equals(user_id, that.user_id) && Objects.equals(extras, that.extras);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user_id, index, is_muted, is_locked, extras);
    }

    @Override
    public String toString() {
        return "ZegoLiveAudioRoomSeatAttribution{" +
                "user_id='" + user_id + '\'' +
                ", index=" + index +
                ", is_muted=" + is_muted +
                ", is_locked=" + is_locked +
                ", extras='" + extras + '\'' +
                '}';
    }
}
