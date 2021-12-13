package im.zego.liveaudioroom.entity;

import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomMessageAction;

public class ZegoLiveAudioRoomMessage {
    ZegoLiveAudioRoomMessageAction action;
    ZegoLiveAudioRoomInvitation invitationMessage;
    ZegoLiveAudioRoomGift giftMessage;
    ZegoLiveAudioRoomText textMessage;
    ZegoLiveAudioRoomInvitationRespond invitationRespond;
    ZegoLiveAudioRoomGiftBroadcast giftBroadcast;

    public ZegoLiveAudioRoomMessage(ZegoLiveAudioRoomMessageAction action, ZegoLiveAudioRoomText textMessage) {
        this.action = action;
        this.textMessage = textMessage;
    }

    public ZegoLiveAudioRoomMessage(ZegoLiveAudioRoomMessageAction action, ZegoLiveAudioRoomInvitation invitationMessage) {
        this.action = action;
        this.invitationMessage = invitationMessage;
    }

    public ZegoLiveAudioRoomMessage(ZegoLiveAudioRoomMessageAction action, ZegoLiveAudioRoomGift giftMessage) {
        this.action = action;
        this.giftMessage = giftMessage;
    }

    public ZegoLiveAudioRoomMessage(ZegoLiveAudioRoomMessageAction action, ZegoLiveAudioRoomInvitationRespond invitationRespond) {
        this.action = action;
        this.invitationRespond = invitationRespond;
    }

    public ZegoLiveAudioRoomMessage(ZegoLiveAudioRoomMessageAction action, ZegoLiveAudioRoomGiftBroadcast giftBroadcast) {
        this.action = action;
        this.giftBroadcast = giftBroadcast;
    }

    public ZegoLiveAudioRoomInvitationRespond getInvitationRespond() {
        return invitationRespond;
    }

    public void setInvitationRespond(ZegoLiveAudioRoomInvitationRespond invitationRespond) {
        this.invitationRespond = invitationRespond;
    }

    public ZegoLiveAudioRoomMessageAction getAction() {
        return action;
    }

    public void setAction(ZegoLiveAudioRoomMessageAction action) {
        this.action = action;
    }

    public ZegoLiveAudioRoomInvitation getInvitationMessage() {
        return invitationMessage;
    }

    public void setInvitationMessage(ZegoLiveAudioRoomInvitation invitationMessage) {
        this.invitationMessage = invitationMessage;
    }

    public ZegoLiveAudioRoomGift getGiftMessage() {
        return giftMessage;
    }

    public void setGiftMessage(ZegoLiveAudioRoomGift giftMessage) {
        this.giftMessage = giftMessage;
    }

    public ZegoLiveAudioRoomText getTextMessage() {
        return textMessage;
    }

    public void setTextMessage(ZegoLiveAudioRoomText textMessage) {
        this.textMessage = textMessage;
    }

    public ZegoLiveAudioRoomGiftBroadcast getGiftBroadcast() {
        return giftBroadcast;
    }

    public void setGiftBroadcast(ZegoLiveAudioRoomGiftBroadcast giftBroadcast) {
        this.giftBroadcast = giftBroadcast;
    }
}
