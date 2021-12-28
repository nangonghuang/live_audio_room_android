package im.zego.liveaudioroom.listener;

import java.util.List;

/**
 * The listener related to gift receiving callbacks.
 * <p>Description: Callbacks that triggered when receiving virtual gifts. </>
 */
public interface ZegoGiftServiceListener {

    /**
     * Callback for receive a virtual gift.
     * <p>Description: This callback will be triggered when there is a virtual gifting event occurs, all room users
     * will receive a notification. You can define your own logic here for UI display.</>
     * <p>Call this method at:  After joining the room and when there is a virtual gifting event occurs</>
     *
     * @param giftID     refers to the gift type.
     * @param fromUserID refers to the gift sender.
     * @param toUserList refers to the gift recipient list.
     */
    void onReceiveGift(String giftID, String fromUserID, List<String> toUserList);
}
