package im.zego.liveaudioroom.listener;

import java.util.List;

import im.zego.liveaudioroom.model.ZegoUserInfo;

/**
 * Created by rocket_wang on 2021/12/14.
 */
public interface ZegoUserServiceListener {
    // room info update
    void userInfoUpdate(ZegoUserInfo userInfo);

    // receive user join room command
    void onRoomUserJoin(List<ZegoUserInfo> memberList);

    // receive user leave room command
    void onRoomUserLeave(List<ZegoUserInfo> memberList);

    void onReceiveTakeSeatInvitation();
}