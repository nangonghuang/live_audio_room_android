package im.zego.liveaudioroomdemo.feature.chatroom.enums;

import androidx.annotation.IdRes;
import androidx.annotation.StringRes;

import java.io.Serializable;

import im.zego.liveaudioroomdemo.R;

public enum RoomGift implements Serializable {
    GIFT_HEART(0, R.string.room_page_gift_heart, R.drawable.gift1);

    private int type;
    @StringRes
    private int name;
    @IdRes
    private int res;

    RoomGift(int type, int name, int res) {
        this.type = type;
        this.name = name;
        this.res = res;
    }

    public int getType() {
        return type;
    }

    public int getName() {
        return name;
    }

    public int getRes() {
        return res;
    }
}
