package im.zego.liveaudioroom.internal;


import static im.zego.liveaudioroom.emus.ZIMChatRoomErrorCode.NOT_IN_SEAT;
import static im.zego.liveaudioroom.emus.ZIMChatRoomErrorCode.SUCCESS;
import static im.zego.liveaudioroom.emus.ZIMChatRoomSeatEvent.ENTERED;
import static im.zego.liveaudioroom.emus.ZIMChatRoomSeatEvent.LEFT;

import android.text.TextUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.entity.ZegoPlayStreamQuality;
import im.zego.zegoexpress.entity.ZegoPublishStreamQuality;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoUser;
import im.zego.zim.ZIM;
import im.zego.zim.callback.ZIMRoomAttributesBatchOperatedCallback;
import im.zego.zim.callback.ZIMRoomAttributesOperatedCallback;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMRoomAttributesBatchOperationConfig;
import im.zego.zim.entity.ZIMRoomAttributesDeleteConfig;
import im.zego.zim.entity.ZIMRoomAttributesSetConfig;
import im.zego.zim.entity.ZIMRoomAttributesUpdateInfo;
import im.zego.zim.enums.ZIMErrorCode;
import im.zego.zim.enums.ZIMRoomAttributesUpdateAction;
import im.zego.liveaudioroom.callback.EnterSeatCallback;
import im.zego.liveaudioroom.callback.KickUserToSeatCallback;
import im.zego.liveaudioroom.callback.LeaveSeatCallback;
import im.zego.liveaudioroom.callback.LockSeatCallback;
import im.zego.liveaudioroom.callback.MuteSeatCallback;
import im.zego.liveaudioroom.callback.SetupRTCModuleCallback;
import im.zego.liveaudioroom.callback.SwitchSeatCallback;
import im.zego.liveaudioroom.callback.ZIMChatRoomEventHandler;
import im.zego.liveaudioroom.emus.ZIMChatRoomErrorCode;
import im.zego.liveaudioroom.emus.ZIMChatRoomSeatEvent;
import im.zego.liveaudioroom.emus.ZIMChatRoomVoiceStatus;
import im.zego.liveaudioroom.entity.ZIMChatRoomSeatAttribution;
import im.zego.liveaudioroom.entity.ZIMChatRoomUser;
import im.zego.liveaudioroom.entity.ZIMSpeakerSeat;
import im.zego.liveaudioroom.entity.ZIMSpeakerSeatUpdateInfo;
import im.zego.liveaudioroom.util.GsonChanger;

/**
 * 麦位管理模块，在这里实现麦位的相关操作，邀请上麦等等操作，
 */
public class ZIMSpeakerSeatManager {
    ZIM zim;
    ZegoExpressEngine zegoExpressEngine;
    IZegoEventHandler expressHandler;
    SetupRTCModuleCallback setupRTCModuleCallback;

    ZIMChatRoomUser user;
    String mRoomID;
    ZIMAudioProfileManager audioProfileManager;

    ArrayList<ZIMSpeakerSeat> speakerSeats;

    public ZIMSpeakerSeatManager() {
        audioProfileManager = new ZIMAudioProfileManager();
        speakerSeats = initAllSeatListInRoom();
    }

