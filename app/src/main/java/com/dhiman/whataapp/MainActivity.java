package com.dhiman.whataapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import es.dmoral.toasty.Toasty;


public class MainActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private ImageView updateIcon;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsAccessorAdaptor myTabsAccessorAdaptor;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private String currentUserID;
    FirebaseUser currentUser;
    private AdView mAdViewMain;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth= FirebaseAuth.getInstance();
        RootRef= FirebaseDatabase.getInstance().getReference();


        mToolbar=findViewById(R.id.main_page_toolbar);
        updateIcon=findViewById(R.id.update_application_button);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("WHATaAPP ");
        myViewPager=findViewById(R.id.main_tabs_pages);
        myTabsAccessorAdaptor=new TabsAccessorAdaptor(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsAccessorAdaptor);

        myTabLayout  =findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);
        mAdViewMain = findViewById(R.id.adView_main);
        MobileAds.initialize(getApplicationContext(),"ca-app-pub-9766707763597997~2741861953");
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdViewMain.loadAd(adRequest);


        updateIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateApplication();
            }
        });

    }

    private void updateApplication() {
        final AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Update Now ?");
        builder.setMessage("This may take a while");
        builder.setCancelable(true);
        builder.setIcon(R.drawable.update_icon2);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                String apkUrl;
                Toasty.success(MainActivity.this, "Will be Updating soon", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser=mAuth.getCurrentUser();




        try { currentUserID=mAuth.getCurrentUser().getUid(); } catch (Exception e){}

        //Toast.makeText(this, ""+currentUser+"\n"+currentUserID, Toast.LENGTH_SHORT).show();
        // System.out.println("wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww"+currentUser+"\n"+currentUserID);

        if(currentUser==null)
        {
            sendUserToLoginActivity();
        }
        else
        {
            currentUserID=mAuth.getCurrentUser().getUid();

            VerifyUserExistense();


        }
    }








    @Override
    protected void onDestroy() {
        super.onDestroy();

        FirebaseUser currentUser=mAuth.getCurrentUser();

        if (currentUser!=null)
        {
            updateUserStatus("offline");
        }
    }

    private void VerifyUserExistense() {
        String CurrentUserID=mAuth.getCurrentUser().getUid();
        RootRef.child("Users").child(CurrentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.child("name").exists()))
                {
                    //Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                    checkForUpdate();
                }
                else
                {
                    sendUserToSettingsActivity();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkForUpdate() {
        try {
            PackageInfo packageInfo=getPackageManager().getPackageInfo(getPackageName(),0);
            final String  versionName = packageInfo.versionName;
            // final int  versionCodeInt=Integer.parseInt(versionName);
            final int versionCode = packageInfo.versionCode;
            //Toast.makeText(this, ""+" "+versionName, Toast.LENGTH_SHORT).show();


            final String CurrentUserID=mAuth.getCurrentUser().getUid();
            RootRef.child("Users").child(CurrentUserID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if((dataSnapshot.child("version").exists()))
                    {
                        RootRef.child("latestVersion").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists())
                                {
                                    int latestVersion=Integer.parseInt(dataSnapshot.getValue().toString());
                                    if (latestVersion>versionCode)
                                    {
                                        updateIcon.setVisibility(View.VISIBLE);
                                    }
                                    else if (latestVersion==versionCode)
                                    {
                                        updateIcon.setVisibility(View.GONE);
                                    }

                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                    else if(false)
                    {
                        //here update the version for newely updated app
                    }
                    else
                    {
                        RootRef.child("Users").child(CurrentUserID).child("version").setValue(versionCode)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful())
                                        {
                                            try {
                                                Thread.sleep(5000);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            //Toast.makeText(MainActivity.this, "version updated", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void sendUserToLoginActivity()
    {
        Intent loginIntent=new Intent(MainActivity.this,LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent );
        finish();
    }
    private void sendUserToSettingsActivity()
    {
        Intent SettingsIntent=new Intent(MainActivity.this,SettingsActivity.class);
        //SettingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(SettingsIntent );
        // finish();
    }
    private void sendUserToFindFriendActivity()
    {
        Intent FindFriendIntent=new Intent(MainActivity.this,FindFriendsActivity.class);
        // SettingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(FindFriendIntent );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu,menu);
        return true;
    }

    @SuppressLint("ResourceType")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.main_logout_option)
        {
            updateUserStatus("offline");

            mAuth.signOut();
            sendUserToLoginActivity();
        }
        if(item.getItemId()==R.id.main_settings_option)
        {
            sendUserToSettingsActivity();
        }
        if(item.getItemId()==R.id.main_find_friends_option)
        {


            NotificationCompat.Builder nBuilder=new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.nortification_icon)
                    .setContentTitle("hello")
                    .setContentText("nice ji")
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.profile_image))
                    .setAutoCancel(true);


            NotificationManager notificationManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.O)
            {
                String channelID="YOUR_CHANNEL_ID";
                NotificationChannel notificationChannel=new NotificationChannel(channelID,"Channel human readable title"
                        ,NotificationManager.IMPORTANCE_DEFAULT);

                notificationManager.createNotificationChannel(notificationChannel);
                nBuilder.setChannelId(channelID);
            }
            notificationManager.notify(0,nBuilder.build());

            sendUserToFindFriendActivity();






        }
        if(item.getItemId()==R.id.main_create_group_option)
        {
            RequestNewGroup();
        }
        if(item.getItemId()==R.id.main_feedback_option)
        {
            Intent intent=new Intent(getApplicationContext(),FeedbackActivity.class);
            startActivity(intent);

        }
        return true;
    }

    private void RequestNewGroup() {

        AlertDialog.Builder builder= new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog);
        builder.setTitle("Enter Group Name");

        final EditText groupNameField=new EditText(MainActivity.this);
        groupNameField.setHint("E.g.  Tui Tui");
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String groupName=groupNameField.getText().toString();

                if(TextUtils.isEmpty(groupName))
                {
                    Toast.makeText(MainActivity.this, "Input Group Name", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    CreateNewGroup(groupName);
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

    private void CreateNewGroup(final String groupName) {
        RootRef.child("Groups").child(groupName).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(MainActivity.this, groupName+" group is Created", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    private void updateUserStatus(String state)
    {
        String saveCurrentTime,saveCurrentDate;
        Calendar calendar =Calendar.getInstance();
        SimpleDateFormat currentDate=new SimpleDateFormat("MMM dd,yyyy");
        saveCurrentDate=currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime=new SimpleDateFormat("hh:mm a");
        saveCurrentTime=currentTime.format(calendar.getTime());

        HashMap <String,Object> onlineStateMap=new HashMap<>();
        onlineStateMap.put("time",saveCurrentTime);
        onlineStateMap.put("date",saveCurrentDate);
        onlineStateMap.put("state",state);

        RootRef.child("Users").child(currentUserID).child("userState").updateChildren(onlineStateMap);
        //Toast.makeText(this, ""+state, Toast.LENGTH_SHORT).show();

    }
}
