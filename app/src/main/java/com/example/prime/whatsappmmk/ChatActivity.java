package com.example.prime.whatsappmmk;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
//import android.widget.Toolbar;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String messageRecieverID, messageRecieverName, messageRecieverImage, messageSenderID;

    private TextView userName, userLastSeen;
    private CircleImageView userImage;

    private Toolbar chatToolBar;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    private ImageButton sendMessageButton;
    private EditText messageInputText;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter;
    private RecyclerView usermessagesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth= FirebaseAuth.getInstance();
        messageSenderID =mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();


        messageRecieverID = getIntent().getExtras().get("visit_user_id").toString();
        messageRecieverName = getIntent().getExtras().get("visit_user_name").toString();
        messageRecieverImage = getIntent().getExtras().get("visit_image").toString();

//        Toast.makeText(ChatActivity.this, messageRecieverID, Toast.LENGTH_SHORT).show();
//        Toast.makeText(ChatActivity.this, messageRecieverName, Toast.LENGTH_SHORT).show();

        initializeControllers();

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        userName.setText(messageRecieverName);
        Picasso.get().load(messageRecieverImage).placeholder(R.drawable.userrs).into(userImage);
    }

    private void initializeControllers() {

        chatToolBar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        userImage = findViewById(R.id.custom_profile_IMAGE);
        userName = (TextView) findViewById(R.id.custom_profile_name);
        userLastSeen = findViewById(R.id.custom_last_seen);

        sendMessageButton = findViewById(R.id.send_message_btn);
        messageInputText = findViewById(R.id.input_message);

        messagesAdapter = new MessagesAdapter(messagesList);
        usermessagesList = findViewById(R.id.private_messages_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this);
        usermessagesList.setLayoutManager(linearLayoutManager);
        usermessagesList.setAdapter(messagesAdapter);

    }


    @Override
    protected void onStart() {
        super.onStart();

        rootRef.child("Message").child(messageSenderID).child(messageRecieverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Messages messages = dataSnapshot.getValue(Messages.class);

                        messagesList.add(messages);

                        messagesAdapter.notifyDataSetChanged();

                        usermessagesList.smoothScrollToPosition(usermessagesList.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void sendMessage() {
        String messageText = messageInputText.getText().toString();

        if (TextUtils.isEmpty(messageText)){
            Toast.makeText(this, "pls type a message", Toast.LENGTH_SHORT).show();
        }else {
            String messageSenderRef = "Message/" + messageSenderID + "/" + messageRecieverID;
            String messageRecieverRef = "Message/" + messageRecieverID + "/" + messageSenderID;

            DatabaseReference userMessageKeyRef = rootRef.child("Message").child(messageSenderID)
                    .child(messageRecieverID).push();

            String messagePushID = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderID);
//            messageTextBody.put("message", messageText);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef+"/"+messagePushID, messageTextBody);
            messageBodyDetails.put(messageRecieverRef+"/"+messagePushID, messageTextBody);

            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        Toast.makeText(ChatActivity.this, "sent", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(ChatActivity.this, "error", Toast.LENGTH_SHORT).show();
                    }messageInputText.setText(null);
                }
            });

        }
    }

}