    private void initExpressHandler() {
        expressHandler = new IZegoEventHandler() {

            @Override
            public void onRoomTokenWillExpire(String roomID, int remainTimeInSecond) {
                super.onRoomTokenWillExpire(roomID, remainTimeInSecond);
                final ZIMChatRoomEventHandler chatRoomEventHandler = ZIMChatRoomManager.getInstance().getEventHandler();
                if (chatRoomEventHandler != null) {
                    chatRoomEventHandler.onRTCTokenWillExpire(roomID, remainTimeInSecond);
                }
            }

            @Override
            public void onPublisherQualityUpdate(String streamID, ZegoPublishStreamQuality quality) {
                super.onPublisherQualityUpdate(streamID, quality);
            }

            @Override
            public void onPublisherStateUpdate(String streamID, ZegoPublisherState state, int errorCode, JSONObject extendedData) {
                super.onPublisherStateUpdate(streamID, state, errorCode, extendedData);
                if (state == ZegoPublisherState.NO_PUBLISH && errorCode != 0) {
                    final ZIMChatRoomEventHandler chatRoomEventHandler = ZIMChatRoomManager.getInstance().getEventHandler();
                    if (chatRoomEventHandler != null) {
                        chatRoomEventHandler.onRTCModuleError(errorCode);
                    }
                }
            }

            @Override
            public void onPlayerStateUpdate(String streamID, ZegoPlayerState state, int errorCode, JSONObject extendedData) {
                super.onPlayerStateUpdate(streamID, state, errorCode, extendedData);
                if (state == ZegoPlayerState.NO_PLAY && errorCode != 0) {
                    final ZIMChatRoomEventHandler chatRoomEventHandler = ZIMChatRoomManager.getInstance().getEventHandler();
                    if (chatRoomEventHandler != null) {
                        chatRoomEventHandler.onRTCModuleError(errorCode);
                    }
                }
            }

            @Override
            public void onPlayerQualityUpdate(String streamID, ZegoPlayStreamQuality quality) {
                super.onPlayerQualityUpdate(streamID, quality);
            }

            @Override
            public void onRoomStateUpdate(String roomID, ZegoRoomState state, int errorCode, JSONObject extendedData) {

                ZIMChatRoomErrorCode zimChatRoomErrorCode;
                if (state != ZegoRoomState.CONNECTING) {
                    if (errorCode == 0 && state == ZegoRoomState.CONNECTED) {
                        zimChatRoomErrorCode = SUCCESS;

                    } else {
                        zimChatRoomErrorCode = ZIMChatRoomErrorCode.ERROR;
                    }

                    final SetupRTCModuleCallback callback = setupRTCModuleCallback;
                    if (callback != null) {
                        callback.onConnectionState(zimChatRoomErrorCode);
                    }
                }

                if (state == ZegoRoomState.DISCONNECTED && errorCode != 0) {
                    final ZIMChatRoomEventHandler chatRoomEventHandler = ZIMChatRoomManager.getInstance().getEventHandler();
                    if (chatRoomEventHandler != null) {
                        chatRoomEventHandler.onRTCModuleError(errorCode);
                    }
                }
            }

            @Override
            public void onCapturedSoundLevelUpdate(float soundlevel) {
                final ZIMChatRoomEventHandler chatRoomEventHandler = ZIMChatRoomManager.getInstance().getEventHandler();
                super.onCapturedSoundLevelUpdate(soundlevel);
                if (chatRoomEventHandler != null) {
                    chatRoomEventHandler.OnLocalUserSoundLevelUpdated(soundlevel);
                }
            }


            @Override
            public void onRemoteSoundLevelUpdate(HashMap<String, Float> soundLevels) {
                final ZIMChatRoomEventHandler chatRoomEventHandler = ZIMChatRoomManager.getInstance().getEventHandler();
                super.onRemoteSoundLevelUpdate(soundLevels);
                if (chatRoomEventHandler != null) {
                    chatRoomEventHandler.OnRemoteUserSoundLevelUpdated(soundLevels);
                }
            }
        };
    }

    public ZIMAudioProfileManager getAudioProfileManager() {
        return audioProfileManager;
    }


    public void setupRTCModule(String rtcToken, final SetupRTCModuleCallback setupRTCModuleCallback) {
        this.setupRTCModuleCallback = setupRTCModuleCallback;

        ZegoUser zegoUser = new ZegoUser(user.getUserID(), user.getUserName());
        ZegoRoomConfig roomConfig = new ZegoRoomConfig();
        roomConfig.token = rtcToken;
        zegoExpressEngine.loginRoom(mRoomID, zegoUser, roomConfig);
        zegoExpressEngine.startSoundLevelMonitor();
    }


    public void releaseRTCModule() {
        zegoExpressEngine.stopSoundLevelMonitor();
        zegoExpressEngine.stopPublishingStream();
        zegoExpressEngine.logoutRoom(mRoomID);
    }

    private static ArrayList<ZIMSpeakerSeat> initAllSeatListInRoom() {
        ArrayList<ZIMSpeakerSeat> allSeats = new ArrayList<>();

        for (int i = 0; i <= 7; i++) {
            ZIMSpeakerSeat speakerSeat = new ZIMSpeakerSeat();
            speakerSeat.getAttribution().setIndex(i);
            speakerSeat.setStatus(ZIMChatRoomVoiceStatus.UNUSED);
            allSeats.add(speakerSeat);
        }
        return allSeats;
    }

