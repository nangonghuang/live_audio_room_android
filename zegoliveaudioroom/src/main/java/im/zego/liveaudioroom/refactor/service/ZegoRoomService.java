package im.zego.liveaudioroom.refactor.service;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Set;

import im.zego.liveaudioroom.refactor.ZegoRoomManager;
import im.zego.liveaudioroom.refactor.ZegoZIMManager;
import im.zego.liveaudioroom.refactor.callback.ZegoOnlineRoomUsersCallback;
import im.zego.liveaudioroom.refactor.callback.ZegoRoomCallback;
import im.zego.liveaudioroom.refactor.constants.ZegoRoomConstants;
import im.zego.liveaudioroom.refactor.helper.ZegoRoomAttributesHelper;
import im.zego.liveaudioroom.refactor.listener.ZegoRoomServiceListener;
import im.zego.liveaudioroom.refactor.model.ZegoRoomInfo;
import im.zego.liveaudioroom.refactor.model.ZegoRoomUserRole;
import im.zego.liveaudioroom.refactor.model.ZegoUserInfo;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoUser;
import im.zego.zim.ZIM;
import im.zego.zim.entity.ZIMRoomAdvancedConfig;
import im.zego.zim.entity.ZIMRoomAttributesUpdateInfo;
import im.zego.zim.entity.ZIMRoomInfo;
import im.zego.zim.enums.ZIMConnectionEvent;
import im.zego.zim.enums.ZIMConnectionState;
import im.zego.zim.enums.ZIMErrorCode;
import im.zego.zim.enums.ZIMRoomAttributesUpdateAction;

/**
 * Created by rocket_wang on 2021/12/14.
 */
public class ZegoRoomService {

    private ZegoRoomServiceListener listener;
    // room info object
    public ZegoRoomInfo roomInfo = new ZegoRoomInfo();

    private static final String TAG = "ZegoRoomService";

