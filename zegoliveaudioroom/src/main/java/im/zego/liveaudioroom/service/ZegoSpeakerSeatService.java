package im.zego.liveaudioroom.service;


import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.lang.math.NumberUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

import im.zego.liveaudioroom.ZegoRoomManager;
import im.zego.liveaudioroom.ZegoZIMManager;
import im.zego.liveaudioroom.callback.ZegoRoomCallback;
import im.zego.liveaudioroom.constants.ZegoRoomErrorCode;
import im.zego.liveaudioroom.helper.CustomTypeAdapterFactory;
import im.zego.liveaudioroom.listener.ZegoSpeakerSeatServiceListener;
import im.zego.liveaudioroom.model.ZegoNetWorkQuality;
import im.zego.liveaudioroom.model.ZegoRoomInfo;
import im.zego.liveaudioroom.model.ZegoRoomUserRole;
import im.zego.liveaudioroom.model.ZegoSpeakerSeatModel;
import im.zego.liveaudioroom.model.ZegoSpeakerSeatStatus;
import im.zego.liveaudioroom.model.ZegoUserInfo;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.constants.ZegoStreamQualityLevel;
import im.zego.zim.ZIM;
import im.zego.zim.callback.ZIMRoomAttributesOperatedCallback;
import im.zego.zim.entity.ZIMRoomAttributesSetConfig;
import im.zego.zim.entity.ZIMRoomAttributesUpdateInfo;
import im.zego.zim.enums.ZIMErrorCode;
import im.zego.zim.enums.ZIMRoomAttributesUpdateAction;

/**
 * Class speaker seat management.
 * <p>Description: This class contains the logics related to speaker seat management, such as take/leave a speaker
 * seat,close a speaker seat, remove user from seat, change speaker seats, etc.</>
 */
public class ZegoSpeakerSeatService {

    private static final String TAG = "SpeakerSeatService";

    /**
     * The speaker seat list.
     */
    private List<ZegoSpeakerSeatModel> speakerSeatList;
    /**
     * The listener related to speaker seat status.
     */
    private ZegoSpeakerSeatServiceListener speakerSeatServiceListener;
    private final Gson gson;

    public ZegoSpeakerSeatService() {
        speakerSeatList = new ArrayList<>();
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapterFactory(new CustomTypeAdapterFactory());
        gson = builder.create();
    }

    public void setListener(ZegoSpeakerSeatServiceListener listener) {
        this.speakerSeatServiceListener = listener;
    }

    /**
     * Remove a user from speaker seat.
     * <p>Description: This method can be used to remove a specified user (except the host) from the speaker seat. </>
     *
     * @param seatIndex refers to the seat index of the user you want to remove.
     * @param callback  refers to the callback for remove a user from the speaker seat.
     */
    public void removeUserFromSeat(int seatIndex, ZegoRoomCallback callback) {
        boolean isOccupied = isSeatOccupied(seatIndex);
        if (!isOccupied) {
            callback.roomCallback(ZegoRoomErrorCode.NOT_IN_SEAT);
        } else {
            changeSeatStatus(seatIndex, "", false, ZegoSpeakerSeatStatus.Untaken, callback);
        }
    }

