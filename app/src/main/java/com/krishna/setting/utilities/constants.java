package com.krishna.setting.utilities;

import java.util.HashMap;

public class constants {
    public static final String KEY_COLLECTION_USERS ="users";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL="email";
    public static final String KEY_PASSWORD="password";
    public static final String KEY_PREFERANCE_NAME="chatAppPreference";
    public  static final String KEY_IS_SIGNED_IN="isSignedIn";
    public static final String KEY_USER_ID="userId";
    public static final String KEY_IMAGE="image";
    public static final String KEY_FCM_TOKEN="fcmToken";
    public static final String KEY_USER ="user";
    public static final String KEY_COLLECTION_CHAT="chat";
    public static final String KEY_SENDER_ID ="senderId" ;
    public static final String KEY_RECEIVER_ID="receiverId";
    public static final String KEY_MESSAGE="message";
    public static final String KEY_TIMESTAMP="timestamp";
    public static final String KEY_COLLECTION_CONVERSATIONS="conversations";
    public static final String KEY_SENDER_NAME="senderName";
    public static final String KEY_RECEIVER_NEME="receiverNeme";
    public static final String KEY_SENDER_IMAGE="senderimage";
    public static final String KEY_RECEIVER_IMAGE="receiverImage";
    public static final String KEY_LAST_MESSAGE="lastmessage";
    public static final String KEY_AVAILABILITY="availability";
    public static final String REMOTE_MSG_AUTHRIZATION="Authorization";
    public static final String REMOTE_MSG_DATA="data";
    public static final String REMOTE_MSG_REGISTRATION_IDS="registration_ids";
    public static final String REMOTE_MSG_CONTENT_TYPE="Content_type";
    public static HashMap<String,String>remotemsgHeaders=null;
    public static HashMap<String,String>getRemotemsgHeaders(){
        if(remotemsgHeaders==null){
            remotemsgHeaders= new HashMap<>();
            remotemsgHeaders.put(
                    REMOTE_MSG_AUTHRIZATION,
"key=AAAAnXD9Z_k:APA91bH3WRvSji-R7qMGBuCQ8aiCyZxdwp07OCTyfk1HjCE4NE-AOUGNMuLNyd1tTaeA0BmZrirs9noL8_sR0_otEme3PrbsDlN-Qz0roTLdjGE6pEa0Y-oxxgFWTV_-WF6wOFzx9EnR"            );
            remotemsgHeaders.put(
                    REMOTE_MSG_CONTENT_TYPE,
                    "application/json"
            );
        }
        return  remotemsgHeaders;
    }







}
