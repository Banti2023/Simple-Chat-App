package com.krishna.setting.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.krishna.setting.databinding.ItemContainerReceiverMessageBinding;
import com.krishna.setting.databinding.ItemContainerSentMessageBinding;
import com.krishna.setting.models.ChatMessage;

import java.util.List;

public class Chatadapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<ChatMessage> chatMessage;
    private Bitmap receiverProfileimage;
    private final String senderId;
public static final int VIEW_TYPE_SENT=1;
public static final int VIEW_TYPE_RECEIVED=2;
public void setReceiverProfileimage(Bitmap bitmap){
    receiverProfileimage= bitmap;
}
    public Chatadapter(List<ChatMessage> chatMessage, Bitmap receiverProfileimage, String senderId) {
        this.chatMessage = chatMessage;
        this.receiverProfileimage = receiverProfileimage;
        this.senderId = senderId;

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==VIEW_TYPE_SENT){
            return new SentMessageViewHolder(
                    ItemContainerSentMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }
        else {
            return new ReceivedMessageViewHolder(ItemContainerReceiverMessageBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,
                    false
            ));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    if(getItemViewType(position)==VIEW_TYPE_SENT){
        ((SentMessageViewHolder)holder).setData(chatMessage.get(position));
    }else {
        ((ReceivedMessageViewHolder)holder).setData(chatMessage.get(position),receiverProfileimage);
    }
    }

    @Override
    public int getItemCount() {
        return chatMessage.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(chatMessage.get(position).senderID.equals(senderId)){
            return VIEW_TYPE_SENT;
        }else{
            return VIEW_TYPE_RECEIVED;
        }
    }

    static  class SentMessageViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerSentMessageBinding binding;
        SentMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding){
            super(itemContainerSentMessageBinding.getRoot());
            binding=itemContainerSentMessageBinding;
        }
        void setData(ChatMessage chatMessage){
            binding.textmessage.setText(chatMessage.message);
            binding.textdatetime.setText(chatMessage.datetime);
        }
    }
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerReceiverMessageBinding binding;
        ReceivedMessageViewHolder(ItemContainerReceiverMessageBinding itemContainerReceiverMessageBinding){
            super(itemContainerReceiverMessageBinding.getRoot());
            binding=itemContainerReceiverMessageBinding;
        }
        void setData(ChatMessage chatMessage,Bitmap receiverProfileimage){
            binding.textmessage.setText(chatMessage.message);
            binding.textdatetime.setText(chatMessage.datetime);
            if(receiverProfileimage!=null) {
                binding.profileimage.setImageBitmap(receiverProfileimage);
            }
        }
    }
}