    public ZIM getZim() {
        return zim;
    }

    public void setZim(ZIM zim) {
        this.zim = zim;
    }

    public void setZegoExpressEngine(ZegoExpressEngine zegoExpressEngine) {
        this.zegoExpressEngine = zegoExpressEngine;
        audioProfileManager.setExpressEngine(zegoExpressEngine);
        initExpressHandler();
        this.zegoExpressEngine.setEventHandler(expressHandler);
    }

    public ZIMChatRoomUser getUser() {
        return user;
    }

    public void setUser(ZIMChatRoomUser user) {
        this.user = user;
    }

    public ZIMSpeakerSeat getSpeakerSeat(int index) {
        return speakerSeats.get(index);
    }

    public void setRoomID(String roomID) {
        this.mRoomID = roomID;
    }

    public void onSpeakerSeatAdded(ZIMSpeakerSeat speakerSeat) {
        int index = speakerSeat.getAttribution().getIndex();
        speakerSeats.set(index, speakerSeat);

        if (speakerSeat.getAttribution().getUser_id().equals(this.user.getUserID())) {
            zegoExpressEngine.startPublishingStream(this.user.getUserID());
        } else {
            zegoExpressEngine.startPlayingStream(speakerSeat.getAttribution().getUser_id(), null);
        }

        final ZIMChatRoomEventHandler handler = ZIMChatRoomManager.getInstance().getEventHandler();
        if (handler != null) {
            ZIMSpeakerSeatUpdateInfo info = new ZIMSpeakerSeatUpdateInfo(speakerSeat, ZIMChatRoomSeatEvent.ENTERED);
            ArrayList<ZIMSpeakerSeatUpdateInfo> infoList = new ArrayList<>();
            infoList.add(info);
            handler.onChatRoomSpeakerSeatUpdated(infoList);
        }
    }

    public void onSpeakerSeatMuted(int seatIndex, boolean isMuted) {
        ZIMSpeakerSeat seat = speakerSeats.get(seatIndex);
        seat.getAttribution().setIs_muted(isMuted);
        speakerSeats.set(seatIndex, seat);

        final ZIMChatRoomEventHandler handler = ZIMChatRoomManager.getInstance().getEventHandler();
        if (handler != null) {
            ZIMSpeakerSeatUpdateInfo info = new ZIMSpeakerSeatUpdateInfo(seat, isMuted ? ZIMChatRoomSeatEvent.MUTED : ZIMChatRoomSeatEvent.UNMUTED);
            ArrayList<ZIMSpeakerSeatUpdateInfo> infoList = new ArrayList<>();
            infoList.add(info);
            handler.onChatRoomSpeakerSeatUpdated(infoList);
        }

    }

    public void onSpeakerSeatRemoved(int seatIndex) {


        ZIMSpeakerSeat zimSpeakerSeat = speakerSeats.get(seatIndex);
        zimSpeakerSeat.setStatus(ZIMChatRoomVoiceStatus.UNUSED);
        if (zimSpeakerSeat.getAttribution().getUser_id().equals(this.user.getUserID())) {
            zegoExpressEngine.stopPublishingStream();
        } else {
            zegoExpressEngine.stopPlayingStream(zimSpeakerSeat.getAttribution().getUser_id());
        }

        final ZIMChatRoomEventHandler handler = ZIMChatRoomManager.getInstance().getEventHandler();
        if (handler != null) {
            ZIMSpeakerSeat seat = new ZIMSpeakerSeat();
            seat.setStatus(zimSpeakerSeat.getStatus());
            ZIMChatRoomSeatAttribution attribution = new ZIMChatRoomSeatAttribution();
            attribution.setIs_locked(zimSpeakerSeat.getAttribution().isIs_locked());
            attribution.setUser_id(zimSpeakerSeat.getAttribution().getUser_id());
            attribution.setIndex(zimSpeakerSeat.getAttribution().getIndex());
            attribution.setIs_muted(zimSpeakerSeat.getAttribution().isIs_muted());
            attribution.setExtras(zimSpeakerSeat.getAttribution().getExtras());
            seat.setAttribution(attribution);
            ZIMSpeakerSeatUpdateInfo info = new ZIMSpeakerSeatUpdateInfo(seat, ZIMChatRoomSeatEvent.LEFT);
            ArrayList<ZIMSpeakerSeatUpdateInfo> infoList = new ArrayList<>();
            infoList.add(info);
            zimSpeakerSeat.getAttribution().setUser_id("");
            handler.onChatRoomSpeakerSeatUpdated(infoList);
        }


        speakerSeats.set(seatIndex, zimSpeakerSeat);
    }

