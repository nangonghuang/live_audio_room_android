package im.zego.liveaudioroom.internal;

import static im.zego.liveaudioroom.emus.ZIMChatRoomErrorCode.SUCCESS;

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
import im.zego.liveaudioroom.callback.CreateChatRoomCallback;
import im.zego.liveaudioroom.callback.EnterSeatCallback;
import im.zego.liveaudioroom.callback.JoinChatRoomCallback;
import im.zego.liveaudioroom.callback.KickUserToSeatCallback;
import im.zego.liveaudioroom.callback.LeaveSeatCallback;
import im.zego.liveaudioroom.callback.LockSeatCallback;
import im.zego.liveaudioroom.callback.LogUploadedCallback;
import im.zego.liveaudioroom.callback.MuteAllMessageCallback;
import im.zego.liveaudioroom.callback.MuteSeatCallback;
import im.zego.liveaudioroom.callback.OnLeaveCallback;
import im.zego.liveaudioroom.callback.OnLoginCallback;
import im.zego.liveaudioroom.callback.OnQueryChatRoomInfoCallback;
import im.zego.liveaudioroom.callback.OnQueryRoomMemberCallback;
import im.zego.liveaudioroom.callback.SendGiftMessageCallback;
import im.zego.liveaudioroom.callback.SendInvitationStatusCallback;
import im.zego.liveaudioroom.callback.SendRoomMessageCallback;
import im.zego.liveaudioroom.callback.SetupRTCModuleCallback;
import im.zego.liveaudioroom.callback.SwitchSeatCallback;
import im.zego.liveaudioroom.callback.ZIMChatRoomEventHandler;
import im.zego.liveaudioroom.emus.ZIMChatRoomErrorCode;
import im.zego.liveaudioroom.emus.ZIMChatRoomEvent;
import im.zego.liveaudioroom.emus.ZIMChatRoomInvitationStatus;
import im.zego.liveaudioroom.emus.ZIMChatRoomState;
import im.zego.liveaudioroom.emus.ZIMChatRoomUserRole;
import im.zego.liveaudioroom.emus.ZIMChatRoomVoiceStatus;
import im.zego.liveaudioroom.entity.ZIMChatRoomGift;
import im.zego.liveaudioroom.entity.ZIMChatRoomGiftBroadcast;
import im.zego.liveaudioroom.entity.ZIMChatRoomInvitationRespond;
import im.zego.liveaudioroom.entity.ZIMChatRoomMessage;
import im.zego.liveaudioroom.entity.ZIMChatRoomQueryMemberConfig;
import im.zego.liveaudioroom.entity.ZIMChatRoomSeatAttribution;
import im.zego.liveaudioroom.entity.ZIMChatRoomText;
import im.zego.liveaudioroom.entity.ZIMChatRoomUser;
import im.zego.liveaudioroom.entity.ZIMChatRoomUserInfo;
import im.zego.liveaudioroom.entity.ZIMSpeakerSeat;
import im.zego.liveaudioroom.entity.ZIMSpeakerSeatUpdateInfo;
import im.zego.liveaudioroom.internal.entity.ZIMChatRoomInfo;
import im.zego.liveaudioroom.util.GsonChanger;


public class ZIMChatRoomManager {
    private static final String TAG = "ZIMChatRoomManager";
    ZegoExpressEngine zegoExpressEngine;
    ZIM zim;
    ZIMChatRoomUser mUser = new ZIMChatRoomUser();
    Application application;
    ZIMEventHandler zimEventHandler;


    ZIMChatRoomInfo mChatRoomInfo;

    ZIMMessageManager messageManager;

    ZIMSpeakerSeatManager speakerSeatManager;
    //eventHandler
    ZIMChatRoomEventHandler zimChatRoomEventHandler;
    static ZIMChatRoomManager instance;
    private ArrayList<ZIMChatRoomUser> chatRoomUsers;

    public static ZIMChatRoomManager getInstance() {
        if (instance == null) {
            instance = new ZIMChatRoomManager();
        }
        return instance;
    }

    public ZIMChatRoomManager() {
        messageManager = new ZIMMessageManager();
        speakerSeatManager = new ZIMSpeakerSeatManager();
        chatRoomUsers = new ArrayList<>();
    }


