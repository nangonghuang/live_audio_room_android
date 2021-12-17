package im.zego.liveaudioroomdemo.helper;

import android.graphics.drawable.Drawable;
import com.blankj.utilcode.util.ResourceUtils;
import im.zego.liveaudioroom.refactor.ZegoRoomManager;
import im.zego.liveaudioroom.refactor.model.ZegoRoomInfo;
import im.zego.liveaudioroom.refactor.model.ZegoRoomUserRole;
import im.zego.liveaudioroom.refactor.model.ZegoUserInfo;

public final class UserInfoHelper {

    private static final int MAX_INDEX = 8;

    public static boolean isSelfOwner() {
        ZegoUserInfo userInfo = ZegoRoomManager.getInstance().userService.localUserInfo;
        return userInfo.getRole() == ZegoRoomUserRole.Host;
    }

    public static boolean isUserOwner(String userId) {
        ZegoRoomInfo roomInfo = ZegoRoomManager.getInstance().roomService.roomInfo;
        return roomInfo.getHostID().equals(userId);
    }

    public static Drawable getUserAvatar(int position) {
        return ResourceUtils.getDrawable(
            ResourceUtils.getDrawableIdByName("icon_avatar_" + (position % MAX_INDEX + 1)));
    }
}