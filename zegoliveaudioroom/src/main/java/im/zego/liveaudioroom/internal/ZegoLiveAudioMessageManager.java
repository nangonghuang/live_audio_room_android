package im.zego.liveaudioroom.internal;

import static im.zego.liveaudioroom.emus.ZegoLiveAudioRoomErrorCode.ERROR;
import static im.zego.liveaudioroom.emus.ZegoLiveAudioRoomErrorCode.SUCCESS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import im.zego.zim.ZIM;
import im.zego.zim.callback.ZIMMessageSentCallback;
import im.zego.zim.callback.ZIMRoomAttributesOperatedCallback;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.entity.ZIMRoomAttributesSetConfig;
import im.zego.zim.entity.ZIMTextMessage;
import im.zego.zim.enums.ZIMErrorCode;
import im.zego.liveaudioroom.callback.MuteAllMessageCallback;
import im.zego.liveaudioroom.callback.SendGiftMessageCallback;
import im.zego.liveaudioroom.callback.SendInvitationStatusCallback;
import im.zego.liveaudioroom.callback.SendRoomMessageCallback;
import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomErrorCode;
import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomInvitationStatus;
import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomMessageAction;
import im.zego.liveaudioroom.entity.ZegoLiveAudioRoomGift;
import im.zego.liveaudioroom.entity.ZegoLiveAudioRoomGiftBroadcast;
import im.zego.liveaudioroom.entity.ZegoLiveAudioRoomInvitation;
import im.zego.liveaudioroom.entity.ZegoLiveAudioRoomInvitationRespond;
import im.zego.liveaudioroom.entity.ZegoLiveAudioRoomMessage;
import im.zego.liveaudioroom.entity.ZegoLiveAudioRoomText;
import im.zego.liveaudioroom.entity.ZegoLiveAudioRoomUser;
import im.zego.liveaudioroom.internal.entity.ZegoLiveAudioRoomInfo;
import im.zego.liveaudioroom.util.GsonChanger;

/**
 * 消息管理模块，在这里实现消息的相关操作，邀请消息等等操作，
 */
public class ZegoLiveAudioMessageManager {
    ZIM zim;
    ZegoLiveAudioRoomUser user;
    String fromInvitationUserID;

    ZegoLiveAudioRoomInfo roomInfo;

    public ZegoLiveAudioMessageManager() {

    }

    public ZIM getZim() {
        return zim;
    }

    public void setZim(ZIM zim) {
        this.zim = zim;
    }


    public ZegoLiveAudioRoomUser getUser() {
        return user;
    }

    public void setUser(ZegoLiveAudioRoomUser user) {
        this.user = user;
    }

    public String getFromInvitationUserID() {
        return fromInvitationUserID;
    }

    public void setFromInvitationUserID(String fromInvitationUserID) {
        this.fromInvitationUserID = fromInvitationUserID;
    }

    public ZegoLiveAudioRoomInfo getRoomInfo() {
        return roomInfo;
    }

    public void setRoomInfo(ZegoLiveAudioRoomInfo roomInfo) {
        this.roomInfo = roomInfo;

    }

    /**
     * 在这里具体实现消息的相应方法
     */

    public void sendRoomMessage(String message, final SendRoomMessageCallback sendRoomMessageCallback) {

        ZegoLiveAudioRoomText textMessage = new ZegoLiveAudioRoomText();
        textMessage.setContent(message);
        textMessage.setFromUserID(user.getUserID());
        ZegoLiveAudioRoomMessage ZegoLiveAudioRoomMessage = new ZegoLiveAudioRoomMessage(ZegoLiveAudioRoomMessageAction.TEXT, textMessage);
        String realMessage = GsonChanger.getInstance().getJsonOfZegoLiveAudioRoomMessage(ZegoLiveAudioRoomMessage);
        ZIMTextMessage zimTextMessage = new ZIMTextMessage();
        zimTextMessage.message = realMessage;
        if (!roomInfo.isIs_mute_msg() || (roomInfo.getAuthor() != null && roomInfo.getAuthor().equals(user.getUserID()))) {
            zim.sendRoomMessage(zimTextMessage, roomInfo.getRoom_id(), new ZIMMessageSentCallback() {
                @Override
                public void onMessageSent(ZIMMessage message, ZIMError errorInfo) {
                    ZegoLiveAudioRoomErrorCode errorCode;
                    if (errorInfo.code.equals(ZIMErrorCode.SUCCESS)) {
                        errorCode = SUCCESS;
                    } else {
                        errorCode = ERROR;
                    }
                    sendRoomMessageCallback.onSendRoomMessage(errorCode);
                }
            });
        } else {
            sendRoomMessageCallback.onSendRoomMessage(ERROR);
        }
    }