    /**
     * Close all untaken speaker seat/Open all closed speaker seat.
     * <p>Description: This method can be used to close all untaken seats or open all closed seats. And the status of
     * the isSeatClosed will also be updated automatically.</>
     * <p>Call this method at: After joining the room</>
     *
     * @param isClose  isClose can be used to close all untaken speaker seats.
     * @param callback callback refers to the callback for close all speaker seats.
     */
    public void closeAllSeat(boolean isClose, ZegoRoomCallback callback) {
        ZegoUserInfo localUserInfo = ZegoRoomManager.getInstance().userService.localUserInfo;
        HashMap<String, String> seatAttributes = new HashMap<>();

        for (int i = 0; i < speakerSeatList.size(); i++) {
            ZegoSpeakerSeatModel model = speakerSeatList.get(i);
            if (localUserInfo.getUserID().equals(model.userID)) {
                continue;
            }
            if (isClose) {
                if (model.status == ZegoSpeakerSeatStatus.Untaken) {
                    model.status = ZegoSpeakerSeatStatus.Closed;
                    model.userID = "";
                    model.mic = false;
                    seatAttributes.put(String.valueOf(model.seatIndex), gson.toJson(model));
                }
            } else {
                if (model.status == ZegoSpeakerSeatStatus.Closed) {
                    model.status = ZegoSpeakerSeatStatus.Untaken;
                    model.userID = "";
                    model.mic = false;
                    seatAttributes.put(String.valueOf(model.seatIndex), gson.toJson(model));
                }
            }
        }

        String roomID = ZegoRoomManager.getInstance().roomService.roomInfo.getRoomID();
        ZIMRoomAttributesSetConfig setConfig = new ZIMRoomAttributesSetConfig();
        setConfig.isForce = true;
        setConfig.isDeleteAfterOwnerLeft = true;
        setConfig.isUpdateOwner = true;
        Log.d(TAG, "closeAllSeat() called with: seatAttributes = [" + seatAttributes + "]");

        if (seatAttributes.size() == 0) {
            makeRoomSeatCloseWhenUnTaken(isClose, setConfig, errorInfo1 -> {
                if (errorInfo1.code.equals(ZIMErrorCode.SUCCESS)) {
                    int errorCode = ZegoRoomErrorCode.SUCCESS;
                    callback.roomCallback(errorCode);
                }
            });
        } else {
            ZegoZIMManager.getInstance().zim.setRoomAttributes(seatAttributes, roomID, setConfig, errorInfo -> {
                if (errorInfo.code.equals(ZIMErrorCode.SUCCESS)) {
                    for (String index : seatAttributes.keySet()) {
                        int seatIndex = Integer.parseInt(index);
                        onSpeakerSeatStatusChanged(speakerSeatList.get(seatIndex));
                    }
                    makeRoomSeatCloseWhenUnTaken(isClose, setConfig, errorInfo1 -> {
                        if (errorInfo1.code.equals(ZIMErrorCode.SUCCESS)) {
                            int errorCode = ZegoRoomErrorCode.SUCCESS;
                            callback.roomCallback(errorCode);
                        }
                    });
                } else {
                    for (int i = 0; i < speakerSeatList.size(); i++) {
                        ZegoSpeakerSeatModel model = speakerSeatList.get(i);
                        if (isClose) {
                            if (model.status == ZegoSpeakerSeatStatus.Closed) {
                                model.status = ZegoSpeakerSeatStatus.Untaken;
                                model.userID = "";
                                model.mic = false;
                                seatAttributes.put(String.valueOf(model.seatIndex), gson.toJson(model));
                            }

                        } else {
                            if (model.status == ZegoSpeakerSeatStatus.Untaken) {
                                model.status = ZegoSpeakerSeatStatus.Closed;
                                model.userID = "";
                                model.mic = false;
                                seatAttributes.put(String.valueOf(model.seatIndex), gson.toJson(model));
                            }
                        }
                    }
                    callback.roomCallback(ZegoRoomErrorCode.SET_SEAT_INFO_FAILED);
                }
            });
        }

    }

    private void makeRoomSeatCloseWhenUnTaken(boolean close, ZIMRoomAttributesSetConfig setConfig,
        ZIMRoomAttributesOperatedCallback callback) {
        ZegoRoomInfo roomInfo = ZegoRoomManager.getInstance().roomService.roomInfo;
        roomInfo.setClosed(close);
        HashMap<String, String> roomAttributes = new HashMap<>();
        roomAttributes.put("roomInfo", gson.toJson(roomInfo));
        String roomID = roomInfo.getRoomID();
        ZegoZIMManager.getInstance().zim.setRoomAttributes(roomAttributes, roomID, setConfig, callback);
    }

    @NonNull
    private ZIMRoomAttributesSetConfig getZimRoomAttributesSetConfig() {
        ZIMRoomAttributesSetConfig setConfig = new ZIMRoomAttributesSetConfig();
        setConfig.isForce = true;
        setConfig.isDeleteAfterOwnerLeft = false;
        return setConfig;
    }

