package im.zego.liveaudioroom.callback;

/**
 * Callback for get the total number of in-room users.
 */
public interface ZegoOnlineRoomUsersNumCallback {

    /**
     * This callback will be triggered when the method call that get the total number of in-room users has finished its
     * execution.
     *
     * @param errorCode refers to the operation status code. <br> 0: Operation successful. <br> 600xxxx: The ZIM SDK
     *                  error code. For details, refer to the error code documentation.https://doc-en.zego.im/article/13792
     * @param count     refers to the in-room user list
     */
    void userCountCallback(int errorCode, int count);
}