package com.krishna.setting.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.krishna.setting.adapters.recentConversationAdapter;
import com.krishna.setting.databinding.ActivityMainBinding;
import com.krishna.setting.listeners.conversationListener;
import com.krishna.setting.models.ChatMessage;
import com.krishna.setting.models.User;
import com.krishna.setting.utilities.PreferanceManager;
import com.krishna.setting.utilities.constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity implements conversationListener {
    private ActivityMainBinding binding;
    private PreferanceManager preferanceManager;
    private List<ChatMessage> conversation;
    private recentConversationAdapter conversationAdapter;
    private FirebaseFirestore database;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferanceManager = new PreferanceManager(getApplicationContext());
        loadUserDetails();
        getToken();
        setListeners();
        init();
        listenConversations();


    }
    private void init(){
        conversation=new ArrayList<>();
        conversationAdapter=new recentConversationAdapter(conversation,this );
        binding.conversionRecycle.setAdapter(conversationAdapter);
        database=FirebaseFirestore.getInstance();
    }
    private void setListeners(){
        binding.Imagesignout.setOnClickListener(v -> signOut());
        binding.fab.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(),UsersActivity.class)));
    }

    private  void loadUserDetails(){
        binding.textname.setText(preferanceManager.getString(constants.KEY_NAME));
        byte[] bytes= Base64.decode(preferanceManager.getString(constants.KEY_IMAGE),Base64.DEFAULT);
        Bitmap bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        binding.profileimage.setImageBitmap(bitmap);
    }
    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message,Toast.LENGTH_SHORT).show();

    }

    private void listenConversations(){
        database.collection(constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(constants.KEY_SENDER_ID,preferanceManager.getString(constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        database.collection(constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(constants.KEY_RECEIVER_ID,preferanceManager.getString(constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }
    private final EventListener<QuerySnapshot> eventListener =(value,error)->{
        if(error!=null){
            return;
        }
        if(value != null){
            for(DocumentChange documentChange: value.getDocumentChanges()){
                if(documentChange.getType()==DocumentChange.Type.ADDED){
                    String senderId=documentChange.getDocument().getString(constants.KEY_SENDER_ID);
                    String receiverId=documentChange.getDocument().getString(constants.KEY_RECEIVER_ID);
                    ChatMessage chatMessage=new ChatMessage();
                    chatMessage.senderID=senderId;
                    chatMessage.receiverId=receiverId;
                    if(preferanceManager.getString(constants.KEY_USER_ID).equals(senderId)){
                        chatMessage.conversationimage=documentChange.getDocument().getString(constants.KEY_RECEIVER_IMAGE);
                        chatMessage.conversationname=documentChange.getDocument().getString(constants.KEY_RECEIVER_NEME);
                        chatMessage.conversationid=documentChange.getDocument().getString(constants.KEY_RECEIVER_ID);

                    }else{
                        chatMessage.conversationimage=documentChange.getDocument().getString(constants.KEY_SENDER_IMAGE);
                        chatMessage.conversationname=documentChange.getDocument().getString(constants.KEY_SENDER_NAME);
                        chatMessage.conversationid=documentChange.getDocument().getString(constants.KEY_SENDER_ID);

                    }
                    chatMessage.message=documentChange.getDocument().getString(constants.KEY_LAST_MESSAGE);
                    chatMessage.dateobject=documentChange.getDocument().getDate(constants.KEY_TIMESTAMP);
                    conversation.add(chatMessage);


                }
                else if(documentChange.getType()==DocumentChange.Type.MODIFIED){
                    for (int i=0;i< conversation.size();i++){
                        String senderId=documentChange.getDocument().getString(constants.KEY_SENDER_ID);
                        String receiverId=documentChange.getDocument().getString(constants.KEY_RECEIVER_ID);
                        if(conversation.get(i).senderID.equals(senderId)&& conversation.get(i).receiverId.equals(receiverId)){
                            conversation.get(i).message=documentChange.getDocument().getString(constants.KEY_LAST_MESSAGE);
                            conversation.get(i).dateobject=documentChange.getDocument().getDate(constants.KEY_TIMESTAMP);
                            break;
                        }


                    }
                }
            }
            Collections.sort(conversation,(obj1,obj2)->obj2.dateobject.compareTo(obj1.dateobject));
            conversationAdapter.notifyDataSetChanged();
            binding.conversionRecycle.smoothScrollToPosition(0);
            binding.conversionRecycle.setVisibility(View.VISIBLE);
            binding.pd.setVisibility(View.GONE);
        }
    };
    private void  getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }
    private void updateToken(String token){
        preferanceManager.putString(constants.KEY_FCM_TOKEN,token);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference=
                database.collection(constants.KEY_COLLECTION_USERS).document(
                        preferanceManager.getString(constants.KEY_USER_ID)
                );
        documentReference.update(constants.KEY_FCM_TOKEN,token)
                .addOnFailureListener(e -> showToast("something went wrong "));
    }
    private  void signOut(){
        showToast("signing out....");
        FirebaseFirestore database= FirebaseFirestore.getInstance();
        DocumentReference documentReference=
                database.collection(constants.KEY_COLLECTION_USERS).document(
                        preferanceManager.getString(constants.KEY_USER_ID)
                );
        HashMap<String,Object>updates= new HashMap<>();
        updates.put(constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    preferanceManager.clear();
                    startActivity(new Intent(getApplicationContext(),SigninActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> showToast("unable to sign out"));
    }
    @Override
    public void onconversationClicked(User user){
        Intent intent= new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(constants.KEY_USER,user);
        startActivity(intent);
    }
}