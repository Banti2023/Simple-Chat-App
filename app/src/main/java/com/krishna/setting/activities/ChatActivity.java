package com.krishna.setting.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.krishna.setting.Network.ApiClient;
import com.krishna.setting.Network.ApiService;
import com.krishna.setting.adapters.Chatadapter;
import com.krishna.setting.databinding.ActivityChatBinding;
import com.krishna.setting.models.ChatMessage;
import com.krishna.setting.models.User;
import com.krishna.setting.utilities.PreferanceManager;
import com.krishna.setting.utilities.constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity {
    private ActivityChatBinding binding;
    private User receiverUser;
    private List<ChatMessage> chatMessages;
    private Chatadapter chatadapter;
    private PreferanceManager preferanceManager;
    private FirebaseFirestore database;
    private String conversationId = null;
    private Boolean isReceiverAvailable=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListers();
        loadReceiverDetails();
        init();
        listenMessages();
    }
    private void init(){
        preferanceManager=new PreferanceManager(getApplicationContext());
        chatMessages=new ArrayList<>();
        chatadapter=new Chatadapter(
                chatMessages,
                getBitmapFromEncodedString(receiverUser.image),
                preferanceManager.getString(constants.KEY_USER_ID)
        );
        binding.chatrecycler.setAdapter(chatadapter);
        database=FirebaseFirestore.getInstance();
    }
    private void sendMessage(){
        HashMap<String,Object> message = new HashMap<>();
        message.put(constants.KEY_SENDER_ID,preferanceManager.getString(constants.KEY_USER_ID));
        message.put(constants.KEY_RECEIVER_ID,receiverUser.id);
        message.put(constants.KEY_MESSAGE,binding.inputtext.getText().toString());
        message.put(constants.KEY_TIMESTAMP,new Date());
        database.collection(constants.KEY_COLLECTION_CHAT).add(message);
        if(conversationId!=null){
            updateconversion(binding.inputtext.getText().toString());
        }else
        {
            HashMap<String ,Object> conversion= new HashMap<>();
            conversion.put(constants.KEY_SENDER_ID,preferanceManager.getString(constants.KEY_USER_ID));
            conversion.put(constants.KEY_SENDER_NAME,preferanceManager.getString(constants.KEY_NAME));
            conversion.put(constants.KEY_SENDER_IMAGE,preferanceManager.getString(constants.KEY_IMAGE));
            conversion.put(constants.KEY_RECEIVER_ID,receiverUser.id);
            conversion.put(constants.KEY_RECEIVER_NEME,receiverUser.name);
            conversion.put(constants.KEY_RECEIVER_IMAGE,receiverUser.image);
            conversion.put(constants.KEY_LAST_MESSAGE,binding.inputtext.getText().toString());
            conversion.put(constants.KEY_TIMESTAMP,new Date());
            addconversion(conversion);
        }
        if(!isReceiverAvailable){
            try {
                JSONArray tokens= new JSONArray();
                tokens.put(receiverUser.token);
                JSONObject data= new JSONObject();
                data.put(constants.KEY_USER_ID,preferanceManager.getString(constants.KEY_USER_ID));
                data.put(constants.KEY_NAME,preferanceManager.getString(constants.KEY_NAME));
                data.put(constants.KEY_FCM_TOKEN,preferanceManager.getString(constants.KEY_FCM_TOKEN));
                data.put(constants.KEY_MESSAGE,binding.inputtext.getText().toString());
                JSONObject body= new JSONObject();
                body.put(constants.REMOTE_MSG_DATA,data);
                body.put(constants.REMOTE_MSG_REGISTRATION_IDS,tokens);
                sendNotification(body.toString());




            }catch (Exception exception){
                showToast(exception.getMessage());
            }
        }
        binding.inputtext.setText(null);
    }
    private void showToast(String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }
    private void sendNotification(String messageBody){
        ApiClient.getClient().create(ApiService.class).sendMessage(
                constants.getRemotemsgHeaders(),
                messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call,@NonNull Response<String> response) {
                if(response.isSuccessful()){
                    try {
                        if(response.body()!=null){
                            JSONObject responseJson=new JSONObject(response.body());
                            JSONArray results= responseJson.getJSONArray("results");
                            if(responseJson.getInt("failure")==1){
                                JSONObject error=(JSONObject) results.get(0);
                                showToast(error.getString("error"));
                                return;
                            }
                        }
                    }catch (JSONException e ){
                        e.printStackTrace();
                    }
                    showToast("Message sent Successfully");
                }else{
                    showToast("Error"+ response.code());
                }

            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                showToast(t.getMessage());
            }
        });
    }
    private void listenAvailabilityofReceiver(){
        database.collection(constants.KEY_COLLECTION_USERS).document(
                receiverUser.id

        ).addSnapshotListener(ChatActivity.this,((value, error) -> {
            if(error!=null){
                return;
            }if(value!=null){
                if(value.getLong(constants.KEY_AVAILABILITY)!=null){
                    int availability= Objects.requireNonNull(
                            value.getLong(constants.KEY_AVAILABILITY)

                    ).intValue();
                    isReceiverAvailable=availability==1;
                }
                receiverUser.token=value.getString(constants.KEY_FCM_TOKEN);
                if(receiverUser.image==null){
                    receiverUser.image=value.getString(constants.KEY_IMAGE);
                    chatadapter.setReceiverProfileimage(getBitmapFromEncodedString(receiverUser.image));
                    chatadapter.notifyItemRangeChanged(0,chatMessages.size());
                }
            }
            if(isReceiverAvailable) {
                binding.txtavailable.setVisibility(View.VISIBLE);
            }else{
                binding.txtavailable.setVisibility(View.GONE);
            }
        }));
    }
    private void listenMessages(){
        database.collection(constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(constants.KEY_SENDER_ID,preferanceManager.getString(constants.KEY_USER_ID))
                .whereEqualTo(constants.KEY_RECEIVER_ID,receiverUser.id)
                .addSnapshotListener(eventListener);
        database.collection(constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(constants.KEY_SENDER_ID,receiverUser.id)
                .whereEqualTo(constants.KEY_RECEIVER_ID,preferanceManager.getString(constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }
    private final EventListener<QuerySnapshot> eventListener = ((value, error) -> {
        if(error!=null){
            return;
        }
        if(value != null){
            int count = chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()){
                if(documentChange.getType()== DocumentChange.Type.ADDED){
                    ChatMessage chatMessage= new ChatMessage();
                    chatMessage.senderID=documentChange.getDocument().getString(constants.KEY_SENDER_ID);
                    chatMessage.receiverId=documentChange.getDocument().getString(constants.KEY_RECEIVER_ID);
                    chatMessage.message=documentChange.getDocument().getString(constants.KEY_MESSAGE);
                    chatMessage.datetime=getReadableDateTime(documentChange.getDocument().getDate(constants.KEY_TIMESTAMP));
                    chatMessage.dateobject=documentChange.getDocument().getDate(constants.KEY_TIMESTAMP);
                    chatMessages.add(chatMessage);

                }
            }
            Collections.sort(chatMessages,(obj1,obj2)->obj1.dateobject.compareTo(obj2.dateobject));
            if(count==0){
                chatadapter.notifyDataSetChanged();
            }else{
                chatadapter.notifyItemRangeInserted(chatMessages.size(),chatMessages.size());
                binding.chatrecycler.smoothScrollToPosition(chatMessages.size()-1);
            }
            binding.chatrecycler.setVisibility(View.VISIBLE);
        }
        binding.pd.setVisibility(View.GONE);
        if(conversationId==null){
            checkforconversion();
        }
    });
    private Bitmap getBitmapFromEncodedString(String encodedImage){
        if(encodedImage != null) {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }else{
            return  null;
        }
    }
    private void loadReceiverDetails(){
        receiverUser = (User)getIntent().getSerializableExtra(constants.KEY_USER);
        binding.textname.setText(receiverUser.name);
    }
    private void setListers(){
        binding.imageback.setOnClickListener(v -> onBackPressed());
        binding.layoutsend.setOnClickListener(v -> sendMessage());

    }
    private String getReadableDateTime(Date date){
        return new SimpleDateFormat(" dd/MM/yy - hh:mm a", Locale.getDefault()).format(date);
    }
    private void addconversion(HashMap<String,Object> conversion){
        database.collection(constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversationId=documentReference.getId());
    }
    private void updateconversion(String message){
        DocumentReference documentReference=
                database.collection(constants.KEY_COLLECTION_CONVERSATIONS).document(conversationId);
        documentReference.update(
                constants.KEY_LAST_MESSAGE,message,
                constants.KEY_TIMESTAMP,new Date()
        );
    }
    private void checkforconversion(){
        if(chatMessages.size()!=0){
            checkforconversationRemotely(
                    preferanceManager.getString(constants.KEY_USER_ID),
                    receiverUser.id
            );
            checkforconversationRemotely(
                    receiverUser.id,
                    preferanceManager.getString(constants.KEY_USER_ID)
            );
        }
    }
    private void checkforconversationRemotely(String senderId,String receiverId){
        database.collection(constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(constants.KEY_SENDER_ID,senderId)
                .whereEqualTo(constants.KEY_RECEIVER_ID,receiverId)
                .get()
                .addOnCompleteListener(conversationOnCompleteListener);
    }
    private  final OnCompleteListener<QuerySnapshot>conversationOnCompleteListener= task -> {
        if(task.isSuccessful() && task.getResult()!= null && task.getResult().getDocuments().size()>0){
            DocumentSnapshot documentSnapshot=task.getResult().getDocuments().get(0);
            conversationId=documentSnapshot.getId();
        }
    };
    @Override
    protected void onResume(){
        super.onResume();
        listenAvailabilityofReceiver();
    }
}