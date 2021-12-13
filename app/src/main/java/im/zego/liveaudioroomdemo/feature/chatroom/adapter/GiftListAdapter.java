package im.zego.liveaudioroomdemo.feature.chatroom.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import im.zego.liveaudioroomdemo.R;
import im.zego.liveaudioroomdemo.feature.chatroom.enums.RoomGift;

/**
 * Show the gift list and click on the gift UI to display the gift page.
 */
public class GiftListAdapter extends RecyclerView.Adapter<GiftListAdapter.GiftInRoomListHolder> {
    public List<RoomGift> giftList = new ArrayList<>();

    public GiftListAdapter() {
        giftList.add(RoomGift.GIFT_HEART);
    }

    @NonNull
    @Override
    public GiftInRoomListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gift, parent, false);
        return new GiftInRoomListHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GiftInRoomListHolder holder, int position) {
        RoomGift roomGift = giftList.get(position);
        holder.ivGift.setImageDrawable(ResourcesCompat.getDrawable(holder.itemView.getResources(), roomGift.getRes(), null));
        holder.tvGiftName.setText(roomGift.getName());
    }

    @Override
    public int getItemCount() {
        return giftList.size();
    }

    public RoomGift getSelectedGift(){
        return RoomGift.GIFT_HEART;
    }

    static class GiftInRoomListHolder extends RecyclerView.ViewHolder {
        public ImageView ivGift;
        public TextView tvGiftName;

        public GiftInRoomListHolder(@NonNull View itemView) {
            super(itemView);
            ivGift = itemView.findViewById(R.id.iv_gift);
            tvGiftName = itemView.findViewById(R.id.tv_gift_name);
        }
    }
}
