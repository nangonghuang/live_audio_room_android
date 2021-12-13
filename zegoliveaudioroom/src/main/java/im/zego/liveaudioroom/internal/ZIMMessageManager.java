package im.zego.liveaudioroom.internal;

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
import im.zego.liveaudioroom.emus.ZIMChatRoomErrorCode;
import im.zego.liveaudioroom.emus.ZIMChatRoomInvitationStatus;
import im.zego.liveaudioroom.emus.ZIMChatRoomMessageAction;
import im.zego.liveaudioroom.entity.ZIMChatRoomGift;
import im.zego.liveaudioroom.entity.ZIMChatRoomGiftBroadcast;
import im.zego.liveaudioroom.entity.ZIMChatRoomInvitation;
import im.zego.liveaudioroom.entity.ZIMChatRoomInvitationRespond;
import im.zego.liveaudioroom.entity.ZIMChatRoomMessage;
import im.zego.liveaudioroom.entity.ZIMChatRoomText;
import im.zego.liveaudioroom.entity.ZIMChatRoomUser;
import im.zego.liveaudioroom.internal.entity.ZIMChatRoomInfo;
import im.zego.liveaudioroom.util.GsonChanger;

/**
 * 消息管理模块，在这里实现消息的相关操作，邀请消息等等操作，
 */
public class ZIMMessageManager {
    ZIM zim;
    ZIMChatRoomUser user;
    String fromInvitationUserID;

    ZIMChatRoomInfo chatRoomInfo;

    public ZIMMessageManager() {

    }

    public ZIM getZim() {
        return zim;
    }

    public void setZim(ZIM zim) {
        this.zim = zim;
    }


    public ZIMChatRoomUser getUser() {
        return user;
    }

    public void setUser(ZIMChatRoomUser user) {
        this.user = user;
    }

    public String getFromInvitationUserID() {
        return fromInvitationUserID;
    }

    public void setFromInvitationUserID(String fromInvitationUserID) {
        this.fromInvitationUserID = fromInvitationUserID;
    }

    public ZIMChatRoomInfo getChatRoomInfo() {
        return chatRoomInfo;
    }

    public void setChatRoomInfo(ZIMChatRoomInfo chatRoomInfo) {
        this.chatRoomInfo = chatRoomInfo;

    }

    /**
     * 在这里具体实现消息的相应方法
     */

    public void sendRoomMessage(String message, final SendRoomMessageCallback sendRoomMessageCallback) {

        ZIMChatRoomText textMessage = new ZIMChatRoomText();
        textMessage.setContent(message);
        textMessage.setFromUserID(user.getUserID());
        ZIMChatRoomMessage zimChatRoomMessage = new ZIMChatRoomMessage(ZIMChatRoomMessageAction.TEXT, textMessage);
        String realMessage = GsonChanger.getInstance().getJsonOfZIMChatRoomMessage(zimChatRoomMessage);
        ZIMTextMessage zimTextMessage = new ZIMTextMessage();
        zimTextMessage.message = realMessage;
        if (!chatRoomInfo.isIs_mute_msg() || (chatRoomInfo.getAuthor() != null && chatRoomInfo.getAuthor().equals(user.getUserID()))) {
            zim.sendRoomMessage(zimTextMessage, chatRoomInfo.getRoom_id(), new ZIMMessageSentCallback() {
                @Override
                public void onMessageSent(ZIMMessage message, ZIMError errorInfo) {
                    ZIMChatRoomErrorCode zimChatRoomErrorCode;
                    if (errorInfo.code.equals(ZIMErrorCode.SUCCESS)) {
                        zimChatRoomErrorCode = ZIMChatRoomErrorCode.SUCCESS;
                    } else {
                        zimChatRoomErrorCode = ZIMChatRoomErrorCode.ERROR;
                    }
                    sendRoomMessageCallback.onSendRoomMessage(zimChatRoomErrorCode);
                }
            });
        } else {
            sendRoomMessageCallback.onSendRoomMessage(ZIMChatRoomErrorCode.ERROR);
        }
    }


