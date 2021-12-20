package im.zego.liveaudioroom.listener;

import java.util.List;

public interface ZegoGiftServiceListener {

    void onReceiveGift(String giftID,String fromUserID, List<String> toUserList);
}
