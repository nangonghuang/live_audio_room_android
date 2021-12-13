package im.zego.liveaudioroom.internal.impl;

import android.app.Application;

import java.util.List;

import im.zego.liveaudioroom.ZIMChatRoom;
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
import im.zego.liveaudioroom.callback.SwitchSeatCallback;
import im.zego.liveaudioroom.callback.ZIMChatRoomEventHandler;
import im.zego.liveaudioroom.emus.ZIMChatRoomInvitationStatus;
import im.zego.liveaudioroom.entity.ZIMChatRoomQueryMemberConfig;
import im.zego.liveaudioroom.entity.ZIMChatRoomUserInfo;
import im.zego.liveaudioroom.internal.ZIMChatRoomManager;


public class ZIMChatRoomImpl extends ZIMChatRoom {
    private static ZIMChatRoom instance;

    public ZIMChatRoomImpl() {

    }


    public static ZIMChatRoom getInstance() {
        if (instance == null) {
            instance = new ZIMChatRoomImpl();
        }
        return instance;
    }

    @Override
    public void setZIMChatRoomEventHandler(ZIMChatRoomEventHandler chatRoomEventHandler) {
        ZIMChatRoomManager.getInstance().setEventHandler(chatRoomEventHandler);
    }

    /**
     * 房间模块
     */
    @Override
    public void init(Long appID, String appSign, Application application) {
        ZIMChatRoomManager.getInstance().init(appID, appSign, false, application);
    }

    @Override
    public void login(ZIMChatRoomUserInfo userInfo, String token, OnLoginCallback callback) {
        ZIMChatRoomManager.getInstance().login(userInfo, token, callback);
    }

    @Override
    public void createChatRoom(String roomID, String roomName, String rtcToken, CreateChatRoomCallback createChatRoomCallback) {
        ZIMChatRoomManager.getInstance().createChatRoom(roomID, roomName, rtcToken, createChatRoomCallback);
    }


    @Override
    public void joinChatRoom(String roomID, String rtcToken, JoinChatRoomCallback joinChatRoomCallback) {
        ZIMChatRoomManager.getInstance().joinChatRoom(roomID, rtcToken, joinChatRoomCallback);
    }

    @Override
    public void leaveChatRoom(String roomID, OnLeaveCallback onLeaveCallback) {
        ZIMChatRoomManager.getInstance().leaveChatRoom(roomID, onLeaveCallback);
    }

    @Override
    public void renewRTCToken(String token) {
        ZIMChatRoomManager.getInstance().renewRTCToken(token);
    }

    @Override
    public void renewZIMToken(String token) {
        ZIMChatRoomManager.getInstance().renewZIMToken(token);
    }

    @Override
    public void unInit() {
        ZIMChatRoomManager.getInstance().unInit();
    }

    @Override
    public void queryRoomMember(String roomID, ZIMChatRoomQueryMemberConfig config, OnQueryRoomMemberCallback onQueryRoomMember) {
        ZIMChatRoomManager.getInstance().queryRoomMember(roomID, config, onQueryRoomMember);
    }

    @Override
    public void queryChatRoomInfo(String roomID, OnQueryChatRoomInfoCallback onQueryRoomMember) {
        ZIMChatRoomManager.getInstance().queryChatRoomInfo(roomID, onQueryRoomMember);
    }


    /**
     * Message模块
     */
    @Override
    public void sendRoomMessage(String message, SendRoomMessageCallback sendRoomMessageCallback) {
        ZIMChatRoomManager.getInstance().sendRoomMessage(message, sendRoomMessageCallback);
    }

    @Override
    public void sendGiftMessage(int giftType, List<String> userIDs, SendGiftMessageCallback sendGiftMessageCallback) {
        ZIMChatRoomManager.getInstance().sendGiftMessage(giftType, userIDs, sendGiftMessageCallback);
    }

    @Override
    public void sendInvitation(String toUserID, SendInvitationStatusCallback sendInvitationStatusCallback) {
        ZIMChatRoomManager.getInstance().sendInvitation(toUserID, sendInvitationStatusCallback);
    }

    @Override
    public void respondInvitation(ZIMChatRoomInvitationStatus status, SendInvitationStatusCallback sendInvitationStatusCallback) {
        ZIMChatRoomManager.getInstance().respondInvitation(status, sendInvitationStatusCallback);
    }

    @Override
    public void muteAllMessage(boolean isMuted, MuteAllMessageCallback muteAllMessageCallback) {
        ZIMChatRoomManager.getInstance().muteAllMessage(isMuted, muteAllMessageCallback);
    }


    /**
     * 麦位管理
     */
    @Override
    public void kickUserToSeat(String userID, KickUserToSeatCallback kickUserToSeat) {
        ZIMChatRoomManager.getInstance().kickUserToSeat(userID, kickUserToSeat);
    }

    @Override
    public void lockSeat(boolean isLocked, int seatIndex, LockSeatCallback lockSeat) {
        ZIMChatRoomManager.getInstance().lockSeat(isLocked, seatIndex, lockSeat);
    }

    @Override
    public void muteSeat(boolean isMuted, MuteSeatCallback muteSeat) {
        ZIMChatRoomManager.getInstance().muteSeat(isMuted, muteSeat);
    }

    @Override
    public void takeSpeakerSeat(int seatIndex, EnterSeatCallback enterSeatCallback) {
        ZIMChatRoomManager.getInstance().enterSeat(seatIndex, enterSeatCallback);
    }

    @Override
    public void leaveSpeakerSeat(LeaveSeatCallback leaveSeatCallback) {
        ZIMChatRoomManager.getInstance().leaveSeat(leaveSeatCallback);
    }

    @Override
    public void switchSeat(int toSeatIndex, SwitchSeatCallback switchSeatCallback) {
        ZIMChatRoomManager.getInstance().switchSeat(toSeatIndex, switchSeatCallback);
    }

    @Override
    public void logout() {
        ZIMChatRoomManager.getInstance().logout();
    }

    @Override
    public void muteSpeaker(boolean isMuted) {
        ZIMChatRoomManager.getInstance().muteSpeaker(isMuted);
    }

    @Override
    public void uploadLog(LogUploadedCallback callback) {
        ZIMChatRoomManager.getInstance().uploadLog(callback);
    }
}
