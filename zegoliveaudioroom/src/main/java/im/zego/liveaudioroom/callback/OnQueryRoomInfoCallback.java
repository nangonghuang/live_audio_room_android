package im.zego.liveaudioroom.callback;

import im.zego.liveaudioroom.internal.entity.ZegoLiveAudioRoomInfo;

import java.util.ArrayList;


public interface OnQueryRoomInfoCallback {
    void onQueryRoomMember(ZegoLiveAudioRoomInfo info);
}