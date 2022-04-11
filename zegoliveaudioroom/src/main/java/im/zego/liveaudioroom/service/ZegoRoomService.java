package im.zego.liveaudioroom.service;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import im.zego.liveaudioroom.ZegoRoomManager;
import im.zego.liveaudioroom.ZegoZIMManager;
import im.zego.liveaudioroom.callback.ZegoRoomCallback;
import im.zego.liveaudioroom.constants.ZegoRoomConstants;
import im.zego.liveaudioroom.helper.ZegoRoomAttributesHelper;
import im.zego.liveaudioroom.listener.ZegoRoomServiceListener;
import im.zego.liveaudioroom.model.ZegoRoomInfo;
import im.zego.liveaudioroom.model.ZegoRoomUserRole;
import im.zego.liveaudioroom.model.ZegoUserInfo;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoUser;
import im.zego.zim.ZIM;
import im.zego.zim.callback.ZIMTokenRenewedCallback;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMRoomAdvancedConfig;
import im.zego.zim.entity.ZIMRoomAttributesUpdateInfo;
import im.zego.zim.entity.ZIMRoomInfo;
import im.zego.zim.enums.ZIMErrorCode;
import im.zego.zim.enums.ZIMRoomAttributesUpdateAction;
import im.zego.zim.enums.ZIMRoomEvent;
import im.zego.zim.enums.ZIMRoomState;

/**
 * Class LiveAudioRoom information management.
 * <p>Description: This class contains the room information management logics, such as the logic of create a room, join
 * a room, leave a room, disable the text chat in room, etc.</>
 */
public class ZegoRoomService {

    /**
     * The listener related to the room status.
     */
    private ZegoRoomServiceListener listener;
    /**
     * Room information, it will be assigned after join the room successfully. And it will be updated synchronously when
     * the room status updates.
     */
    public ZegoRoomInfo roomInfo = new ZegoRoomInfo();

    private static final String TAG = "ZegoRoomService";

