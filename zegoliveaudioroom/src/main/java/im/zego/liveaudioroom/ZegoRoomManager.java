package im.zego.liveaudioroom;

import android.app.Application;
import im.zego.liveaudioroom.callback.ZegoRoomCallback;
import im.zego.liveaudioroom.service.ZegoGiftService;
import im.zego.liveaudioroom.service.ZegoMessageService;
import im.zego.liveaudioroom.service.ZegoRoomService;
import im.zego.liveaudioroom.service.ZegoSpeakerSeatService;
import im.zego.liveaudioroom.service.ZegoUserService;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.constants.ZegoStreamQualityLevel;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.entity.ZegoEngineProfile;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zim.ZIM;
import im.zego.zim.callback.ZIMEventHandler;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.entity.ZIMRoomAttributesUpdateInfo;
import im.zego.zim.entity.ZIMUserInfo;
import im.zego.zim.enums.ZIMConnectionEvent;
import im.zego.zim.enums.ZIMConnectionState;
import im.zego.zim.enums.ZIMRoomEvent;
import im.zego.zim.enums.ZIMRoomState;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONObject;

/**
 * Created by rocket_wang on 2021/12/14.
 */
public class ZegoRoomManager {

    private static volatile ZegoRoomManager singleton = null;

    private ZegoRoomManager() {
    }

    public static ZegoRoomManager getInstance() {
        if (singleton == null) {
            synchronized (ZegoRoomManager.class) {
                if (singleton == null) {
                    singleton = new ZegoRoomManager();
                }
            }
        }
        return singleton;
    }

    public ZegoRoomService roomService;
    public ZegoUserService userService;
    public ZegoSpeakerSeatService speakerSeatService;
    public ZegoMessageService messageService;
    public ZegoGiftService giftService;

    public void init(long appID, String appSign, Application application) {
        roomService = new ZegoRoomService();
        userService = new ZegoUserService();
        speakerSeatService = new ZegoSpeakerSeatService();
        messageService = new ZegoMessageService();
        giftService = new ZegoGiftService();

        ZegoEngineProfile profile = new ZegoEngineProfile();
        profile.appID = appID;
        profile.appSign = appSign;
        profile.scenario = ZegoScenario.COMMUNICATION;
        profile.application = application;
        ZegoExpressEngine.createEngine(profile, new IZegoEventHandler() {
            @Override
            public void onNetworkQuality(String userID, ZegoStreamQualityLevel upstreamQuality,
                ZegoStreamQualityLevel downstreamQuality) {
                super.onNetworkQuality(userID, upstreamQuality, downstreamQuality);
                if (speakerSeatService != null) {
                    speakerSeatService.onNetworkQuality(userID, upstreamQuality, downstreamQuality);
                }
            }

            @Override
            public void onCapturedSoundLevelUpdate(float soundLevel) {
                super.onCapturedSoundLevelUpdate(soundLevel);
                if (speakerSeatService != null) {
                    speakerSeatService.updateLocalUserSoundLevel(soundLevel);
                }
            }


            @Override
            public void onRemoteSoundLevelUpdate(HashMap<String, Float> soundLevels) {
                super.onRemoteSoundLevelUpdate(soundLevels);
                if (speakerSeatService != null) {
                    speakerSeatService.updateRemoteUsersSoundLevel(soundLevels);
                }
            }

            @Override
            public void onRoomStreamUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoStream> streamList,
                JSONObject extendedData) {
                super.onRoomStreamUpdate(roomID, updateType, streamList, extendedData);
                if (roomService != null) {
                    roomService.onRoomStreamUpdate(roomID, updateType, streamList);
                }
            }
        });

        ZegoZIMManager.getInstance().createZIM(appID, application);
        // distribute to specific services which listening what they want
        ZegoZIMManager.getInstance().zim.setEventHandler(new ZIMEventHandler() {
            @Override
            public void onConnectionStateChanged(ZIM zim, ZIMConnectionState state, ZIMConnectionEvent event,
                JSONObject extendedData) {
                super.onConnectionStateChanged(zim, state, event, extendedData);
                if (roomService != null) {
                    roomService.onConnectionStateChanged(zim, state, event, extendedData);
                }
            }

            @Override
            public void onError(ZIM zim, ZIMError errorInfo) {
                super.onError(zim, errorInfo);
            }

            @Override
            public void onTokenWillExpire(ZIM zim, int second) {
                super.onTokenWillExpire(zim, second);
            }

            @Override
            public void onReceivePeerMessage(ZIM zim, ArrayList<ZIMMessage> messageList, String fromUserID) {
                super.onReceivePeerMessage(zim, messageList, fromUserID);
                if (userService != null) {
                    userService.onReceivePeerMessage(zim, messageList, fromUserID);
                }
            }

            @Override
            public void onReceiveRoomMessage(ZIM zim, ArrayList<ZIMMessage> messageList, String fromRoomID) {
                super.onReceiveRoomMessage(zim, messageList, fromRoomID);
                if (giftService != null) {
                    giftService.onReceiveRoomMessage(zim, messageList, fromRoomID);
                }
                if (messageService != null) {
                    messageService.onReceiveRoomMessage(zim, messageList, fromRoomID);
                }
            }

            @Override
            public void onRoomMemberJoined(ZIM zim, ArrayList<ZIMUserInfo> memberList, String roomID) {
                super.onRoomMemberJoined(zim, memberList, roomID);
                if (userService != null) {
                    userService.onRoomMemberJoined(zim, memberList, roomID);
                }
            }

            @Override
            public void onRoomMemberLeft(ZIM zim, ArrayList<ZIMUserInfo> memberList, String roomID) {
                super.onRoomMemberLeft(zim, memberList, roomID);
                if (userService != null) {
                    userService.onRoomMemberLeft(zim, memberList, roomID);
                }
            }

            @Override
            public void onRoomStateChanged(ZIM zim, ZIMRoomState state, ZIMRoomEvent event, JSONObject extendedData,
                String roomID) {
                super.onRoomStateChanged(zim, state, event, extendedData, roomID);
                if (roomService != null) {
                    roomService.onRoomStateChanged(zim, state, event, extendedData, roomID);
                }
            }

            @Override
            public void onRoomAttributesUpdated(ZIM zim, ZIMRoomAttributesUpdateInfo info, String roomID) {
                super.onRoomAttributesUpdated(zim, info, roomID);
                if (roomService != null) {
                    roomService.onRoomAttributesUpdated(zim, info, roomID);
                }
                if (speakerSeatService != null) {
                    speakerSeatService.onRoomAttributesUpdated(zim, info, roomID);
                }
            }

            @Override
            public void onRoomAttributesBatchUpdated(ZIM zim, ArrayList<ZIMRoomAttributesUpdateInfo> infos,
                String roomID) {
                super.onRoomAttributesBatchUpdated(zim, infos, roomID);
            }
        });
    }

    public void unInit() {
        ZegoZIMManager.getInstance().destroyZIM();
        ZegoExpressEngine.destroyEngine(null);
    }

    public void uploadLog(final ZegoRoomCallback callback) {
        ZegoZIMManager.getInstance().zim
            .uploadLog(errorInfo -> callback.roomCallback(errorInfo.code.value()));
    }
}
