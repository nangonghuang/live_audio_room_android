package im.zego.liveaudioroom.internal;

import static im.zego.liveaudioroom.emus.ZegoLiveAudioRoomErrorCode.ERROR;
import static im.zego.liveaudioroom.emus.ZegoLiveAudioRoomErrorCode.SUCCESS;

import android.app.Application;
import android.util.Log;

import com.google.gson.JsonSyntaxException;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zim.ZIM;
import im.zego.zim.callback.ZIMEventHandler;
import im.zego.zim.callback.ZIMLogUploadedCallback;
import im.zego.zim.callback.ZIMLoggedInCallback;
import im.zego.zim.callback.ZIMMemberQueriedCallback;
import im.zego.zim.callback.ZIMRoomAllAttributesQueriedCallback;
import im.zego.zim.callback.ZIMRoomCreatedCallback;
import im.zego.zim.callback.ZIMRoomJoinedCallback;
import im.zego.zim.callback.ZIMRoomLeftCallback;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.entity.ZIMQueryMemberConfig;
import im.zego.zim.entity.ZIMRoomAdvancedConfig;
import im.zego.zim.entity.ZIMRoomAttributesUpdateInfo;
import im.zego.zim.entity.ZIMRoomFullInfo;
import im.zego.zim.entity.ZIMRoomInfo;
import im.zego.zim.entity.ZIMTextMessage;
import im.zego.zim.entity.ZIMUserInfo;
import im.zego.zim.enums.ZIMConnectionEvent;
import im.zego.zim.enums.ZIMConnectionState;
import im.zego.zim.enums.ZIMErrorCode;
import im.zego.zim.enums.ZIMRoomAttributesUpdateAction;
import im.zego.zim.enums.ZIMRoomEvent;
import im.zego.zim.enums.ZIMRoomState;
import im.zego.liveaudioroom.callback.CreateRoomCallback;
import im.zego.liveaudioroom.callback.EnterSeatCallback;
import im.zego.liveaudioroom.callback.JoinRoomCallback;
import im.zego.liveaudioroom.callback.KickUserToSeatCallback;
import im.zego.liveaudioroom.callback.LeaveSeatCallback;
import im.zego.liveaudioroom.callback.LockSeatCallback;
import im.zego.liveaudioroom.callback.LogUploadedCallback;
import im.zego.liveaudioroom.callback.MuteAllMessageCallback;
import im.zego.liveaudioroom.callback.MuteSeatCallback;
import im.zego.liveaudioroom.callback.OnLeaveCallback;
import im.zego.liveaudioroom.callback.OnLoginCallback;
import im.zego.liveaudioroom.callback.OnQueryRoomInfoCallback;
import im.zego.liveaudioroom.callback.OnQueryRoomMemberCallback;
import im.zego.liveaudioroom.callback.SendGiftMessageCallback;
import im.zego.liveaudioroom.callback.SendInvitationStatusCallback;
import im.zego.liveaudioroom.callback.SendRoomMessageCallback;
import im.zego.liveaudioroom.callback.SetupRTCModuleCallback;
import im.zego.liveaudioroom.callback.SwitchSeatCallback;
import im.zego.liveaudioroom.callback.LiveAudioRoomEventHandler;
import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomErrorCode;
import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomEvent;
import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomInvitationStatus;
import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomState;
import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomUserRole;
import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomVoiceStatus;
import im.zego.liveaudioroom.entity.ZegoLiveAudioRoomGift;
import im.zego.liveaudioroom.entity.ZegoLiveAudioRoomGiftBroadcast;
import im.zego.liveaudioroom.entity.ZegoLiveAudioRoomInvitationRespond;
import im.zego.liveaudioroom.entity.ZegoLiveAudioRoomMessage;
import im.zego.liveaudioroom.entity.ZegoLiveAudioRoomQueryMemberConfig;
import im.zego.liveaudioroom.entity.ZegoLiveAudioRoomSeatAttribution;
import im.zego.liveaudioroom.entity.ZegoLiveAudioRoomText;
import im.zego.liveaudioroom.entity.ZegoLiveAudioRoomUser;
import im.zego.liveaudioroom.entity.ZegoLiveAudioRoomUserInfo;
import im.zego.liveaudioroom.entity.ZIMSpeakerSeat;
import im.zego.liveaudioroom.entity.ZIMSpeakerSeatUpdateInfo;
import im.zego.liveaudioroom.internal.entity.ZegoLiveAudioRoomInfo;
import im.zego.liveaudioroom.util.GsonChanger;

