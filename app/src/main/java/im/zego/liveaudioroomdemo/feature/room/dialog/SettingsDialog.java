package im.zego.liveaudioroomdemo.feature.room.dialog;

import android.content.Context;
import androidx.appcompat.widget.SwitchCompat;
import com.blankj.utilcode.util.ToastUtils;
import im.zego.liveaudioroom.ZegoRoomManager;
import im.zego.liveaudioroom.constants.ZegoRoomErrorCode;
import im.zego.liveaudioroom.service.ZegoSpeakerSeatService;
import im.zego.liveaudioroomdemo.R;

public class SettingsDialog extends BaseBottomDialog {
    private SwitchCompat switchDisableMessage;
    private SwitchCompat switchCloseSeat;

    public SettingsDialog(Context context) {
        super(context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_settings;
    }

    @Override
    protected void initView() {
        super.initView();
        switchDisableMessage = findViewById(R.id.switch_disable_message);
        switchCloseSeat = findViewById(R.id.switch_close_seat);
    }

    @Override
    protected void initListener() {
        super.initListener();
        /**
         * disable text message:
         * let the whole user in room can not speak via IM, except Host
         */
        switchDisableMessage.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ZegoRoomManager.getInstance().roomService.disableTextMessage(isChecked, errorCode -> {
                if (errorCode == ZegoRoomErrorCode.SUCCESS) {
                    if (isChecked) {
                        ToastUtils.showShort(R.string.toast_disable_text_chat_success);
                    } else {
                        ToastUtils.showShort(R.string.toast_allow_text_chat_success);
                    }
                }
            });
        });

        /**
         * close all seat:
         * let the whole user in room can not take seat
         */
        switchCloseSeat.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ZegoSpeakerSeatService seatService = ZegoRoomManager.getInstance().speakerSeatService;
            seatService.closeAllSeat(isChecked, errorCode -> {
                if (errorCode != ZegoRoomErrorCode.SUCCESS) {
                    if (isChecked) {
                        ToastUtils.showShort(R.string.toast_lock_seat_fail, errorCode);
                    } else {
                        ToastUtils.showShort(R.string.toast_unlock_seat_fail, errorCode);
                    }
                }
            });
        });
    }
}
