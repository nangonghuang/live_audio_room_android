package im.zego.liveaudioroomdemo.feature.room.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.blankj.utilcode.util.LanguageUtils;
import com.blankj.utilcode.util.ResourceUtils;
import im.zego.liveaudioroom.ZegoRoomManager;
import im.zego.liveaudioroom.model.ZegoSpeakerSeatModel;
import im.zego.liveaudioroom.model.ZegoUserInfo;
import im.zego.liveaudioroom.service.ZegoUserService;
import im.zego.liveaudioroomdemo.R;
import im.zego.liveaudioroomdemo.helper.UserInfoHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SeatListAdapter extends RecyclerView.Adapter<SeatListAdapter.SeatListHolder> {

    private static final String TAG = "SeatListAdapter";
    private static final float SOUND_LEVEL_THRESHOLD = 10F;

    public OnSeatClickListener onSeatClickListener = null;

    private List<ZegoSpeakerSeatModel> seatList = new ArrayList<>();

    @NonNull
    @Override
    public SeatListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_seat_user, parent, false);
        return new SeatListHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SeatListHolder holder, int position) {
        ZegoSpeakerSeatModel speakerSeatModel = seatList.get(position);
        holder.itemView.setOnClickListener(v -> {
            if (onSeatClickListener != null) {
                onSeatClickListener.onClick(speakerSeatModel);
            }
        });

        ZegoUserService userService = ZegoRoomManager.getInstance().userService;
        boolean isSelfUserOnSeat = false;
        for (ZegoSpeakerSeatModel model : seatList) {
            ZegoUserInfo localUserInfo = userService.localUserInfo;
            if (localUserInfo.getUserID().equals(model.userID)) {
                isSelfUserOnSeat = true;
                break;
            }
        }

        switch (speakerSeatModel.status) {
            case Occupied:
                holder.toOnSeatUI();

                String userID = speakerSeatModel.userID;
                String userName = userService.getUserName(userID);
                holder.tvUserName.setText(userName);
                holder.ivAvatar.setImageDrawable(UserInfoHelper.getAvatarByUserName(userName));

                if (UserInfoHelper.isUserOwner(userID)) {
                    holder.ivOwnerAvatar.setVisibility(View.VISIBLE);
                    if (LanguageUtils.getSystemLanguage().equals(Locale.CHINESE)) {
                        holder.ivOwnerAvatar
                            .setImageDrawable(ResourceUtils.getDrawable(R.drawable.icon_owner_zh));
                    } else {
                        holder.ivOwnerAvatar
                            .setImageDrawable(ResourceUtils.getDrawable(R.drawable.icon_owner));
                    }
                } else {
                    holder.ivOwnerAvatar.setVisibility(View.INVISIBLE);
                }

                if (canShowSoundWaves(speakerSeatModel.soundLevel)) {
                    holder.ivAvatarTalking.setVisibility(View.VISIBLE);
                } else {
                    holder.ivAvatarTalking.setVisibility(View.INVISIBLE);
                }

                if (!speakerSeatModel.mic) {
                    holder.ivMicOff.setVisibility(View.VISIBLE);
                } else {
                    holder.ivMicOff.setVisibility(View.INVISIBLE);
                }

                switch (speakerSeatModel.network) {
                    case Good:
                        holder.ivNetworkStatus.setImageDrawable(
                            ResourceUtils.getDrawable(R.drawable.icon_network_good));
                        break;
                    case Medium:
                        holder.ivNetworkStatus.setImageDrawable(
                            ResourceUtils.getDrawable(R.drawable.icon_network_medium));
                        break;
                    case Bad:
                        holder.ivNetworkStatus.setImageDrawable(
                            ResourceUtils.getDrawable(R.drawable.icon_network_bad));
                        break;
                }
                break;
            case Untaken:
                holder.toOffSeatUI();

                holder.ivLock.setVisibility(View.INVISIBLE);
                if (isSelfUserOnSeat) {
                    holder.ivSeat.setVisibility(View.VISIBLE);
                    holder.ivJoin.setVisibility(View.INVISIBLE);
                } else {
                    holder.ivSeat.setVisibility(View.INVISIBLE);
                    holder.ivJoin.setVisibility(View.VISIBLE);
                }
                break;
            case Closed:
                holder.toOffSeatUI();

                holder.ivLock.setVisibility(View.VISIBLE);
                holder.ivSeat.setVisibility(View.INVISIBLE);
                holder.ivJoin.setVisibility(View.INVISIBLE);
                break;
        }
    }

    private boolean canShowSoundWaves(float soundLevel) {
        return soundLevel > SOUND_LEVEL_THRESHOLD;
    }

    @Override
    public int getItemCount() {
        return seatList.size();
    }

    public void setSeatList(List<ZegoSpeakerSeatModel> seatList) {
        Log.d(TAG, "setSeatList() called with: seatList = [" + seatList + "]");
        this.seatList = seatList;
    }

    public void updateUserInfo(ZegoSpeakerSeatModel zimSpeakerSeat) {
        Log.d(TAG, "updateUserInfo() called with: zimSpeakerSeat = [" + zimSpeakerSeat + "]");
        seatList.set(zimSpeakerSeat.seatIndex, zimSpeakerSeat);
        notifyDataSetChanged();
//        notifyItemChanged(zimSpeakerSeat.seatIndex, new Object());
    }

    public void setOnSeatClickListener(OnSeatClickListener itemListener) {
        this.onSeatClickListener = itemListener;
    }

    public interface OnSeatClickListener {

        void onClick(ZegoSpeakerSeatModel seatModel);
    }

    static class SeatListHolder extends RecyclerView.ViewHolder {

        private ImageView ivAvatarTalking;
        private ImageView ivAvatar;
        private ImageView ivMicOff;
        private ImageView ivOwnerAvatar;
        private ImageView ivNetworkStatus;
        private TextView tvUserName;
        private ImageView ivLock;
        private ImageView ivSeat;
        private ImageView ivJoin;

        public SeatListHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatarTalking = itemView.findViewById(R.id.iv_avatar_talking);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            ivMicOff = itemView.findViewById(R.id.iv_mic_off);
            ivOwnerAvatar = itemView.findViewById(R.id.iv_owner_avatar);
            ivNetworkStatus = itemView.findViewById(R.id.ic_network_status);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            ivLock = itemView.findViewById(R.id.iv_lock);
            ivSeat = itemView.findViewById(R.id.iv_seat);
            ivJoin = itemView.findViewById(R.id.iv_join);
        }

        void toOnSeatUI() {
            ivAvatarTalking.setVisibility(View.VISIBLE);
            ivAvatar.setVisibility(View.VISIBLE);
            ivMicOff.setVisibility(View.VISIBLE);
            ivOwnerAvatar.setVisibility(View.VISIBLE);
            ivNetworkStatus.setVisibility(View.VISIBLE);
            tvUserName.setVisibility(View.VISIBLE);
            ivLock.setVisibility(View.INVISIBLE);
            ivSeat.setVisibility(View.INVISIBLE);
            ivJoin.setVisibility(View.INVISIBLE);
        }

        void toOffSeatUI() {
            ivAvatarTalking.setVisibility(View.INVISIBLE);
            ivAvatar.setVisibility(View.INVISIBLE);
            ivMicOff.setVisibility(View.INVISIBLE);
            ivOwnerAvatar.setVisibility(View.INVISIBLE);
            ivNetworkStatus.setVisibility(View.INVISIBLE);
            tvUserName.setVisibility(View.INVISIBLE);
            ivLock.setVisibility(View.VISIBLE);
            ivSeat.setVisibility(View.VISIBLE);
            ivJoin.setVisibility(View.VISIBLE);
        }
    }
}