public class ZegoLiveAudioRoomManager {
    private static final String TAG = "ZegoLiveAudioRoomManager";
    ZegoExpressEngine zegoExpressEngine;
    ZIM zim;
    ZegoLiveAudioRoomUser mUser = new ZegoLiveAudioRoomUser();
    Application application;
    ZIMEventHandler zimEventHandler;


    ZegoLiveAudioRoomInfo mRoomInfo;

    ZegoLiveAudioMessageManager messageManager;

    ZegoLiveAudioSpeakerSeatManager speakerSeatManager;
    //eventHandler
    LiveAudioRoomEventHandler liveAudioRoomEventHandler;
    static ZegoLiveAudioRoomManager instance;
    private ArrayList<ZegoLiveAudioRoomUser> roomUsers;

    public static ZegoLiveAudioRoomManager getInstance() {
        if (instance == null) {
            instance = new ZegoLiveAudioRoomManager();
        }
        return instance;
    }

    public ZegoLiveAudioRoomManager() {
        messageManager = new ZegoLiveAudioMessageManager();
        speakerSeatManager = new ZegoLiveAudioSpeakerSeatManager();
        roomUsers = new ArrayList<>();
    }


    public void setEventHandler(LiveAudioRoomEventHandler roomEventHandler) {
        this.liveAudioRoomEventHandler = roomEventHandler;
    }

    public LiveAudioRoomEventHandler getEventHandler() {
        return this.liveAudioRoomEventHandler;
    }


