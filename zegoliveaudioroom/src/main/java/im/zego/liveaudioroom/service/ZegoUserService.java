package im.zego.liveaudioroom.service;

import android.util.Log;
import im.zego.liveaudioroom.ZegoRoomManager;
import im.zego.liveaudioroom.ZegoZIMManager;
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
import im.zego.zim.entity.ZIMUserInfo;
import im.zego.zim.enums.ZIMErrorCode;
import im.zego.zim.enums.ZIMMessageType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rocket_wang on 2021/12/14.
 */
public class ZegoUserService {

    private static final String TAG = "ZegoUserService";

    // local login user info
    public ZegoUserInfo localUserInfo;
    // room member list,contains self
    private final List<ZegoUserInfo> userList = new ArrayList<>();
    private final Map<String, ZegoUserInfo> userMap = new HashMap<>();
    private ZegoUserServiceListener listener;

    // user login
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

    // user logout
    public void logout() {
        Log.d(TAG, "logout() called");
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

    public void onRoomMemberJoined(ZIM zim, ArrayList<ZIMUserInfo> memberList, String roomID) {
        List<ZegoUserInfo> joinUsers = generateRoomUsers(memberList);
        userList.addAll(joinUsers);
        for (ZegoUserInfo joinUser : joinUsers) {
            userMap.put(joinUser.getUserID(), joinUser);
        }
        if (listener != null) {
            listener.onRoomUserJoin(joinUsers);
        }
    }

    public void onRoomMemberLeft(ZIM zim, ArrayList<ZIMUserInfo> memberList, String roomID) {
        List<ZegoUserInfo> leaveUsers = generateRoomUsers(memberList);
        userList.removeAll(leaveUsers);
        for (ZegoUserInfo leaveUser : leaveUsers) {
            userMap.remove(leaveUser.getUserID());
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

    ZegoUserInfo getUserInfo(String userID) {
        return userMap.get(userID);
    }

    /**
     * send invitation to room user.
     *
     * @param userID   userID
     * @param callback operation result callback
     */
    public void sendInvitation(String userID, ZegoRoomCallback callback) {
        ZegoUserInfo localUserInfo = ZegoRoomManager.getInstance().userService.localUserInfo;
        ZegoCustomCommand command = new ZegoCustomCommand();
        command.actionType = ZegoCustomCommand.INVITATION;
        command.target = Collections.singletonList(userID);
        command.userID = localUserInfo.getUserID();
        command.toJson();
        String roomID = ZegoRoomManager.getInstance().roomService.roomInfo.getRoomID();
        ZegoZIMManager.getInstance().zim.sendRoomMessage(command, roomID, (message, errorInfo) -> {
            if (callback != null) {
                callback.roomCallback(errorInfo.code.value());
            }
        });
    }

    public void onReceiveRoomMessage(ZIM zim, ArrayList<ZIMMessage> messageList, String fromRoomID) {
        for (ZIMMessage zimMessage : messageList) {
            if (zimMessage.type == ZIMMessageType.CUSTOM) {
                ZIMCustomMessage zimCustomMessage = (ZIMCustomMessage) zimMessage;
                ZegoCustomCommand command = new ZegoCustomCommand();
                command.type = zimCustomMessage.type;
                command.userID = zimCustomMessage.userID;
                command.fromJson(zimCustomMessage.message);
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
}