package im.zego.liveaudioroomdemo.feature.room.adapter;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.blankj.utilcode.util.SizeUtils;
import im.zego.liveaudioroom.ZegoRoomManager;
import im.zego.liveaudioroom.model.ZegoRoomInfo;
import im.zego.liveaudioroom.model.ZegoTextMessage;
import im.zego.liveaudioroom.service.ZegoRoomService;
import im.zego.liveaudioroom.service.ZegoUserService;
import im.zego.liveaudioroomdemo.R;
import im.zego.liveaudioroomdemo.helper.RoundBackgroundColorSpan;
import java.util.List;
import java.util.Objects;

/**
 * Show all the information sent
 */
public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.MessageHolder> {

    List<ZegoTextMessage> messageList;

    public MessageListAdapter(List<ZegoTextMessage> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_message, parent, false);
        return new MessageHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageHolder holder, int position) {
        ZegoTextMessage message = messageList.get(position);
        ZegoUserService userService = ZegoRoomManager.getInstance().userService;
        ZegoRoomService roomService = ZegoRoomManager.getInstance().roomService;
        ZegoRoomInfo roomInfo = roomService.roomInfo;
        boolean isHostMessage = Objects.equals(message.userID, roomInfo.getHostID());
        String fromUserName = userService.getUserName(message.userID);
        String content = message.message;
        Context context = holder.itemView.getContext();
        if (!TextUtils.isEmpty(fromUserName)) {
            StringBuilder builder = new StringBuilder();
            if (isHostMessage) {
                builder.append(context.getString(R.string.room_page_host));
            }
            builder.append(fromUserName);
            builder.append(": ");
            builder.append(content);
            String source = builder.toString();
            SpannableString string = new SpannableString(source);
            RoundBackgroundColorSpan backgroundColorSpan = new RoundBackgroundColorSpan(
                ContextCompat.getColor(context, R.color.yellow),
                ContextCompat.getColor(context, R.color.white));
            if (isHostMessage) {
                AbsoluteSizeSpan absoluteSizeSpan = new AbsoluteSizeSpan(SizeUtils.sp2px(10));
                string.setSpan(absoluteSizeSpan, 0,
                    context.getString(R.string.room_page_host).length(),
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                string.setSpan(backgroundColorSpan, 0,
                    context.getString(R.string.room_page_host).length(),
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            }
            ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(
                ContextCompat.getColor(context, R.color.blue_text)
            );
            int indexOfUser = source.indexOf(fromUserName);
            string.setSpan(foregroundColorSpan, indexOfUser, indexOfUser + fromUserName.length(),
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            AbsoluteSizeSpan absoluteSizeSpan = new AbsoluteSizeSpan(SizeUtils.sp2px(12));
            string.setSpan(absoluteSizeSpan, indexOfUser, indexOfUser + fromUserName.length(),
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

            holder.tvSendMessage.setText(string);
        } else {
            SpannableString string = new SpannableString(content);
            ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(
                ContextCompat.getColor(context, R.color.blue_text)
            );
            AbsoluteSizeSpan absoluteSizeSpan = new AbsoluteSizeSpan(SizeUtils.sp2px(12));
            string.setSpan(foregroundColorSpan, 0, content.length(),
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            string
                .setSpan(absoluteSizeSpan, 0, content.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            holder.tvSendMessage.setText(string);
        }

    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class MessageHolder extends RecyclerView.ViewHolder {

        public TextView tvSendMessage;

        public MessageHolder(@NonNull View itemView) {
            super(itemView);
            tvSendMessage = itemView.findViewById(R.id.tv_send_message);
        }
    }
}
