package com.krishna.setting.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.krishna.setting.activities.MainActivity;
import com.krishna.setting.databinding.ItemRecentConversionBinding;
import com.krishna.setting.models.ChatMessage;
import com.krishna.setting.models.User;

import java.util.List;

public class recentConversationAdapter extends RecyclerView.Adapter<recentConversationAdapter.conversationViewHolder>{
    private final List<ChatMessage> chatMessages;
    private  final com.krishna.setting.listeners.conversationListener conversationListener;

    public recentConversationAdapter(List<ChatMessage> chatMessages, MainActivity conversationListener) {
        this.chatMessages = chatMessages;
        this.conversationListener=conversationListener;
    }

    @NonNull
    @Override
    public conversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new conversationViewHolder(
                ItemRecentConversionBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull conversationViewHolder holder, int position) {
        holder.setData(chatMessages.get(position));

    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    class conversationViewHolder extends RecyclerView.ViewHolder{
        ItemRecentConversionBinding binding;
        conversationViewHolder(ItemRecentConversionBinding itemRecentConversionBinding){
            super(itemRecentConversionBinding.getRoot());
            binding= itemRecentConversionBinding;
        }
        void setData(ChatMessage chatMessage){
            binding.profileimage.setImageBitmap(getconversationimage(chatMessage.conversationimage));
            binding.textname.setText(chatMessage.conversationname);
            binding.textrecentmessage.setText(chatMessage.message);
            binding.getRoot().setOnClickListener(v -> {
                User user = new User();
                user.id=chatMessage.conversationid;
                user.name=chatMessage.conversationname;
                user.image=chatMessage.conversationimage;
                conversationListener.onconversationClicked(user);
            });
        }
    }
    private Bitmap getconversationimage(String encodeImage){
        byte[]bytes=Base64.decode(encodeImage,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }
}