    // create a room
    public void createRoom(String roomID, String roomName, final String token,
        final ZegoRoomCallback callback) {
        ZegoUserInfo localUserInfo = ZegoRoomManager.getInstance().userService.localUserInfo;
        localUserInfo.setRole(ZegoRoomUserRole.Host);

        roomInfo.setRoomID(roomID);
        roomInfo.setRoomName(roomName);
        roomInfo.setHostID(localUserInfo.getUserID());
        roomInfo.setSeatNum(8);
        roomInfo.setTextMessageDisabled(false);
        roomInfo.setClosed(false);

        ZIMRoomInfo zimRoomInfo = new ZIMRoomInfo();
        zimRoomInfo.roomID = roomID;
        zimRoomInfo.roomName = roomName;

        HashMap<String, String> roomAttributes = new HashMap<>();
        roomAttributes.put(ZegoRoomConstants.KEY_ROOM_INFO, new Gson().toJson(roomInfo));
        ZIMRoomAdvancedConfig config = new ZIMRoomAdvancedConfig();
        config.roomAttributes = roomAttributes;

        ZegoZIMManager.getInstance().zim.createRoom(zimRoomInfo, config, (roomInfo, errorInfo) -> {
            if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                loginRTCRoom(roomID, token, localUserInfo);
                initRoomSeat();
            }
            if (callback != null) {
                callback.roomCallback(errorInfo.code.value());
            }
        });
    }

    // join a room
    public void joinRoom(String roomID, final String token, final ZegoRoomCallback callback) {
        ZegoUserInfo localUserInfo = ZegoRoomManager.getInstance().userService.localUserInfo;
        localUserInfo.setRole(ZegoRoomUserRole.Listener);

        ZegoZIMManager.getInstance().zim.joinRoom(roomID, (roomInfo, errorInfo) -> {
            if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                loginRTCRoom(roomID, token, localUserInfo);
                this.roomInfo.setRoomID(roomInfo.baseInfo.roomID);
                this.roomInfo.setRoomName(roomInfo.baseInfo.roomName);
            }
            if (callback != null) {
                callback.roomCallback(errorInfo.code.value());
            }
        });
    }

    private void initRoomSeat() {
        ZegoSpeakerSeatService speakerSeatService = ZegoRoomManager.getInstance().speakerSeatService;
        if (speakerSeatService != null) {
            speakerSeatService.initRoomSeat();
        }
    }

    private void loginRTCRoom(String roomID, String token, ZegoUserInfo localUserInfo) {
        ZegoUser user = new ZegoUser(localUserInfo.getUserID(), localUserInfo.getUserName());
        ZegoRoomConfig roomConfig = new ZegoRoomConfig();
        roomConfig.token = token;
        ZegoExpressEngine.getEngine().loginRoom(roomID, user, roomConfig);
        ZegoExpressEngine.getEngine().startSoundLevelMonitor(500);
    }

    // leave the room
    public void leaveRoom(final ZegoRoomCallback callback) {
        ZegoSpeakerSeatService seatService = ZegoRoomManager.getInstance().speakerSeatService;
        if (seatService != null) {
            seatService.reset();
        }
        ZegoMessageService messageService = ZegoRoomManager.getInstance().messageService;
        if (messageService != null) {
            messageService.reset();
        }
        ZegoUserService userService = ZegoRoomManager.getInstance().userService;
        if (userService != null) {
            userService.leaveRoom();
        }
        ZegoZIMManager.getInstance().zim.leaveRoom(roomInfo.getRoomID(), errorInfo -> {
            if (callback != null) {
                callback.roomCallback(errorInfo.code.value());
            }
        });
        ZegoExpressEngine.getEngine().stopSoundLevelMonitor();
        ZegoExpressEngine.getEngine().stopPublishingStream();
        ZegoExpressEngine.getEngine().logoutRoom(roomInfo.getRoomID());
    }

    // query the number of chat rooms available online
    public void queryOnlineRoomUsers(final ZegoOnlineRoomUsersCallback callback) {
        ZegoZIMManager.getInstance().zim
            .queryRoomOnlineMemberCount(roomInfo.getRoomID(), (count, errorInfo) -> {
                if (callback != null) {
                    callback.userCountCallback(errorInfo.code.value(), count);
                }
            });
    }

    // disable text chat for all
    public void disableTextMessage(boolean isMuted, ZegoRoomCallback callback) {
        ZegoZIMManager.getInstance().zim.setRoomAttributes(
            ZegoRoomAttributesHelper.getRoomConfigByTextMessage(isMuted, roomInfo),
            roomInfo.getRoomID(),
            ZegoRoomAttributesHelper.getAttributesSetConfig(), errorInfo -> {
                if (callback != null) {
                    callback.roomCallback(errorInfo.code.value());
                }
            });
    }

    public void setListener(ZegoRoomServiceListener listener) {
        this.listener = listener;
    }

    /**
     *
     * @param zim
     * @param info
     * @param roomID
     */
    public void onRoomAttributesUpdated(ZIM zim, ZIMRoomAttributesUpdateInfo info, String roomID) {
        if (info.action == ZIMRoomAttributesUpdateAction.SET) {
            Set<String> keys = info.roomAttributes.keySet();
            for (String key : keys) {
                if (key.equals(ZegoRoomConstants.KEY_ROOM_INFO)) {
                    ZegoRoomInfo roomInfo = new Gson().fromJson(info.roomAttributes.get(key), ZegoRoomInfo.class);
                    boolean firstInit = (this.roomInfo.getSeatNum() == 0);
                    this.roomInfo = roomInfo;
                    if (firstInit) {
                        initRoomSeat();
                    }
                    if (listener != null) {
                        listener.onReceiveRoomInfoUpdate(roomInfo);
                    }
                }
            }
        } else {
            if (listener != null) {
                listener.onReceiveRoomInfoUpdate(null);
            }
        }
    }

    public void onConnectionStateChanged(ZIM zim, ZIMConnectionState state, ZIMConnectionEvent event,
        JSONObject extendedData) {
        if (listener != null) {
            listener.onConnectionStateChanged(state, event);
        }
    }
}