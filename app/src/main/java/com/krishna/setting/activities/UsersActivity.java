package com.krishna.setting.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.krishna.setting.adapters.UserAdapter;
import com.krishna.setting.databinding.ActivityUsersBinding;
import com.krishna.setting.listeners.UserListener;
import com.krishna.setting.models.User;
import com.krishna.setting.utilities.PreferanceManager;
import com.krishna.setting.utilities.constants;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends BaseActivity implements UserListener {
    private ActivityUsersBinding binding;
    private PreferanceManager preferanceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferanceManager = new PreferanceManager(getApplicationContext());
        setListeners();
        getUsers();
    }
    private void setListeners(){
        binding.imageback.setOnClickListener(v -> onBackPressed());

    }
    private void getUsers(){
        loading(true);
        FirebaseFirestore database =FirebaseFirestore.getInstance();
        database.collection(constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = preferanceManager.getString(constants.KEY_USER_ID);
                    if(task.isSuccessful() && task.getResult()!=null ){
                        List<User> users = new ArrayList<>();
                        for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                            if(currentUserId.equals(queryDocumentSnapshot.getId())){
                                continue;
                            }
                            User user=new User();
                            user.name=queryDocumentSnapshot.getString(constants.KEY_NAME);
                            user.email=queryDocumentSnapshot.getString(constants.KEY_EMAIL);
                            user.image=queryDocumentSnapshot.getString(constants.KEY_IMAGE);
                            user.token=queryDocumentSnapshot.getString(constants.KEY_FCM_TOKEN);
                            user.id=queryDocumentSnapshot.getId();
                            users.add(user);
                        }
                        if(users.size()>0){
                            UserAdapter userAdapter = new UserAdapter(users,this);
                            binding.userrecyclerview.setAdapter(userAdapter);
                            binding.userrecyclerview.setVisibility(View.VISIBLE);
                        }
                        else {
                            showErrorMessage();
                        }
                    }else {
                        showErrorMessage();
                    }
                });
    }
    private void showErrorMessage(){
        binding.texterrormsg.setText(String.format("%s","No User Available"));
        binding.texterrormsg.setVisibility(View.VISIBLE);
    }
    private void loading(Boolean isLoading){
        if(isLoading){
            binding.pd1.setVisibility(View.VISIBLE);
        }
        else {
            binding.pd1.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent= new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(constants.KEY_USER,user);
        startActivity(intent);
        finish();
    }
}