    public void setEventHandler(ZIMChatRoomEventHandler chatRoomEventHandler) {
        this.zimChatRoomEventHandler = chatRoomEventHandler;
    }

    public ZIMChatRoomEventHandler getEventHandler() {
        return this.zimChatRoomEventHandler;
    }


    private void initZIMEventHandler() {
        zimEventHandler = new ZIMEventHandler() {
            /**
             消息管理中的回调参数
             */
            @Override
            public void onReceivePeerMessage(ZIM zim, ArrayList<ZIMMessage> messageList, String fromUserID) {
                super.onReceivePeerMessage(zim, messageList, fromUserID);
                for (ZIMMessage zimMessage : messageList) {
                    ZIMTextMessage message = (ZIMTextMessage) zimMessage;
                    ZIMChatRoomMessage chatRoomMessage = GsonChanger.getInstance().getZIMChatRoomMessage(message.message);
                    final ZIMChatRoomEventHandler chatRoomEventHandler = zimChatRoomEventHandler;
                    switch (chatRoomMessage.getAction()) {
                        case INVITATION:

                            if (chatRoomEventHandler != null) {
                                chatRoomEventHandler.onReceiveInvitation(fromUserID);
                            }
                            messageManager.fromInvitationUserID = fromUserID;

                            break;
                        case INVITATION_RESPOND:
                            ZIMChatRoomInvitationRespond invitationRespond = chatRoomMessage.getInvitationRespond();

                            if (chatRoomEventHandler != null) {
                                chatRoomEventHandler.onResponseInvitation(invitationRespond.getStatus(), fromUserID);
                            }
                            break;
                        case GIFT:

                            ZIMChatRoomGift gift = chatRoomMessage.getGiftMessage();
                            if (chatRoomEventHandler != null) {
                                chatRoomEventHandler.onReceiveGiftMessage(gift.getGiftType(), gift.getFromUserID());
                            }
                            break;
                    }
                }
            }

            @Override
            public void onReceiveRoomMessage(ZIM zim, ArrayList<ZIMMessage> messageList, String fromRoomID) {
                super.onReceiveRoomMessage(zim, messageList, fromRoomID);
                final ZIMChatRoomEventHandler chatRoomEventHandler = zimChatRoomEventHandler;

                for (ZIMMessage zimMessage : messageList) {
                    ZIMTextMessage message = (ZIMTextMessage) zimMessage;
                    ZIMChatRoomMessage chatRoomMessage = GsonChanger.getInstance().getZIMChatRoomMessage(message.message);
                    switch (chatRoomMessage.getAction()) {
                        case GIFT_BROADCAST:
                            ZIMChatRoomGiftBroadcast giftBroadcast = chatRoomMessage.getGiftBroadcast();
                            if (chatRoomEventHandler != null) {
                                chatRoomEventHandler.onReceiveGiftBroadcastMessage(giftBroadcast.getToUserIDList(), giftBroadcast.getGiftType(), zimMessage.userID);
                            }
                            break;
                        case TEXT:
                            ZIMChatRoomText text = chatRoomMessage.getTextMessage();
                            if (chatRoomEventHandler != null) {
                                chatRoomEventHandler.onReceiveRoomMassage(text.getContent(), zimMessage.userID);
                            }
                    }
                }
            }

            @Override
            public void onRoomStateChanged(ZIM zim, ZIMRoomState state, ZIMRoomEvent event, JSONObject extendedData, String roomID) {
                super.onRoomStateChanged(zim, state, event, extendedData, roomID);
                final ZIMChatRoomEventHandler chatRoomEventHandler = zimChatRoomEventHandler;
                ZIMChatRoomState chatRoomState = ZIMChatRoomState.getZIMChatRoomState(state.value());
                ZIMChatRoomEvent chatRoomEvent = ZIMChatRoomEvent.getZIMChatRoomEvent(event.value());
                if (chatRoomEventHandler != null) {
                    chatRoomEventHandler.onChatRoomStateUpdated(chatRoomState, chatRoomEvent, roomID);
                }
            }

            @Override
            public void onTokenWillExpire(ZIM zim, int second) {
                super.onTokenWillExpire(zim, second);
                final ZIMChatRoomEventHandler chatRoomEventHandler = zimChatRoomEventHandler;

                if (chatRoomEventHandler != null) {
                    chatRoomEventHandler.onZIMTokenWillExpire(second);
                }
            }

            /**
             用户模块的回调
             */
            @Override
            public void onRoomMemberJoined(ZIM zim, ArrayList<ZIMUserInfo> memberList, String roomID) {
                super.onRoomMemberJoined(zim, memberList, roomID);
                ArrayList<ZIMChatRoomUser> joinUsers = getChatRoomUsers(memberList);
                chatRoomUsers.addAll(joinUsers);
                final ZIMChatRoomEventHandler chatRoomEventHandler = zimChatRoomEventHandler;
                if (chatRoomEventHandler != null) {
                    chatRoomEventHandler.onChatRoomMemberJoined(joinUsers);
                }
            }

            @Override
            public void onRoomMemberLeft(ZIM zim, ArrayList<ZIMUserInfo> memberList, String roomID) {
                super.onRoomMemberLeft(zim, memberList, roomID);
                final ZIMChatRoomEventHandler chatRoomEventHandler = zimChatRoomEventHandler;
                ArrayList<ZIMChatRoomUser> leftUsers = getChatRoomUsers(memberList);
                Iterator<ZIMChatRoomUser> iterator = chatRoomUsers.iterator();
                if (iterator.hasNext()) {
                    ZIMChatRoomUser next = iterator.next();
                    if(leftUsers.contains(next.getUserID())){
                        iterator.remove();
                    }
                }
                if (chatRoomEventHandler != null) {
                    chatRoomEventHandler.onChatRoomMemberLeft(leftUsers);
                }
            }

            /**
             麦位变化监听回调（ZIM房间属性）
             */
            @Override
            public void onRoomAttributesUpdated(ZIM zim, ZIMRoomAttributesUpdateInfo info, String roomID) {
                super.onRoomAttributesUpdated(zim, info, roomID);
                final ZIMChatRoomEventHandler chatRoomEventHandler = zimChatRoomEventHandler;
                ArrayList<ZIMSpeakerSeatUpdateInfo> seatUpdateInfos;

                Set<String> keys = info.roomAttributes.keySet();
                for (String key : keys) {

                    if (!key.equals("room_info")) {
                        if (info.action == ZIMRoomAttributesUpdateAction.SET) {
                            ZIMChatRoomSeatAttribution seatAttribution = null;
                            try {
                                seatAttribution = GsonChanger.getInstance().getZIMChatRoomSeatAttribution(info.roomAttributes.get(key));
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
                                    ZIMSpeakerSeat chatRoomSeat = new ZIMSpeakerSeat();
                                    chatRoomSeat.setAttribution(seatAttribution);
                                    chatRoomSeat.setStatus(ZIMChatRoomVoiceStatus.USED);
                                    speakerSeatManager.onSpeakerSeatAdded(chatRoomSeat);
                                }
                            } else {
                                speakerSeatManager.onSpeakerSeatMuted(seatAttribution.getIndex(), seatAttribution.isIs_muted());
                            }
                        } else if (info.action == ZIMRoomAttributesUpdateAction.DELETE) {
                            String idxStr = key.split("_")[1];
                            int index = Integer.parseInt(idxStr);
                            ZIMChatRoomSeatAttribution seatAttribution = null;
                            try {
                                seatAttribution = GsonChanger.getInstance().getZIMChatRoomSeatAttribution(info.roomAttributes.get(key));
                            } catch (JsonSyntaxException exception) {
                                exception.printStackTrace();
                            }
                            ZIMSpeakerSeat oldSeat = speakerSeatManager.getSpeakerSeat(index);
                            if (oldSeat.getStatus() == ZIMChatRoomVoiceStatus.LOCKED) {
                                speakerSeatManager.onSpeakerSeatLocked(index, false);
                            } else if (oldSeat.getStatus() == ZIMChatRoomVoiceStatus.USED) {
                                speakerSeatManager.onSpeakerSeatRemoved(index);
                            }
                            if (seatAttribution == null) {
                                continue;
                            }
                            speakerSeatManager.speakerSeats.get(seatAttribution.getIndex()).getAttribution().setUser_id("");

                        }
                    } else {

                        if (info.action == ZIMRoomAttributesUpdateAction.SET) {
                            ZIMChatRoomInfo roomInfo = GsonChanger.getInstance().getZIMChatRoomInfo(info.roomAttributes.get(key));

                            if (mChatRoomInfo == null) {
                                setChatRoomInfo(roomInfo);
                            } else {

                                if (chatRoomEventHandler != null) {
                                    chatRoomEventHandler.onChatRoomInfoUpdated(roomInfo);
                                }


                                boolean originIsMuted = mChatRoomInfo.isIs_mute_msg();
                                boolean nowIsMuted = roomInfo.isIs_mute_msg();
                                if (nowIsMuted != originIsMuted) {
                                    if (chatRoomEventHandler != null) {
                                        chatRoomEventHandler.onMuteAllMessage(nowIsMuted);
                                    }
                                }
                                mChatRoomInfo = roomInfo;
                            }
                            messageManager.setChatRoomInfo(mChatRoomInfo);
                        } else {


                            if (chatRoomEventHandler != null) {
                                chatRoomEventHandler.onChatRoomInfoUpdated(null);
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
                final ZIMChatRoomEventHandler chatRoomEventHandler = zimChatRoomEventHandler;
                if (chatRoomEventHandler != null)
                    chatRoomEventHandler.onConnectionStateChanged(ZIMChatRoomState.getZIMChatRoomState(state.value()), ZIMChatRoomEvent.getZIMChatRoomEvent(event.value()), extendedData);
            }

        };

    }

    public void setChatRoomInfo(ZIMChatRoomInfo mChatRoomInfo) {
        this.mChatRoomInfo = mChatRoomInfo;
    }

    /**
     * 在这里具体实现房间内的相应方法
     */
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


    public void login(ZIMChatRoomUserInfo userInfo, String token,
                      final OnLoginCallback onLoginCallback) {
        mUser = new ZIMChatRoomUser();
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
                ZIMChatRoomErrorCode zimChatRoomErrorCode;
                if (errorInfo.code.equals(ZIMErrorCode.SUCCESS)) {
                    zimChatRoomErrorCode = SUCCESS;
                } else {
                    zimChatRoomErrorCode = ZIMChatRoomErrorCode.ERROR;
                }
                if (onLoginCallback != null) {
                    onLoginCallback.onConnectionState(zimChatRoomErrorCode);
                }
            }
        });
    }


