package im.zego.liveaudioroomdemo.helper;

import android.graphics.drawable.Drawable;

import com.blankj.utilcode.util.ResourceUtils;

import im.zego.liveaudioroom.emus.ZIMChatRoomUserRole;
import im.zego.liveaudioroom.entity.ZIMChatRoomUser;
import im.zego.liveaudioroom.internal.ZIMChatRoomManager;
import im.zego.liveaudioroom.internal.entity.ZIMChatRoomInfo;

public final class UserInfoHelper {
    private static final int MAX_INDEX = 8;

    public static boolean isSelfOwner() {
        ZIMChatRoomUser selfUser = ZIMChatRoomManager.getInstance().getMyUserInfo();
        return selfUser.getUserRole() == ZIMChatRoomUserRole.OWNER;
    }

    public static boolean isUserOwner(String userId) {
        ZIMChatRoomInfo roomInfo = ZIMChatRoomManager.getInstance().getChatRoomInfo();
        String ownerID = roomInfo.getAuthor();
        return ownerID.equals(userId);
    }

    public static Drawable getUserAvatar(int position) {
        return ResourceUtils.getDrawable(ResourceUtils.getDrawableIdByName("icon_avatar_" + (position % MAX_INDEX + 1)));
    }
}