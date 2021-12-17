package im.zego.liveaudioroomdemo.feature.room.dialog;

import android.content.Context;
import android.view.Gravity;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.ToastUtils;
import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomErrorCode;
import im.zego.liveaudioroom.refactor.ZegoRoomManager;
import im.zego.liveaudioroom.refactor.service.ZegoGiftService;
import im.zego.liveaudioroom.refactor.service.ZegoSpeakerSeatService;
import im.zego.liveaudioroom.refactor.service.ZegoUserService;
import im.zego.liveaudioroomdemo.R;
import im.zego.liveaudioroomdemo.feature.room.adapter.GiftListAdapter;
import im.zego.liveaudioroomdemo.feature.room.enums.RoomGift;
import java.util.List;

/**
 * dialog display when click gift button in the bottom.
 */
public class SendGiftDialog extends BaseBottomDialog {

    private static final String TAG = "SendGiftDialog";
    private TextView tvSendGift;
    private TextView tvChooseMember;
    private GiftTargetPopWindow giftTargetPopWindow;
    private SendGiftListener sendGiftListener;

    public SendGiftDialog(@NonNull Context context) {
        super(context);
    }

    public SendGiftDialog(@NonNull Context context, int theme) {
        super(context, theme);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_send_gift;
    }

    @Override
    protected void initView() {
        RecyclerView rvGiftList = findViewById(R.id.rv_gift_list);
        tvSendGift = findViewById(R.id.tv_gift_send);
        tvChooseMember = findViewById(R.id.tv_gift_choose_member);
        GiftListAdapter giftListAdapter = new GiftListAdapter();
        rvGiftList.setLayoutManager(new LinearLayoutManager(getContext()));
        rvGiftList.setAdapter(giftListAdapter);

        tvSendGift.setEnabled(false);
        tvSendGift.setOnClickListener(view -> {
            RoomGift selectedGift = giftListAdapter.getSelectedGift();
            if (giftTargetPopWindow != null) {
                List<String> giftTargetUsers = giftTargetPopWindow.getGiftTargetUsers();
                ZegoGiftService giftService = ZegoRoomManager.getInstance().giftService;
                giftService.sendGift(selectedGift.getId(), giftTargetUsers, errorCode -> {
                    if (errorCode == ZegoLiveAudioRoomErrorCode.SUCCESS.getValue()) {
                        if (sendGiftListener != null) {
                            sendGiftListener.onSendGift(giftTargetUsers, selectedGift.getId());
                        }
                    } else {
                        ToastUtils
                            .showShort(R.string.toast_send_gift_error, errorCode);
                    }
                });
            }
            dismiss();

        });
        tvChooseMember.setOnClickListener(view -> {
            ZegoSpeakerSeatService speakerSeatService = ZegoRoomManager
                .getInstance().speakerSeatService;
            List<String> seatedUserList = speakerSeatService.getSeatedUserList();

            String myUserID = ZegoRoomManager.getInstance().userService.localUserInfo.getUserID();
            seatedUserList.remove(myUserID);
            giftTargetPopWindow = new GiftTargetPopWindow(getContext(), seatedUserList,
                tvChooseMember.getWidth());
            giftTargetPopWindow.setGiftTargetListener((index, targetList) -> {
                if (index == 0) {
                    if (targetList.size() > 0) {
                        tvChooseMember.setText(R.string.room_page_select_all_speakers);
                    }
                } else {
                    if (targetList.size() > 0) {
                        String userID = targetList.get(0);
                        ZegoUserService userService = ZegoRoomManager.getInstance().userService;
                        tvChooseMember.setText(userService.getUserName(userID));
                    }
                }
                tvSendGift.setEnabled(targetList.size() > 0);
            });
            giftTargetPopWindow.show(tvChooseMember, Gravity.TOP, 0, -SizeUtils.dp2px(10f));
        });
    }

    public void setSendGiftListener(SendGiftListener sendGiftListener) {
        this.sendGiftListener = sendGiftListener;
    }

    public interface SendGiftListener {

        void onSendGift(List<String> sendTo, String giftID);
    }
}
