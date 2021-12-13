package im.zego.liveaudioroom.entity;

import java.util.List;

public class ZegoLiveAudioRoomGiftBroadcast {
    List<String> toUserIDList;
    int giftType;

    public ZegoLiveAudioRoomGiftBroadcast() {
    }


    public List<String> getToUserIDList() {
        return toUserIDList;
    }

    public void setToUserIDList(List<String> toUserIDList) {
        this.toUserIDList = toUserIDList;
    }

    public int getGiftType() {
        return giftType;
    }

    public void setGiftType(int giftType) {
        this.giftType = giftType;
    }

}