    public void createChatRoom(String roomID,
                               String roomName,
                               final String rtcToken,
                               final CreateChatRoomCallback createChatRoomCallback) {
        mUser.setUserRole(ZIMChatRoomUserRole.OWNER);
        mChatRoomInfo = new ZIMChatRoomInfo();
        mChatRoomInfo.setRoom_id(roomID);
        mChatRoomInfo.setRoom_Name(roomName);
        mChatRoomInfo.setAuthor(mUser.getUserID());
        mChatRoomInfo.setSeat_count(8);
        mChatRoomInfo.setIs_mute_msg(false);

        messageManager.setChatRoomInfo(mChatRoomInfo);
        speakerSeatManager.setRoomID(roomID);
        ZIMRoomInfo zimRoomInfo = new ZIMRoomInfo();
        zimRoomInfo.roomID = roomID;
        zimRoomInfo.roomName = roomName;

        HashMap<String, String> roomAttributes = new HashMap<String, String>();
        roomAttributes.put("room_info", GsonChanger.getInstance().getJsonOfZIMChatRoomInfo(mChatRoomInfo));
        ZIMRoomAdvancedConfig config = new ZIMRoomAdvancedConfig();
        config.roomAttributes = roomAttributes;

        zim.createRoom(zimRoomInfo, config, new ZIMRoomCreatedCallback() {
            @Override
            public void onRoomCreated(ZIMRoomFullInfo roomInfo, ZIMError errorInfo) {
                if (errorInfo.code == ZIMErrorCode.SUCCESS) {

                    createChatRoomCallback.onCreateRoomState(SUCCESS);

                    speakerSeatManager.setupRTCModule(rtcToken, new SetupRTCModuleCallback() {
                        @Override
                        public void onConnectionState(ZIMChatRoomErrorCode error) {

                        }
                    });

                } else {
                    if (errorInfo.code == ZIMErrorCode.CREATE_EXIST_ROOM) {
                        createChatRoomCallback.onCreateRoomState(ZIMChatRoomErrorCode.ROOM_EXISTED);
                    } else {
                        createChatRoomCallback.onCreateRoomState(ZIMChatRoomErrorCode.ERROR);
                    }
                }
            }
        });

    }


