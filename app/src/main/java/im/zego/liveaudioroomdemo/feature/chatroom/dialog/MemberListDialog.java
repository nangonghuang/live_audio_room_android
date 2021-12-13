package im.zego.liveaudioroomdemo.feature.chatroom.dialog;

import android.content.Context;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import im.zego.liveaudioroom.ZIMChatRoom;
import im.zego.liveaudioroom.emus.ZIMChatRoomErrorCode;
import im.zego.liveaudioroom.entity.ZIMChatRoomUser;
import im.zego.liveaudioroom.entity.ZIMSpeakerSeat;
import im.zego.liveaudioroomdemo.R;
import im.zego.liveaudioroomdemo.feature.chatroom.adapter.MemberListAdapter;
import im.zego.liveaudioroomdemo.feature.chatroom.model.MemberInfo;
import im.zego.liveaudioroomdemo.helper.DialogHelper;

public class MemberListDialog extends BaseBottomDialog {
    private List<ZIMSpeakerSeat> usersInSeat;
    private List<ZIMChatRoomUser> userIDs;

    private RecyclerView recyclerView;
    private TextView tvTitle;
    private boolean canNotTakeSeat;

    public MemberListDialog(Context context, boolean canNotTakeSeat, List<ZIMSpeakerSeat> usersInSeat, List<ZIMChatRoomUser> userList) {
        super(context);
        this.canNotTakeSeat = canNotTakeSeat;
        this.usersInSeat = usersInSeat;
        this.userIDs = userList;
    }

    public void updateInfo(boolean canNotTakeSeat, List<ZIMSpeakerSeat> usersInSeat, List<ZIMChatRoomUser> userList) {
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
        for (ZIMChatRoomUser user : userIDs) {
            MemberInfo info = new MemberInfo();
            for (ZIMSpeakerSeat seat : usersInSeat) {
                String user_id = seat.getAttribution().getUser_id();
                if (user_id != null && !"".equals(user_id) && user.getUserID().equals(user_id)) {
                    info.showInvitation = false;
                    info.index = seat.getAttribution().getIndex();
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
                        ZIMChatRoom.getInstance().sendInvitation(userID, errorCode -> {
                            if (errorCode == ZIMChatRoomErrorCode.SUCCESS) {
                                ToastUtils.showShort(R.string.room_page_invitation_has_sent);
                            } else {
                                ToastUtils.showShort(R.string.member_list_send_invitation_error, errorCode.getValue());
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
