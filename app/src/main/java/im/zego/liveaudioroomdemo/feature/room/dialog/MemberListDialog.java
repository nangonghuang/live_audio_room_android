package im.zego.liveaudioroomdemo.feature.room.dialog;

import android.content.Context;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import im.zego.liveaudioroom.refactor.ZegoRoomManager;
import im.zego.liveaudioroom.refactor.constants.ZegoRoomErrorCode;
import im.zego.liveaudioroom.refactor.model.ZegoSpeakerSeatModel;
import im.zego.liveaudioroom.refactor.model.ZegoUserInfo;
import im.zego.liveaudioroomdemo.R;
import im.zego.liveaudioroomdemo.feature.room.adapter.MemberListAdapter;
import im.zego.liveaudioroomdemo.feature.room.model.MemberInfo;
import im.zego.liveaudioroomdemo.helper.DialogHelper;

public class MemberListDialog extends BaseBottomDialog {
    private List<ZegoSpeakerSeatModel> usersInSeat;
    private List<ZegoUserInfo> userIDs;

    private RecyclerView recyclerView;
    private TextView tvTitle;
    private boolean canNotTakeSeat;

    public MemberListDialog(Context context, boolean canNotTakeSeat, List<ZegoSpeakerSeatModel> usersInSeat, List<ZegoUserInfo> userList) {
        super(context);
        this.canNotTakeSeat = canNotTakeSeat;
        this.usersInSeat = usersInSeat;
        this.userIDs = userList;
    }

    public void updateInfo(boolean canNotTakeSeat, List<ZegoSpeakerSeatModel> usersInSeat, List<ZegoUserInfo> userList) {
        this.canNotTakeSeat = canNotTakeSeat;
        this.usersInSeat = usersInSeat;
        this.userIDs = userList;
        initData();
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
    }

    @Override
    protected void initData() {
        super.initData();
        int onSeatMemberCounts = 0;
        ArrayList<MemberInfo> arrayList = new ArrayList<>();
        for (ZegoUserInfo user : userIDs) {
            MemberInfo info = new MemberInfo();
            for (ZegoSpeakerSeatModel seat : usersInSeat) {
                String user_id = seat.userID;
                if (user_id != null && !"".equals(user_id) && user.getUserID().equals(user_id)) {
                    info.showInvitation = false;
                    info.index = seat.seatIndex;
                    ++onSeatMemberCounts;
                    break;
                } else {
                    info.showInvitation = true;
                }
            }
            info.userID = user.getUserID();
            info.userName = user.getUserName();
            arrayList.add(info);
        }

        tvTitle.setText(StringUtils.getString(R.string.room_page_user_list, arrayList.size()));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        MemberListAdapter adapter = new MemberListAdapter(arrayList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(linearLayoutManager);
        int finalOnSeatMemberCounts = onSeatMemberCounts;
        adapter.setItemOnClick(userID -> {
            DialogHelper.showToastDialog(getContext(), StringUtils.getString(R.string.room_page_invite_take_seat), dialog -> {
                if (canNotTakeSeat) {
                    ToastUtils.showShort(R.string.member_list_send_invitation_failed);
                } else {
                    if (finalOnSeatMemberCounts < 8) {
                        ZegoRoomManager.getInstance().userService.sendInvitation(userID, errorCode -> {
                            if (errorCode == ZegoRoomErrorCode.SUCCESS) {
                                ToastUtils.showShort(R.string.room_page_invitation_has_sent);
                            } else {
                                ToastUtils.showShort(R.string.member_list_send_invitation_error, errorCode);
                            }
                        });
                    } else {
                        ToastUtils.showShort(R.string.room_page_no_more_seat_available);
                    }
                }
            });
        });
    }
}
