package com.krishna.setting.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.krishna.setting.databinding.ActivitySigninBinding;
import com.krishna.setting.utilities.PreferanceManager;
import com.krishna.setting.utilities.constants;

public class SigninActivity extends AppCompatActivity {
    private ActivitySigninBinding binding;
    private PreferanceManager preferanceManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferanceManager=new PreferanceManager(getApplicationContext());
        if(preferanceManager.getBoolean(constants.KEY_IS_SIGNED_IN)){
            Intent intent=new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();
        }
        binding =ActivitySigninBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }
    private void setListeners(){
        binding.create.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(),SignupActivity.class)));
        binding.buttonsignin.setOnClickListener(v -> {
            if(isValidSignInDetails()){
                signIn();
            }
        });
    }
    private void signIn(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(constants.KEY_COLLECTION_USERS)
                .whereEqualTo(constants.KEY_EMAIL,binding.email.getText().toString())
                .whereEqualTo(constants.KEY_PASSWORD,binding.password.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()&& task.getResult() !=null
                    && task.getResult().getDocuments().size() >0 ){
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferanceManager.putBoolean(constants.KEY_IS_SIGNED_IN,true);
                        preferanceManager.putString(constants.KEY_USER_ID,documentSnapshot.getId());
                        preferanceManager.putString(constants.KEY_NAME,documentSnapshot.getString(constants.KEY_NAME));
                        preferanceManager.putString(constants.KEY_IMAGE,documentSnapshot.getString(constants.KEY_IMAGE));
                        Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent );
                    }else{
                        loading(false);
                        showToast("Please enter valid details !");
                    }
                });

    }
    private void loading(Boolean isLoading){
            if(isLoading){
                binding.buttonsignin.setVisibility(View.INVISIBLE);
                binding.pd2.setVisibility(View.VISIBLE);
            }else {
                binding.pd2.setVisibility(View.INVISIBLE);
                binding.buttonsignin.setVisibility(View.VISIBLE);
            }
        }
    private void showToast(String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }
    private Boolean isValidSignInDetails(){
        if(binding.email.getText().toString().trim().isEmpty()){
            showToast("Enter Email");
            return false;
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(binding.email.getText().toString()).matches()){
            showToast("enter valid Email");
            return false;
        }
        else if(binding.password.getText().toString().trim().isEmpty()){
            showToast("Enter your Password");
            return false;
        }
        else{
            return true;
        }
    }


}