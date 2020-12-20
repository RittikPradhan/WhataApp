package com.dhiman.whataapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
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

    private Button LoginButton,PhoneLoginButton;
    private EditText UserEmail,UserPassword;
    private TextView NeedNewAccountLink,ForgetPasswordLink;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private DatabaseReference UsersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        InitializeFields();
        mAuth=FirebaseAuth.getInstance();
        UsersRef=FirebaseDatabase.getInstance().getReference().child("Users");

        NeedNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToRegisterActivity();
            }
        });

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AllowUserToLogin();
            }
        });

        PhoneLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent PhoneLoginIntent=new Intent(LoginActivity.this,PhoneLoginActivity.class);
                startActivity(PhoneLoginIntent);
            }
        });
    }

    private void AllowUserToLogin() {
        String email=UserEmail.getText().toString();
        String password=UserPassword.getText().toString();
        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(this, "please enter email", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password))
        {
            Toast.makeText(this, "please enter password", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("signing Account");
            loadingBar.setMessage("please wait while processing");;
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
            mAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                String currentUserID=mAuth.getCurrentUser().getUid();
                                String deviceToken= FirebaseInstanceId.getInstance().getToken();

                                UsersRef.child(currentUserID).child("device_token").setValue(deviceToken)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful())
                                        {
                                            sendUserToMainActivity();
                                            Toast.makeText(LoginActivity.this, "Loggin in success", Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        }
                                    }
                                });




                            }
                            else
                            {
                                String msg =task.getException().toString();
                                Toast.makeText(LoginActivity.this, "error :  "+msg, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }

    }

    private void InitializeFields() {
        LoginButton=findViewById(R.id.login_button);
        PhoneLoginButton=findViewById(R.id.phone_login_button);
        UserEmail=findViewById(R.id.login_email);
        UserPassword=findViewById(R.id.login_password);
        NeedNewAccountLink=findViewById(R.id.need_new_account_link);
        ForgetPasswordLink=findViewById(R.id.forget_password_link);
        loadingBar=new ProgressDialog(this);
    }



    private void sendUserToMainActivity()
    {
        Intent mainIntent=new Intent(LoginActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent );
        finish();
    }
    private void sendUserToRegisterActivity() {

        Intent loginIntent=new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(loginIntent );
    }


}
