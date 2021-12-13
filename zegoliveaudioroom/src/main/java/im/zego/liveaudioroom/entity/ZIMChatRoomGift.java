package im.zego.liveaudioroom.entity;

import im.zego.zim.entity.ZIMUserInfo;

public class ZIMChatRoomGift {
    String fromUserID;
    int giftType;

    public String getFromUserID() {
        return fromUserID;
    }

    public void setFromUserID(String fromUserID) {
        this.fromUserID = fromUserID;
    }

    public int getGiftType() {
        return giftType;
    }

    public void setGiftType(int giftType) {
        this.giftType = giftType;
    }
}
