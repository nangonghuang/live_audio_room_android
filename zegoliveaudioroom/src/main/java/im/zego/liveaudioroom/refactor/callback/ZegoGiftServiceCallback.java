package im.zego.liveaudioroom.refactor.callback;

import java.util.List;

public interface ZegoGiftServiceCallback {

    void onReceiveGift(String giftID,String fromUserID, List<String> toUserList);
}