    public void onSpeakerSeatSwitched(ArrayList<ZIMRoomAttributesUpdateInfo> infoList) {
        final ZIMChatRoomEventHandler chatRoomEventHandler = ZIMChatRoomManager.getInstance().getEventHandler();
        ArrayList<ZIMSpeakerSeatUpdateInfo> seatUpdateInfos = new ArrayList<>();

        for (ZIMRoomAttributesUpdateInfo info : infoList) {
            Set<String> keys = info.roomAttributes.keySet();
            if (info.action == ZIMRoomAttributesUpdateAction.SET) {

                for (String key : keys) {
                    ZIMChatRoomSeatAttribution seatAttribution = GsonChanger.getInstance().getZIMChatRoomSeatAttribution(info.roomAttributes.get(key));
                    ZIMSpeakerSeat speakerSeat = new ZIMSpeakerSeat();
                    speakerSeat.setAttribution(seatAttribution);
                    speakerSeat.setStatus(ZIMChatRoomVoiceStatus.USED);
                    speakerSeats.set(seatAttribution.getIndex(), speakerSeat);
                    ZIMSpeakerSeatUpdateInfo updateInfo = new ZIMSpeakerSeatUpdateInfo(speakerSeat, ENTERED);
                    seatUpdateInfos.add(updateInfo);
                }
            } else {
                for (String key : keys) {
                    String idxStr = key.split("_")[1];
                    int index = Integer.valueOf(idxStr).intValue();
                    ZIMSpeakerSeat speakerSeat = new ZIMSpeakerSeat();
                    speakerSeat.getAttribution().setIndex(index);
                    speakerSeat.setStatus(ZIMChatRoomVoiceStatus.UNUSED);
                    speakerSeats.set(index, speakerSeat);
                    ZIMSpeakerSeatUpdateInfo updateInfo = new ZIMSpeakerSeatUpdateInfo(speakerSeat, LEFT);
                    seatUpdateInfos.add(updateInfo);
                }
            }
        }

        if (chatRoomEventHandler != null) {
            chatRoomEventHandler.onChatRoomSpeakerSeatUpdated(seatUpdateInfos);
        }
    }


    public void onSpeakerSeatLocked(int seatIndex, boolean isLocked) {

        ZIMSpeakerSeat seat = speakerSeats.get(seatIndex);
        seat.getAttribution().setIs_locked(isLocked);
        seat.setStatus(isLocked ? ZIMChatRoomVoiceStatus.LOCKED : ZIMChatRoomVoiceStatus.UNUSED);
        speakerSeats.set(seatIndex, seat);

        final ZIMChatRoomEventHandler handler = ZIMChatRoomManager.getInstance().getEventHandler();
        if (handler != null) {
            ZIMSpeakerSeatUpdateInfo info = new ZIMSpeakerSeatUpdateInfo(seat, isLocked ? ZIMChatRoomSeatEvent.LOCKED : ZIMChatRoomSeatEvent.UNLOCKED);
            ArrayList<ZIMSpeakerSeatUpdateInfo> infoList = new ArrayList<>();
            infoList.add(info);
            handler.onChatRoomSpeakerSeatUpdated(infoList);
        }
    }

