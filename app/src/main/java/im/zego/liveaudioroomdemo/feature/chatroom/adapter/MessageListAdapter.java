package im.zego.liveaudioroomdemo.feature.chatroom.adapter;

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

import java.util.List;

import im.zego.liveaudioroom.entity.ZIMChatRoomText;
import im.zego.liveaudioroom.internal.ZIMChatRoomManager;
import im.zego.liveaudioroom.internal.entity.ZIMChatRoomInfo;
import im.zego.liveaudioroomdemo.R;
import im.zego.liveaudioroomdemo.helper.RoundBackgroundColorSpan;

/**
 * Show all the information sent
 */
public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.MessageHolder> {
    List<ZIMChatRoomText> messageList;

    public MessageListAdapter(List<ZIMChatRoomText> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageHolder holder, int position) {
        ZIMChatRoomText message = messageList.get(position);
        // The message to determine whether the owner is the owner is to show the owner's symbol and related information.
        boolean isOwnerMessage = isOwnerMessage(message);
        String fromUserID = message.getFromUserID();
        String fromUserName = ZIMChatRoomManager.getInstance().getChatRoomUserName(fromUserID);
        String content = message.getContent();
        Context context = holder.itemView.getContext();
        if(!TextUtils.isEmpty(fromUserName)){
            StringBuilder builder = new StringBuilder();
            if (isOwnerMessage) {
                builder.append(context.getString(R.string.room_page_role_owner));
            }
            builder.append(fromUserName);
            builder.append(": ");
            builder.append(content);
            String source = builder.toString();
            SpannableString string = new SpannableString(source);
            RoundBackgroundColorSpan backgroundColorSpan = new RoundBackgroundColorSpan(
                    ContextCompat.getColor(context, R.color.yellow),
                    ContextCompat.getColor(context, R.color.white));
            if (isOwnerMessage) {
                AbsoluteSizeSpan absoluteSizeSpan = new AbsoluteSizeSpan(SizeUtils.sp2px(10));
                string.setSpan(absoluteSizeSpan, 0,
                        context.getString(R.string.room_page_role_owner).length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                string.setSpan(backgroundColorSpan, 0,
                        context.getString(R.string.room_page_role_owner).length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            }
            ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(
                    ContextCompat.getColor(context, R.color.blue_text)
            );
            int indexOfUser = source.indexOf(fromUserName);
            string.setSpan(foregroundColorSpan, indexOfUser, indexOfUser + fromUserName.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            AbsoluteSizeSpan absoluteSizeSpan = new AbsoluteSizeSpan(SizeUtils.sp2px(12));
            string.setSpan(absoluteSizeSpan, indexOfUser, indexOfUser + fromUserName.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

            holder.tvSendMessage.setText(string);
        }else {
            SpannableString string = new SpannableString(content);
            ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(
                    ContextCompat.getColor(context, R.color.blue_text)
            );
            AbsoluteSizeSpan absoluteSizeSpan = new AbsoluteSizeSpan(SizeUtils.sp2px(12));
            string.setSpan(foregroundColorSpan,0,content.length(),Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            string.setSpan(absoluteSizeSpan,0,content.length(),Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
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

    private boolean isOwnerMessage(ZIMChatRoomText message) {
        ZIMChatRoomInfo roomInfo = ZIMChatRoomManager.getInstance().getChatRoomInfo();
        String authorID = roomInfo.getAuthor();
        String userID = message.getFromUserID();
        if (userID == null) {
            return false;
        }
        return userID.equals(authorID);
    }
}
