package im.zego.liveaudioroom.callback;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import im.zego.liveaudioroom.emus.ZIMChatRoomEvent;
import im.zego.liveaudioroom.emus.ZIMChatRoomInvitationStatus;
import im.zego.liveaudioroom.emus.ZIMChatRoomState;
import im.zego.liveaudioroom.entity.ZIMChatRoomUser;
import im.zego.liveaudioroom.entity.ZIMSpeakerSeatUpdateInfo;
import im.zego.liveaudioroom.internal.entity.ZIMChatRoomInfo;

public abstract class ZIMChatRoomEventHandler {

    public ZIMChatRoomEventHandler() {
    }


    public void onRTCTokenWillExpire(String roomID, int remainTimeInSecond) {
    }


    public void onZIMTokenWillExpire(int remainTimeInSecond) {
    }


    public void onRTCModuleError(int errorCode) {
    }

    /**
     * 房间回调设置
     */

    public void onChatRoomStateUpdated(ZIMChatRoomState state, ZIMChatRoomEvent event, String roomID) {
    }


    public void onChatRoomInfoUpdated(ZIMChatRoomInfo chatRoomInfo) {
    }


    public void onChatRoomMemberLeft(ArrayList<ZIMChatRoomUser> userList) {
    }


    public void onChatRoomMemberJoined(ArrayList<ZIMChatRoomUser> userList) {
    }

    /**
     * 消息回调设置
     */

    public void onReceiveRoomMassage(String message, String fromUserID) {
    }

    public void onReceiveGiftMessage(int giftType, String fromUserID) {
    }

    public void onReceiveGiftBroadcastMessage(List<String> toUSerIDList, int giftType, String fromUserID) {
    }

    public void onReceiveInvitation(String fromUserID) {
    }

    public void onResponseInvitation(ZIMChatRoomInvitationStatus zimChatRoomInvitationStatus, String fromUserID) {
    }

    //
    public void onMuteAllMessage(boolean isMuted) {
    }

    public void onConnectionStateChanged(ZIMChatRoomState state, ZIMChatRoomEvent event, JSONObject extendedData) {
    }

    /**
     * 麦位回调
     */

    public void onChatRoomSpeakerSeatUpdated(ArrayList<ZIMSpeakerSeatUpdateInfo> speakerSeatUpdateInfos) {
    }

    public void OnLocalUserSoundLevelUpdated(float soundLevel) {
    }

    public void OnRemoteUserSoundLevelUpdated(HashMap<String, Float> soundLevel) {
    }
}
