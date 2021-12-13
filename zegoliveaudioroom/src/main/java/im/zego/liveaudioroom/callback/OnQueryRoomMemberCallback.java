package im.zego.liveaudioroom.callback;

import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomErrorCode;
import im.zego.liveaudioroom.entity.ZegoLiveAudioRoomUser;

import java.util.ArrayList;


public interface OnQueryRoomMemberCallback {
    void onQueryRoomMember(ArrayList<ZegoLiveAudioRoomUser> userList, String nextFlag, ZegoLiveAudioRoomErrorCode errorCode);
}