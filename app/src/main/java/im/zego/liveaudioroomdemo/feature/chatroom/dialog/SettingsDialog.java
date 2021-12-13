package im.zego.liveaudioroomdemo.feature.chatroom.dialog;

import android.content.Context;

import androidx.appcompat.widget.SwitchCompat;

import com.blankj.utilcode.util.ToastUtils;

import java.util.List;

import im.zego.liveaudioroom.ZIMChatRoom;
import im.zego.liveaudioroom.emus.ZIMChatRoomErrorCode;
import im.zego.liveaudioroom.emus.ZIMChatRoomVoiceStatus;
import im.zego.liveaudioroom.entity.ZIMSpeakerSeat;
import im.zego.liveaudioroom.internal.ZIMChatRoomManager;
import im.zego.liveaudioroom.internal.entity.ZIMChatRoomInfo;
import im.zego.liveaudioroomdemo.R;

public class SettingsDialog extends BaseBottomDialog {
    public boolean isCheckedLockAllSeat = false;

    private List<ZIMSpeakerSeat> seatList;
    private SwitchCompat switchAllowMic;
    private SwitchCompat switchAllowMessage;

    public SettingsDialog(Context context, List<ZIMSpeakerSeat> seatList) {
        super(context);
        this.seatList = seatList;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_settings;
    }

    @Override
    protected void initView() {
        super.initView();
        switchAllowMic = findViewById(R.id.switch_allow_mic);
        switchAllowMessage = findViewById(R.id.switch_allow_message);
    }

    @Override
    protected void initListener() {
        super.initListener();
        switchAllowMic.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ZIMChatRoom.getInstance().muteAllMessage(isChecked, error -> {
            });
        });
        switchAllowMessage.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isCheckedLockAllSeat = isChecked;
            for (ZIMSpeakerSeat i : seatList) {
                if (isUserOwner(i.getAttribution().getUser_id())) {
                    continue;
                }
                if (isChecked) {
                    if (i.getStatus() == ZIMChatRoomVoiceStatus.UNUSED) {
                        ZIMChatRoom.getInstance().lockSeat(true, i.getAttribution().getIndex(), error -> {
                            if (error != ZIMChatRoomErrorCode.SUCCESS) {
                                ToastUtils.showShort(R.string.toast_lock_seat_fail, error.getValue());
                            }
                        });
                    }
                } else {
                    if (i.getStatus() == ZIMChatRoomVoiceStatus.LOCKED) {
                        ZIMChatRoom.getInstance().lockSeat(false, i.getAttribution().getIndex(), error -> {
                        });
                    }
                }
            }
        });
    }

    private boolean isUserOwner(String userId) {
        ZIMChatRoomInfo roomInfo = ZIMChatRoomManager.getInstance().getChatRoomInfo();
        String ownerID = roomInfo.getAuthor();
        return ownerID.equals(userId);
    }
}
