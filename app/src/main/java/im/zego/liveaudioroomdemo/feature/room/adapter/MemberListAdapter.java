package im.zego.liveaudioroomdemo.feature.room.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import im.zego.liveaudioroom.model.ZegoRoomUserRole;
import im.zego.liveaudioroom.model.ZegoUserInfo;
import im.zego.liveaudioroomdemo.R;
import im.zego.liveaudioroomdemo.helper.UserInfoHelper;
import java.util.ArrayList;
import java.util.List;

public class MemberListAdapter extends RecyclerView.Adapter<MemberListAdapter.UserListHolder> {

    private IItemOnClickListener itemOnClickListener = null;

    private List<ZegoUserInfo> userListInRoom;

    public MemberListAdapter(List<ZegoUserInfo> userListInRoom) {
        this.userListInRoom = new ArrayList<>();
        this.userListInRoom.addAll(userListInRoom);
    }

    @NonNull
    @Override
    public UserListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_user, parent, false);
        return new UserListHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserListHolder holder, int position) {
        final ZegoUserInfo userInfo = userListInRoom.get(position);

        holder.ivUserAvatar.setImageDrawable(UserInfoHelper.getAvatarByUserName(userInfo.getUserName()));
        holder.tvUserName.setText(userInfo.getUserName());
        if (userInfo.getRole() == ZegoRoomUserRole.Listener) {
            holder.tvUserInfo.setVisibility(View.GONE);
            // Audience
            if (UserInfoHelper.isSelfOwner()) {
                holder.ivInvite.setVisibility(View.VISIBLE);
            } else {
                holder.ivInvite.setVisibility(View.GONE);
            }
        } else {
            // On Seat: Speaker or Owner
            holder.ivInvite.setVisibility(View.GONE);
            holder.tvUserInfo.setVisibility(View.VISIBLE);
            if (UserInfoHelper.isUserOwner(userInfo.getUserID())) {
                holder.tvUserInfo.setText(R.string.room_page_host);
            } else {
                holder.tvUserInfo.setText(R.string.room_page_role_speaker);
            }
        }

        holder.ivInvite.setOnClickListener(v -> {
            if (itemOnClickListener != null) {
                itemOnClickListener.onClick(userInfo);
            }
        });
    }

    public void updateUserList(List<ZegoUserInfo> userList) {
        userListInRoom.clear();
        userListInRoom.addAll(userList);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return userListInRoom.size();
    }

    public void setItemOnClick(IItemOnClickListener itemOnClickListener) {
        this.itemOnClickListener = itemOnClickListener;
    }

    public interface IItemOnClickListener {

        void onClick(ZegoUserInfo userInfo);
    }

    static class UserListHolder extends RecyclerView.ViewHolder {

        public ImageView ivUserAvatar;
        public TextView tvUserName;
        public TextView tvUserInfo;
        public ImageView ivInvite;

        public UserListHolder(@NonNull View itemView) {
            super(itemView);
            ivUserAvatar = itemView.findViewById(R.id.iv_user_avatar);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvUserInfo = itemView.findViewById(R.id.tv_user_info);
            ivInvite = itemView.findViewById(R.id.iv_invite);
        }
    }
}
