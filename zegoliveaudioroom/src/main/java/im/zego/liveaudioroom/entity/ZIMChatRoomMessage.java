package im.zego.liveaudioroom.entity;

import im.zego.liveaudioroom.emus.ZIMChatRoomMessageAction;

public class ZIMChatRoomMessage {
    ZIMChatRoomMessageAction action;
    ZIMChatRoomInvitation invitationMessage;
    ZIMChatRoomGift giftMessage;
    ZIMChatRoomText textMessage;
    ZIMChatRoomInvitationRespond invitationRespond;
    ZIMChatRoomGiftBroadcast giftBroadcast;

    public ZIMChatRoomMessage(ZIMChatRoomMessageAction action, ZIMChatRoomText textMessage) {
        this.action = action;
        this.textMessage = textMessage;
    }

    public ZIMChatRoomMessage(ZIMChatRoomMessageAction action, ZIMChatRoomInvitation invitationMessage) {
        this.action = action;
        this.invitationMessage = invitationMessage;
    }

    public ZIMChatRoomMessage(ZIMChatRoomMessageAction action, ZIMChatRoomGift giftMessage) {
        this.action = action;
        this.giftMessage = giftMessage;
    }

    public ZIMChatRoomMessage(ZIMChatRoomMessageAction action, ZIMChatRoomInvitationRespond invitationRespond) {
        this.action = action;
        this.invitationRespond = invitationRespond;
    }

    public ZIMChatRoomMessage(ZIMChatRoomMessageAction action, ZIMChatRoomGiftBroadcast giftBroadcast) {
        this.action = action;
        this.giftBroadcast = giftBroadcast;
    }

    public ZIMChatRoomInvitationRespond getInvitationRespond() {
        return invitationRespond;
    }

    public void setInvitationRespond(ZIMChatRoomInvitationRespond invitationRespond) {
        this.invitationRespond = invitationRespond;
    }

    public ZIMChatRoomMessageAction getAction() {
        return action;
    }

    public void setAction(ZIMChatRoomMessageAction action) {
        this.action = action;
    }

    public ZIMChatRoomInvitation getInvitationMessage() {
        return invitationMessage;
    }

    public void setInvitationMessage(ZIMChatRoomInvitation invitationMessage) {
        this.invitationMessage = invitationMessage;
    }

    public ZIMChatRoomGift getGiftMessage() {
        return giftMessage;
    }

    public void setGiftMessage(ZIMChatRoomGift giftMessage) {
        this.giftMessage = giftMessage;
    }

    public ZIMChatRoomText getTextMessage() {
        return textMessage;
    }

    public void setTextMessage(ZIMChatRoomText textMessage) {
        this.textMessage = textMessage;
    }

    public ZIMChatRoomGiftBroadcast getGiftBroadcast() {
        return giftBroadcast;
    }

    public void setGiftBroadcast(ZIMChatRoomGiftBroadcast giftBroadcast) {
        this.giftBroadcast = giftBroadcast;
    }
}
