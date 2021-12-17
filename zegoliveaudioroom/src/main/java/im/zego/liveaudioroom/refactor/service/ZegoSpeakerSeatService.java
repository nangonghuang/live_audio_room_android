package im.zego.liveaudioroom.refactor.service;

import static im.zego.liveaudioroom.emus.ZegoLiveAudioRoomErrorCode.SUCCESS;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomErrorCode;
import im.zego.liveaudioroom.refactor.ZegoRoomManager;
import im.zego.liveaudioroom.refactor.ZegoZIMManager;
import im.zego.liveaudioroom.refactor.callback.ZegoRoomCallback;
import im.zego.liveaudioroom.refactor.callback.ZegoSpeakerSeatServiceCallback;
import im.zego.liveaudioroom.refactor.model.ZegoNetWorkQuality;
import im.zego.liveaudioroom.refactor.model.ZegoSpeakerSeatModel;
import im.zego.liveaudioroom.refactor.model.ZegoSpeakerSeatStatus;
import im.zego.liveaudioroom.refactor.model.ZegoUserInfo;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.constants.ZegoStreamQualityLevel;
import im.zego.zim.ZIM;
import im.zego.zim.entity.ZIMRoomAttributesSetConfig;
import im.zego.zim.entity.ZIMRoomAttributesUpdateInfo;
import im.zego.zim.enums.ZIMErrorCode;

/**
 * user interface to manager speaker seat.
 */
public class ZegoSpeakerSeatService {

    private static final String TAG = "SpeakerSeatService";

    private List<ZegoSpeakerSeatModel> speakerSeatList;
    private ZegoSpeakerSeatServiceCallback speakerSeatServiceCallback;

    public ZegoSpeakerSeatService() {
        speakerSeatList = new ArrayList<>();
    }

    public void setSpeakerSeatServiceCallback(ZegoSpeakerSeatServiceCallback callback) {
        this.speakerSeatServiceCallback = callback;
    }

    /**
     * remove user from seat,make it unTaken status.
     *
     * @param seatIndex index
     * @param callback  operation result callback
     */
    public void removeUserFromSeat(int seatIndex, ZegoRoomCallback callback) {
        boolean isOccupied = isSeatOccupied(seatIndex);
        if (!isOccupied) {
            callback.roomCallback(ZegoLiveAudioRoomErrorCode.NOT_IN_SEAT.getValue());
        } else {
            changeSeatStatus(seatIndex, "", false, ZegoSpeakerSeatStatus.Untaken, callback);
        }
    }

    /**
     * close all unused seat.
     *
     * @param isClose  close or not
     * @param callback operation result callback
     */
    public void closeAllSeat(boolean isClose, ZegoRoomCallback callback) {
        ZegoSpeakerSeatStatus status;
        if (isClose) {
            status = ZegoSpeakerSeatStatus.Closed;
        } else {
            status = ZegoSpeakerSeatStatus.Untaken;
        }
        int seatNum = ZegoRoomManager.getInstance().roomService.roomInfo.getSeatNum();
        for (int i = 0; i < seatNum; i++) {
            changeSeatStatus(i, "", false, status, callback);
        }
    }

    /**
     * close specific speaker seat,make it Closed or Untaken.
     *
     * @param isClose   close or not
     * @param seatIndex seat index
     * @param callback  operation result callback
     */
    public void closeSeat(boolean isClose, int seatIndex, ZegoRoomCallback callback) {
        ZegoSpeakerSeatStatus status;
        if (isClose) {
            status = ZegoSpeakerSeatStatus.Closed;
        } else {
            status = ZegoSpeakerSeatStatus.Untaken;
        }
        changeSeatStatus(seatIndex, "", false, status, callback);
    }

    /**
     * mute self's mic and broadcast to all room users.
     *
     * @param isMuted  micPhone state
     * @param callback operation result callback
     */
    public void muteMic(boolean isMuted, ZegoRoomCallback callback) {
        int mySeatIndex = findMySeatIndex();
        if (mySeatIndex == -1) {
            callback.roomCallback(ZegoLiveAudioRoomErrorCode.NOT_IN_SEAT.getValue());
        } else {
            ZegoSpeakerSeatModel speakerSeatModel = speakerSeatList.get(mySeatIndex);
            changeSeatStatus(mySeatIndex, speakerSeatModel.userID, isMuted, speakerSeatModel.status,
                callback);
        }
    }