    private void initZIMEventHandler() {
        zimEventHandler = new ZIMEventHandler() {
            @Override
            public void onReceivePeerMessage(ZIM zim, ArrayList<ZIMMessage> messageList, String fromUserID) {
                super.onReceivePeerMessage(zim, messageList, fromUserID);
                for (ZIMMessage zimMessage : messageList) {
                    ZIMTextMessage message = (ZIMTextMessage) zimMessage;
                    ZegoLiveAudioRoomMessage roomMessage = GsonChanger.getInstance().getZegoLiveAudioRoomMessage(message.message);
                    final LiveAudioRoomEventHandler roomEventHandler = liveAudioRoomEventHandler;
                    switch (roomMessage.getAction()) {
                        case INVITATION:

                            if (roomEventHandler != null) {
                                roomEventHandler.onReceiveInvitation(fromUserID);
                            }
                            messageManager.fromInvitationUserID = fromUserID;

                            break;
                        case INVITATION_RESPOND:
                            ZegoLiveAudioRoomInvitationRespond invitationRespond = roomMessage.getInvitationRespond();

                            if (roomEventHandler != null) {
                                roomEventHandler.onResponseInvitation(invitationRespond.getStatus(), fromUserID);
                            }
                            break;
                        case GIFT:

                            ZegoLiveAudioRoomGift gift = roomMessage.getGiftMessage();
                            if (roomEventHandler != null) {
                                roomEventHandler.onReceiveGiftMessage(gift.getGiftType(), gift.getFromUserID());
                            }
                            break;
                    }
                }
            }

            @Override
            public void onReceiveRoomMessage(ZIM zim, ArrayList<ZIMMessage> messageList, String fromRoomID) {
                super.onReceiveRoomMessage(zim, messageList, fromRoomID);
                final LiveAudioRoomEventHandler roomEventHandler = liveAudioRoomEventHandler;

                for (ZIMMessage zimMessage : messageList) {
                    ZIMTextMessage message = (ZIMTextMessage) zimMessage;
                    ZegoLiveAudioRoomMessage roomMessage = GsonChanger.getInstance().getZegoLiveAudioRoomMessage(message.message);
                    switch (roomMessage.getAction()) {
                        case GIFT_BROADCAST:
                            ZegoLiveAudioRoomGiftBroadcast giftBroadcast = roomMessage.getGiftBroadcast();
                            if (roomEventHandler != null) {
                                roomEventHandler.onReceiveGiftBroadcastMessage(giftBroadcast.getToUserIDList(), giftBroadcast.getGiftType(), zimMessage.userID);
                            }
                            break;
                        case TEXT:
                            ZegoLiveAudioRoomText text = roomMessage.getTextMessage();
                            if (roomEventHandler != null) {
                                roomEventHandler.onReceiveRoomMassage(text.getContent(), zimMessage.userID);
                            }
                    }
                }
            }

            @Override
            public void onRoomStateChanged(ZIM zim, ZIMRoomState state, ZIMRoomEvent event, JSONObject extendedData, String roomID) {
                super.onRoomStateChanged(zim, state, event, extendedData, roomID);
                final LiveAudioRoomEventHandler roomEventHandler = liveAudioRoomEventHandler;
                ZegoLiveAudioRoomState roomState = ZegoLiveAudioRoomState.getZegoLiveAudioRoomState(state.value());
                ZegoLiveAudioRoomEvent roomEvent = ZegoLiveAudioRoomEvent.getZegoLiveAudioRoomEvent(event.value());
                if (roomEventHandler != null) {
                    roomEventHandler.onRoomStateUpdated(roomState, roomEvent, roomID);
                }
            }

            @Override
            public void onTokenWillExpire(ZIM zim, int second) {
                super.onTokenWillExpire(zim, second);
                final LiveAudioRoomEventHandler roomEventHandler = liveAudioRoomEventHandler;

                if (roomEventHandler != null) {
                    roomEventHandler.onZIMTokenWillExpire(second);
                }
            }

            @Override
            public void onRoomMemberJoined(ZIM zim, ArrayList<ZIMUserInfo> memberList, String roomID) {
                super.onRoomMemberJoined(zim, memberList, roomID);
                ArrayList<ZegoLiveAudioRoomUser> joinUsers = getRoomUsers(memberList);
                roomUsers.addAll(joinUsers);
                final LiveAudioRoomEventHandler roomEventHandler = liveAudioRoomEventHandler;
                if (roomEventHandler != null) {
                    roomEventHandler.onRoomMemberJoined(joinUsers);
                }
            }

            @Override
            public void onRoomMemberLeft(ZIM zim, ArrayList<ZIMUserInfo> memberList, String roomID) {
                super.onRoomMemberLeft(zim, memberList, roomID);
                final LiveAudioRoomEventHandler roomEventHandler = liveAudioRoomEventHandler;
                ArrayList<ZegoLiveAudioRoomUser> leftUsers = getRoomUsers(memberList);
                Iterator<ZegoLiveAudioRoomUser> iterator = roomUsers.iterator();
                if (iterator.hasNext()) {
                    ZegoLiveAudioRoomUser next = iterator.next();
                    if(leftUsers.contains(next.getUserID())){
                        iterator.remove();
                    }
                }
                if (roomEventHandler != null) {
                    roomEventHandler.onRoomMemberLeft(leftUsers);
                }
            }

            @Override
            public void onRoomAttributesUpdated(ZIM zim, ZIMRoomAttributesUpdateInfo info, String roomID) {
                super.onRoomAttributesUpdated(zim, info, roomID);
                final LiveAudioRoomEventHandler roomEventHandler = liveAudioRoomEventHandler;
                ArrayList<ZIMSpeakerSeatUpdateInfo> seatUpdateInfos;

                Set<String> keys = info.roomAttributes.keySet();
                for (String key : keys) {

                    if (!key.equals("room_info")) {
                        if (info.action == ZIMRoomAttributesUpdateAction.SET) {
                            ZegoLiveAudioRoomSeatAttribution seatAttribution = null;
                            try {
                                seatAttribution = GsonChanger.getInstance().getZegoLiveAudioRoomSeatAttribution(info.roomAttributes.get(key));
                            } catch (JsonSyntaxException exception) {
                                exception.printStackTrace();
                            }
                            if (seatAttribution == null) {
                                continue;
                            }

                            if (!speakerSeatManager.isSpeakerSeatUsed(seatAttribution.getIndex())) {
                                if (seatAttribution.isIs_locked()) {
                                    speakerSeatManager.speakerSeats.get(seatAttribution.getIndex()).getAttribution().setUser_id("");
                                    speakerSeatManager.onSpeakerSeatLocked(seatAttribution.getIndex(), true);
                                } else {
                                    ZIMSpeakerSeat roomSeat = new ZIMSpeakerSeat();
                                    roomSeat.setAttribution(seatAttribution);
                                    roomSeat.setStatus(ZegoLiveAudioRoomVoiceStatus.USED);
                                    speakerSeatManager.onSpeakerSeatAdded(roomSeat);
                                }
                            } else {
                                speakerSeatManager.onSpeakerSeatMuted(seatAttribution.getIndex(), seatAttribution.isIs_muted());
                            }
                        } else if (info.action == ZIMRoomAttributesUpdateAction.DELETE) {
                            String idxStr = key.split("_")[1];
                            int index = Integer.parseInt(idxStr);
                            ZegoLiveAudioRoomSeatAttribution seatAttribution = null;
                            try {
                                seatAttribution = GsonChanger.getInstance().getZegoLiveAudioRoomSeatAttribution(info.roomAttributes.get(key));
                            } catch (JsonSyntaxException exception) {
                                exception.printStackTrace();
                            }
                            ZIMSpeakerSeat oldSeat = speakerSeatManager.getSpeakerSeat(index);
                            if (oldSeat.getStatus() == ZegoLiveAudioRoomVoiceStatus.LOCKED) {
                                speakerSeatManager.onSpeakerSeatLocked(index, false);
                            } else if (oldSeat.getStatus() == ZegoLiveAudioRoomVoiceStatus.USED) {
                                speakerSeatManager.onSpeakerSeatRemoved(index);
                            }
                            if (seatAttribution == null) {
                                continue;
                            }
                            speakerSeatManager.speakerSeats.get(seatAttribution.getIndex()).getAttribution().setUser_id("");

                        }
                    } else {

                        if (info.action == ZIMRoomAttributesUpdateAction.SET) {
                            ZegoLiveAudioRoomInfo roomInfo = GsonChanger.getInstance().getZegoLiveAudioRoomInfo(info.roomAttributes.get(key));

                            if (mRoomInfo == null) {
                                setRoomInfo(roomInfo);
                            } else {

                                if (roomEventHandler != null) {
                                    roomEventHandler.onRoomInfoUpdated(roomInfo);
                                }


                                boolean originIsMuted = mRoomInfo.isIs_mute_msg();
                                boolean nowIsMuted = roomInfo.isIs_mute_msg();
                                if (nowIsMuted != originIsMuted) {
                                    if (roomEventHandler != null) {
                                        roomEventHandler.onMuteAllMessage(nowIsMuted);
                                    }
                                }
                                mRoomInfo = roomInfo;
                            }
                            messageManager.setRoomInfo(mRoomInfo);
                        } else {


                            if (roomEventHandler != null) {
                                roomEventHandler.onRoomInfoUpdated(null);
                            }
                        }
                    }
                }
            }


            @Override
            public void onRoomAttributesBatchUpdated(ZIM zim, ArrayList<ZIMRoomAttributesUpdateInfo> infos, String roomID) {
                speakerSeatManager.onSpeakerSeatSwitched(infos);
            }

            @Override
            public void onConnectionStateChanged(ZIM zim, ZIMConnectionState state, ZIMConnectionEvent event, JSONObject extendedData) {
                final LiveAudioRoomEventHandler roomEventHandler = liveAudioRoomEventHandler;
                if (roomEventHandler != null)
                    roomEventHandler.onConnectionStateChanged(ZegoLiveAudioRoomState.getZegoLiveAudioRoomState(state.value()), ZegoLiveAudioRoomEvent.getZegoLiveAudioRoomEvent(event.value()), extendedData);
            }

        };

    }