    /**
     * lose specified untaken speaker seat/Open specified closed speaker seat.
     * <p>Description: You can call this method to close untaken speaker seats, and the status of the specified speaker
     * seat will change to closed or unused.</>
     * <p>Call this method at: After joining the room</>
     *
     * @param isClosed   can be used to close specified untaken speaker seats.
     * @param seatIndex refers to the seat index of the seat that you want to close/open.
     * @param callback  refers to the callback for close/open specified speaker seats.
     */
    public void convertClosedOpenSeat(boolean isClosed, int seatIndex, ZegoRoomCallback callback) {
        ZegoSpeakerSeatStatus status;
        if (isClosed) {
            status = ZegoSpeakerSeatStatus.Closed;
        } else {
            status = ZegoSpeakerSeatStatus.Untaken;
        }
        changeSeatStatus(seatIndex, "", false, status, callback);
    }

    /**
     * Mute/Unmute your own microphone.
     * <p>Description: This method can be used to mute/unmute your own microphone.</>
     * <p>Call this method at:  After the host enters the room/listener takes a speaker seat</>
     *
     * @param isMuted  isMuted can be set to [true] to mute the microphone; or set it to [false] to unmute the
     *                 microphone.
     * @param callback refers to the callback for mute/unmute the microphone.
     */
    public void muteMic(boolean isMuted, ZegoRoomCallback callback) {
        int mySeatIndex = findMySeatIndex();
        if (mySeatIndex == -1) {
            callback.roomCallback(ZegoRoomErrorCode.NOT_IN_SEAT);
        } else {
            ZegoSpeakerSeatModel speakerSeatModel = speakerSeatList.get(mySeatIndex);
            changeSeatStatus(mySeatIndex, speakerSeatModel.userID, !isMuted, speakerSeatModel.status,
                callback);
        }
    }

    /**
     * Take the speaker seat.
     * <p>Description: This method can be used to help a listener to take a speaker seat to speak. And at the same
     * time,the microphone will be enabled, the audio streams will be published.</>
     * <p>Call this method at:  After joining the room</>
     *
     * @param seatIndex seatIndex to take
     * @param callback  operation result callback
     */
    public void takeSeat(int seatIndex, ZegoRoomCallback callback) {
        ZegoUserInfo selfUserInfo = ZegoRoomManager.getInstance().userService.localUserInfo;
        if (TextUtils.isEmpty(selfUserInfo.getUserID())) {
            callback.roomCallback(ZegoRoomErrorCode.ERROR);
            return;
        }
        if (isSeatAvailable(seatIndex)) {
            changeSeatStatus(seatIndex, selfUserInfo.getUserID(), true, ZegoSpeakerSeatStatus.Occupied, callback);
        } else {
            callback.roomCallback(ZegoRoomErrorCode.SEAT_EXISTED);
        }
    }

    @NonNull
    private String getSelfStreamID() {
        String selfUserID = ZegoRoomManager.getInstance().userService.localUserInfo.getUserID();
        String roomID = ZegoRoomManager.getInstance().roomService.roomInfo.getRoomID();
        return String.format("%s_%s_%s", roomID, selfUserID, "main");
    }

    /**
     * leave the speaker seat.
     * <p>Description: This method can be used to help a speaker to leave the speaker seat to become a listener again.
     * And at the same time, the microphone will be disabled, the audio stream publishing will be stopped.</>
     * <p>Call this method at:  After the listener takes a speaker seat</>
     *
     * @param callback refers to the callback for leave the speaker seat.
     */
    public void leaveSeat(ZegoRoomCallback callback) {
        int mySeatIndex = findMySeatIndex();
        if (mySeatIndex == -1) {
            callback.roomCallback(ZegoRoomErrorCode.NOT_IN_SEAT);
        } else {
            ZegoSpeakerSeatModel speakerSeatModel = speakerSeatList.get(mySeatIndex);
            changeSeatStatus(mySeatIndex, "", speakerSeatModel.mic, ZegoSpeakerSeatStatus.Untaken, callback);
        }
    }

