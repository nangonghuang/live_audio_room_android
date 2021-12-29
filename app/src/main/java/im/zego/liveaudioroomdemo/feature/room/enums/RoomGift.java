package im.zego.liveaudioroomdemo.feature.room.enums;

import androidx.annotation.IdRes;
import androidx.annotation.StringRes;
import im.zego.liveaudioroomdemo.R;
import java.io.Serializable;

public enum RoomGift implements Serializable {
    GIFT_HEART("0", R.string.room_page_gift_heart, R.drawable.gift1);

    private final String id;
    @StringRes
    private final int name;
    @IdRes
    private final int res;

    RoomGift(String id, int name, int res) {
        this.id = id;
        this.name = name;
        this.res = res;
    }

    public String getId() {
        return id;
    }

    public int getName() {
        return name;
    }

    public int getRes() {
        return res;
    }
}
