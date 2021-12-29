package im.zego.liveaudioroom.listener;

import im.zego.liveaudioroom.model.ZegoTextMessage;

/**
 * The listener related to the message receiving callbacks.
 * <p>Description: Callbacks that be triggered when new IM messages received.</>
 */
public interface ZegoMessageServiceListener {

    /**
     * Callback for receive IM text messages.
     * <p>Description: This callback will be triggered when existing users in the room send IM messages, and all users
     * in the room will reveive a notification. The message list will be updated synchronously</>
     *
     * @param textMessage refers to the received text message information.
     */
    void onReceiveTextMessage(ZegoTextMessage textMessage);
}
