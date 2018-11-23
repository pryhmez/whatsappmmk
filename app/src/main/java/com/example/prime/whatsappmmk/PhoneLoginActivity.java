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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private Button sendVerificationCode, verifyBtn;
    private EditText inputPhoneNumber, inputVerificationCode;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        FirebaseAuth.getInstance();

        sendVerificationCode = (Button)findViewById(R.id.sed_ver_code_btn);
        verifyBtn = (Button)findViewById(R.id.verify_btn);
        inputPhoneNumber = (EditText)findViewById(R.id.phone_number_input);
        inputVerificationCode = (EditText)findViewById(R.id.verification_code_input);

        loadingBar = new ProgressDialog(this);

        sendVerificationCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String phoneNumber = inputPhoneNumber.getText().toString().trim();

                if (TextUtils.isEmpty(phoneNumber)){
                    verifyBtn.setError("phone number required");
                }else{
                        sendVCode();
                }
            }
        });

        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerificationCode.setVisibility(View.INVISIBLE);
                inputPhoneNumber.setVisibility(View.INVISIBLE);

                String verificationCode = inputVerificationCode.getText().toString().trim();

                if (TextUtils.isEmpty(verificationCode)){

                    Toast.makeText(PhoneLoginActivity.this, "pls write quickly", Toast.LENGTH_SHORT).show();
                }else{

                    loadingBar.setTitle("code verification");
                    loadingBar.setMessage("pls wait while we verify your phone");
                    loadingBar.show();
                    loadingBar.setCanceledOnTouchOutside(false);

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(
                            mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                loadingBar.dismiss();
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(PhoneLoginActivity.this,
                        "Invalid Phone Number, please enter correct phone number with country code",
                        Toast.LENGTH_SHORT).show();

                sendVerificationCode.setVisibility(View.VISIBLE);
                inputPhoneNumber.setVisibility(View.VISIBLE);

                verifyBtn.setVisibility(View.INVISIBLE);
                inputVerificationCode.setVisibility(View.INVISIBLE);
            }
            @Override
            //this is what happens when the code is sent
            //when the code is sent it returns a string which is the id
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                super.onCodeSent(verificationId, token);
                //the verification id is saved as a string mverification id
                mVerificationId = verificationId;
                //and tells the user that code has been sent
                mResendToken = token;
                Toast.makeText(PhoneLoginActivity.this, "verification code sent", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();

                sendVerificationCode.setVisibility(View.INVISIBLE);
                inputPhoneNumber.setVisibility(View.INVISIBLE);

                verifyBtn.setVisibility(View.VISIBLE);
                inputVerificationCode.setVisibility(View.VISIBLE);
            }
        };
    }

    private void sendVCode() {
        loadingBar.setTitle("phone verication");
        loadingBar.setMessage("pls wait while we authenticate your phone");
        loadingBar.show();
        loadingBar.setCanceledOnTouchOutside(false);

        PhoneAuthProvider.getInstance().verifyPhoneNumber(/*mcallbacks is an instance of a
        firebase class that shows what happens after the code is sent*/
                inputPhoneNumber.getText().toString(), 60, TimeUnit.SECONDS, this, mCallbacks) ;
    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential phoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential).addOnCompleteListener
                (this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            loadingBar.dismiss();
                            Toast.makeText(PhoneLoginActivity.this,
                                    "you are logged in successfully", Toast.LENGTH_SHORT).show();
                            sendUserToMainActivity();
//                    userIsLoggedIn();
                            FirebaseUser user = task.getResult().getUser();
                        }else{
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException)
                            {
                                String message = task.getException().toString();
                                Toast.makeText(
                                        PhoneLoginActivity.this, "invalid code entered or " + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void sendUserToMainActivity() {
        Intent mainintent = new Intent(PhoneLoginActivity.this, MainActivity.class);
        startActivity(mainintent);
        finish();
    }
}
