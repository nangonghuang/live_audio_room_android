package im.zego.liveaudioroom.service;

import android.util.Log;
import com.google.gson.Gson;
import im.zego.liveaudioroom.ZegoRoomManager;
import im.zego.liveaudioroom.ZegoZIMManager;
import im.zego.liveaudioroom.callback.ZegoOnlineRoomUsersCallback;
import im.zego.liveaudioroom.callback.ZegoOnlineRoomUsersNumCallback;
import im.zego.liveaudioroom.callback.ZegoRoomCallback;
import im.zego.liveaudioroom.listener.ZegoUserServiceListener;
import im.zego.liveaudioroom.model.ZegoCustomCommand;
import im.zego.liveaudioroom.model.ZegoRoomInfo;
import im.zego.liveaudioroom.model.ZegoRoomUserRole;
import im.zego.liveaudioroom.model.ZegoSpeakerSeatModel;
import im.zego.liveaudioroom.model.ZegoSpeakerSeatStatus;
import im.zego.liveaudioroom.model.ZegoUserInfo;
import im.zego.zim.ZIM;
import im.zego.zim.entity.ZIMCustomMessage;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.entity.ZIMQueryMemberConfig;
import im.zego.zim.entity.ZIMUserInfo;
import im.zego.zim.enums.ZIMConnectionEvent;
import im.zego.zim.enums.ZIMConnectionState;
import im.zego.zim.enums.ZIMErrorCode;
import im.zego.zim.enums.ZIMMessageType;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;

/**
 * Class user information management.
 * <p>Description: This class contains the user information management logics, such as the logic of log in, log out,
 * get the logged-in user info, get the in-room user list, and add co-hosts, etc. </>
 */
public class ZegoUserService {

    private static final String TAG = "ZegoUserService";

    /**
     * The local logged-in user information.
     */
    public ZegoUserInfo localUserInfo;
    /**
     * In-room user list, can be used when displaying the user list in the room.
     */
    private final List<ZegoUserInfo> userList = new ArrayList<>();
    /**
     * In-room user dictionary,  can be used to update user information.¬
     */
    private final Map<String, ZegoUserInfo> userMap = new HashMap<>();

    /**
     * The listener related to user status.
     */
    private ZegoUserServiceListener listener;

