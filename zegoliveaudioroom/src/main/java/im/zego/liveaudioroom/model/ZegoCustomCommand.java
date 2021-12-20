package im.zego.liveaudioroom.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import im.zego.zim.entity.ZIMCustomMessage;

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
    public List<String> target = new ArrayList<>();

    @SerializedName("content")
    public Map<String, String> content = new HashMap<>();

    public void toJson() {
        message = new Gson().toJson(this).getBytes(StandardCharsets.UTF_8);
    }

    public void fromJson(byte[] message) {
        ZegoCustomCommand zegoCustomCommand = new Gson().fromJson(new String(message), ZegoCustomCommand.class);
        message = zegoCustomCommand.message;
        actionType = zegoCustomCommand.actionType;
        target = zegoCustomCommand.target;
        content = zegoCustomCommand.content;
    }
}
