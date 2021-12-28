package im.zego.liveaudioroom.model;

import com.google.gson.annotations.SerializedName;
import im.zego.zim.entity.ZIMCustomMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class custom signaling.
 * <p>Description: This class contains the custom signaling related logics, such as send virtual gift, send seat-taking
 * invitation, etc.</>
 */
public class ZegoCustomCommand extends ZIMCustomMessage {

    public static final int INVITATION = 1;
    public static final int Gift = 2;

    /**
     * Custom signaling type 1: Invite to take the speaker seat 2: Send virtual gifts.
     */
    @SerializedName("actionType")
    public int actionType;

    /**
     * Target users.
     */
    @SerializedName("target")
    public List<String> target = new ArrayList<>();

    /**
     * Signaling content Invite to speak: {}, send gift: {"giftID": ""}
     */
    @SerializedName("content")
    public Map<String, String> content = new HashMap<>();
}