    /**
     * User to log in.
     * <p>Description: Call this method with user ID and username to log in to the LiveAudioRoom service.</>
     * <p>Call this method at: After the SDK initialization</>
     *
     * @param userInfo refers to the user information. You only need to enter the user ID and username.
     * @param token    refers to the authentication token. To get this, refer to the documentation:
     *                 https://doc-en.zego.im/article/11648
     * @param callback refers to the callback for log in.
     */
    public void login(ZegoUserInfo userInfo, String token, final ZegoRoomCallback callback) {
        ZIMUserInfo zimUserInfo = new ZIMUserInfo();
        zimUserInfo.userID = userInfo.getUserID();
        zimUserInfo.userName = userInfo.getUserName();
        ZegoZIMManager.getInstance().zim.login(zimUserInfo, token, errorInfo -> {
            Log.d(TAG, "onLoggedIn() called with: errorInfo = [" + errorInfo.code + ", "
                + errorInfo.message + "]");
            if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                localUserInfo = new ZegoUserInfo();
                localUserInfo.setUserID(userInfo.getUserID());
                localUserInfo.setUserName(userInfo.getUserName());
            }
            if (callback != null) {
                callback.roomCallback(errorInfo.code.value());
            }
        });
    }

    /**
     * User to log out.
     * <p>Description: This method can be used to log out from the current user account.</>
     * <p>Call this method at: After the user login</>
     */
    public void logout() {
        ZegoZIMManager.getInstance().zim.logout();
        reset();
    }

    void reset() {
        userList.clear();
        userMap.clear();
        listener = null;
    }

    public void setListener(ZegoUserServiceListener listener) {
        this.listener = listener;
    }

    /**
     * contains self
     *
     * @param zim
     * @param memberList
     * @param roomID
     */
    public void onRoomMemberJoined(ZIM zim, ArrayList<ZIMUserInfo> memberList, String roomID) {
        List<ZegoUserInfo> joinUsers = generateRoomUsers(memberList);
        Iterator<ZegoUserInfo> iterator = joinUsers.iterator();
        while (iterator.hasNext()) {
            ZegoUserInfo next = iterator.next();
            Log.d(TAG, "onRoomMemberJoined() called with: joinUser = [" + next);
            if (!userMap.containsKey(next.getUserID())) {
                userList.add(next); // avoid duplicate
                userMap.put(next.getUserID(), next);
            } else {
                // if duplicate,don't notify outside
                iterator.remove();
            }
        }
        if (joinUsers.size() > 0 && listener != null) {
            listener.onRoomUserJoin(joinUsers);
        }
    }

    public void onRoomMemberLeft(ZIM zim, ArrayList<ZIMUserInfo> memberList, String roomID) {
        List<ZegoUserInfo> leaveUsers = generateRoomUsers(memberList);
        userList.removeAll(leaveUsers);
        for (ZegoUserInfo leaveUser : leaveUsers) {
            userMap.remove(leaveUser.getUserID());
            Log.d(TAG, "onRoomMemberLeft() called with: leaveUser = [" + leaveUser);
        }
        if (listener != null) {
            listener.onRoomUserLeave(leaveUsers);
        }
        ZegoSpeakerSeatService seatService = ZegoRoomManager.getInstance().speakerSeatService;
        List<ZegoSpeakerSeatModel> seatList = seatService.getSpeakerSeatList();
        for (ZegoUserInfo leaveUser : leaveUsers) {
            String leaveUserID = leaveUser.getUserID();
            for (ZegoSpeakerSeatModel model : seatList) {
                if (model.userID.equals(leaveUserID) && model.status == ZegoSpeakerSeatStatus.Occupied) {
                    seatService.removeUserFromSeat(model.seatIndex, errorCode -> {
                        Log.d(TAG, "removeUserFromSeat() called with: errorCode = [" + errorCode);
                    });
                }
            }
        }
    }

    private List<ZegoUserInfo> generateRoomUsers(List<ZIMUserInfo> memberList) {
        ZegoRoomInfo roomInfo = ZegoRoomManager.getInstance().roomService.roomInfo;

        List<ZegoUserInfo> roomUsers = new ArrayList<>();
        for (ZIMUserInfo userInfo : memberList) {
            ZegoUserInfo roomUser = new ZegoUserInfo();
            roomUser.setUserID(userInfo.userID);
            roomUser.setUserName(userInfo.userName);

            if (userInfo.userID.equals(roomInfo.getHostID())) {
                roomUser.setRole(ZegoRoomUserRole.Host);
            } else {
                roomUser.setRole(ZegoRoomUserRole.Listener);
            }
            roomUsers.add(roomUser);
        }
        return roomUsers;
    }

    public List<ZegoUserInfo> getUserList() {
        return userList;
    }

    public String getUserName(String userID) {
        ZegoUserInfo zegoUserInfo = getUserInfo(userID);
        if (zegoUserInfo != null) {
            return zegoUserInfo.getUserName();
        } else {
            return "";
        }
    }

    public ZegoUserInfo getUserInfo(String userID) {
        return userMap.get(userID);
    }

    /**
     * Invite users to speak .
     * <p>Description: This method can be called to invite users to take a speaker seat to speak, and the invitee will
     * receive an invitation.</>
     * <p>Call this method at:  After joining a room</>
     *
     * @param userID   refers to the ID of the user that you want to invite
     * @param callback refers to the callback for invite users to speak
     */
    public void sendInvitation(String userID, ZegoRoomCallback callback) {
        ZegoUserInfo localUserInfo = ZegoRoomManager.getInstance().userService.localUserInfo;
        ZegoCustomCommand command = new ZegoCustomCommand();
        command.actionType = ZegoCustomCommand.INVITATION;
        command.target = Collections.singletonList(userID);
        command.userID = localUserInfo.getUserID();
        String string = new Gson().toJson(command);
        Log.d(TAG, "sendInvitation: " + string);
        command.message = string.getBytes(StandardCharsets.UTF_8);
        ZegoZIMManager.getInstance().zim.sendPeerMessage(command, userID, (message, errorInfo) -> {
            Log.d(TAG, "sendInvitation: " + errorInfo.code);
            if (callback != null) {
                callback.roomCallback(errorInfo.code.value());
            }
        });
    }

    /**
     * Get the total number of in-room users
     * <p>Description: This method can be called to get the total number of the in-room users.</>
     * <p>Call this method at: After joining a room</>
     *
     * @param callback refers to the callback for get the total number of in-room users.
     */
    public void getOnlineRoomUsersNum(final ZegoOnlineRoomUsersNumCallback callback) {
        ZegoRoomInfo roomInfo = ZegoRoomManager.getInstance().roomService.roomInfo;
        ZegoZIMManager.getInstance().zim.queryRoomOnlineMemberCount(roomInfo.getRoomID(), (count, errorInfo) -> {
            if (callback != null) {
                callback.userCountCallback(errorInfo.code.value(), count);
            }
        });
    }

    /**
     * Get the in-room user list
     * <p>Description: Description: This method can be called to get the in-room user list.</>
     * <p>Call this method at:  After joining the room</>
     *
     * @param config
     * @param callback refers to the callback for get the in-room user list.
     */
    public void getOnlineRoomUsers(ZIMQueryMemberConfig config, ZegoOnlineRoomUsersCallback callback) {
        ZegoRoomInfo roomInfo = ZegoRoomManager.getInstance().roomService.roomInfo;
        ZegoZIMManager.getInstance().zim
            .queryRoomMember(roomInfo.getRoomID(), config, (memberList, nextFlag, errorInfo) -> {
                List<ZegoUserInfo> zegoUserInfos = generateRoomUsers(memberList);
                callback.onlineUserCallback(errorInfo.code.value(), nextFlag, zegoUserInfos);
            });
    }

    public void onReceivePeerMessage(ZIM zim, ArrayList<ZIMMessage> messageList, String fromUserID) {
        Log.d(TAG, "onReceivePeerMessage() called with: zim = [" + zim + "], messageList = [" + messageList
            + "], fromUserID = [" + fromUserID + "]");
        for (ZIMMessage zimMessage : messageList) {
            if (zimMessage.type == ZIMMessageType.CUSTOM) {
                ZIMCustomMessage zimCustomMessage = (ZIMCustomMessage) zimMessage;
                ZegoCustomCommand command = new Gson()
                    .fromJson(new String(zimCustomMessage.message), ZegoCustomCommand.class);
                Log.d(TAG, "onReceivePeerMessage: command" + command.actionType);
                if (command.actionType == ZegoCustomCommand.INVITATION) {
                    ZegoUserInfo localUserInfo = ZegoRoomManager.getInstance().userService.localUserInfo;
                    if (command.target.contains(localUserInfo.getUserID())) {
                        if (listener != null) {
                            listener.onReceiveTakeSeatInvitation();
                        }
                    }
                }
            }
        }
    }

    public void onConnectionStateChanged(ZIM zim, ZIMConnectionState state, ZIMConnectionEvent event,
        JSONObject extendedData) {
        Log.d(TAG,
            "onConnectionStateChanged() called with: zim = [" + zim + "], state = [" + state + "], event = ["
                + event + "], extendedData = [" + extendedData + "]");
        if (listener != null) {
            listener.onConnectionStateChanged(state, event);
        }

    }
}