    /**
     * take a specific speaker seat.
     *
     * @param seatIndex seatIndex to take
     * @param callback  operation result callback
     */
    public void takeSeat(int seatIndex, ZegoRoomCallback callback) {
        ZegoUserInfo selfUserInfo = ZegoRoomManager.getInstance().userService.localUserInfo;
        if (TextUtils.isEmpty(selfUserInfo.getUserID())) {
            callback.roomCallback(ZegoLiveAudioRoomErrorCode.ERROR.getValue());
            return;
        }
        if (isSeatAvailable(seatIndex)) {
            changeSeatStatus(seatIndex, selfUserInfo.getUserID(), false,
                ZegoSpeakerSeatStatus.Occupied, callback);
        } else {
            callback.roomCallback(ZegoLiveAudioRoomErrorCode.SEAT_EXISTED.getValue());
        }
    }

    /**
     * leave speaker seat.
     *
     * @param callback operation result callback
     */
    public void leaveSeat(ZegoRoomCallback callback) {
        int mySeatIndex = findMySeatIndex();
        if (mySeatIndex == -1) {
            callback.roomCallback(ZegoLiveAudioRoomErrorCode.NOT_IN_SEAT.getValue());
        } else {
            ZegoSpeakerSeatModel speakerSeatModel = speakerSeatList.get(mySeatIndex);
            changeSeatStatus(mySeatIndex, "", speakerSeatModel.isMicMuted,
                ZegoSpeakerSeatStatus.Untaken, callback);
        }
    }

    /**
     * switch seat from current to toSeatIndex.
     *
     * @param toSeatIndex seat index to switch to
     * @param callback    operation result callback
     */
    public void switchSeat(int toSeatIndex, ZegoRoomCallback callback) {
        int mySeatIndex = findMySeatIndex();
        if (mySeatIndex == -1) {
            callback.roomCallback(ZegoLiveAudioRoomErrorCode.NOT_IN_SEAT.getValue());
        } else {
            ZegoSpeakerSeatModel speakerSeatModel1 = new ZegoSpeakerSeatModel();
            speakerSeatModel1.userID = "";
            speakerSeatModel1.seatIndex = mySeatIndex;
            speakerSeatModel1.isMicMuted = false;
            speakerSeatModel1.status = ZegoSpeakerSeatStatus.Untaken;
            final String modelString1 = new Gson().toJson(speakerSeatModel1);

            ZegoSpeakerSeatModel currentSeat = speakerSeatList.get(mySeatIndex);
            ZegoSpeakerSeatModel speakerSeatModel2 = new ZegoSpeakerSeatModel();
            speakerSeatModel2.userID = currentSeat.userID;
            speakerSeatModel2.seatIndex = toSeatIndex;
            speakerSeatModel2.isMicMuted = currentSeat.isMicMuted;
            speakerSeatModel2.status = ZegoSpeakerSeatStatus.Occupied;
            final String modelString2 = new Gson().toJson(speakerSeatModel2);

            ZIMRoomAttributesSetConfig setConfig = new ZIMRoomAttributesSetConfig();
            setConfig.isForce = false;
            setConfig.isDeleteAfterOwnerLeft = true;

            HashMap<String, String> seatAttributes = new HashMap<>();
            seatAttributes.put("seat_" + mySeatIndex, modelString1);
            seatAttributes.put("seat_" + toSeatIndex, modelString2);

            String roomID = ZegoRoomManager.getInstance().roomService.roomInfo.getRoomID();
            Log.d(TAG, "switchSeat() called with: seatAttributes = [" + seatAttributes + "]");

            ZegoZIMManager.getInstance().zim.setRoomAttributes(seatAttributes, roomID, setConfig,
                errorInfo -> {
                    ZegoLiveAudioRoomErrorCode errorCode;
                    if (errorInfo.code.equals(ZIMErrorCode.SUCCESS)) {
                        errorCode = SUCCESS;
                        speakerSeatList.set(mySeatIndex, speakerSeatModel1);
                        speakerSeatList.set(toSeatIndex, speakerSeatModel2);
                        ZegoExpressEngine.getEngine().muteMicrophone(speakerSeatModel2.isMicMuted);
                    } else {
                        errorCode = ZegoLiveAudioRoomErrorCode.SET_SEAT_INFO_FAILED;
                    }
                    callback.roomCallback(errorCode.getValue());
                });
        }
    }

