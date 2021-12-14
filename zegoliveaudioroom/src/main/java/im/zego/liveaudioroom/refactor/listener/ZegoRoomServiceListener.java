package im.zego.liveaudioroom.refactor.listener;

import im.zego.liveaudioroom.refactor.model.ZegoRoomInfo;
import im.zego.zim.enums.ZIMConnectionEvent;
import im.zego.zim.enums.ZIMConnectionState;

/**
 * Created by rocket_wang on 2021/12/14.
 */
public interface ZegoRoomServiceListener {
    // room info update
    void receiveRoomInfoUpdate(ZegoRoomInfo roomInfo);

    void connectionStateChanged(ZIMConnectionState state, ZIMConnectionEvent event);
}
