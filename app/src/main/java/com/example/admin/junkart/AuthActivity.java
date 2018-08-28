package com.example.admin.junkart;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class AuthActivity extends AppCompatActivity {
    private static final String TAG = "PhoneAuth";
    private Button send,resend,verify;
    private FirebaseAuth mAuth;
    private EditText editText1,editText2;
    private String phoneverificationid;

    private PhoneAuthProvider.ForceResendingToken resendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks verificationCallbacks;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        mAuth = FirebaseAuth.getInstance();
        send = (Button)findViewById(R.id.button);
        resend = (Button)findViewById(R.id.button2);
        verify = (Button)findViewById(R.id.button3);
        editText1 = (EditText)findViewById(R.id.editText);
        editText2 = (EditText)findViewById(R.id.editText2);
        setUpVerificationCallbacks();
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phonenumber = editText1.getText().toString();
                sendCode(phonenumber);
            }
        });
        resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phonenumber = editText1.getText().toString();
                resendVerificationCode(phonenumber);
            }
        });
        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String codetext = editText2.getText().toString();
                verifyCode(phoneverificationid,codetext);
            }
        });
    }

    private void verifyCode( String phoneverificationid,String codetext) {
        String codetexts = editText2.getText().toString();
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(phoneverificationid,codetext);
        signInWithPhoneAuthCredential(credential);
    }

    private void sendCode(String phonenumber){
        String phonenumbers = editText1.getText().toString();
        setUpVerificationCallbacks();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phonenumber,
          60,
                TimeUnit.SECONDS,
                this,
                verificationCallbacks);
    }

    private void setUpVerificationCallbacks() {
        verificationCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                resend.setEnabled(false);
                verify.setEnabled(false);
                String code = phoneAuthCredential.getSmsCode();
                if (code!=null){
                    verifyCode(phoneverificationid,code);
                }
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
               if (e instanceof FirebaseAuthInvalidCredentialsException){
                   Log.d(TAG,"Invalid Credential:"+e.getLocalizedMessage());
               }else if (e instanceof FirebaseTooManyRequestsException){
                   Log.d(TAG,"SMS Quota exceeded");
               }
            }
            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                //Log.d(TAG, "onCodeSent:" + verificationId);
                verify.setEnabled(true);
                send.setEnabled(false);
                resend.setEnabled(true);
                super.onCodeSent(verificationId,token);
                // Save verification ID and resending token so we can use them later
                phoneverificationid = verificationId;
                resendToken = token;

                // ...
            }

        };
    }
  /*  public void verifyCode(View view){
        String codetext = editText2.getText().toString();
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(phoneverificationid,codetext);
        signInWithPhoneAuthCredential(credential);
    }*/

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            resend.setEnabled(false);
                            verify.setEnabled(false);
                            Intent mainintent = new Intent(AuthActivity.this,MainActivity.class);
                            startActivity(mainintent);
                            finish();
                            FirebaseUser user = task.getResult().getUser();
                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }
    public void resendVerificationCode(String phonenumber){
        String phonenumbers = editText1.getText().toString();
        setUpVerificationCallbacks();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phonenumber,
                60,
                TimeUnit.SECONDS,
                this,
                verificationCallbacks,
                resendToken
        );
    }
}