    /**
     * 在这里具体实现麦位操作的相应方法
     */
    /*
    房主端的操作
     */
    public void kickUserToSeat(String userID, final KickUserToSeatCallback kickUserToSeatCallback) {

        ZIMSpeakerSeat chatRoomSeat = getZIMChatRoomSeatByUserID(userID);
        if (chatRoomSeat != null) {
            int index = chatRoomSeat.getAttribution().getIndex();
            ArrayList<String> keys = new ArrayList<>();
            keys.add("seat_" + index);
            ZIMRoomAttributesDeleteConfig deleteConfig = new ZIMRoomAttributesDeleteConfig();
            deleteConfig.isForce = true;
            zim.deleteRoomAttributes(keys, mRoomID, deleteConfig, new ZIMRoomAttributesOperatedCallback() {
                @Override
                public void onRoomAttributesOperated(ZIMError errorInfo) {
                    ZIMChatRoomErrorCode zimChatRoomErrorCode;
                    if (errorInfo.code.equals(ZIMErrorCode.SUCCESS)) {
                        zimChatRoomErrorCode = ZIMChatRoomErrorCode.SUCCESS;
                    } else {
                        zimChatRoomErrorCode = ZIMChatRoomErrorCode.ERROR;
                    }
                    kickUserToSeatCallback.kickUserToSeat(zimChatRoomErrorCode);
                }
            });
        } else {
            kickUserToSeatCallback.kickUserToSeat(NOT_IN_SEAT);
        }

    }


    public void lockSeat(boolean isLocked, int seatIndex, final LockSeatCallback lockSeatCallback) {
        if (isLocked) {
            HashMap<String, String> roomAttributes = new HashMap<String, String>();

            ZIMChatRoomSeatAttribution seatAttributes = new ZIMChatRoomSeatAttribution();
            seatAttributes.setIndex(seatIndex);
            seatAttributes.setUser_id("");
            seatAttributes.setIs_muted(false);
            seatAttributes.setIs_locked(true);

            ZIMRoomAttributesSetConfig setConfig = new ZIMRoomAttributesSetConfig();
            setConfig.isForce = false;
            setConfig.isDeleteAfterOwnerLeft = true;


            roomAttributes.put("seat_" + seatIndex, GsonChanger.getInstance().getJsonOfZIMChatRoomSeat(seatAttributes));


            zim.setRoomAttributes(roomAttributes, mRoomID, setConfig, new ZIMRoomAttributesOperatedCallback() {
                @Override
                public void onRoomAttributesOperated(ZIMError errorInfo) {
                    ZIMChatRoomErrorCode zimChatRoomErrorCode;
                    if (errorInfo.code.equals(ZIMErrorCode.SUCCESS)) {
                        zimChatRoomErrorCode = ZIMChatRoomErrorCode.SUCCESS;
                    } else {
                        zimChatRoomErrorCode = ZIMChatRoomErrorCode.ERROR;
                    }
                    lockSeatCallback.lockSeat(zimChatRoomErrorCode);
                }
            });
        } else {
            String seatIndexKey = "seat_" + seatIndex;
            ArrayList<String> keyList = new ArrayList<>();
            keyList.add(seatIndexKey);
            ZIMRoomAttributesDeleteConfig config = new ZIMRoomAttributesDeleteConfig();
            config.isForce = true;
            zim.deleteRoomAttributes(keyList, mRoomID, config, new ZIMRoomAttributesOperatedCallback() {
                @Override
                public void onRoomAttributesOperated(ZIMError errorInfo) {
                    ZIMChatRoomErrorCode zimChatRoomErrorCode;
                    if (errorInfo.code.equals(ZIMErrorCode.SUCCESS)) {
                        zimChatRoomErrorCode = ZIMChatRoomErrorCode.SUCCESS;
                    } else {
                        zimChatRoomErrorCode = ZIMChatRoomErrorCode.ERROR;
                    }
                    lockSeatCallback.lockSeat(zimChatRoomErrorCode);
                }
            });
        }

    }


    public void muteSeat(boolean isMuted, final MuteSeatCallback muteSeatCallback) {

        ZIMSpeakerSeat mySeat = findMySeat();
        if (mySeat != null) {

            audioProfileManager.muteMicrophone(isMuted);

            mySeat.getAttribution().setIs_muted(isMuted);
            HashMap<String, String> seatAttributes = new HashMap<String, String>();
            seatAttributes.put("seat_" + mySeat.getAttribution().getIndex(),
                    GsonChanger.getInstance().getJsonOfZIMChatRoomSeat(mySeat.getAttribution()));
            ZIMRoomAttributesSetConfig setConfig = new ZIMRoomAttributesSetConfig();
            setConfig.isDeleteAfterOwnerLeft = true;
            setConfig.isForce = false;
            zim.setRoomAttributes(seatAttributes, mRoomID, setConfig, new ZIMRoomAttributesOperatedCallback() {
                @Override
                public void onRoomAttributesOperated(ZIMError errorInfo) {
                    ZIMChatRoomErrorCode zimChatRoomErrorCode;
                    if (errorInfo.code.equals(ZIMErrorCode.SUCCESS)) {
                        zimChatRoomErrorCode = ZIMChatRoomErrorCode.SUCCESS;
                    } else {
                        zimChatRoomErrorCode = ZIMChatRoomErrorCode.ERROR;
                    }
                    muteSeatCallback.muteSeat(zimChatRoomErrorCode);
                }
            });
        } else {
            muteSeatCallback.muteSeat(ZIMChatRoomErrorCode.NOT_IN_SEAT);
        }
    }


