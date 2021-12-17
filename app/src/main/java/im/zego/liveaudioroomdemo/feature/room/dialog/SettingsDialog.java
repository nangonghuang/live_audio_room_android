package im.zego.liveaudioroomdemo.feature.room.dialog;

import android.content.Context;

import androidx.appcompat.widget.SwitchCompat;

import com.blankj.utilcode.util.ToastUtils;

import java.util.List;

import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomErrorCode;
import im.zego.liveaudioroom.internal.ZegoLiveAudioRoomManager;
import im.zego.liveaudioroom.internal.entity.ZegoLiveAudioRoomInfo;
import im.zego.liveaudioroom.refactor.ZegoRoomManager;
import im.zego.liveaudioroom.refactor.model.ZegoSpeakerSeatModel;
import im.zego.liveaudioroom.refactor.model.ZegoSpeakerSeatStatus;
import im.zego.liveaudioroom.refactor.service.ZegoSpeakerSeatService;
import im.zego.liveaudioroomdemo.R;

public class SettingsDialog extends BaseBottomDialog {

    public boolean isCheckedLockAllSeat = false;

    private List<ZegoSpeakerSeatModel> seatList;
    private SwitchCompat switchAllowMic;
    private SwitchCompat switchAllowMessage;

    public SettingsDialog(Context context, List<ZegoSpeakerSeatModel> seatList) {
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
            ZegoRoomManager.getInstance().roomService.disableTextMessage(isChecked, errorCode -> {

            });
        });
        switchAllowMessage.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isCheckedLockAllSeat = isChecked;
            for (ZegoSpeakerSeatModel i : seatList) {
                if (isUserOwner(i.userID)) {
                    continue;
                }
                ZegoSpeakerSeatService seatService = ZegoRoomManager
                    .getInstance().speakerSeatService;
                if (isChecked) {
                    if (i.status == ZegoSpeakerSeatStatus.Untaken) {
                        seatService.closeSeat(true, i.seatIndex, errorCode -> {
                            if (errorCode != ZegoLiveAudioRoomErrorCode.SUCCESS.getValue()) {
                                ToastUtils
                                    .showShort(R.string.toast_lock_seat_fail, errorCode);
                            }
                        });
                    }
                } else {
                    if (i.status == ZegoSpeakerSeatStatus.Closed) {
                        seatService.closeSeat(false, i.seatIndex, errorCode -> {

                        });
                    }
                }
            }
        });
    }

    private boolean isUserOwner(String userId) {
        ZegoLiveAudioRoomInfo roomInfo = ZegoLiveAudioRoomManager.getInstance().getRoomInfo();
        String ownerID = roomInfo.getAuthor();
        return ownerID.equals(userId);
    }
}
