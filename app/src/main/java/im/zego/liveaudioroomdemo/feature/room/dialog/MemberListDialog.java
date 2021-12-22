package im.zego.liveaudioroomdemo.feature.room.dialog;

import android.content.Context;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import im.zego.liveaudioroom.ZegoRoomManager;
import im.zego.liveaudioroom.constants.ZegoRoomErrorCode;
import im.zego.liveaudioroom.model.ZegoSpeakerSeatModel;
import im.zego.liveaudioroom.model.ZegoSpeakerSeatStatus;
import im.zego.liveaudioroom.model.ZegoUserInfo;
import im.zego.liveaudioroom.service.ZegoSpeakerSeatService;
import im.zego.liveaudioroomdemo.R;
import im.zego.liveaudioroomdemo.feature.room.adapter.MemberListAdapter;
import im.zego.liveaudioroomdemo.helper.DialogHelper;
import java.util.List;

public class MemberListDialog extends BaseBottomDialog {

    private List<ZegoUserInfo> userList;

    private RecyclerView recyclerView;
    private TextView tvTitle;
    private MemberListAdapter memberListAdapter;

    public MemberListDialog(Context context, List<ZegoUserInfo> userList) {
        super(context);
        this.userList = userList;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_member;
    }

    @Override
    protected void initView() {
        super.initView();
        tvTitle = findViewById(R.id.tv_title);
        recyclerView = findViewById(R.id.rv_user_list);

        tvTitle.setText(StringUtils.getString(R.string.room_page_user_list, userList.size()));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        memberListAdapter = new MemberListAdapter(userList);
        recyclerView.setAdapter(memberListAdapter);
        recyclerView.setLayoutManager(linearLayoutManager);
        memberListAdapter.setItemOnClick(userInfo -> {
            String string = StringUtils.getString(R.string.room_page_invite_take_seat);
            DialogHelper.showToastDialog(getContext(), string, dialog -> {
                ZegoSpeakerSeatService seatService = ZegoRoomManager.getInstance().speakerSeatService;
                boolean haveUnTakenSeat = false;
                List<ZegoSpeakerSeatModel> seatList = seatService.getSpeakerSeatList();
                for (int i = 0; i < seatList.size(); i++) {
                    if (seatList.get(i).status == ZegoSpeakerSeatStatus.Untaken) {
                        haveUnTakenSeat = true;
                        break;
                    }
                }
                if (haveUnTakenSeat) {
                    ZegoRoomManager.getInstance().userService.sendInvitation(userInfo.getUserID(), errorCode -> {
                        if (errorCode == ZegoRoomErrorCode.SUCCESS) {
                            ToastUtils.showShort(R.string.room_page_invitation_has_sent);
                        } else {
                            ToastUtils.showShort(R.string.member_list_send_invitation_error, errorCode);
                        }
                    });

                } else {
                    ToastUtils.showShort(R.string.room_page_no_more_seat_available);
                }
            });
        });
    }

    public void updateUserList(List<ZegoUserInfo> userList) {
        this.userList = userList;
        memberListAdapter.updateUserList(userList);
        tvTitle.setText(StringUtils.getString(R.string.room_page_user_list, userList.size()));
    }
}
