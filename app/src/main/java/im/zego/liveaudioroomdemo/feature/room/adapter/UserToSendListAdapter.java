package im.zego.liveaudioroomdemo.feature.room.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import im.zego.liveaudioroom.entity.ZegoLiveAudioRoomSeatAttribution;
import im.zego.liveaudioroom.entity.ZIMSpeakerSeat;
import im.zego.liveaudioroomdemo.R;

/**
 * Show a list of users who can send gifts, click on the user name to send gifts
 */
public class UserToSendListAdapter extends RecyclerView.Adapter<UserToSendListAdapter.UserToSendHolder> {
    public IItemCheckListener checkListener;
    private final List<ZIMSpeakerSeat> memberInSeatExceptSelf;

    public UserToSendListAdapter(List<ZIMSpeakerSeat> memberInSeatExceptSelf) {
        this.memberInSeatExceptSelf = memberInSeatExceptSelf;
    }

    @NonNull
    @Override
    public UserToSendHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_to_send_gift, parent, false);
        return new UserToSendHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserToSendHolder holder, int position) {
        ZegoLiveAudioRoomSeatAttribution seatAttribution = memberInSeatExceptSelf.get(position).getAttribution();
        holder.tvUserName.setText(seatAttribution.getUser_id());
        holder.ivChosen.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (checkListener != null) {
                String userID = seatAttribution.getUser_id();
                checkListener.onCheckedChanged(userID, isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return memberInSeatExceptSelf.size();
    }

    public void setItemCheckedChangeListener(IItemCheckListener checkListener) {
        this.checkListener = checkListener;
    }

    public interface IItemCheckListener {
        void onCheckedChanged(String userID, boolean isChecked);
    }

    static class UserToSendHolder extends RecyclerView.ViewHolder {
        public TextView tvUserName;
        public CheckBox ivChosen;

        public UserToSendHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            ivChosen = itemView.findViewById(R.id.checkbox);
        }
    }
}
