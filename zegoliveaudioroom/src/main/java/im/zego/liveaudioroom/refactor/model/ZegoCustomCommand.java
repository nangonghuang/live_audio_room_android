package im.zego.liveaudioroom.refactor.model;

import com.google.gson.annotations.SerializedName;
import im.zego.zim.entity.ZIMCustomMessage;
import java.util.List;
import java.util.Map;

/**
 * send gift or invitation.
 */
public class ZegoCustomCommand extends ZIMCustomMessage {

    public static final int INVITATION = 1;
    public static final int Gift = 2;

    /**
     * [actionType] == 1 means invitation, [actionType] == 2 means gift
     */
    @SerializedName("actionType")
    public int actionType;

    @SerializedName("target")
    public List<String> target;

    @SerializedName("content")
    public Map<String, String> content;
}
