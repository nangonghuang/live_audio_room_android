package im.zego.liveaudioroom.callback;

import im.zego.liveaudioroom.internal.entity.ZIMChatRoomInfo;

import java.util.ArrayList;


public interface OnQueryChatRoomInfoCallback {
    void onQueryRoomMember(ZIMChatRoomInfo info);
}