    public void setRoomInfo(ZegoLiveAudioRoomInfo mRoomInfo) {
        this.mRoomInfo = mRoomInfo;
    }

    public void init(Long appID, String appSign, boolean isTestEnv, Application application) {
        zegoExpressEngine = ZegoExpressEngine.createEngine(appID, appSign, isTestEnv, ZegoScenario.GENERAL, application, null);
        zim = ZIM.create(appID, application);
        messageManager.setZim(zim);
        speakerSeatManager.setZim(zim);
        speakerSeatManager.setZegoExpressEngine(zegoExpressEngine);
        this.application = application;


        initZIMEventHandler();
        zim.setEventHandler(zimEventHandler);
    }


    public void login(ZegoLiveAudioRoomUserInfo userInfo, String token,
                      final OnLoginCallback onLoginCallback) {
        mUser = new ZegoLiveAudioRoomUser();
        mUser.setUserID(userInfo.getUserID());
        mUser.setUserName(userInfo.getUserName());
        messageManager.setUser(mUser);
        speakerSeatManager.setUser(mUser);
        ZIMUserInfo zimUserInfo = new ZIMUserInfo();
        zimUserInfo.userID = userInfo.getUserID();
        zimUserInfo.userName = userInfo.getUserName();
        zim.login(zimUserInfo, token, new ZIMLoggedInCallback() {
            @Override
            public void onLoggedIn(ZIMError errorInfo) {
                Log.d(TAG, "onLoggedIn() called with: errorInfo = [" + errorInfo.code + ", " + errorInfo.message + "]");
                ZegoLiveAudioRoomErrorCode errorCode;
                if (errorInfo.code.equals(ZIMErrorCode.SUCCESS)) {
                    errorCode = SUCCESS;
                } else {
                    errorCode = ERROR;
                }
                if (onLoginCallback != null) {
                    onLoginCallback.onConnectionState(errorCode);
                }
            }
        });
    }


