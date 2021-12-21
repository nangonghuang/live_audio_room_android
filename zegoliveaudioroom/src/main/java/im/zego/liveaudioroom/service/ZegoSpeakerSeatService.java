package im.zego.liveaudioroom.service;


import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.gson.Gson;
import im.zego.liveaudioroom.ZegoRoomManager;
import im.zego.liveaudioroom.ZegoZIMManager;
import im.zego.liveaudioroom.callback.ZegoRoomCallback;
import im.zego.liveaudioroom.constants.ZegoRoomErrorCode;
import im.zego.liveaudioroom.listener.ZegoSpeakerSeatServiceListener;
import im.zego.liveaudioroom.model.ZegoNetWorkQuality;
import im.zego.liveaudioroom.model.ZegoSpeakerSeatModel;
import im.zego.liveaudioroom.model.ZegoSpeakerSeatStatus;
import im.zego.liveaudioroom.model.ZegoUserInfo;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.constants.ZegoStreamQualityLevel;
import im.zego.zim.ZIM;
import im.zego.zim.entity.ZIMRoomAttributesSetConfig;
import im.zego.zim.entity.ZIMRoomAttributesUpdateInfo;
import im.zego.zim.enums.ZIMErrorCode;
import im.zego.zim.enums.ZIMRoomAttributesUpdateAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import org.apache.commons.lang.math.NumberUtils;

/**
 * user interface to manager speaker seat.
 */
public class ZegoSpeakerSeatService {

    private static final String TAG = "SpeakerSeatService";

    private List<ZegoSpeakerSeatModel> speakerSeatList;
    private ZegoSpeakerSeatServiceListener speakerSeatServiceListener;

    public ZegoSpeakerSeatService() {
        speakerSeatList = new ArrayList<>();
    }

