package im.zego.liveaudioroom.callback;

import im.zego.liveaudioroom.emus.ZIMChatRoomErrorCode;
import im.zego.liveaudioroom.entity.ZIMChatRoomUser;

import java.util.ArrayList;


public interface OnQueryRoomMemberCallback {
    void onQueryRoomMember(ArrayList<ZIMChatRoomUser> userList, String nextFlag, ZIMChatRoomErrorCode errorCode);
}