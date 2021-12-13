package im.zego.liveaudioroomdemo.feature.room.dialog;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.SizeUtils;

import java.util.ArrayList;
import java.util.List;

import im.zego.liveaudioroom.internal.ZegoLiveAudioRoomManager;
import im.zego.liveaudioroomdemo.R;
import im.zego.liveaudioroomdemo.helper.OnRecyclerViewItemTouchListener;
import im.zego.liveaudioroomdemo.helper.RecyclerDivider;

public class GiftTargetPopWindow extends PopupWindow {
    private static final String TAG = "GiftTargetPopWindow";
    private GiftTargetListener giftTargetListener;
    List<String> target = new ArrayList<>();

    public GiftTargetPopWindow(Context context, List<String> userList, int width) {
        super(context);
        ViewGroup viewGroup = (ViewGroup) View.inflate(context, R.layout.popwindow_gift_target, null);
        RecyclerView recyclerView = viewGroup.findViewById(R.id.rv_gift_target_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(new GiftTargetAdapter(userList));
        RecyclerDivider divider = new RecyclerDivider(context);
        divider.setPadding(SizeUtils.dp2px(16f), SizeUtils.dp2px(16f));
        divider.setHeight(SizeUtils.dp2px(1f));
        divider.setColor(ContextCompat.getColor(context, R.color.light_gray));
        recyclerView.addItemDecoration(divider);
        recyclerView.addOnItemTouchListener(new OnRecyclerViewItemTouchListener(recyclerView) {
            @Override
            public void onItemClick(RecyclerView.ViewHolder vh) {
                int adapterPosition = vh.getAdapterPosition();
                target.clear();
                if (adapterPosition == 0) {
                    target.addAll(userList);
                } else {
                    String user = userList.get(adapterPosition - 1);
                    target.add(user);
                }
                if (giftTargetListener != null) {
                    giftTargetListener.onGiftTargetSelected(adapterPosition,target);
                }
                dismiss();
            }
        });

        setContentView(viewGroup);
        setWidth(width);
        setHeight(SizeUtils.dp2px(200f));
        setBackgroundDrawable(AppCompatResources.getDrawable(context, R.drawable.gray_fill_round_rectangle_bg));
        setOutsideTouchable(true);
        getContentView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    public void show(View anchor, int gravity, int offsetX, int offsetY) {
        int[] location = new int[2];
        anchor.getLocationInWindow(location);
        int showX;
        int showY;
        switch (gravity) {
            case Gravity.TOP: {
                showX = location[0] + anchor.getWidth() / 2 - getWidth() / 2 + offsetX;
                showY = location[1] - getHeight() + offsetY;
                showAtLocation(anchor, Gravity.NO_GRAVITY, showX, showY);

            }
            break;
            case Gravity.LEFT:
            case Gravity.START: {
                showX = location[0] - getWidth() + offsetX;
                showY = location[1] + anchor.getHeight() / 2 - getHeight() / 2 + offsetY;
                showAtLocation(anchor, Gravity.NO_GRAVITY, showX, showY);
            }
            break;
            case Gravity.BOTTOM: {
                showX = location[0] + anchor.getWidth() / 2 - getWidth() / 2 + offsetX;
                showY = location[1] + anchor.getHeight() + +offsetY;
                showAtLocation(anchor, Gravity.NO_GRAVITY, showX, showY);
            }
            break;
            default:
        }
    }

    public List<String> getGiftTargetUsers() {
        return target;
    }

    public void setGiftTargetListener(GiftTargetListener giftTargetListener) {
        this.giftTargetListener = giftTargetListener;
    }

    private static class GiftTargetAdapter extends RecyclerView.Adapter {
        private List<String> userList;

        public GiftTargetAdapter(List<String> users) {
            userList = users;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View inflate = View.inflate(parent.getContext(), R.layout.item_gift_target, null);
            inflate.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, SizeUtils.dp2px(42)));
            return new RecyclerView.ViewHolder(inflate) {
            };
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            TextView itemView = (TextView) holder.itemView.findViewById(R.id.tv_gift_target);
            if (position == 0) {
                if (getItemCount() == 1) {
                    itemView.setText(R.string.room_page_gift_no_speaker);
                } else {
                    itemView.setText(R.string.room_page_select_all_speakers);
                }
            } else {
                String userID = userList.get(position - 1);
                String userName = ZegoLiveAudioRoomManager.getInstance().getRoomUserName(userID);
                itemView.setText(userName);
            }
        }

        @Override
        public int getItemCount() {
            return userList.size() + 1;
        }
    }

    public interface GiftTargetListener {
        void onGiftTargetSelected(int index,List<String> targetList);
    }
}