    private void changeSeatStatus(int seatIndex, String userID, boolean muteMic,
        ZegoSpeakerSeatStatus status, ZegoRoomCallback callback) {
        Log.d(TAG,
            "changeSeatStatus() called with: seatIndex = [" + seatIndex + "], userID = [" + userID
                + "], muteMic = [" + muteMic + "], status = [" + status + "], callback = ["
                + callback + "]");

        ZegoSpeakerSeatModel speakerSeatModel = new ZegoSpeakerSeatModel();
        speakerSeatModel.userID = userID;
        speakerSeatModel.seatIndex = seatIndex;
        speakerSeatModel.isMicMuted = muteMic;
        speakerSeatModel.status = status;
        String modelString = new Gson().toJson(speakerSeatModel);

        ZIMRoomAttributesSetConfig setConfig = new ZIMRoomAttributesSetConfig();
        setConfig.isForce = false;
        setConfig.isDeleteAfterOwnerLeft = true;

        HashMap<String, String> seatAttributes = new HashMap<>();
        seatAttributes.put("seat_" + seatIndex, modelString);

        String roomID = ZegoRoomManager.getInstance().roomService.roomInfo.getRoomID();

        Log.d(TAG, "changeSeatStatus() called with: seatAttributes = [" + seatAttributes + "]");

        ZegoZIMManager.getInstance().zim.setRoomAttributes(seatAttributes, roomID, setConfig,
            errorInfo -> {
                ZegoLiveAudioRoomErrorCode errorCode;
                if (errorInfo.code.equals(ZIMErrorCode.SUCCESS)) {
                    errorCode = SUCCESS;
                    speakerSeatList.set(seatIndex, speakerSeatModel);
                    ZegoExpressEngine.getEngine().muteMicrophone(speakerSeatModel.isMicMuted);
                } else {
                    errorCode = ZegoLiveAudioRoomErrorCode.SET_SEAT_INFO_FAILED;
                }
                callback.roomCallback(errorCode.getValue());
            });
    }

    private void onSpeakerSeatStatusChanged(ZegoSpeakerSeatModel updateModel) {
        ZegoSpeakerSeatModel speakerSeatModel = speakerSeatList.get(updateModel.seatIndex);
        speakerSeatModel.userID = updateModel.userID;
        speakerSeatModel.seatIndex = updateModel.seatIndex;
        speakerSeatModel.isMicMuted = updateModel.isMicMuted;
        speakerSeatModel.status = updateModel.status;
        if (speakerSeatServiceCallback != null) {
            speakerSeatServiceCallback.onSpeakerSeatUpdate(speakerSeatModel);
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

    public void onRoomAttributesUpdated(ZIM zim, ZIMRoomAttributesUpdateInfo info,
        String roomID) {
        Gson gson = new Gson();
        HashMap<String, String> roomAttributes = info.roomAttributes;
        for (Entry<String, String> entry : roomAttributes.entrySet()) {
            if (entry.getKey().startsWith("seat_")) {
                String jsonValue = entry.getValue();
                ZegoSpeakerSeatModel model = gson.fromJson(jsonValue, ZegoSpeakerSeatModel.class);
                onSpeakerSeatStatusChanged(model);
            }
        }
    }

    void reset() {
        for (ZegoSpeakerSeatModel model : speakerSeatList) {
            model.userID = "";
            model.status = ZegoSpeakerSeatStatus.Untaken;
        }
        speakerSeatServiceCallback = null;
    }

    void initRoomSeat() {
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
                if (speakerSeatServiceCallback != null) {
                    speakerSeatServiceCallback.onSpeakerSeatUpdate(model);
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
            if (model.userID.equals(selfUserInfo.getUserID())) {
                model.soundLevel = soundLevel;
                if (speakerSeatServiceCallback != null) {
                    speakerSeatServiceCallback.onSpeakerSeatUpdate(model);
                }
            }
        }
    }

    public void updateRemoteUsersSoundLevel(HashMap<String, Float> soundLevels) {
        for (ZegoSpeakerSeatModel model : speakerSeatList) {
            Float soundLevel = soundLevels.get(model.userID);
            if (soundLevel != null) {
                model.soundLevel = soundLevel;
                if (speakerSeatServiceCallback != null) {
                    speakerSeatServiceCallback.onSpeakerSeatUpdate(model);
                }
            }
        }
    }
}
