package com.example.prime.whatsappmmk;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by prime on 12/16/18.
 */

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    public MessagesAdapter(List<Messages> userMessagesList){
        this.userMessagesList = userMessagesList;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView senderMessageText, recieverMessageText;
        public CircleImageView recieverProfileImage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText = itemView.findViewById(R.id.sender_message_text);
            recieverMessageText = itemView.findViewById(R.id.receiver_message_text);
            recieverProfileImage = itemView.findViewById(R.id.message_profile_image);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_messages_layout, viewGroup, false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, int i) {
        String messageSenderID = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(i);

        String fromUserID = messages.getFrom();
        String fromMessagesType = messages.getType();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("image")){
                    String recieverimage = dataSnapshot.child("image").getValue().toString();

                    Picasso.get().load(recieverimage).placeholder(R.drawable.userrs).into(messageViewHolder.recieverProfileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (fromMessagesType.equals("text")){
            messageViewHolder.recieverMessageText.setVisibility(View.INVISIBLE);
            messageViewHolder.recieverProfileImage.setVisibility(View.INVISIBLE);

            messageViewHolder.senderMessageText.setVisibility(View.INVISIBLE);

            if (fromUserID.equals(messageSenderID)){

                messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);
                messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                messageViewHolder.senderMessageText.setTextColor(Color.BLACK);
                messageViewHolder.senderMessageText.setText(messages.getMessage());
            }else {

                messageViewHolder.recieverProfileImage.setVisibility(View.VISIBLE);

                messageViewHolder.recieverMessageText.setVisibility(View.VISIBLE);

                messageViewHolder.recieverMessageText.setBackgroundResource(R.drawable.reciever_messages_layout);
                messageViewHolder.recieverMessageText.setTextColor(Color.BLACK);
                messageViewHolder.recieverMessageText.setText(messages.getMessage());

            }
        }

    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }


}