    /**
     * Change the speaker seats.
     * <p>Description: This method can be used for users to change from the current speaker seat to another speaker
     * seat, and make the current seat available.</>
     * <p>Call this method at: After the listener takes a speaker seat</>
     *
     * @param toSeatIndex refers to the seat index of the seat that you want to switch to, you can only change to the
     *                    open and untaken speaker seats.
     * @param callback    refers to the callback for change the speaker seats.
     */
    public void switchSeat(int toSeatIndex, ZegoRoomCallback callback) {
        int mySeatIndex = findMySeatIndex();
        if (mySeatIndex == -1) {
            callback.roomCallback(ZegoRoomErrorCode.NOT_IN_SEAT);
        } else {
            ZegoSpeakerSeatModel speakerSeatModel1 = new ZegoSpeakerSeatModel();
            speakerSeatModel1.userID = "";
            speakerSeatModel1.seatIndex = mySeatIndex;
            speakerSeatModel1.mic = false;
            speakerSeatModel1.status = ZegoSpeakerSeatStatus.Untaken;
            final String modelString1 = gson.toJson(speakerSeatModel1);

            ZegoSpeakerSeatModel currentSeat = speakerSeatList.get(mySeatIndex);
            ZegoSpeakerSeatModel speakerSeatModel2 = new ZegoSpeakerSeatModel();
            speakerSeatModel2.userID = currentSeat.userID;
            speakerSeatModel2.seatIndex = toSeatIndex;
            speakerSeatModel2.mic = currentSeat.mic;
            speakerSeatModel2.status = ZegoSpeakerSeatStatus.Occupied;
            final String modelString2 = gson.toJson(speakerSeatModel2);

            HashMap<String, String> seatAttributes = new HashMap<>();
            seatAttributes.put(String.valueOf(mySeatIndex), modelString1);
            seatAttributes.put(String.valueOf(toSeatIndex), modelString2);

            ZIMRoomAttributesSetConfig setConfig = getZimRoomAttributesSetConfig();
            String roomID = ZegoRoomManager.getInstance().roomService.roomInfo.getRoomID();
            Log.d(TAG, "switchSeat() called with: seatAttributes = [" + seatAttributes + "]");

            ZegoZIMManager.getInstance().zim.setRoomAttributes(seatAttributes, roomID, setConfig, errorInfo -> {
                int errorCode;
                if (errorInfo.code.equals(ZIMErrorCode.SUCCESS)) {
                    errorCode = ZegoRoomErrorCode.SUCCESS;
                    onSpeakerSeatStatusChanged(speakerSeatModel1);
                    onSpeakerSeatStatusChanged(speakerSeatModel2);
                } else {
                    errorCode = ZegoRoomErrorCode.SET_SEAT_INFO_FAILED;
                }
                callback.roomCallback(errorCode);
            });
        }
    }

    private void changeSeatStatus(int seatIndex, String userID, boolean micStatus,
        ZegoSpeakerSeatStatus status, ZegoRoomCallback callback) {

        ZegoSpeakerSeatModel speakerSeatModel = new ZegoSpeakerSeatModel();
        speakerSeatModel.userID = userID;
        speakerSeatModel.seatIndex = seatIndex;
        speakerSeatModel.mic = micStatus;
        speakerSeatModel.status = status;
        ZegoRoomService roomService = ZegoRoomManager.getInstance().roomService;
        boolean closed = roomService.roomInfo.isClosed();
        ZegoSpeakerSeatStatus currentStatus = speakerSeatList.get(seatIndex).status;
        if (currentStatus == ZegoSpeakerSeatStatus.Occupied && status == ZegoSpeakerSeatStatus.Untaken) {
            // is leave seat action ,make it closed
            if (closed) {
                speakerSeatModel.status = ZegoSpeakerSeatStatus.Closed;
            }
        }
        String modelString = gson.toJson(speakerSeatModel);

        HashMap<String, String> seatAttributes = new HashMap<>();
        seatAttributes.put(String.valueOf(seatIndex), modelString);

        String roomID = roomService.roomInfo.getRoomID();
        ZIMRoomAttributesSetConfig setConfig = getZimRoomAttributesSetConfig();

        Log.d(TAG, "changeSeatStatus() called with: seatAttributes = [" + seatAttributes + "]");

        ZegoZIMManager.getInstance().zim.setRoomAttributes(seatAttributes, roomID, setConfig, errorInfo -> {
            int errorCode;
            if (errorInfo.code.equals(ZIMErrorCode.SUCCESS)) {
                errorCode = ZegoRoomErrorCode.SUCCESS;
                onSpeakerSeatStatusChanged(speakerSeatModel);
            } else {
                errorCode = ZegoRoomErrorCode.SET_SEAT_INFO_FAILED;
            }
            callback.roomCallback(errorCode);
        });
    }

