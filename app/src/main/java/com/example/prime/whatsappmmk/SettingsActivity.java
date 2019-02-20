package com.example.prime.whatsappmmk;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
//import com.theartofdev.edmodo.cropper.CropImage;
//import com.theartofdev.edmodo.cropper.CropImageView;
/*import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;*/

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    private Button updateAccountSettings;
    private EditText userName, userStatus;
    private CircleImageView userProfileImage;
    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    private String currentUserID, id;
    private DatabaseReference rootRef;
    private static final int galleryPick = 2;
    private static final int picturePick = 1;
    private StorageReference userProfileImageRef;

    private Toolbar settingsToolBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("profile images");


        initializeFields();

        userName.setVisibility(View.INVISIBLE);

        updateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSettings();
            }
        });
        retrieveUserInfo();


        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent pictureIntent = new Intent(
                            MediaStore.ACTION_IMAGE_CAPTURE
                    );
                    if(pictureIntent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(pictureIntent,
                                picturePick);
                    }
//
//                Intent galleryIntent = new Intent();
//                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
//                galleryIntent.setType("Image/*");
//
//                startActivityForResult(galleryIntent, galleryPick);
////
////                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
////                getIntent.setType("image/*");
//
//                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                pickIntent.setType("image/*");
//
//                Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Image");
//                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

            }
        });

        userProfileImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("Image/*");

                startActivityForResult(galleryIntent, galleryPick);
                return false;
            }
        });
    }

    private void retrieveUserInfo() {
        rootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChild("name") && dataSnapshot.hasChild("image"))
                {
                  String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                    String retrieveStatus = dataSnapshot.child("status").getValue().toString();
                    String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();

                    userName.setText(retrieveUserName);
                    userStatus.setText(retrieveStatus);
                    Picasso.get().load(retrieveProfileImage).into(userProfileImage);

                }else if(dataSnapshot.exists() && dataSnapshot.hasChild("name") && dataSnapshot.hasChild("status")){

                    String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                    String retrieveStatus = dataSnapshot.child("status").getValue().toString();

                    userName.setText(retrieveUserName);
                    userStatus.setText(retrieveStatus);

                }else{
                    userName.setVisibility(View.VISIBLE);
                    Toast.makeText(SettingsActivity.this, "Please set and update profile info", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updateSettings() {
        String setUserName = userName.getText().toString().trim();
        String setStatus = userStatus.getText().toString().trim();

        if(TextUtils.isEmpty(setUserName)){
            userName.setError("pls write your userName first");
        }
        if(TextUtils.isEmpty(setStatus)){
            userStatus.setError("pls write status");
        }else{
            HashMap<String, Object> profileMap = new HashMap<>();
                    profileMap.put("uid", currentUserID);
                    profileMap.put("name", setUserName);
                    profileMap.put("status", setStatus);
                    profileMap.put("phone", "");
            rootRef.child("Users").child(currentUserID).updateChildren(profileMap).
                    addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                sendUserToMainActivity();
                            }
                            else{
                                String message = task.getException().toString();
                                Toast.makeText(SettingsActivity.this, "Error" + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


        }
    }

    private void sendUserToMainActivity() {
        Intent mainintent = new Intent(SettingsActivity.this, MainActivity.class);
        mainintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainintent);
        finish();
    }

    private void initializeFields() {

        settingsToolBar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(settingsToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Settings");

        updateAccountSettings = findViewById(R.id.updatesettings);
        userName = findViewById(R.id.set_user_name);
        userStatus = findViewById(R.id.set_profile_status);
        userProfileImage = findViewById(R.id.set_profile_image);
        loadingBar = new ProgressDialog(this);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == picturePick && resultCode == RESULT_OK && data !=null);
        {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap)extras.get("data");
            userProfileImage.setImageBitmap(imageBitmap);

            uploadImage();
//            Uri imageUri = data.getData();
//
//           CropImage.activity()
//                    .setGuidelines(CropImageView.Guidelines.ON)
//                   .setAspectRatio(1, 1)
//                    .start(this);
//        }
//
//        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
//            CropImage.ActivityResult result = CropImage.getActivityResult(data);
//            if (resultCode == RESULT_OK){
//                Uri resultUri = result.getUri();
//
//                StorageReference filePath = userProfileImageRef.child(currentUserID + ".jpg");
//
//                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//                        if (task.isSuccessful()){
//                            Toast.makeText(SettingsActivity.this,
//                                    "Profile Image Uploaded successfully", Toast.LENGTH_SHORT).show();
//                        }else{
//                            String message = task.getException().toString();
//                            Toast.makeText(SettingsActivity.this, "sorry :"+ message, Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
            }
            if (galleryPick == requestCode && resultCode == RESULT_OK && data != null){
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap)extras.get("data");
                userProfileImage.setImageBitmap(imageBitmap);

                uploadImage();
            }
        }

    private void uploadImage() {
        loadingBar.setTitle("set Profile Image");
        loadingBar.setMessage("pls wait");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();
        StorageReference filePath = userProfileImageRef.child(currentUserID + ".jpg");
//                .child(currentUserID)
//                .child("image.jpg");

        if (userProfileImage != null){

            userProfileImage.setDrawingCacheEnabled(true);
            userProfileImage.buildDrawingCache();
            Bitmap bitmap = userProfileImage.getDrawingCache();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[]data = baos.toByteArray();

            final UploadTask uploadTask = filePath.putBytes(data);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

//                    Uri downloadURI = taskSnapshot.getDownloadUrl();
                    final String downloadUrl = uploadTask.getResult().getDownloadUrl().toString();
                    id = rootRef.push().getKey();

//                    rootRef.child(id).child("Profile Image").setValue(downloadURI.toString());
                    rootRef.child("Users").child(currentUserID).child("image").setValue(downloadUrl)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(SettingsActivity.this,
                                        "Image saved to database", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }else {
                                String mess = task.getException().toString();
                                Toast.makeText(SettingsActivity.this, "sorry : " + mess, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
                    Toast.makeText(SettingsActivity.this, "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            Toast.makeText(this, "An unexpected error occured pls try again", Toast.LENGTH_SHORT).show();
            loadingBar.dismiss();
        }
    }
//    }

    public void verifyUserExistence(){
        String currentUserID = mAuth.getCurrentUser().getUid();

    }
}
