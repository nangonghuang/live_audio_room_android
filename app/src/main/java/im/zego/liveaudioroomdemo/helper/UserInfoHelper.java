package im.zego.liveaudioroomdemo.helper;

import android.graphics.drawable.Drawable;

import com.blankj.utilcode.util.ResourceUtils;

import java.security.MessageDigest;

import im.zego.liveaudioroom.ZegoRoomManager;
import im.zego.liveaudioroom.model.ZegoRoomInfo;
import im.zego.liveaudioroom.model.ZegoRoomUserRole;
import im.zego.liveaudioroom.model.ZegoUserInfo;

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

    public static Drawable getAvatarByUserName(String userName) {
        int index = getIndex(userName);
        return getUserAvatar(index);
    }

    private static Drawable getUserAvatar(int position) {
        return ResourceUtils.getDrawable(
            ResourceUtils.getDrawableIdByName("icon_avatar_" + (position % MAX_INDEX + 1)));
    }

    private static int getIndex(String userName) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("md5");
            digest.update(userName.getBytes());
            final byte[] value = digest.digest();
            int value0 = value[0] & 0xff;
            return Math.abs(value0 % MAX_INDEX);
        } catch (Exception exc) {
            return 0;
        }
    }
}