package com.dhiman.whataapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ToolbarWidgetWrapper;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;



import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    private Button UpdateAccountSettings;
    private EditText Username,UserStatus;
    private CircleImageView UserProfileImage;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private DatabaseReference RootRef;
    private StorageReference UserProfileImageRef;
    private ProgressDialog loadingBar;
    private static final int GalleryPick=1;
    private Toolbar SettingsToolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mAuth=FirebaseAuth.getInstance();
        try {
            currentUserID=mAuth.getCurrentUser().getUid();
        }catch (Exception e){}
        RootRef= FirebaseDatabase.getInstance().getReference();
        UserProfileImageRef= FirebaseStorage.getInstance().getReference().child("Profile_Images");

        requestStoragePermission();
        InitializeFields();
        UserProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                requestStoragePermission();
                Intent galleryIntent=new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GalleryPick);
            }
        });

        
        UpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateSettings();
            }
        });

        RetriveUserInfo();
        
    }

    private void requestStoragePermission() {
        if((ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)&&
        ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GalleryPick&&resultCode==RESULT_OK&&data!=null)
        {
            Uri ImageUri=data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode==RESULT_OK)
            {
                loadingBar.setTitle("Updating profile Photo");
                loadingBar.setMessage("Please Wait");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri resultUri=result.getUri();
                StorageReference filePath=UserProfileImageRef.child(currentUserID+".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(SettingsActivity.this, "Profile Image Stored Upload complete", Toast.LENGTH_SHORT).show();

                            final String downloadUrl =task.getResult().getDownloadUrl().toString();
                           // downloadUrlToUpdate=downloadUrl;


                            RootRef.child("Users").child(currentUserID).child("image")
                                    .setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){

                                        Toast.makeText(SettingsActivity.this, "Image Saved In Database Complete", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                    else
                                    {
                                        String msg= task.getException().toString();
                                        Toast.makeText(SettingsActivity.this, "Error: "+msg, Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }

                                }
                            });
                        }
                        else
                        {
                            String msg=task.getException().toString();
                            Toast.makeText(SettingsActivity.this, "Error:  "+msg, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
            }

        }
        RetriveUserInfo();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.advanced_options,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId()==R.id.advanced_setting_options)
        {
            Toast.makeText(this, "Will be working on it", Toast.LENGTH_SHORT).show();

        }
        return true;
    }

    private void InitializeFields() {
        UpdateAccountSettings=findViewById(R.id.update_setting_button);
        Username=findViewById(R.id.set_user_name);
        UserStatus=findViewById(R.id.set_profile_status);
        UserProfileImage=findViewById(R.id.set_profile_image);
        loadingBar=new ProgressDialog(this);
        SettingsToolbar=findViewById(R.id.setting_toolbar);
        setSupportActionBar(SettingsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Settings");

    }

    private void UpdateSettings() {
        String setUserName=Username.getText().toString().trim();
        String setStatus=UserStatus.getText().toString().trim();

        if(TextUtils.isEmpty(setUserName)){
            Toast.makeText(this, "Please write username", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(setStatus))
        {
            Toast.makeText(this, "Status cannot be empty", Toast.LENGTH_SHORT).show();
        }
        else
        {
            HashMap<String,Object> profileMap=new HashMap<>();
            profileMap.put("uid",currentUserID);
            profileMap.put("name",setUserName);
            profileMap.put("status",setStatus);
           //   profileMap.put("image",downloadUrlToUpdate);

            RootRef.child("Users").child(currentUserID).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                sendUserToMainActivity();
                                Toast.makeText(SettingsActivity.this, "profile updated Successfully", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                String msg=task.getException().toString();
                                Toast.makeText(SettingsActivity.this, "error   :   ", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
        }
    }


    private void sendUserToMainActivity()
    {
        Intent mainIntent=new Intent(SettingsActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent );
        finish();
    }

    private void RetriveUserInfo() {

        RootRef.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if((dataSnapshot.exists())&&(dataSnapshot.hasChild("name"))&&
                        dataSnapshot.hasChild("image"))
                        {
                            String retrieveUserName=dataSnapshot.child("name").getValue().toString();
                            String retrieveStatus=dataSnapshot.child("status").getValue().toString();
                            String retrieveProfileImage=dataSnapshot.child("image").getValue().toString();

                            Username.setText(retrieveUserName);
                            UserStatus.setText(retrieveStatus);

                            Picasso.get().load(retrieveProfileImage).placeholder(R.drawable.profile_image).into(UserProfileImage);


                        }
                        else if((dataSnapshot.exists())&&(dataSnapshot.hasChild("name")))
                        {
                            String retrieveUserName=dataSnapshot.child("name").getValue().toString();
                            String retrieveStatus=dataSnapshot.child("status").getValue().toString();

                            Username.setText(retrieveUserName);
                            UserStatus.setText(retrieveStatus);

                        }
                        else if ((dataSnapshot.exists())&&dataSnapshot.hasChild("image")){
                            String retrieveProfileImage=dataSnapshot.child("image").getValue().toString();
                            Picasso.get().load(retrieveProfileImage).into(UserProfileImage);
                        }
                        else
                        {
                            Toast.makeText(SettingsActivity.this, "please update settings info", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

}