    public void joinChatRoom(String roomID, final String rtcToken, final JoinChatRoomCallback joinChatRoomCallback) {
        mUser.setUserRole(ZIMChatRoomUserRole.VISITOR);
        speakerSeatManager.setRoomID(roomID);
        mChatRoomInfo = new ZIMChatRoomInfo();
        mChatRoomInfo.setRoom_id(roomID);
        mChatRoomInfo.setAuthor(mUser.getUserID());
        mChatRoomInfo.setSeat_count(8);
        mChatRoomInfo.setIs_mute_msg(false);
        messageManager.setChatRoomInfo(mChatRoomInfo);
        zim.joinRoom(roomID, new ZIMRoomJoinedCallback() {
            @Override
            public void onRoomJoined(ZIMRoomFullInfo roomInfo, ZIMError errorInfo) {
                //ZIMChatRoomErrorCode zimChatRoomErrorCode;
                if (errorInfo.code.equals(ZIMErrorCode.SUCCESS)) {
                    if (joinChatRoomCallback != null) {
                        joinChatRoomCallback.onConnectionState(ZIMChatRoomErrorCode.SUCCESS);
                    }


                    speakerSeatManager.setupRTCModule(rtcToken, new SetupRTCModuleCallback() {
                        @Override
                        public void onConnectionState(ZIMChatRoomErrorCode error) {

                        }
                    });
                } else if (errorInfo.code.equals(ZIMErrorCode.ROOM_NOT_EXIST)) {

                    if (joinChatRoomCallback != null) {
                        joinChatRoomCallback.onConnectionState(ZIMChatRoomErrorCode.ROOM_NOT_FOUND);
                    }

                } else {
                    if (joinChatRoomCallback != null) {
                        joinChatRoomCallback.onConnectionState(ZIMChatRoomErrorCode.ERROR);
                    }
                }
            }
        });
    }


