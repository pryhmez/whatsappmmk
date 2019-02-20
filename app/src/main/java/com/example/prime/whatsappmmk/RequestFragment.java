package com.example.prime.whatsappmmk;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {

    private  View requestFragmentView;
    private RecyclerView myReqList;

    private DatabaseReference chatReqRef, usersRef, contactRef;
    private FirebaseAuth mAuth;
    private String currentUserID;


    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        requestFragmentView = inflater.inflate(R.layout.fragment_request, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid().toString();
        chatReqRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        contactRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        myReqList = (RecyclerView) requestFragmentView.findViewById(R.id.chat_requests_list);
        myReqList.setLayoutManager(new LinearLayoutManager(getContext()));

        return requestFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatReqRef.child(currentUserID), Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, RequestViewHolder>adapter = new FirebaseRecyclerAdapter<Contacts, RequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestViewHolder holder, int position, @NonNull Contacts model) {
                      holder.itemView.findViewById(R.id.req_accept_btn).setVisibility(View.VISIBLE);
                      holder.itemView.findViewById(R.id.req_reject_btn).setVisibility(View.VISIBLE);

                      final String list_user_id = getRef(position).getKey();

                      DatabaseReference getTyperef = getRef(position).child("request_type").getRef();

                      getTyperef.addValueEventListener(new ValueEventListener() {
                          @Override
                          public void onDataChange(DataSnapshot dataSnapshot) {

                              if (dataSnapshot.exists()){
                                  String type = dataSnapshot.getValue().toString();

                                  if (type.equals("recieved")){
                                      usersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                          @Override
                                          public void onDataChange(DataSnapshot dataSnapshot) {
                                              if (dataSnapshot.hasChild("image")){

                                                  final String requestProfileImage = dataSnapshot.child("image").getValue().toString();

                                                  Picasso.get().load(requestProfileImage).into(holder.profileImage);

                                              }

                                              final String requestUsername = dataSnapshot.child("name").getValue().toString();
                                              final String requestUserStatus = dataSnapshot.child("status").getValue().toString();

                                              holder.userName.setText(requestUsername);
                                              holder.userStatus.setText(requestUserStatus);

                                              holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                  @Override
                                                  public void onClick(View v) {
                                                      CharSequence options[] = new CharSequence[]{
                                                              "Accept",
                                                              "Cancel"
                                                      };

                                                      AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                      builder.setTitle(requestUsername + " Chat Request");

                                                      builder.setItems(options, new DialogInterface.OnClickListener() {
                                                          @Override
                                                          public void onClick(DialogInterface dialog, int which) {
                                                              if (which == 0){
                                                                  contactRef.child(currentUserID).child(list_user_id).child("Contact")
                                                                          .setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                      @Override
                                                                      public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()) {
                                                                            contactRef.child(list_user_id).child(currentUserID).child("Contact")
                                                                                    .setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()) {
                                                                                        chatReqRef.child(currentUserID).child(list_user_id)
                                                                                                .removeValue()
                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                       if (task.isSuccessful()){
                                                                                                           chatReqRef.child(list_user_id).child(currentUserID)
                                                                                                                   .removeValue()
                                                                                                                   .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                       @Override
                                                                                                                       public void onComplete(@NonNull Task<Void> task) {
                                                                                                                           if (task.isSuccessful()){
                                                                                                                               Toast.makeText(getContext(), "Contact Saved", Toast.LENGTH_SHORT).show();
                                                                                                                           }
                                                                                                                       }
                                                                                                                   });
                                                                                                       }
                                                                                                    }
                                                                                                });

                                                                                    }
                                                                                }
                                                                            });
                                                                        }
                                                                      }
                                                                  });
                                                              }
                                                              if (which == 1){
                                                                  chatReqRef.child(currentUserID).child(list_user_id)
                                                                          .removeValue()
                                                                          .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                              @Override
                                                                              public void onComplete(@NonNull Task<Void> task) {
                                                                                  if (task.isSuccessful()){
                                                                                      chatReqRef.child(list_user_id).child(currentUserID)
                                                                                              .removeValue()
                                                                                              .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                  @Override
                                                                                                  public void onComplete(@NonNull Task<Void> task) {
                                                                                                      if (task.isSuccessful()){
                                                                                                          Toast.makeText(getContext(), "Contact Removed", Toast.LENGTH_SHORT).show();
                                                                                                      }
                                                                                                  }
                                                                                              });
                                                                                  }
                                                                              }
                                                                          });

                                                              }
                                                          }
                                                      });

                                                      builder.show();
                                                  }
                                              });
                                          }

                                          @Override
                                          public void onCancelled(DatabaseError databaseError) {

                                          }
                                      });
                                  }
                              }
                          }

                          @Override
                          public void onCancelled(DatabaseError databaseError) {

                          }
                      });
            }

            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(
                        R.layout.users_display_layout, viewGroup,false);
                RequestViewHolder holder = new RequestViewHolder(view);
                return holder;
            }
        };

        myReqList.setAdapter(adapter);
        adapter.startListening();
    }


    public static class RequestViewHolder extends RecyclerView.ViewHolder{

        TextView userName, userStatus;
        CircleImageView profileImage;
        Button acceptBtn, cancelBtn;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);


            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            acceptBtn = itemView.findViewById(R.id.req_accept_btn);
            cancelBtn = itemView.findViewById(R.id.req_reject_btn);
        }
    }
}