    public void sendGiftMessage(int giftType, final List<String> userIDs, final SendGiftMessageCallback sendGiftMessageCallback) {
        ZIMChatRoomGift gift = new ZIMChatRoomGift();
        gift.setGiftType(giftType);
        ZIMChatRoomMessage zimChatRoomMessage = new ZIMChatRoomMessage(ZIMChatRoomMessageAction.GIFT, gift);

        String message = GsonChanger.getInstance().getJsonOfZIMChatRoomMessage(zimChatRoomMessage);
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


        ZIMChatRoomGiftBroadcast giftBroadcast = new ZIMChatRoomGiftBroadcast();
        giftBroadcast.setGiftType(giftType);
        giftBroadcast.setToUserIDList(userIDs);
        ZIMChatRoomMessage chatRoomGiftBroadcastMessage = new ZIMChatRoomMessage(ZIMChatRoomMessageAction.GIFT_BROADCAST, giftBroadcast);

        String giftBroadcastMessage = GsonChanger.getInstance().getJsonOfZIMChatRoomMessage(chatRoomGiftBroadcastMessage);

        ZIMTextMessage zimGiftBroadcastTextMessage = new ZIMTextMessage();
        zimGiftBroadcastTextMessage.message = giftBroadcastMessage;
        zim.sendRoomMessage(zimGiftBroadcastTextMessage, chatRoomInfo.getRoom_id(), new ZIMMessageSentCallback() {
            @Override
            public void onMessageSent(ZIMMessage message, ZIMError errorInfo) {
                ZIMChatRoomErrorCode zimChatRoomErrorCode;
                if (errorInfo.code.equals(ZIMErrorCode.SUCCESS)) {
                    zimChatRoomErrorCode = ZIMChatRoomErrorCode.SUCCESS;

                } else {
                    zimChatRoomErrorCode = ZIMChatRoomErrorCode.ERROR;
                }
                sendGiftMessageCallback.onSendGiftMessage(zimChatRoomErrorCode, failToUserIDList);
            }
        });
    }


    public void sendInvitation(String toUserID, final SendInvitationStatusCallback sendInvitationStatusCallback) {

        ZIMChatRoomInvitation invitation = new ZIMChatRoomInvitation();
        invitation.setFromUserID(user.getUserID());
        ZIMChatRoomMessage invitationMessage = new ZIMChatRoomMessage(ZIMChatRoomMessageAction.INVITATION, invitation);
        String message = GsonChanger.getInstance().getJsonOfZIMChatRoomMessage(invitationMessage);
        ZIMTextMessage zimMessage = new ZIMTextMessage();
        zimMessage.message = message;
        zim.sendPeerMessage(zimMessage, toUserID, new ZIMMessageSentCallback() {
            @Override
            public void onMessageSent(ZIMMessage message, ZIMError errorInfo) {
                ZIMChatRoomErrorCode zimChatRoomErrorCode;
                if (errorInfo.code.equals(ZIMErrorCode.SUCCESS)) {
                    zimChatRoomErrorCode = ZIMChatRoomErrorCode.SUCCESS;

                } else {
                    zimChatRoomErrorCode = ZIMChatRoomErrorCode.ERROR;
                }
                sendInvitationStatusCallback.sendInvitationStatus(zimChatRoomErrorCode);
            }
        });

    }


    public void respondInvitation(ZIMChatRoomInvitationStatus status, final SendInvitationStatusCallback sendInvitationStatusCallback) {

        ZIMChatRoomInvitationRespond invitationRespond = new ZIMChatRoomInvitationRespond();
        invitationRespond.setStatus(status);
        ZIMChatRoomMessage invitationMessage = new ZIMChatRoomMessage(ZIMChatRoomMessageAction.INVITATION_RESPOND, invitationRespond);
        String message = GsonChanger.getInstance().getJsonOfZIMChatRoomMessage(invitationMessage);
        ZIMTextMessage zimMessage = new ZIMTextMessage();
        zimMessage.message = message;
        zim.sendPeerMessage(zimMessage, fromInvitationUserID, new ZIMMessageSentCallback() {
            @Override
            public void onMessageSent(ZIMMessage message, ZIMError errorInfo) {
                ZIMChatRoomErrorCode zimChatRoomErrorCode;
                if (errorInfo.code.equals(ZIMErrorCode.SUCCESS)) {
                    zimChatRoomErrorCode = ZIMChatRoomErrorCode.SUCCESS;

                } else {
                    zimChatRoomErrorCode = ZIMChatRoomErrorCode.ERROR;
                }
                sendInvitationStatusCallback.sendInvitationStatus(zimChatRoomErrorCode);
            }
        });
    }


    public void muteAllMessage(boolean isMuted, final MuteAllMessageCallback muteAllMessageCallback) {
        HashMap<String, String> roomConfig = new HashMap<String, String>();
        chatRoomInfo.setIs_mute_msg(isMuted);

        String string_config = GsonChanger.getInstance().getJsonOfZIMChatRoomInfo(chatRoomInfo);
        roomConfig.put("room_info", string_config);

        ZIMRoomAttributesSetConfig setConfig = new ZIMRoomAttributesSetConfig();
        setConfig.isForce = true;
        setConfig.isDeleteAfterOwnerLeft = true;
        zim.setRoomAttributes(roomConfig, chatRoomInfo.getRoom_id(), setConfig, new ZIMRoomAttributesOperatedCallback() {
            @Override
            public void onRoomAttributesOperated(ZIMError errorInfo) {
                ZIMChatRoomErrorCode zimChatRoomErrorCode;
                if (errorInfo.code.equals(ZIMErrorCode.SUCCESS)) {
                    zimChatRoomErrorCode = ZIMChatRoomErrorCode.SUCCESS;

                } else {
                    zimChatRoomErrorCode = ZIMChatRoomErrorCode.ERROR;
                }
                muteAllMessageCallback.onMuteAllMessage(zimChatRoomErrorCode);
            }
        });
    }

}