    private void onSpeakerSeatStatusChanged(ZegoSpeakerSeatModel updateModel) {
        ZegoUserService userService = ZegoRoomManager.getInstance().userService;
        ZegoRoomService roomService = ZegoRoomManager.getInstance().roomService;
        ZegoUserInfo selfUserInfo = userService.localUserInfo;
        List<String> seatedUserListBefore = getSeatedUserList();
        if (speakerSeatList.size() > updateModel.seatIndex) {
            ZegoSpeakerSeatModel model = speakerSeatList.get(updateModel.seatIndex);
            String oldUserID = model.userID;
            model.userID = updateModel.userID;
            model.seatIndex = updateModel.seatIndex;
            model.mic = updateModel.mic;
            model.status = updateModel.status;

            List<String> seatedUserListNow = getSeatedUserList();
            // all involved user set to Listener,and the update Role
            for (String userID : seatedUserListBefore) {
                ZegoUserInfo userInfo = userService.getUserInfo(userID);
                if (userInfo != null && !Objects.equals(userID, roomService.roomInfo.getHostID())) {
                    userInfo.setRole(ZegoRoomUserRole.Listener);
                }
            }
            for (String userID : seatedUserListNow) {
                ZegoUserInfo userInfo = userService.getUserInfo(userID);
                if (userInfo != null && !Objects.equals(userID, roomService.roomInfo.getHostID())) {
                    userInfo.setRole(ZegoRoomUserRole.Speaker);
                }
            }

            boolean myStateBefore = seatedUserListBefore.contains(selfUserInfo.getUserID());
            boolean myStateNow = seatedUserListNow.contains(selfUserInfo.getUserID());
            if (myStateNow) {
                int mySeatIndex = findMySeatIndex();
                ZegoSpeakerSeatModel seatModel = speakerSeatList.get(mySeatIndex);
                ZegoExpressEngine.getEngine().muteMicrophone(!seatModel.mic);
                ZegoExpressEngine.getEngine().startPublishingStream(getSelfStreamID());
            } else {
                ZegoExpressEngine.getEngine().muteMicrophone(true);
                ZegoExpressEngine.getEngine().stopPublishingStream();
            }

            if (speakerSeatServiceListener != null) {
                speakerSeatServiceListener.onSpeakerSeatUpdate(model);
            }
        }
    }

    private int findMySeatIndex() {
        ZegoUserInfo selfUserInfo = ZegoRoomManager.getInstance().userService.localUserInfo;
        if (TextUtils.isEmpty(selfUserInfo.getUserID())) {
            return -1;
        }
        for (int i = 0; i < speakerSeatList.size(); i++) {
            ZegoSpeakerSeatModel speakerSeatModel = speakerSeatList.get(i);
            if (speakerSeatModel.userID.equals(selfUserInfo.getUserID())
                && speakerSeatModel.status == ZegoSpeakerSeatStatus.Occupied) {
                return i;
            }
        }
        return -1;
    }

    private boolean isSeatOccupied(int seatIndex) {
        ZegoSpeakerSeatModel speakerSeatModel = speakerSeatList.get(seatIndex);
        return (speakerSeatModel.status == ZegoSpeakerSeatStatus.Occupied);
    }

    private boolean isSeatAvailable(int seatIndex) {
        ZegoSpeakerSeatModel speakerSeatModel = speakerSeatList.get(seatIndex);
        return (speakerSeatModel.status == ZegoSpeakerSeatStatus.Untaken);
    }

