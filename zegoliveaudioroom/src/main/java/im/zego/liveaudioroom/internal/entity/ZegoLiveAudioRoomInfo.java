package im.zego.liveaudioroom.internal.entity;

import java.util.Objects;

public class ZegoLiveAudioRoomInfo {
    String room_name;
    String room_id;
    String author;
    int seat_count;
    boolean is_mute_msg;

    public ZegoLiveAudioRoomInfo() {
    }

    public ZegoLiveAudioRoomInfo(String room_name, String room_id) {
        this.room_name = room_name;
        this.room_id = room_id;
    }

    public String getRoom_Name() {
        return room_name;
    }

    public void setRoom_Name(String room_Name) {
        this.room_name = room_Name;
    }

    public String getRoom_id() {
        return room_id;
    }

    public void setRoom_id(String room_id) {
        this.room_id = room_id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getSeat_count() {
        return seat_count;
    }

    public void setSeat_count(int seat_count) {
        this.seat_count = seat_count;
    }

    public boolean isIs_mute_msg() {
        return is_mute_msg;
    }

    public void setIs_mute_msg(boolean is_mute_msg) {
        this.is_mute_msg = is_mute_msg;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ZegoLiveAudioRoomInfo that = (ZegoLiveAudioRoomInfo) o;
        return seat_count == that.seat_count && is_mute_msg == that.is_mute_msg && Objects.equals(room_name, that.room_name) && Objects.equals(room_id, that.room_id) && Objects.equals(author, that.author);
    }

    @Override
    public int hashCode() {
        return Objects.hash(room_name, room_id, author, seat_count, is_mute_msg);
    }
}
