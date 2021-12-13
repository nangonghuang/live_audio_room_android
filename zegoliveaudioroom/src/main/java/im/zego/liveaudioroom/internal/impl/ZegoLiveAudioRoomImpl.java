package im.zego.liveaudioroom.internal.impl;

import android.app.Application;

import java.util.List;

import im.zego.liveaudioroom.ZegoLiveAudioRoom;
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
import im.zego.liveaudioroom.internal.ZegoLiveAudioRoomManager;


public class ZegoLiveAudioRoomImpl extends ZegoLiveAudioRoom {
    private static ZegoLiveAudioRoom instance;

    public ZegoLiveAudioRoomImpl() {

    }


    public static ZegoLiveAudioRoom getInstance() {
        if (instance == null) {
            instance = new ZegoLiveAudioRoomImpl();
        }
        return instance;
    }

    @Override
    public void setZegoLiveAudioRoomEventHandler(LiveAudioRoomEventHandler roomEventHandler) {
        ZegoLiveAudioRoomManager.getInstance().setEventHandler(roomEventHandler);
    }

    /**
     * 房间模块
     */
    @Override
    public void init(Long appID, String appSign, Application application) {
        ZegoLiveAudioRoomManager.getInstance().init(appID, appSign, false, application);
    }

    @Override
    public void login(ZegoLiveAudioRoomUserInfo userInfo, String token, OnLoginCallback callback) {
        ZegoLiveAudioRoomManager.getInstance().login(userInfo, token, callback);
    }

    @Override
    public void createRoom(String roomID, String roomName, String rtcToken, CreateRoomCallback createRoomCallback) {
        ZegoLiveAudioRoomManager.getInstance().createRoom(roomID, roomName, rtcToken, createRoomCallback);
    }


    @Override
    public void joinRoom(String roomID, String rtcToken, JoinRoomCallback joinRoomCallback) {
        ZegoLiveAudioRoomManager.getInstance().joinRoom(roomID, rtcToken, joinRoomCallback);
    }

    @Override
    public void leaveRoom(String roomID, OnLeaveCallback onLeaveCallback) {
        ZegoLiveAudioRoomManager.getInstance().leaveRoom(roomID, onLeaveCallback);
    }

    @Override
    public void renewRTCToken(String token) {
        ZegoLiveAudioRoomManager.getInstance().renewRTCToken(token);
    }

    @Override
    public void renewZIMToken(String token) {
        ZegoLiveAudioRoomManager.getInstance().renewZIMToken(token);
    }

    @Override
    public void unInit() {
        ZegoLiveAudioRoomManager.getInstance().unInit();
    }

    @Override
    public void queryRoomMember(String roomID, ZegoLiveAudioRoomQueryMemberConfig config, OnQueryRoomMemberCallback onQueryRoomMember) {
        ZegoLiveAudioRoomManager.getInstance().queryRoomMember(roomID, config, onQueryRoomMember);
    }

    @Override
    public void queryRoomInfo(String roomID, OnQueryRoomInfoCallback onQueryRoomMember) {
        ZegoLiveAudioRoomManager.getInstance().queryRoomInfo(roomID, onQueryRoomMember);
    }


    /**
     * Message模块
     */
    @Override
    public void sendRoomMessage(String message, SendRoomMessageCallback sendRoomMessageCallback) {
        ZegoLiveAudioRoomManager.getInstance().sendRoomMessage(message, sendRoomMessageCallback);
    }

    @Override
    public void sendGiftMessage(int giftType, List<String> userIDs, SendGiftMessageCallback sendGiftMessageCallback) {
        ZegoLiveAudioRoomManager.getInstance().sendGiftMessage(giftType, userIDs, sendGiftMessageCallback);
    }

    @Override
    public void sendInvitation(String toUserID, SendInvitationStatusCallback sendInvitationStatusCallback) {
        ZegoLiveAudioRoomManager.getInstance().sendInvitation(toUserID, sendInvitationStatusCallback);
    }

    @Override
    public void respondInvitation(ZegoLiveAudioRoomInvitationStatus status, SendInvitationStatusCallback sendInvitationStatusCallback) {
        ZegoLiveAudioRoomManager.getInstance().respondInvitation(status, sendInvitationStatusCallback);
    }

    @Override
    public void muteAllMessage(boolean isMuted, MuteAllMessageCallback muteAllMessageCallback) {
        ZegoLiveAudioRoomManager.getInstance().muteAllMessage(isMuted, muteAllMessageCallback);
    }


    /**
     * 麦位管理
     */
    @Override
    public void kickUserToSeat(String userID, KickUserToSeatCallback kickUserToSeat) {
        ZegoLiveAudioRoomManager.getInstance().kickUserToSeat(userID, kickUserToSeat);
    }

    @Override
    public void lockSeat(boolean isLocked, int seatIndex, LockSeatCallback lockSeat) {
        ZegoLiveAudioRoomManager.getInstance().lockSeat(isLocked, seatIndex, lockSeat);
    }

    @Override
    public void muteSeat(boolean isMuted, MuteSeatCallback muteSeat) {
        ZegoLiveAudioRoomManager.getInstance().muteSeat(isMuted, muteSeat);
    }

    @Override
    public void takeSpeakerSeat(int seatIndex, EnterSeatCallback enterSeatCallback) {
        ZegoLiveAudioRoomManager.getInstance().enterSeat(seatIndex, enterSeatCallback);
    }

    @Override
    public void leaveSpeakerSeat(LeaveSeatCallback leaveSeatCallback) {
        ZegoLiveAudioRoomManager.getInstance().leaveSeat(leaveSeatCallback);
    }

    @Override
    public void switchSeat(int toSeatIndex, SwitchSeatCallback switchSeatCallback) {
        ZegoLiveAudioRoomManager.getInstance().switchSeat(toSeatIndex, switchSeatCallback);
    }

    @Override
    public void logout() {
        ZegoLiveAudioRoomManager.getInstance().logout();
    }

    @Override
    public void muteSpeaker(boolean isMuted) {
        ZegoLiveAudioRoomManager.getInstance().muteSpeaker(isMuted);
    }

    @Override
    public void uploadLog(LogUploadedCallback callback) {
        ZegoLiveAudioRoomManager.getInstance().uploadLog(callback);
    }
}
