package im.zego.liveaudioroom.callback;

import im.zego.liveaudioroom.model.ZegoUserInfo;
import java.util.List;

/**
 * Callback for get the user list.
 * <p>Description: This callback will be triggered when the method call that get the user list has finished its
 * execution.</>
 */
public interface ZegoOnlineRoomUsersCallback {

    /**
     * @param error    error refers to the operation status code.
     *                 <p>0: Operation successful.</>
     *                 <p>600xxxx: The ZIM SDK error code. For details, refer to the error code documentation.
     *                 https://doc-en.zego.im/article/13792</>
     * @param userList refers to the in-room user list
     */
    void onlineUserCallback(int error, List<ZegoUserInfo> userList);
}
