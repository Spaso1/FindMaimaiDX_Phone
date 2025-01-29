package org.ast.findmaimaidx.adapter;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.ast.findmaimaidx.R;
import org.ast.findmaimaidx.been.ChatMessage;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<ChatMessage> chatMessages;
    private OnMessageAddedListener onMessageAddedListener;
    private int currentBotMessageIndex = -1;
    private boolean isThinking = false;

    public ChatAdapter(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    public void setOnMessageAddedListener(OnMessageAddedListener listener) {
        this.onMessageAddedListener = listener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage chatMessage = chatMessages.get(position);
        holder.bind(chatMessage);
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    public void addMessage(ChatMessage chatMessage) {
        chatMessages.add(chatMessage);
        notifyItemInserted(chatMessages.size() - 1);
        if (onMessageAddedListener != null) {
            onMessageAddedListener.onMessageAdded();
        }
    }

    /**
     * 就这样吧不想改了我草
     * @param response
     */
    public void updateBotMessage(String response) {
        if (currentBotMessageIndex == -1) {
            // 如果当前没有AI消息，添加一个新的AI消息
            ChatMessage botMessage = new ChatMessage(response, false);
            chatMessages.add(botMessage);
            currentBotMessageIndex = chatMessages.size() - 1;
            notifyItemInserted(currentBotMessageIndex);
        } else {
            // 如果当前有AI消息，更新这个消息
            ChatMessage botMessage = chatMessages.get(currentBotMessageIndex);
            botMessage.setMessage(botMessage.getMessage() + response);
            notifyItemChanged(currentBotMessageIndex);
        }

        // 触发滚动到最新消息
        if (onMessageAddedListener != null) {
            onMessageAddedListener.onMessageAdded();
        }
    }

    public void resetBotMessageIndex() {
        currentBotMessageIndex = -1;
        isThinking = false;
    }

    /**
     * 狗屎啊写个判断死活不行
     */
    static class ChatViewHolder extends RecyclerView.ViewHolder {
        private TextView messageTextView;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
        }

        public void bind(ChatMessage chatMessage) {
            String message = chatMessage.getMessage();
            SpannableString spannableString = new SpannableString(message);
            Pattern pattern = Pattern.compile("--------------------------------------\n|---------------------------------------");
            Matcher matcher = pattern.matcher(message);

            int start = 0;
            boolean isThinking = false;

            while (matcher.find()) {
                if (matcher.group().equals("--------------------------------------\n")) {
                    isThinking = true;
                } else if (matcher.group().equals("---------------------------------------")) {
                }
                spannableString.setSpan(new ForegroundColorSpan(isThinking ? Color.parseColor("#808080") : Color.BLACK), start, matcher.start(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannableString.setSpan(new AbsoluteSizeSpan(isThinking ? 12 : 16, true), start, matcher.start(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                start = matcher.end();
            }
            messageTextView.setText(messageTextView.getText().toString().replace("--------------------------------------\n",""));
            messageTextView.setText(messageTextView.getText().toString().replace("---------------------------------------",""));

            // Apply color and size to remaining text
            if (start < message.length()) {
                spannableString.setSpan(new ForegroundColorSpan(isThinking ? Color.parseColor("#808080") : Color.BLACK), start, message.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannableString.setSpan(new AbsoluteSizeSpan(isThinking ? 12 : 16, true), start, message.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            messageTextView.setText(spannableString);
            if (chatMessage.isUser()) {
                messageTextView.setBackgroundResource(R.drawable.user_message_background);
                messageTextView.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
                messageTextView.setGravity(View.TEXT_ALIGNMENT_TEXT_END);
            } else {
                messageTextView.setBackgroundResource(R.drawable.bot_message_background);
                messageTextView.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
                messageTextView.setGravity(View.TEXT_ALIGNMENT_TEXT_START);
            }
        }
    }

    public interface OnMessageAddedListener {
        void onMessageAdded();
    }
}
