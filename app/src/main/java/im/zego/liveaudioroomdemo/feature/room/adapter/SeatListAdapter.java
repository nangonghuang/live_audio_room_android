package im.zego.liveaudioroomdemo.feature.room.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.LanguageUtils;
import com.blankj.utilcode.util.ResourceUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomVoiceStatus;
import im.zego.liveaudioroom.entity.ZIMSpeakerSeat;
import im.zego.liveaudioroom.entity.ZegoLiveAudioRoomUser;
import im.zego.liveaudioroom.internal.ZegoLiveAudioRoomManager;
import im.zego.liveaudioroomdemo.R;
import im.zego.liveaudioroomdemo.helper.UserInfoHelper;

public class SeatListAdapter extends RecyclerView.Adapter<SeatListAdapter.SeatListHolder> {
    private static final String TAG = "SeatListAdapter";
    private static final float SOUND_LEVEL_THRESHOLD = 10F;

    public IChatListItemListener itemListener = null;

    private List<ZIMSpeakerSeat> seatList;

    public SeatListAdapter() {
        this.seatList = initAllSeatListInRoom();
    }

    private List<ZIMSpeakerSeat> initAllSeatListInRoom() {
        ArrayList<ZIMSpeakerSeat> allSeats = new ArrayList<>();
        for (int i = 0; i <= 7; i++) {
            ZIMSpeakerSeat speakerSeat = new ZIMSpeakerSeat();
            speakerSeat.getAttribution().setIndex(i);
            allSeats.add(speakerSeat);
        }
        return allSeats;
    }

    public List<ZIMSpeakerSeat> getSeatListInRoom() {
        return seatList;
    }

    @NonNull
    @Override
    public SeatListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_seat_user, parent, false);
        return new SeatListHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SeatListHolder holder, int position) {
        holder.itemView.setOnClickListener(v -> {
            if (itemListener != null) {
                itemListener.onClick(position);
            }
        });

        boolean isSelfUserOnSeat = isSelfUserOnSeat();

        ZIMSpeakerSeat speakerSeat = seatList.get(position);

        switch (speakerSeat.getStatus()) {
            case USED:
                holder.toOnSeatUI();

                String user_id = speakerSeat.getAttribution().getUser_id();
                String userName = ZegoLiveAudioRoomManager.getInstance().getRoomUserName(user_id);
                holder.tvUserName.setText(userName);
                holder.ivAvatar.setImageDrawable(UserInfoHelper.getUserAvatar(position));

                if (UserInfoHelper.isUserOwner(user_id)) {
                    holder.ivOwnerAvatar.setVisibility(View.VISIBLE);
                    if (LanguageUtils.getSystemLanguage().equals(Locale.CHINESE)) {
                        holder.ivOwnerAvatar.setImageDrawable(ResourceUtils.getDrawable(R.drawable.icon_owner_zh));
                    } else {
                        holder.ivOwnerAvatar.setImageDrawable(ResourceUtils.getDrawable(R.drawable.icon_owner));
                    }
                } else {
                    holder.ivOwnerAvatar.setVisibility(View.INVISIBLE);
                }

                if (canShowSoundWaves(speakerSeat.getSoundLevel())) {
                    holder.ivAvatarTalking.setVisibility(View.VISIBLE);
                } else {
                    holder.ivAvatarTalking.setVisibility(View.INVISIBLE);
                }

                if (speakerSeat.getAttribution().isIs_muted()) {
                    holder.ivMicOff.setVisibility(View.VISIBLE);
                } else {
                    holder.ivMicOff.setVisibility(View.INVISIBLE);
                }

                switch (speakerSeat.getNetWorkQuality()) {
                    case Good:
                        holder.ivNetworkStatus.setImageDrawable(ResourceUtils.getDrawable(R.drawable.icon_network_good));
                        break;
                    case Medium:
                        holder.ivNetworkStatus.setImageDrawable(ResourceUtils.getDrawable(R.drawable.icon_network_medium));
                        break;
                    case Bad:
                        holder.ivNetworkStatus.setImageDrawable(ResourceUtils.getDrawable(R.drawable.icon_network_bad));
                        break;
                }
                break;
            case UNUSED:
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
            case LOCKED:
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

    private boolean isSelfUserOnSeat() {
        ZegoLiveAudioRoomUser selfUser = ZegoLiveAudioRoomManager.getInstance().getMyUserInfo();
        for (ZIMSpeakerSeat speakerSeat : seatList) {
            if (selfUser.getUserID().equals(speakerSeat.getAttribution().getUser_id())
                    && speakerSeat.getStatus() == ZegoLiveAudioRoomVoiceStatus.USED) {
                return true;
            }
        }
        return false;
    }

    public void updateUserInfo(int index, ZIMSpeakerSeat zimSpeakerSeat) {
        seatList.set(index, zimSpeakerSeat);
        notifyDataSetChanged();
    }

    public void updateSoundWaves(String userId, float soundLevel) {
        if (canShowSoundWaves(soundLevel)) {
            for (ZIMSpeakerSeat speakerSeat : seatList) {
                if (userId.equals(speakerSeat.getAttribution().getUser_id())) {
                    speakerSeat.setSoundLevel(soundLevel);
                }
            }
            notifyDataSetChanged();
        } else {
            boolean needNotify = false;
            for (ZIMSpeakerSeat speakerSeat : seatList) {
                if (userId.equals(speakerSeat.getAttribution().getUser_id())
                        && speakerSeat.getSoundLevel() != 0) {
                    speakerSeat.setSoundLevel(0);
                    needNotify = true;
                }
            }
            if (needNotify) {
                notifyDataSetChanged();
            }
        }
    }

    public void setItemListener(IChatListItemListener itemListener) {
        this.itemListener = itemListener;
    }

    public interface IChatListItemListener {
        void onClick(int index);
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
