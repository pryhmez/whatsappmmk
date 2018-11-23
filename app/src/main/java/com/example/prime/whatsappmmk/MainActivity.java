package com.example.prime.whatsappmmk;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ViewPager myviewPager;
    private TabLayout myTabLayout;
    private  TabsAccessorAdapter myTabsAccessorAdapter;

    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();


        mToolbar = (Toolbar) findViewById(R.id.main_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("IGHUB SOCIAL");

        myviewPager = (ViewPager) findViewById(R.id.mains_tabs_pager);
        myTabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        myviewPager.setAdapter(myTabsAccessorAdapter);

        myTabLayout = (TabLayout)findViewById(R.id.mains_tabs);
        myTabLayout.setupWithViewPager(myviewPager);

    }

    @Override
    protected void onStart() {
        super.onStart();

        if(currentUser == null){
            sendUserToLoginActivity();
        }else{
            verifyUserExistence();
        }
    }

    private void verifyUserExistence() {
        final String currentUserId = currentUser.getUid();

        rootRef = FirebaseDatabase.getInstance().getReference();

        rootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {


            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if((dataSnapshot.child("name").exists())){
                    Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                }else{
                    sendUserToSettingsActivity();
//                    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                    Toast.makeText(MainActivity.this, "okay ohhh", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.options_menu, menu);

        return  true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

//        if(item.getItemId() == R.id.main_logout_options);{
//          mAuth.signOut();
////          sendUserToLoginActivity();
//          sendUserToLoginActivity();
//        }
//
//       if(item.getItemId() == R.id.main_find_friends_options);{
//            sendUserToSettingsActivity();
//        }
//
//        if(item.getItemId() == R.id.main_settings_options);{
//
//        }
//        if(item.getItemId() == R.id.main_create_group_options);{
//            requestNewGroup();
//
//        }
        switch (item.getItemId()){
            case R.id.main_find_friends_options:
          sendUserToFindFriendsActivity();
                Toast.makeText(this, "main friends", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.main_create_group_options:
                requestNewGroup();
                Toast.makeText(this, "create group", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.main_settings_options:
                sendUserToSettingsActivity();
                Toast.makeText(this, "settings", Toast.LENGTH_SHORT).show();
               return true;
            case R.id.main_logout_options:
                mAuth.signOut();
                sendUserToLoginActivity();
                Toast.makeText(this, "logout", Toast.LENGTH_SHORT).show();
                return true;
        }
        return  true;
    }

    private void requestNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                MainActivity.this, R.style.AlertDialog);
        builder.setTitle("Enter Group Name :");

        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("e.g ighub social");
        builder.setView(groupNameField);

        builder.setPositiveButton("create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                String groupName = groupNameField.getText().toString();

                if (TextUtils.isEmpty(groupName)){
                    Toast.makeText(MainActivity.this, "Pls write your group name...", Toast.LENGTH_SHORT).show();
                }
                else{
                    createNewGroup(groupName);

                }

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

             dialogInterface.cancel();

            }
        });
        builder.show();

    }

    private void createNewGroup(final String groupName) {

        rootRef.child("Groups").child(groupName).setValue("").addOnCompleteListener(
                new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(MainActivity.this, groupName +
                            "group is created successfully", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendUserToLoginActivity() {
        Intent loginintent = new Intent(MainActivity.this, LoginActivity.class);
        loginintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(loginintent);
        finish();
    }
    private void sendUserToSettingsActivity() {
        Intent settingsintent = new Intent(MainActivity.this, SettingsActivity.class);
//        settingsintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(settingsintent);
        finish();
    }

    private void sendUserToFindFriendsActivity() {
        Intent friendsintent = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(friendsintent);
        finish();
    }
}
