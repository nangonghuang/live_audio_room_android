package im.zego.liveaudioroomdemo.helper;

import android.graphics.drawable.Drawable;

import com.blankj.utilcode.util.ResourceUtils;

import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomUserRole;
import im.zego.liveaudioroom.entity.ZegoLiveAudioRoomUser;
import im.zego.liveaudioroom.internal.ZegoLiveAudioRoomManager;
import im.zego.liveaudioroom.internal.entity.ZegoLiveAudioRoomInfo;

public final class UserInfoHelper {
    private static final int MAX_INDEX = 8;

    public static boolean isSelfOwner() {
        ZegoLiveAudioRoomUser selfUser = ZegoLiveAudioRoomManager.getInstance().getMyUserInfo();
        return selfUser.getUserRole() == ZegoLiveAudioRoomUserRole.OWNER;
    }

    public static boolean isUserOwner(String userId) {
        ZegoLiveAudioRoomInfo roomInfo = ZegoLiveAudioRoomManager.getInstance().getRoomInfo();
        String ownerID = roomInfo.getAuthor();
        return ownerID.equals(userId);
    }

    public static Drawable getUserAvatar(int position) {
        return ResourceUtils.getDrawable(ResourceUtils.getDrawableIdByName("icon_avatar_" + (position % MAX_INDEX + 1)));
    }
}