    public void setListener(ZegoSpeakerSeatServiceListener listener) {
        this.speakerSeatServiceListener = listener;
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
            callback.roomCallback(ZegoRoomErrorCode.NOT_IN_SEAT);
        } else {
            changeSeatStatus(seatIndex, "", true, ZegoSpeakerSeatStatus.Untaken, callback);
        }
    }

    /**
     * close all unused seat.
     *
     * @param isClose  close or not
     * @param callback operation result callback
     */
    public void closeAllSeat(boolean isClose, ZegoRoomCallback callback) {
        ZegoUserInfo localUserInfo = ZegoRoomManager.getInstance().userService.localUserInfo;
        HashMap<String, String> seatAttributes = new HashMap<>();
        Gson gson = new Gson();
        for (int i = 0; i < speakerSeatList.size(); i++) {
            ZegoSpeakerSeatModel model = speakerSeatList.get(i);
            if (localUserInfo.getUserID().equals(model.userID)) {
                continue;
            }
            if (isClose) {
                if (model.status == ZegoSpeakerSeatStatus.Untaken) {
                    model.status = ZegoSpeakerSeatStatus.Closed;
                    model.userID = "";
                    model.isMicMuted = true;
                    seatAttributes.put(String.valueOf(model.seatIndex), gson.toJson(model));
                }
            } else {
                if (model.status == ZegoSpeakerSeatStatus.Closed) {
                    model.status = ZegoSpeakerSeatStatus.Untaken;
                    model.userID = "";
                    model.isMicMuted = true;
                    seatAttributes.put(String.valueOf(model.seatIndex), gson.toJson(model));
                }
            }
        }

        String roomID = ZegoRoomManager.getInstance().roomService.roomInfo.getRoomID();
        ZIMRoomAttributesSetConfig setConfig = getZimRoomAttributesSetConfig();
        Log.d(TAG, "closeAllSeat() called with: seatAttributes = [" + seatAttributes + "]");

        ZegoZIMManager.getInstance().zim.setRoomAttributes(seatAttributes, roomID, setConfig, errorInfo -> {
            int errorCode;
            if (errorInfo.code.equals(ZIMErrorCode.SUCCESS)) {
                errorCode = ZegoRoomErrorCode.SUCCESS;
                for (String index : seatAttributes.keySet()) {
                    int seatIndex = Integer.parseInt(index);
                    onSpeakerSeatStatusChanged(speakerSeatList.get(seatIndex));
                }
            } else {
                for (int i = 0; i < speakerSeatList.size(); i++) {
                    ZegoSpeakerSeatModel model = speakerSeatList.get(i);
                    if (isClose) {
                        if (model.status == ZegoSpeakerSeatStatus.Closed) {
                            model.status = ZegoSpeakerSeatStatus.Untaken;
                            model.userID = "";
                            model.isMicMuted = true;
                            seatAttributes.put(String.valueOf(model.seatIndex), gson.toJson(model));
                        }

                    } else {
                        if (model.status == ZegoSpeakerSeatStatus.Untaken) {
                            model.status = ZegoSpeakerSeatStatus.Closed;
                            model.userID = "";
                            model.isMicMuted = true;
                            seatAttributes.put(String.valueOf(model.seatIndex), gson.toJson(model));
                        }
                    }
                }
                errorCode = ZegoRoomErrorCode.SET_SEAT_INFO_FAILED;
            }
            callback.roomCallback(errorCode);
        });
    }

    @NonNull
    private ZIMRoomAttributesSetConfig getZimRoomAttributesSetConfig() {
        ZIMRoomAttributesSetConfig setConfig = new ZIMRoomAttributesSetConfig();
        setConfig.isForce = true;
        setConfig.isDeleteAfterOwnerLeft = false;
        return setConfig;
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
        changeSeatStatus(seatIndex, "", true, status, callback);
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
            callback.roomCallback(ZegoRoomErrorCode.NOT_IN_SEAT);
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
            callback.roomCallback(ZegoRoomErrorCode.ERROR);
            return;
        }
        if (isSeatAvailable(seatIndex)) {
            changeSeatStatus(seatIndex, selfUserInfo.getUserID(), false, ZegoSpeakerSeatStatus.Occupied, errorCode -> {
                if (errorCode == ZegoRoomErrorCode.SUCCESS) {
                    ZegoExpressEngine.getEngine().startPublishingStream(getSelfStreamID());
                }
                callback.roomCallback(errorCode);
            });
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
     * leave speaker seat.
     *
     * @param callback operation result callback
     */
    public void leaveSeat(ZegoRoomCallback callback) {
        int mySeatIndex = findMySeatIndex();
        if (mySeatIndex == -1) {
            callback.roomCallback(ZegoRoomErrorCode.NOT_IN_SEAT);
        } else {
            ZegoSpeakerSeatModel speakerSeatModel = speakerSeatList.get(mySeatIndex);
            changeSeatStatus(mySeatIndex, "", speakerSeatModel.isMicMuted, ZegoSpeakerSeatStatus.Untaken, errorCode -> {
                if (errorCode == ZegoRoomErrorCode.SUCCESS) {
                    ZegoExpressEngine.getEngine().stopPublishingStream();
                }
                callback.roomCallback(errorCode);
            });
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
            callback.roomCallback(ZegoRoomErrorCode.NOT_IN_SEAT);
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
                    speakerSeatList.set(mySeatIndex, speakerSeatModel1);
                    speakerSeatList.set(toSeatIndex, speakerSeatModel2);
                    onSpeakerSeatStatusChanged(speakerSeatModel1);
                    onSpeakerSeatStatusChanged(speakerSeatModel2);
                } else {
                    errorCode = ZegoRoomErrorCode.SET_SEAT_INFO_FAILED;
                }
                callback.roomCallback(errorCode);
            });
        }
    }

    private void changeSeatStatus(int seatIndex, String userID, boolean muteMic,
        ZegoSpeakerSeatStatus status, ZegoRoomCallback callback) {

        ZegoSpeakerSeatModel speakerSeatModel = new ZegoSpeakerSeatModel();
        speakerSeatModel.userID = userID;
        speakerSeatModel.seatIndex = seatIndex;
        speakerSeatModel.isMicMuted = muteMic;
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
        String modelString = new Gson().toJson(speakerSeatModel);

        HashMap<String, String> seatAttributes = new HashMap<>();
        seatAttributes.put(String.valueOf(seatIndex), modelString);

        String roomID = roomService.roomInfo.getRoomID();
        ZIMRoomAttributesSetConfig setConfig = getZimRoomAttributesSetConfig();

        Log.d(TAG, "changeSeatStatus() called with: seatAttributes = [" + seatAttributes + "]");

        ZegoZIMManager.getInstance().zim.setRoomAttributes(seatAttributes, roomID, setConfig, errorInfo -> {
            int errorCode;
            if (errorInfo.code.equals(ZIMErrorCode.SUCCESS)) {
                errorCode = ZegoRoomErrorCode.SUCCESS;
                speakerSeatList.set(seatIndex, speakerSeatModel);
                onSpeakerSeatStatusChanged(speakerSeatModel);
            } else {
                errorCode = ZegoRoomErrorCode.SET_SEAT_INFO_FAILED;
            }
            callback.roomCallback(errorCode);
        });
    }

    private void onSpeakerSeatStatusChanged(ZegoSpeakerSeatModel updateModel) {
        Log.d(TAG, "onSpeakerSeatStatusChanged() called with: updateModel = [" + updateModel + "]");
        if (speakerSeatList.size() > updateModel.seatIndex) {
            ZegoSpeakerSeatModel model = speakerSeatList.get(updateModel.seatIndex);
            model.userID = updateModel.userID;
            model.seatIndex = updateModel.seatIndex;
            model.isMicMuted = updateModel.isMicMuted;
            model.status = updateModel.status;

            ZegoUserInfo selfUserInfo = ZegoRoomManager.getInstance().userService.localUserInfo;
            if (selfUserInfo.getUserID().equals(updateModel.userID)) {
                ZegoExpressEngine.getEngine().muteMicrophone(updateModel.isMicMuted);
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
        Gson gson = new Gson();
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
            if (model.userID.equals(selfUserInfo.getUserID())) {
                model.soundLevel = soundLevel;
                if (speakerSeatServiceListener != null) {
                    speakerSeatServiceListener.onSpeakerSeatUpdate(model);
                }
            }
        }
    }

    public void updateRemoteUsersSoundLevel(HashMap<String, Float> soundLevels) {
        for (ZegoSpeakerSeatModel model : speakerSeatList) {
            Float soundLevel = soundLevels.get(model.userID);
            if (soundLevel != null) {
                model.soundLevel = soundLevel;
                if (speakerSeatServiceListener != null) {
                    speakerSeatServiceListener.onSpeakerSeatUpdate(model);
                }
            }
        }
    }
}