    /**
     * Create a room.
     * <p>Description: This method can be used to create a room. The room creator will be the Host by default when the
     * room is created successfully.</>
     * <p>Call this method at: After user logs in </>
     *
     * @param roomID   roomID refers to the room ID, the unique identifier of the room. This is required to join a room
     *                 and cannot be null.
     * @param roomName roomName refers to the room name. This is used for display in the room and cannot be null.
     * @param token    token refers to the authentication token. To get this, see the documentation:
     *                 https://doc-en.zego.im/article/11648
     * @param callback callback refers to the callback for create a room.
     */
    public void createRoom(String roomID, String roomName, final String token,
        final ZegoRoomCallback callback) {
        ZegoUserInfo localUserInfo = ZegoRoomManager.getInstance().userService.localUserInfo;
        localUserInfo.setRole(ZegoRoomUserRole.Host);

        ZegoRoomInfo createRoomInfo = new ZegoRoomInfo();
        createRoomInfo.setRoomID(roomID);
        createRoomInfo.setRoomName(roomName);
        createRoomInfo.setHostID(localUserInfo.getUserID());
        createRoomInfo.setSeatNum(8);
        createRoomInfo.setTextMessageDisabled(false);
        createRoomInfo.setClosed(false);

        ZIMRoomInfo zimRoomInfo = new ZIMRoomInfo();
        zimRoomInfo.roomID = roomID;
        zimRoomInfo.roomName = roomName;

        HashMap<String, String> roomAttributes = new HashMap<>();
        roomAttributes.put(ZegoRoomConstants.KEY_ROOM_INFO, new Gson().toJson(createRoomInfo));
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

    /**
     * Join a room.
     * <p>Description: This method can be used to join a room, the room must be an existing room.</>
     * <p>Call this method at: After user logs in</>
     *
     * @param roomID   refers to the ID of the room you want to join, and cannot be null.
     * @param token    token refers to the authentication token. To get this, see the documentation:
     *                 https://doc-en.zego.im/article/11648
     * @param callback callback refers to the callback for join a room.
     */
    public void joinRoom(String roomID, final String token, final ZegoRoomCallback callback) {
        ZegoUserInfo localUserInfo = ZegoRoomManager.getInstance().userService.localUserInfo;
        localUserInfo.setRole(ZegoRoomUserRole.Listener);

        ZegoZIMManager.getInstance().zim.joinRoom(roomID, (roomInfo, errorInfo) -> {
            if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                loginRTCRoom(roomID, token, localUserInfo);
                this.roomInfo.setRoomID(roomInfo.baseInfo.roomID);
                this.roomInfo.setRoomName(roomInfo.baseInfo.roomName);
                ZegoZIMManager.getInstance().zim.queryRoomAllAttributes(roomID, (roomAttributes, errorInfo2) -> {
                    Set<String> keys = roomAttributes.keySet();
                    for (String key : keys) {
                        if (key.equals(ZegoRoomConstants.KEY_ROOM_INFO)) {
                            this.roomInfo = new Gson().fromJson(roomAttributes.get(key), ZegoRoomInfo.class);
                        }
                    }

                    if (callback != null) {
                        callback.roomCallback(errorInfo2.code.value());
                    }
                });
            } else {
                if (callback != null) {
                    callback.roomCallback(errorInfo.code.value());
                }
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

    /**
     * Leave the room.
     * <p>Description: This method can be used to leave the room you joined. The room will be ended when the Host
     * leaves, and all users in the room will be forced to leave the room.</>
     * <p>Call this method at: After joining a room</>
     *
     * @param callback callback refers to the callback for leave a room.
     */
    public void leaveRoom(final ZegoRoomCallback callback) {
        ZegoSpeakerSeatService seatService = ZegoRoomManager.getInstance().speakerSeatService;
        if (seatService != null) {
            seatService.leaveSeat(errorCode -> {

            });
            seatService.reset();
        }
        ZegoMessageService messageService = ZegoRoomManager.getInstance().messageService;
        if (messageService != null) {
            messageService.reset();
        }
        ZegoUserService userService = ZegoRoomManager.getInstance().userService;
        if (userService != null) {
            userService.reset();
        }
        ZegoGiftService giftService = ZegoRoomManager.getInstance().giftService;
        if (giftService != null) {
            giftService.reset();
        }
        reset();

        ZegoExpressEngine.getEngine().stopSoundLevelMonitor();
        ZegoExpressEngine.getEngine().stopPublishingStream();

        ZegoExpressEngine.getEngine().logoutRoom(roomInfo.getRoomID());

        ZegoZIMManager.getInstance().zim.leaveRoom(roomInfo.getRoomID(), errorInfo -> {
            Log.d(TAG, "leaveRoom() called with: errorInfo = [" + errorInfo.code + "]" + errorInfo.message);
            if (callback != null) {
                callback.roomCallback(errorInfo.code.value());
            }
        });
    }

    public void onRoomTokenWillExpire(int second, String roomID) {
        if (listener != null) {
            listener.onRoomTokenWillExpire(second, roomID);
        }
    }

    void reset() {
        roomInfo.setRoomName("");
        roomInfo.setSeatNum(0);
        roomInfo.setHostID("");
        listener = null;
    }

    /**
     * Disable text chat in the room.
     * <p>Description: This method can be used to disable the text chat in the room.</>
     * <p>Call this method at: After joining a room</>
     *
     * @param disable  refers to the parameter that whether to disable the text chat. To disable the text chat, set it
     *                 to [true]; To allow the text chat, set it to [false].
     * @param callback refers to the callback for disable text chat.
     */
    public void disableTextMessage(boolean disable, ZegoRoomCallback callback) {
        ZegoZIMManager.getInstance().zim.setRoomAttributes(
            ZegoRoomAttributesHelper.getRoomConfigByTextMessage(disable, roomInfo),
            roomInfo.getRoomID(),
            ZegoRoomAttributesHelper.getAttributesSetConfig(), errorInfo -> {
                Log.d(TAG, "disableTextMessage() called with: disable = [" + disable);
                if (callback != null) {
                    callback.roomCallback(errorInfo.code.value());
                }
            });
    }

    public void setListener(ZegoRoomServiceListener listener) {
        this.listener = listener;
    }

    /**
     * @param zim
     * @param info
     * @param roomID
     */
    public void onRoomAttributesUpdated(ZIM zim, ZIMRoomAttributesUpdateInfo info, String roomID) {
        Log.d(TAG,
            "onRoomAttributesUpdated() called with: info.action = [" + info.action + "], info.roomAttributes = ["
                + info.roomAttributes + "], roomID = [" + roomID + "]");
        if (info.action == ZIMRoomAttributesUpdateAction.SET) {
            Set<String> keys = info.roomAttributes.keySet();
            for (String key : keys) {
                if (key.equals(ZegoRoomConstants.KEY_ROOM_INFO)) {
                    ZegoRoomInfo roomInfo = new Gson().fromJson(info.roomAttributes.get(key), ZegoRoomInfo.class);
                    boolean firstInit = (this.roomInfo.getSeatNum() == 0);
                    Log.d(TAG, "onRoomAttributesUpdated: firstInit " + firstInit);
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

    public void onRoomStateChanged(ZIM zim, ZIMRoomState state, ZIMRoomEvent event, JSONObject extendedData,
        String roomID) {
        Log.d(TAG, "onRoomStateChanged() called with: zim = [" + zim + "], state = [" + state + "], event = [" + event
            + "], extendedData = [" + extendedData + "], roomID = [" + roomID + "]");
        if (state == ZIMRoomState.CONNECTED) {
            boolean newInRoom = (this.roomInfo.getSeatNum() == 0);
            if (!newInRoom && !TextUtils.isEmpty(roomID)) {
                ZegoZIMManager.getInstance().zim.queryRoomAllAttributes(roomID, (roomAttributes, errorInfo) -> {
                    boolean hostLeft = errorInfo.getCode() == ZIMErrorCode.SUCCESS
                        && !roomAttributes.keySet().contains(ZegoRoomConstants.KEY_ROOM_INFO);
                    boolean roomNotExisted = errorInfo.getCode() == ZIMErrorCode.ROOM_NOT_EXIST;
                    if (hostLeft || roomNotExisted) {
                        if (listener != null) {
                            listener.onReceiveRoomInfoUpdate(null);
                        }
                    }
                });
            }
        } else if (state == ZIMRoomState.DISCONNECTED) {
            if (listener != null) {
                listener.onReceiveRoomInfoUpdate(null);
            }
        }
    }

    public void onRoomStreamUpdate(String roomID, ZegoUpdateType updateType, List<ZegoStream> streamList) {
        for (ZegoStream zegoStream : streamList) {
            if (updateType == ZegoUpdateType.ADD) {
                ZegoExpressEngine.getEngine().startPlayingStream(zegoStream.streamID, null);
            } else {
                ZegoExpressEngine.getEngine().stopPlayingStream(zegoStream.streamID);
            }
        }
    }

    /**
     * Renew token.
     * <p>
     * Description: After the developer receives [onRoomTokenWillExpire], they can use this API to update the token to ensure that the subsequent RTC&ZIM functions are normal.
     *
     * @param token  The token that needs to be renew.
     * @param roomID Room ID.
     */
    public void renewToken(String token, String roomID) {
        ZegoZIMManager.getInstance().zim.renewToken(token, new ZIMTokenRenewedCallback() {
            @Override
            public void onTokenRenewed(String token, ZIMError errorInfo) {

            }
        });
    }
}