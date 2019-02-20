package com.example.prime.whatsappmmk;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

//    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private DatabaseReference usersRef;

    private Button loginButton;
    private EditText userEmail, userPassword;
    private TextView needNewAcctLink, forgotPasswordLink, phoneLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
//        currentUser = mAuth.getCurrentUser();

        InitializeFields();

        needNewAcctLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allowUserToLogin();
            }
        });

        phoneLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, PhoneLoginActivity.class));
            }
        });
    }

    private void allowUserToLogin() {
        String email = userEmail.getText().toString().trim();
        String password = userPassword.getText().toString().trim();

        if(TextUtils.isEmpty(email)){
            userEmail.setError("Please Enter Email...");
        }
        if(TextUtils.isEmpty(password)){
            userPassword.setError("Please Enter Password...");
        }else{
            loadingBar.setTitle("Signing You In");
            loadingBar.setMessage("One moment please");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(
                    new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                String currentUserID = mAuth.getCurrentUser().getUid();
                                String deviceToken = FirebaseInstanceId.getInstance().getToken();

                                usersRef.child(currentUserID).child("device_token")
                                        .setValue(deviceToken)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){


                                                    takeUserToMainActivity();
                                                    Toast.makeText(LoginActivity.this,
                                                            "Signed in successfully", Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();

                                                }
                                            }
                                        });

                            }else{
                                String message = task.getException().toString();
                                Toast.makeText(LoginActivity.this, "Error : " + message,
                                        Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    }
            );
        }
    }

    private void InitializeFields() {
        loginButton = (Button) findViewById(R.id.login_button);
        phoneLoginButton = (TextView) findViewById(R.id.phone_login);
        userEmail = findViewById(R.id.login_email);
        userPassword = findViewById(R.id.login_password);
        needNewAcctLink = findViewById(R.id.already_have_acct);
        forgotPasswordLink = findViewById(R.id.forget_password_link);
        loadingBar = new ProgressDialog(this);
    }


/*   @Override
//    protected void onStart() {
//        super.onStart();
//
//        if(currentUser != null){
//            sendUserToMainActivity();
//        }
    }*/

    private void takeUserToMainActivity() {
        Intent intent = new Intent(LoginActivity.this,
                MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