    public void onRoomAttributesUpdated(ZIM zim, ZIMRoomAttributesUpdateInfo info, String roomID) {
        HashMap<String, String> roomAttributes = info.roomAttributes;
        if (info.action == ZIMRoomAttributesUpdateAction.SET) {
            for (Entry<String, String> entry : roomAttributes.entrySet()) {
                if (NumberUtils.isNumber(entry.getKey())) {
                    String jsonValue = entry.getValue();
                    ZegoSpeakerSeatModel model = gson.fromJson(jsonValue, ZegoSpeakerSeatModel.class);
                    onSpeakerSeatStatusChanged(model);
                }
            }
        }
    }

    void reset() {
        speakerSeatList.clear();
        speakerSeatServiceListener = null;
    }

    void initRoomSeat() {
        speakerSeatList.clear();
        int seatNum = ZegoRoomManager.getInstance().roomService.roomInfo.getSeatNum();
        for (int i = 0; i < seatNum; i++) {
            ZegoSpeakerSeatModel model = new ZegoSpeakerSeatModel();
            model.userID = "";
            model.seatIndex = i;
            model.status = ZegoSpeakerSeatStatus.Untaken;
            model.network = ZegoNetWorkQuality.Good;
            speakerSeatList.add(model);
        }
    }

    public void onNetworkQuality(String userID, ZegoStreamQualityLevel upstreamQuality,
        ZegoStreamQualityLevel downstreamQuality) {
        ZegoNetWorkQuality quality;
        if (upstreamQuality == ZegoStreamQualityLevel.EXCELLENT
            || upstreamQuality == ZegoStreamQualityLevel.GOOD) {
            quality = ZegoNetWorkQuality.Good;
        } else if (upstreamQuality == ZegoStreamQualityLevel.MEDIUM) {
            quality = ZegoNetWorkQuality.Medium;
        } else {
            quality = ZegoNetWorkQuality.Bad;
        }

        for (ZegoSpeakerSeatModel model : speakerSeatList) {
            if (model.userID.equals(userID)) {
                model.network = quality;
                if (speakerSeatServiceListener != null) {
                    speakerSeatServiceListener.onSpeakerSeatUpdate(model);
                }
            }
        }
    }

    public List<ZegoSpeakerSeatModel> getSpeakerSeatList() {
        return speakerSeatList;
    }

    /**
     * get userID list in speaker seat.
     *
     * @return userID list in speaker seat.
     */
    public List<String> getSeatedUserList() {
        List<String> seatedUserList = new ArrayList<>();
        for (ZegoSpeakerSeatModel speakerSeatModel : speakerSeatList) {
            if (speakerSeatModel.status == ZegoSpeakerSeatStatus.Occupied
                && !TextUtils.isEmpty(speakerSeatModel.userID)) {
                seatedUserList.add(speakerSeatModel.userID);
            }
        }
        return seatedUserList;
    }

    public void updateLocalUserSoundLevel(float soundLevel) {
        ZegoUserInfo selfUserInfo = ZegoRoomManager.getInstance().userService.localUserInfo;
        for (ZegoSpeakerSeatModel model : speakerSeatList) {
            if (selfUserInfo.getUserID().equals(model.userID)) {
                model.soundLevel = soundLevel;
                if (speakerSeatServiceListener != null) {
                    speakerSeatServiceListener.onSpeakerSeatUpdate(model);
                }
            }
        }
    }

    public void updateRemoteUsersSoundLevel(HashMap<String, Float> soundLevels) {
        for (ZegoSpeakerSeatModel model : speakerSeatList) {
            for (Entry<String, Float> entry : soundLevels.entrySet()) {
                if (entry.getKey().contains(model.userID)) {
                    Float soundLevel = entry.getValue();
                    if (soundLevel != null) {
                        model.soundLevel = soundLevel;
                        if (speakerSeatServiceListener != null) {
                            speakerSeatServiceListener.onSpeakerSeatUpdate(model);
                        }
                    }
                }
            }
        }
    }
}
