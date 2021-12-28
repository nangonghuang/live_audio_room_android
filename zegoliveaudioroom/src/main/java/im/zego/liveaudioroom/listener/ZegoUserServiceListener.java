package im.zego.liveaudioroom.listener;

import im.zego.liveaudioroom.model.ZegoUserInfo;
import java.util.List;

/**
 * The listener related to user status.
 * <p>Description: Callbacks that be triggered when in-room user status change.</>
 */
public interface ZegoUserServiceListener {

    /**
     * Callback for new user joins the room.
     * <p>Description: This callback will be triggered when a new user joins the room, and all users in the room will
     * receive a notification. The in-room user list data will be updated automatically.</>
     *
     * @param userList refers to the latest new-comer user list. Existing users are not included.
     */
    void onRoomUserJoin(List<ZegoUserInfo> userList);

    /**
     * Callback for existing user leaves the room.
     * <p>Description: This callback will be triggered when an existing user leaves the room, and all users in the room
     * will receive a notification. The in-room user list data will be updated automatically.</>
     *
     * @param userList refers to the list of users who left the room.
     */
    void onRoomUserLeave(List<ZegoUserInfo> userList);

    /**
     * The notification of seat-taking invitation.
     * <p>Description: The invitee receives a notification when he is be invited to take a speaker seat to speak.</>
     */
    void onReceiveTakeSeatInvitation();
}