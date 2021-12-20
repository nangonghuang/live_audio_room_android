package im.zego.liveaudioroomdemo.helper;

import android.graphics.drawable.Drawable;

import com.blankj.utilcode.util.ResourceUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

    public static Drawable getAvatarByUserName(String userName) {
        int index = getIndex(userName);
        return getUserAvatar(index);
    }

    private static Drawable getUserAvatar(int position) {
        return ResourceUtils.getDrawable(
                ResourceUtils.getDrawableIdByName("icon_avatar_" + (position % MAX_INDEX + 1)));
    }

    private static int getIndex(String userName) {
        byte[] value;
        try {
            value = md5(userName);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return 0;
        }

        if (value.length > 0) {
            return Math.abs(value[0] % MAX_INDEX);
        } else {
            return 0;
        }
    }

    private static byte[] md5(String input) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("MD5").digest(input.getBytes());
    }
}