    public void createRoom(String roomID,
                           String roomName,
                           final String rtcToken,
                           final CreateRoomCallback createRoomCallback) {
        mUser.setUserRole(ZegoLiveAudioRoomUserRole.OWNER);
        mRoomInfo = new ZegoLiveAudioRoomInfo();
        mRoomInfo.setRoom_id(roomID);
        mRoomInfo.setRoom_Name(roomName);
        mRoomInfo.setAuthor(mUser.getUserID());
        mRoomInfo.setSeat_count(8);
        mRoomInfo.setIs_mute_msg(false);

        messageManager.setRoomInfo(mRoomInfo);
        speakerSeatManager.setRoomID(roomID);
        ZIMRoomInfo zimRoomInfo = new ZIMRoomInfo();
        zimRoomInfo.roomID = roomID;
        zimRoomInfo.roomName = roomName;

        HashMap<String, String> roomAttributes = new HashMap<String, String>();
        roomAttributes.put("room_info", GsonChanger.getInstance().getJsonOfZegoLiveAudioRoomInfo(mRoomInfo));
        ZIMRoomAdvancedConfig config = new ZIMRoomAdvancedConfig();
        config.roomAttributes = roomAttributes;

        zim.createRoom(zimRoomInfo, config, new ZIMRoomCreatedCallback() {
            @Override
            public void onRoomCreated(ZIMRoomFullInfo roomInfo, ZIMError errorInfo) {
                if (errorInfo.code == ZIMErrorCode.SUCCESS) {

                    createRoomCallback.onCreateRoomState(SUCCESS);

                    speakerSeatManager.setupRTCModule(rtcToken, new SetupRTCModuleCallback() {
                        @Override
                        public void onConnectionState(ZegoLiveAudioRoomErrorCode error) {

                        }
                    });

                } else {
                    if (errorInfo.code == ZIMErrorCode.CREATE_EXIST_ROOM) {
                        createRoomCallback.onCreateRoomState(ZegoLiveAudioRoomErrorCode.ROOM_EXISTED);
                    } else {
                        createRoomCallback.onCreateRoomState(ERROR);
                    }
                }
            }
        });

    }


    public void joinRoom(String roomID, final String rtcToken, final JoinRoomCallback joinRoomCallback) {
        mUser.setUserRole(ZegoLiveAudioRoomUserRole.VISITOR);
        speakerSeatManager.setRoomID(roomID);
        mRoomInfo = new ZegoLiveAudioRoomInfo();
        mRoomInfo.setRoom_id(roomID);
        mRoomInfo.setAuthor(mUser.getUserID());
        mRoomInfo.setSeat_count(8);
        mRoomInfo.setIs_mute_msg(false);
        messageManager.setRoomInfo(mRoomInfo);
        zim.joinRoom(roomID, new ZIMRoomJoinedCallback() {
            @Override
            public void onRoomJoined(ZIMRoomFullInfo roomInfo, ZIMError errorInfo) {
                //ZegoLiveAudioRoomErrorCode ZegoLiveAudioRoomErrorCode;
                if (errorInfo.code.equals(ZIMErrorCode.SUCCESS)) {
                    if (joinRoomCallback != null) {
                        joinRoomCallback.onConnectionState(SUCCESS);
                    }


                    speakerSeatManager.setupRTCModule(rtcToken, new SetupRTCModuleCallback() {
                        @Override
                        public void onConnectionState(ZegoLiveAudioRoomErrorCode error) {

                        }
                    });
                } else if (errorInfo.code.equals(ZIMErrorCode.ROOM_NOT_EXIST)) {

                    if (joinRoomCallback != null) {
                        joinRoomCallback.onConnectionState(ZegoLiveAudioRoomErrorCode.ROOM_NOT_FOUND);
                    }

                } else {
                    if (joinRoomCallback != null) {
                        joinRoomCallback.onConnectionState(ERROR);
                    }
                }
            }
        });
    }