    public void sendGiftMessage(int giftType, final List<String> userIDs, final SendGiftMessageCallback sendGiftMessageCallback) {
        ZegoLiveAudioRoomGift gift = new ZegoLiveAudioRoomGift();
        gift.setGiftType(giftType);
        ZegoLiveAudioRoomMessage ZegoLiveAudioRoomMessage = new ZegoLiveAudioRoomMessage(ZegoLiveAudioRoomMessageAction.GIFT, gift);

        String message = GsonChanger.getInstance().getJsonOfZegoLiveAudioRoomMessage(ZegoLiveAudioRoomMessage);
        ZIMTextMessage zimTextMessage = new ZIMTextMessage();
        zimTextMessage.message = message;
        final ArrayList<String> failToUserIDList = new ArrayList<>();

        for (final String toUserID : userIDs) {
            zim.sendPeerMessage(zimTextMessage, toUserID, new ZIMMessageSentCallback() {
                @Override
                public void onMessageSent(ZIMMessage message, ZIMError errorInfo) {
                    if (errorInfo.code.equals(ZIMErrorCode.SUCCESS)) {

                    } else {
                        failToUserIDList.add(toUserID);
                        userIDs.remove(toUserID);
                    }
                }
            });
        }


        ZegoLiveAudioRoomGiftBroadcast giftBroadcast = new ZegoLiveAudioRoomGiftBroadcast();
        giftBroadcast.setGiftType(giftType);
        giftBroadcast.setToUserIDList(userIDs);
        ZegoLiveAudioRoomMessage roomMessage = new ZegoLiveAudioRoomMessage(ZegoLiveAudioRoomMessageAction.GIFT_BROADCAST, giftBroadcast);
        String giftBroadcastMessage = GsonChanger.getInstance().getJsonOfZegoLiveAudioRoomMessage(roomMessage);

        ZIMTextMessage zimGiftBroadcastTextMessage = new ZIMTextMessage();
        zimGiftBroadcastTextMessage.message = giftBroadcastMessage;
        zim.sendRoomMessage(zimGiftBroadcastTextMessage, roomInfo.getRoom_id(), new ZIMMessageSentCallback() {
            @Override
            public void onMessageSent(ZIMMessage message, ZIMError errorInfo) {
                ZegoLiveAudioRoomErrorCode errorCode;
                if (errorInfo.code.equals(ZIMErrorCode.SUCCESS)) {
                    errorCode = SUCCESS;

                } else {
                    errorCode = ERROR;
                }
                sendGiftMessageCallback.onSendGiftMessage(errorCode, failToUserIDList);
            }
        });
    }


    public void sendInvitation(String toUserID, final SendInvitationStatusCallback sendInvitationStatusCallback) {

        ZegoLiveAudioRoomInvitation invitation = new ZegoLiveAudioRoomInvitation();
        invitation.setFromUserID(user.getUserID());
        ZegoLiveAudioRoomMessage invitationMessage = new ZegoLiveAudioRoomMessage(ZegoLiveAudioRoomMessageAction.INVITATION, invitation);
        String message = GsonChanger.getInstance().getJsonOfZegoLiveAudioRoomMessage(invitationMessage);
        ZIMTextMessage zimMessage = new ZIMTextMessage();
        zimMessage.message = message;
        zim.sendPeerMessage(zimMessage, toUserID, new ZIMMessageSentCallback() {
            @Override
            public void onMessageSent(ZIMMessage message, ZIMError errorInfo) {
                ZegoLiveAudioRoomErrorCode errorCode;
                if (errorInfo.code.equals(ZIMErrorCode.SUCCESS)) {
                    errorCode = SUCCESS;

                } else {
                    errorCode = ERROR;
                }
                sendInvitationStatusCallback.sendInvitationStatus(errorCode);
            }
        });

    }


    public void respondInvitation(ZegoLiveAudioRoomInvitationStatus status, final SendInvitationStatusCallback sendInvitationStatusCallback) {

        ZegoLiveAudioRoomInvitationRespond invitationRespond = new ZegoLiveAudioRoomInvitationRespond();
        invitationRespond.setStatus(status);
        ZegoLiveAudioRoomMessage invitationMessage = new ZegoLiveAudioRoomMessage(ZegoLiveAudioRoomMessageAction.INVITATION_RESPOND, invitationRespond);
        String message = GsonChanger.getInstance().getJsonOfZegoLiveAudioRoomMessage(invitationMessage);
        ZIMTextMessage zimMessage = new ZIMTextMessage();
        zimMessage.message = message;
        zim.sendPeerMessage(zimMessage, fromInvitationUserID, new ZIMMessageSentCallback() {
            @Override
            public void onMessageSent(ZIMMessage message, ZIMError errorInfo) {
                ZegoLiveAudioRoomErrorCode errorCode;
                if (errorInfo.code.equals(ZIMErrorCode.SUCCESS)) {
                    errorCode = SUCCESS;

                } else {
                    errorCode = ERROR;
                }
                sendInvitationStatusCallback.sendInvitationStatus(errorCode);
            }
        });
    }


    public void muteAllMessage(boolean isMuted, final MuteAllMessageCallback muteAllMessageCallback) {
        HashMap<String, String> roomConfig = new HashMap<String, String>();
        roomInfo.setIs_mute_msg(isMuted);

        String string_config = GsonChanger.getInstance().getJsonOfZegoLiveAudioRoomInfo(roomInfo);
        roomConfig.put("room_info", string_config);

        ZIMRoomAttributesSetConfig setConfig = new ZIMRoomAttributesSetConfig();
        setConfig.isForce = true;
        setConfig.isDeleteAfterOwnerLeft = true;
        zim.setRoomAttributes(roomConfig, roomInfo.getRoom_id(), setConfig, new ZIMRoomAttributesOperatedCallback() {
            @Override
            public void onRoomAttributesOperated(ZIMError errorInfo) {
                ZegoLiveAudioRoomErrorCode errorCode;
                if (errorInfo.code.equals(ZIMErrorCode.SUCCESS)) {
                    errorCode = SUCCESS;
                } else {
                    errorCode = ERROR;
                }
                muteAllMessageCallback.onMuteAllMessage(errorCode);
            }
        });
    }

}
