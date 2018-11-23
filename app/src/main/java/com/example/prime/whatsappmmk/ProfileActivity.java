package com.example.prime.whatsappmmk;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private String recieverUserID, current_state, senderUserID;

    private ImageView userProfileImage;
    private TextView userProfileName, userProfileStatus;
    private Button sendMessageReqButton;

    private DatabaseReference userRef, chatReqRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatReqRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");

        recieverUserID = getIntent().getExtras().get("visit_user_id").toString();
        senderUserID = mAuth.getCurrentUser().getUid();

//        Toast.makeText(this, "User ID: "+ recieverUserID, Toast.LENGTH_SHORT).show();

        userProfileImage = findViewById(R.id.visit_profile_image);
        userProfileName = findViewById(R.id.visit_user_name);
        userProfileStatus = findViewById(R.id.visit_profile_status);
        sendMessageReqButton = findViewById(R.id.send_message_request_btn);
        current_state = "new";

        retrieveUserInfo();


    }

    private void retrieveUserInfo() {
        userRef.child(recieverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && (dataSnapshot.hasChild("image"))){
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                  Picasso.get().load(userImage).placeholder(R.drawable.default_image).into(userProfileImage);
                  userProfileName.setText(userName);
                  userProfileStatus.setText(userStatus);

                  manageChatReq();
                }else{

                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

//                    Picasso.get().load(userImage).placeholder(R.drawable.default_image).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void manageChatReq() {

        chatReqRef.child(senderUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(recieverUserID)){
                    String request_type = dataSnapshot.child(recieverUserID).child("request_type")
                            .getValue().toString();

                    if (request_type.equals("sent")){
                        current_state = "request_sent";
                        sendMessageReqButton.setText("Cancel Chat Request");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        if (!senderUserID.equals(recieverUserID)){
            sendMessageReqButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessageReqButton.setEnabled(false);

                    if(current_state.equals("new")){
                        sendChatRequest();
                    }
                    if (current_state.equals("request_sent")){
                        cancelChatRequest();
                    }
                }
            });
        }else{
            sendMessageReqButton.setVisibility(View.INVISIBLE);
        }
    }

    private void cancelChatRequest() {
        chatReqRef.child(senderUserID).child(recieverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            chatReqRef.child(recieverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                sendMessageReqButton.setEnabled(true);
                                                current_state = "new";
                                                sendMessageReqButton.setText("Send Message");
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void sendChatRequest() {
        chatReqRef.child(senderUserID).child(recieverUserID).child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    chatReqRef.child(recieverUserID).child(senderUserID).child("request_type")
                            .setValue("recieved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                sendMessageReqButton.setEnabled(true);
                                current_state = "request_sent";
                                sendMessageReqButton.setText("Cancel Chat Request");
                            }
                        }
                    });
                }
            }
        });
    }
}