    public void leaveRoom(String roomID, final OnLeaveCallback onLeaveCallback) {

        speakerSeatManager.releaseRTCModule();
        zim.leaveRoom(roomID, new ZIMRoomLeftCallback() {
            @Override
            public void onRoomLeft(ZIMError errorInfo) {
                ZegoLiveAudioRoomErrorCode errorCode;
                if (errorInfo.code.equals(ZIMErrorCode.SUCCESS)) {
                    errorCode = SUCCESS;
                } else {
                    errorCode = ERROR;
                }
                if (onLeaveCallback != null) {
                    onLeaveCallback.onConnectionState(errorCode);
                }
            }
        });
        speakerSeatManager.releaseVideoSeats();
    }


    public void renewRTCToken(String token) {
        zegoExpressEngine.renewToken(mRoomInfo.getRoom_id(), token);
    }


    public void renewZIMToken(String token) {
        zim.renewToken(token, null);
    }


    public void unInit() {
        zim.destroy();

        ZegoExpressEngine.destroyEngine(null);
    }

    public void queryRoomMember(String roomID, ZegoLiveAudioRoomQueryMemberConfig config, final OnQueryRoomMemberCallback onQueryRoomMember) {
        ZIMQueryMemberConfig zimConfig = new ZIMQueryMemberConfig();
        zimConfig.count = config.getCount();
        zimConfig.nextFlag = config.getNextFlag();
        zim.queryRoomMember(roomID, zimConfig, new ZIMMemberQueriedCallback() {
            @Override
            public void onMemberQueried(ArrayList<ZIMUserInfo> memberList, String nextFlag, ZIMError errorInfo) {
                roomUsers = getRoomUsers(memberList);
                ZegoLiveAudioRoomErrorCode errorCode;
                if (errorInfo.code.equals(ZIMErrorCode.SUCCESS)) {
                    errorCode = SUCCESS;
                } else {
                    errorCode = ERROR;
                }
                if (onQueryRoomMember != null) {
                    onQueryRoomMember.onQueryRoomMember(roomUsers, nextFlag, errorCode);
                }
            }
        });
    }

    /*
         Util
    */

    private ArrayList<ZegoLiveAudioRoomUser> getRoomUsers(ArrayList<ZIMUserInfo> memberList) {
        ArrayList<ZegoLiveAudioRoomUser> roomUsers = new ArrayList<>();
        for (ZIMUserInfo userInfo : memberList) {
            ZegoLiveAudioRoomUser roomUser = new ZegoLiveAudioRoomUser();
            roomUser.setUserID(userInfo.userID);
            roomUser.setUserName(userInfo.userName);

            if (userInfo.userID.equals(mRoomInfo.getAuthor())) {
                roomUser.setUserRole(ZegoLiveAudioRoomUserRole.OWNER);
            } else {
                roomUser.setUserRole(ZegoLiveAudioRoomUserRole.VISITOR);
            }
            roomUsers.add(roomUser);
        }
        return roomUsers;
    }

    /**
     * dont contain self
     * @return
     */
    public List<ZegoLiveAudioRoomUser> getRoomUserList(){
        return roomUsers;
    }

    public String getRoomUserName(String userID){
        for (ZegoLiveAudioRoomUser roomUser : roomUsers) {
            if(roomUser.getUserID().equals(userID)){
                return roomUser.getUserName();
            }
        }
        if(userID.equals(mUser.getUserID())){
            return mUser.getUserName();
        }
        return "";
    }

