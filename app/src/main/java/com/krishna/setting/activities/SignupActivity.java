package com.krishna.setting.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.Constants;
import com.krishna.setting.databinding.ActivitySigninBinding;
import com.krishna.setting.databinding.ActivitySignupBinding;
import com.krishna.setting.utilities.PreferanceManager;
import com.krishna.setting.utilities.constants;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.regex.Pattern;

public class SignupActivity extends AppCompatActivity {
    private ActivitySignupBinding binding;
    private ProgressBar pd;
    private String encodeImage;
    private PreferanceManager preferanceManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferanceManager=new PreferanceManager(getApplicationContext());
        setListeners();

    }
    private void setListeners() {
        binding.Log.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), SigninActivity.class)));
        binding.signupbutton.setOnClickListener(v -> {
            if(isValidSignUpDetails()){
                signUp();
            }
        });
        binding.profileimage.setOnClickListener(v -> {
            Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });

    }
    private void showToast(String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }
    private void signUp(){
        loading(true);
        FirebaseFirestore database =FirebaseFirestore.getInstance();
        HashMap<String,Object>user=new HashMap<>();
        user.put(constants.KEY_NAME,binding.inputname.getText().toString());
        user.put(constants.KEY_EMAIL,binding.inputemail.getText().toString());
        user.put(constants.KEY_PASSWORD,binding.inputpassword.getText().toString());
        user.put(constants.KEY_IMAGE,encodeImage);
        database.collection(constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    preferanceManager.putBoolean(constants.KEY_IS_SIGNED_IN,true);
                    preferanceManager.putString(constants.KEY_USER_ID,documentReference.getId());
                    preferanceManager.putString(constants.KEY_NAME,binding.inputname.getText().toString());
                    preferanceManager.putString(constants.KEY_IMAGE,encodeImage);
                    Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                    startActivity(intent);
                })
                .addOnFailureListener(exception ->{
                    loading(false);
                    showToast(exception.getMessage() );
                });


    }
    private String encodeImage(Bitmap bitmap){
        int previewWidth =150;
        int previewHeight = bitmap.getHeight()*previewWidth/bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap,previewWidth,previewHeight,false);
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
        byte[] bytes=byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes,Base64.DEFAULT);
    }
    private final ActivityResultLauncher<Intent>pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),result -> {
                if(result.getResultCode()==RESULT_OK){
                    if(result.getData() !=null){
                        Uri imageUri = result.getData().getData();
                        try{
                            InputStream inputStream=getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap= BitmapFactory.decodeStream(inputStream);
                            binding.profileimage.setImageBitmap(bitmap);
                            binding.addimage.setVisibility(View.GONE);
                            encodeImage=encodeImage(bitmap);

                        }catch (FileNotFoundException e){
                            e.printStackTrace();

                        }
                    }
                }
            }
    );
    private Boolean isValidSignUpDetails() {
        if (encodeImage == null) {
            showToast("select profile image");
            return false;
        } else if (binding.inputname.getText().toString().trim().isEmpty()) {
            showToast("Enter name");
            return false;
        } else if (binding.inputemail.getText().toString().trim().isEmpty()) {
            showToast("Enter email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputemail.getText().toString()).matches()) {
            showToast("enter valid email");
            return false;
        } else if (binding.inputpassword.getText().toString().trim().isEmpty()) {
            showToast("Enter Password");
            return false;
        } else if (binding.confirmpassword.getText().toString().trim().isEmpty()) {
            showToast("confirm your password");
            return false;
        } else if (!binding.inputpassword.getText().toString().equals(binding.confirmpassword.getText().toString())) {
            showToast("Password Must be same ");
            return false;
        } else {
            return true;
        }

    }
    private void loading(Boolean isLoading){
        if(isLoading){
            binding.signupbutton.setVisibility(View.INVISIBLE);
            binding.pd.setVisibility(View.VISIBLE);
        }else {
            binding.pd.setVisibility(View.VISIBLE);
            binding.signupbutton.setVisibility(View.VISIBLE);
        }
    }
    }