package im.zego.liveaudioroom.callback;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomEvent;
import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomInvitationStatus;
import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomState;
import im.zego.liveaudioroom.entity.ZegoLiveAudioRoomUser;
import im.zego.liveaudioroom.entity.ZIMSpeakerSeatUpdateInfo;
import im.zego.liveaudioroom.internal.entity.ZegoLiveAudioRoomInfo;

public abstract class LiveAudioRoomEventHandler {

    public LiveAudioRoomEventHandler() {
    }


    public void onRTCTokenWillExpire(String roomID, int remainTimeInSecond) {
    }


    public void onZIMTokenWillExpire(int remainTimeInSecond) {
    }


    public void onRTCModuleError(int errorCode) {
    }

    public void onRoomStateUpdated(ZegoLiveAudioRoomState state, ZegoLiveAudioRoomEvent event, String roomID) {
    }


    public void onRoomInfoUpdated(ZegoLiveAudioRoomInfo roomInfo) {
    }


    public void onRoomMemberLeft(ArrayList<ZegoLiveAudioRoomUser> userList) {
    }


    public void onRoomMemberJoined(ArrayList<ZegoLiveAudioRoomUser> userList) {
    }

    public void onReceiveRoomMassage(String message, String fromUserID) {
    }

    public void onReceiveGiftMessage(int giftType, String fromUserID) {
    }

    public void onReceiveGiftBroadcastMessage(List<String> toUSerIDList, int giftType, String fromUserID) {
    }

    public void onReceiveInvitation(String fromUserID) {
    }

    public void onResponseInvitation(ZegoLiveAudioRoomInvitationStatus ZegoLiveAudioRoomInvitationStatus, String fromUserID) {
    }

    //
    public void onMuteAllMessage(boolean isMuted) {
    }

    public void onConnectionStateChanged(ZegoLiveAudioRoomState state, ZegoLiveAudioRoomEvent event, JSONObject extendedData) {
    }

    public void onRoomSpeakerSeatUpdated(ArrayList<ZIMSpeakerSeatUpdateInfo> speakerSeatUpdateInfos) {
    }

    public void OnLocalUserSoundLevelUpdated(float soundLevel) {
    }

    public void OnRemoteUserSoundLevelUpdated(HashMap<String, Float> soundLevel) {
    }
}