    public void leaveChatRoom(String roomID, final OnLeaveCallback onLeaveCallback) {

        speakerSeatManager.releaseRTCModule();
        zim.leaveRoom(roomID, new ZIMRoomLeftCallback() {
            @Override
            public void onRoomLeft(ZIMError errorInfo) {
                ZIMChatRoomErrorCode zimChatRoomErrorCode;
                if (errorInfo.code.equals(ZIMErrorCode.SUCCESS)) {
                    zimChatRoomErrorCode = SUCCESS;
                } else {
                    zimChatRoomErrorCode = ZIMChatRoomErrorCode.ERROR;
                }
                if (onLeaveCallback != null) {
                    onLeaveCallback.onConnectionState(zimChatRoomErrorCode);
                }
            }
        });
        speakerSeatManager.releaseVideoSeats();
    }


    public void renewRTCToken(String token) {
        zegoExpressEngine.renewToken(mChatRoomInfo.getRoom_id(), token);
    }


    public void renewZIMToken(String token) {
        zim.renewToken(token, null);
    }


    public void unInit() {
        zim.destroy();

        ZegoExpressEngine.destroyEngine(null);
    }

    /**
     * 用户查询的功能
     */

    public void queryRoomMember(String roomID, ZIMChatRoomQueryMemberConfig config, final OnQueryRoomMemberCallback onQueryRoomMember) {
        ZIMQueryMemberConfig zimConfig = new ZIMQueryMemberConfig();
        zimConfig.count = config.getCount();
        zimConfig.nextFlag = config.getNextFlag();
        zim.queryRoomMember(roomID, zimConfig, new ZIMMemberQueriedCallback() {
            @Override
            public void onMemberQueried(ArrayList<ZIMUserInfo> memberList, String nextFlag, ZIMError errorInfo) {
                chatRoomUsers = getChatRoomUsers(memberList);
                ZIMChatRoomErrorCode zimChatRoomErrorCode;
                if (errorInfo.code.equals(ZIMErrorCode.SUCCESS)) {
                    zimChatRoomErrorCode = SUCCESS;
                } else {
                    zimChatRoomErrorCode = ZIMChatRoomErrorCode.ERROR;
                }
                if (onQueryRoomMember != null) {
                    onQueryRoomMember.onQueryRoomMember(chatRoomUsers, nextFlag, zimChatRoomErrorCode);
                }
            }
        });
    }

