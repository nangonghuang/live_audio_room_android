package im.zego.liveaudioroom.refactor.model;

import im.zego.zim.entity.ZIMCustomMessage;
import java.util.List;
import java.util.Map;

public class ZegoCoustomCommand extends ZIMCustomMessage {

    private int actionType;
    private List<String> target;
    private Map<String, Object> content;
}