    // may contain self
    public List<String> getSeatedIDList(){
        List<String> seatedUserIDs = speakerSeatManager.getSeatedUserIDs();
        List<String> list = new ArrayList<>();
        list.addAll(seatedUserIDs);
        return list;
    }

    public void queryRoomInfo(String roomID, final OnQueryRoomInfoCallback onQueryRoomMember) {
        final ZegoLiveAudioRoomInfo[] ZegoLiveAudioRoomInfo = new ZegoLiveAudioRoomInfo[1];
        zim.queryRoomAllAttributes(roomID, new ZIMRoomAllAttributesQueriedCallback() {
            @Override
            public void onAllAttributesQueried(HashMap<String, String> roomAttributes, ZIMError errorInfo) {
                String room_info_string = roomAttributes.get("room_info");
                ZegoLiveAudioRoomInfo[0] = GsonChanger.getInstance().getZegoLiveAudioRoomInfo(room_info_string);
                onQueryRoomMember.onQueryRoomMember(ZegoLiveAudioRoomInfo[0]);
            }
        });
    }

    public ZegoLiveAudioRoomUser getMyUserInfo() {
        return mUser;
    }

    public ZegoLiveAudioRoomInfo getRoomInfo() {
        return mRoomInfo;
    }

    public void sendRoomMessage(String message, SendRoomMessageCallback sendRoomMessageCallback) {
        messageManager.sendRoomMessage(message, sendRoomMessageCallback);
    }


    public void sendGiftMessage(int giftType, List<String> userIDs, SendGiftMessageCallback
            sendGiftMessageCallback) {
        messageManager.sendGiftMessage(giftType, userIDs, sendGiftMessageCallback);
    }


    public void sendInvitation(String toUserID, SendInvitationStatusCallback
            sendInvitationStatusCallback) {
        messageManager.sendInvitation(toUserID, sendInvitationStatusCallback);
    }


    public void respondInvitation(ZegoLiveAudioRoomInvitationStatus
                                          status, SendInvitationStatusCallback sendInvitationStatusCallback) {
        messageManager.respondInvitation(status, sendInvitationStatusCallback);
    }


    public void muteAllMessage(boolean isMuted, MuteAllMessageCallback muteAllMessageCallback) {
        messageManager.muteAllMessage(isMuted, muteAllMessageCallback);
    }

    public void kickUserToSeat(String userID, KickUserToSeatCallback kickUserToSeat) {
        speakerSeatManager.kickUserToSeat(userID, kickUserToSeat);
    }

    public void lockSeat(boolean isLocked, int seatIndex, LockSeatCallback lockSeat) {
        speakerSeatManager.lockSeat(isLocked, seatIndex, lockSeat);
    }

    public void muteSeat(boolean isMuted, MuteSeatCallback muteSeat) {
        speakerSeatManager.muteSeat(isMuted, muteSeat);
    }

    public void enterSeat(int seatIndex, EnterSeatCallback enterSeatCallback) {
        speakerSeatManager.enterSeat(seatIndex, false, enterSeatCallback);
    }

    public void leaveSeat(LeaveSeatCallback leaveSeatCallback) {
        speakerSeatManager.leaveSeat(leaveSeatCallback);
    }

    public void switchSeat(int toSeatIndex, SwitchSeatCallback switchSeatCallback) {
        speakerSeatManager.switchSeat(toSeatIndex, switchSeatCallback);
    }

    public void muteSpeaker(boolean isMuted) {
        speakerSeatManager.getAudioProfileManager().muteSpeaker(isMuted);
    }

    public void logout() {
        if (zim != null) {
            zim.logout();
        }
        roomUsers.clear();
    }

    public void uploadLog(final LogUploadedCallback callback) {
        if (zim != null) {
            zim.uploadLog(new ZIMLogUploadedCallback() {
                @Override
                public void onLogUploaded(ZIMError errorInfo) {
                    ZegoLiveAudioRoomErrorCode errorCode;
                    if (errorInfo.code.equals(ZIMErrorCode.SUCCESS)) {
                        errorCode = SUCCESS;
                    } else {
                        errorCode = ERROR;
                    }
                    callback.onLogUploaded(errorCode);
                }
            });
        }
    }
}
