package im.zego.liveaudioroomdemo.feature.chatroom.dialog;

import android.content.Context;
import android.view.Gravity;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.ToastUtils;

import java.util.List;

import im.zego.liveaudioroom.ZIMChatRoom;
import im.zego.liveaudioroom.emus.ZIMChatRoomErrorCode;
import im.zego.liveaudioroom.entity.ZIMChatRoomUser;
import im.zego.liveaudioroom.internal.ZIMChatRoomManager;
import im.zego.liveaudioroomdemo.R;
import im.zego.liveaudioroomdemo.feature.chatroom.adapter.GiftListAdapter;
import im.zego.liveaudioroomdemo.feature.chatroom.enums.RoomGift;

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
                ZIMChatRoom.getInstance().sendGiftMessage(selectedGift.getType(), giftTargetUsers, (error, sendFailToUsers) -> {
                    if (error == ZIMChatRoomErrorCode.SUCCESS) {
                        if (sendGiftListener != null) {
                            sendGiftListener.onSendGift(giftTargetUsers, selectedGift.getType());
                        }
                    } else {
                        ToastUtils.showShort(R.string.toast_send_gift_error, error.getValue());
                    }
                });
            }
            dismiss();

        });
        tvChooseMember.setOnClickListener(view -> {
            List<String> seatedUserList = ZIMChatRoomManager.getInstance().getSeatedIDList();
            ZIMChatRoomUser myUserInfo = ZIMChatRoomManager.getInstance().getMyUserInfo();
            seatedUserList.remove(myUserInfo.getUserID());
            giftTargetPopWindow = new GiftTargetPopWindow(getContext(), seatedUserList, tvChooseMember.getWidth());
            giftTargetPopWindow.setGiftTargetListener((index, targetList) -> {
                if (index == 0) {
                    if (targetList.size() > 0) {
                        tvChooseMember.setText(R.string.room_page_select_all_speakers);
                    }
                } else {
                    if (targetList.size() > 0) {
                        String userID = targetList.get(0);
                        String userName = ZIMChatRoomManager.getInstance().getChatRoomUserName(userID);
                        tvChooseMember.setText(userName);
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
        void onSendGift(List<String> sendTo, int giftType);
    }
}