    /*
    所有操作
     */

    public void enterSeat(final int seatIndex, boolean isSwitchSeat, final EnterSeatCallback enterSeatCallback) {

        if (!isInSeat(seatIndex) && (isSwitchSeat || isInSeat(user.getUserID()) == -1)) {

            HashMap<String, String> seatAttributes = new HashMap<String, String>();

            ZIMChatRoomSeatAttribution roomAttributes = new ZIMChatRoomSeatAttribution();
            roomAttributes.setIndex(seatIndex);
            roomAttributes.setUser_id(user.getUserID());
            roomAttributes.setIs_muted(false);
            roomAttributes.setIs_locked(false);
            speakerSeats.get(seatIndex).getAttribution().setUser_id(user.getUserID());
            ZIMRoomAttributesSetConfig setConfig = new ZIMRoomAttributesSetConfig();
            setConfig.isForce = false;
            setConfig.isDeleteAfterOwnerLeft = true;

            seatAttributes.put("seat_" + seatIndex, GsonChanger.getInstance().getJsonOfZIMChatRoomSeat(roomAttributes));
            audioProfileManager.muteMicrophone(false);
            zim.setRoomAttributes(seatAttributes, mRoomID, setConfig, new ZIMRoomAttributesOperatedCallback() {
                @Override
                public void onRoomAttributesOperated(ZIMError errorInfo) {
                    ZIMChatRoomErrorCode zimChatRoomErrorCode;
                    if (errorInfo.code.equals(ZIMErrorCode.SUCCESS)) {
                        zimChatRoomErrorCode = ZIMChatRoomErrorCode.SUCCESS;
                    } else {
                        speakerSeats.get(seatIndex).getAttribution().setUser_id("");
                        zimChatRoomErrorCode = ZIMChatRoomErrorCode.SET_SEAT_INFO_FAILED;
                    }
                    enterSeatCallback.enterSeat(zimChatRoomErrorCode);
                }
            });
        } else {
            enterSeatCallback.enterSeat(ZIMChatRoomErrorCode.SEAT_EXISTED);
        }
    }


    public void leaveSeat(final LeaveSeatCallback leaveSeatCallback) {

        int index = isInSeat(user.getUserID());
        if (index != -1) {
            ZIMSpeakerSeat mySeat = findMySeat();

            if (mySeat != null) {
                audioProfileManager.muteMicrophone(false);
                mySeat.getAttribution().setIs_muted(false);
            }
            ArrayList<String> keys = new ArrayList<>();
            keys.add("seat_" + index);
            ZIMRoomAttributesDeleteConfig deleteConfig = new ZIMRoomAttributesDeleteConfig();
            deleteConfig.isForce = false;
            zim.deleteRoomAttributes(keys, mRoomID, deleteConfig, new ZIMRoomAttributesOperatedCallback() {
                @Override
                public void onRoomAttributesOperated(ZIMError errorInfo) {
                    ZIMChatRoomErrorCode zimChatRoomErrorCode;
                    if (errorInfo.code.equals(ZIMErrorCode.SUCCESS)) {
                        zimChatRoomErrorCode = ZIMChatRoomErrorCode.SUCCESS;
                    } else {
                        zimChatRoomErrorCode = ZIMChatRoomErrorCode.ERROR;
                    }
                    leaveSeatCallback.leaveSeat(zimChatRoomErrorCode);
                }
            });
        } else {
            leaveSeatCallback.leaveSeat(ZIMChatRoomErrorCode.ERROR);
        }


    }

    boolean isSwitch = false;