    /*
         Util
    */

    private ArrayList<ZIMChatRoomUser> getChatRoomUsers(ArrayList<ZIMUserInfo> memberList) {
        ArrayList<ZIMChatRoomUser> chatRoomUsers = new ArrayList<>();
        for (ZIMUserInfo userInfo : memberList) {
            ZIMChatRoomUser chatRoomUser = new ZIMChatRoomUser();
            chatRoomUser.setUserID(userInfo.userID);
            chatRoomUser.setUserName(userInfo.userName);

            if (userInfo.userID.equals(mChatRoomInfo.getAuthor())) {
                chatRoomUser.setUserRole(ZIMChatRoomUserRole.OWNER);
            } else {
                chatRoomUser.setUserRole(ZIMChatRoomUserRole.VISITOR);
            }
            chatRoomUsers.add(chatRoomUser);
        }
        return chatRoomUsers;
    }

    /**
     * dont contain self
     * @return
     */
    public List<ZIMChatRoomUser> getChatRoomUserList(){
        return chatRoomUsers;
    }

    public String getChatRoomUserName(String userID){
        for (ZIMChatRoomUser chatRoomUser : chatRoomUsers) {
            if(chatRoomUser.getUserID().equals(userID)){
                return chatRoomUser.getUserName();
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

    public void queryChatRoomInfo(String roomID, final OnQueryChatRoomInfoCallback onQueryRoomMember) {
        final ZIMChatRoomInfo[] zimChatRoomInfo = new ZIMChatRoomInfo[1];
        zim.queryRoomAllAttributes(roomID, new ZIMRoomAllAttributesQueriedCallback() {
            @Override
            public void onAllAttributesQueried(HashMap<String, String> roomAttributes, ZIMError errorInfo) {
                String room_info_string = roomAttributes.get("room_info");
                zimChatRoomInfo[0] = GsonChanger.getInstance().getZIMChatRoomInfo(room_info_string);
                onQueryRoomMember.onQueryRoomMember(zimChatRoomInfo[0]);
            }
        });
    }

    public ZIMChatRoomUser getMyUserInfo() {
        return mUser;
    }

    public ZIMChatRoomInfo getChatRoomInfo() {
        return mChatRoomInfo;
    }

    /**
     * 消息模块，在这里实现消息通信的相关操作；
     */
    /*
        消息模块的功能
     */
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


    public void respondInvitation(ZIMChatRoomInvitationStatus
                                          status, SendInvitationStatusCallback sendInvitationStatusCallback) {
        messageManager.respondInvitation(status, sendInvitationStatusCallback);
    }


    public void muteAllMessage(boolean isMuted, MuteAllMessageCallback muteAllMessageCallback) {
        messageManager.muteAllMessage(isMuted, muteAllMessageCallback);
    }

    /**
     * 麦位管理
     */
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
        chatRoomUsers.clear();
    }

    public void uploadLog(final LogUploadedCallback callback) {
        if (zim != null) {
            zim.uploadLog(new ZIMLogUploadedCallback() {
                @Override
                public void onLogUploaded(ZIMError errorInfo) {
                    ZIMChatRoomErrorCode zimChatRoomErrorCode;
                    if (errorInfo.code.equals(ZIMErrorCode.SUCCESS)) {
                        zimChatRoomErrorCode = ZIMChatRoomErrorCode.SUCCESS;
                    } else {
                        zimChatRoomErrorCode = ZIMChatRoomErrorCode.ERROR;
                    }
                    callback.onLogUploaded(zimChatRoomErrorCode);
                }
            });
        }
    }
}
