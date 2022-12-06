package com.krishna.setting.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.krishna.setting.utilities.PreferanceManager;
import com.krishna.setting.utilities.constants;

import javax.annotation.Nullable;

public class BaseActivity extends AppCompatActivity {
    private DocumentReference documentReference;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        PreferanceManager preferanceManager= new PreferanceManager(getApplicationContext());
        FirebaseFirestore database =FirebaseFirestore.getInstance();
        documentReference=database.collection(constants.KEY_COLLECTION_USERS)
                .document(preferanceManager.getString(constants.KEY_USER_ID));
    }

    @Override
    protected void onPause() {
        super.onPause();
        documentReference.update(constants.KEY_AVAILABILITY,0);
    }
    @Override
    protected void onResume() {
        super.onResume();
        documentReference.update(constants.KEY_AVAILABILITY,1);
    }
}
