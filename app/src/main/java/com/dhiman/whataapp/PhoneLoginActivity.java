package com.dhiman.whataapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
    private Button sendVerificationCodeButton,verifyButton;
    private EditText InputPhoneNumber,InputVerificationCode;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private FirebaseAuth mAuth;

    private ProgressDialog loadingBar;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);
        sendVerificationCodeButton=findViewById(R.id.send_ver_code_button);
        verifyButton=findViewById(R.id.verify_button);
        InputPhoneNumber=findViewById(R.id.phone_number_login);
        InputVerificationCode=findViewById(R.id.verification_code_input);
        loadingBar=new ProgressDialog(this);

        mAuth=FirebaseAuth.getInstance();

        sendVerificationCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String PhoneNumber=InputPhoneNumber.getText().toString();

                if(TextUtils.isEmpty(PhoneNumber))
                {
                    Toast.makeText(PhoneLoginActivity.this, "Please Enter Phone Number", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    PhoneNumber="+91"+PhoneNumber;
                    loadingBar.setTitle("Phone Verification");
                    loadingBar.setMessage("Please while we are Authentication your Phone Number");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            PhoneNumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            PhoneLoginActivity.this,               // Activity (for callback binding)
                            callbacks);        // OnVerificationStateChangedCallbacks
                }
            }
        });

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String verificationCode=InputVerificationCode.getText().toString();

                if(TextUtils.isEmpty(verificationCode))
                {
                    Toast.makeText(PhoneLoginActivity.this, "Please Enter code", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    loadingBar.setTitle(" Verifing Code ");
                    loadingBar.setMessage("just a Second..");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

        callbacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(PhoneLoginActivity.this, "Invalid Phone Number ,\nplease enter number withcountry code", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {

                mVerificationId = verificationId;
                mResendToken = token;

                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Code Sent", Toast.LENGTH_SHORT).show();
                sendVerificationCodeButton.setVisibility(View.INVISIBLE);
                verifyButton.setVisibility(View.VISIBLE);
                InputVerificationCode.setVisibility(View.VISIBLE);

                // ...
            }
        };
    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            loadingBar.dismiss();
                            Toast.makeText(PhoneLoginActivity.this, "Login complete", Toast.LENGTH_SHORT).show();
                            sendUserToMainActivity();
                        }
                        else
                        {
                            loadingBar.dismiss();
                            String msg=task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this, "Error:  "+msg, Toast.LENGTH_SHORT).show();
                        }


                    }
                });
    }
   private void  sendUserToMainActivity(){
       Intent mainIntent=new Intent(PhoneLoginActivity.this,MainActivity.class);
       startActivity(mainIntent);

    }

}
