package im.zego.liveaudioroom;

import android.app.Application;

import java.util.List;

import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zim.ZIM;
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
import im.zego.liveaudioroom.callback.SwitchSeatCallback;
import im.zego.liveaudioroom.callback.LiveAudioRoomEventHandler;
import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomInvitationStatus;
import im.zego.liveaudioroom.entity.ZegoLiveAudioRoomQueryMemberConfig;
import im.zego.liveaudioroom.entity.ZegoLiveAudioRoomUserInfo;
import im.zego.liveaudioroom.internal.impl.ZegoLiveAudioRoomImpl;

public abstract class ZegoLiveAudioRoom {
    public ZegoLiveAudioRoom() {
    }

    public static ZegoLiveAudioRoom getInstance() {
        return ZegoLiveAudioRoomImpl.getInstance();
    }

    public abstract void setZegoLiveAudioRoomEventHandler(LiveAudioRoomEventHandler roomEventHandler);

    public abstract void init(Long appID, String appSign, Application application);

    public abstract void login(ZegoLiveAudioRoomUserInfo userInfo, String token, OnLoginCallback onLoginCallback);

    public abstract void renewRTCToken(String token);

    public abstract void renewZIMToken(String token);

    public abstract void logout();

    public abstract void unInit();

    public abstract void createRoom(String roomID, String roomName, String rtcToken, CreateRoomCallback createRoomCallback);

    public abstract void joinRoom(String roomID, String rtcToken, JoinRoomCallback joinRoomCallback);

    public abstract void leaveRoom(String roomID, OnLeaveCallback onLeaveCallback);

    public abstract void queryRoomMember(String roomID, ZegoLiveAudioRoomQueryMemberConfig config, OnQueryRoomMemberCallback onQueryRoomMember);

    public abstract void queryRoomInfo(String roomID, OnQueryRoomInfoCallback onQueryRoomMember);

    public abstract void sendRoomMessage(String message, SendRoomMessageCallback sendRoomMessageCallback);

    public abstract void sendGiftMessage(int giftType, List<String> userIDs, SendGiftMessageCallback sendGiftMessageCallback);

    public abstract void sendInvitation(String toUserID, SendInvitationStatusCallback sendInvitationStatusCallback);

    public abstract void respondInvitation(ZegoLiveAudioRoomInvitationStatus status, SendInvitationStatusCallback sendInvitationStatusCallback);

    public abstract void muteAllMessage(boolean isMuted, MuteAllMessageCallback muteAllMessageCallback);

    public abstract void kickUserToSeat(String userID, KickUserToSeatCallback kickUserToSeat);

    public abstract void lockSeat(boolean isLocked, int seatIndex, LockSeatCallback lockSeat);

    public abstract void muteSeat(boolean isMuted, MuteSeatCallback muteSeat);

    public abstract void takeSpeakerSeat(int seatIndex, EnterSeatCallback enterSeatCallback);

    public abstract void leaveSpeakerSeat(LeaveSeatCallback leaveSeatCallback);

    public abstract void switchSeat(int toSeatIndex, SwitchSeatCallback switchSeatCallback);

    public abstract void muteSpeaker(boolean isMuted);

    public abstract void uploadLog(LogUploadedCallback callback);

    public static String getZIMVersion() {
        return ZIM.getVersion();
    }

    public static String getRTCVersion() {
        return ZegoExpressEngine.getVersion();
    }
}