    public void switchSeat(int toSeatIndex, final SwitchSeatCallback switchSeatCallback) {

        final ZIMSpeakerSeat mySeat = findMySeat();
        if (mySeat == null) {
            switchSeatCallback.switchSeat(NOT_IN_SEAT);
            return;
        }
        if (isSwitch) {
            return;
        }
        isSwitch = true;

        ZIMRoomAttributesBatchOperationConfig operationConfig = new ZIMRoomAttributesBatchOperationConfig();
        operationConfig.isForce = false;
        operationConfig.isDeleteAfterOwnerLeft = true;


        zim.beginRoomAttributesBatchOperation(mRoomID, operationConfig);

        final boolean isMuted;
        isMuted = mySeat.getAttribution().isIs_muted();

        enterSeat(toSeatIndex, true, new EnterSeatCallback() {
            @Override
            public void enterSeat(ZIMChatRoomErrorCode error) {
                if (error == SUCCESS) {

                }
            }
        });


        ZIMRoomAttributesDeleteConfig deleteConfig = new ZIMRoomAttributesDeleteConfig();
        deleteConfig.isForce = true;
        ArrayList<String> keys = new ArrayList<>();
        final int originIndex = mySeat.getAttribution().getIndex();
        keys.add("seat_" + originIndex);
        zim.deleteRoomAttributes(keys, mRoomID, deleteConfig, new ZIMRoomAttributesOperatedCallback() {
            @Override
            public void onRoomAttributesOperated(ZIMError errorInfo) {

            }
        });


        zim.endRoomAttributesBatchOperation(mRoomID, new ZIMRoomAttributesBatchOperatedCallback() {
            @Override
            public void onRoomAttributesBatchOperated(ZIMError errorInfo) {
                ZIMChatRoomErrorCode zimChatRoomErrorCode;
                if (errorInfo.code.equals(ZIMErrorCode.SUCCESS)) {
                    zimChatRoomErrorCode = ZIMChatRoomErrorCode.SUCCESS;
                    if (isMuted) {
                        muteSeat(isMuted, new MuteSeatCallback() {
                            @Override
                            public void muteSeat(ZIMChatRoomErrorCode error) {

                            }
                        });
                    }
                } else {
                    zimChatRoomErrorCode = ZIMChatRoomErrorCode.ERROR;
                    speakerSeats.get(originIndex).getAttribution().setUser_id("");
                }
                isSwitch = false;
                switchSeatCallback.switchSeat(zimChatRoomErrorCode);
            }
        });

    }

    public boolean isSpeakerSeatUsed(int index) {
        return speakerSeats.get(index).getStatus() == ZIMChatRoomVoiceStatus.USED;
    }

    public ZIMSpeakerSeat findMySeat() {
        for (int i = 0; i < speakerSeats.size(); i++) {
            ZIMSpeakerSeat seat = speakerSeats.get(i);
            if (user.getUserID().equals(seat.getAttribution().getUser_id())) {
                return seat;
            }
        }
        return null;
    }


    public ZIMSpeakerSeat getZIMChatRoomSeatByUserID(String userID) {
        for (ZIMSpeakerSeat roomSeat : speakerSeats) {
            if (roomSeat.getAttribution().getUser_id() != null && roomSeat.getAttribution().getUser_id().equals(userID)) {
                return roomSeat;
            }
            continue;
        }
        return null;
    }


    private boolean isInSeat(int seatIndex) {
        for (ZIMSpeakerSeat roomSeat : speakerSeats) {
            if (roomSeat.getStatus().equals(ZIMChatRoomVoiceStatus.USED) && roomSeat.getAttribution().getIndex() == seatIndex) {
                return true;
            }
        }
        return false;
    }


    private int isInSeat(String userID) {
        for (ZIMSpeakerSeat roomSeat : speakerSeats) {
            if (userID.equals(roomSeat.getAttribution().getUser_id())) {
                return roomSeat.getAttribution().getIndex();
            }
        }
        return -1;
    }

    public void releaseVideoSeats() {
        speakerSeats = initAllSeatListInRoom();
    }

    public List<String> getSeatedUserIDs(){
        List<String> userIDs = new ArrayList<>();
        for (ZIMSpeakerSeat roomSeat : speakerSeats) {
            String user_id = roomSeat.getAttribution().getUser_id();
            if (!TextUtils.isEmpty(user_id)) {
                userIDs.add(user_id);
            }
        }
        return userIDs;
    }
}
