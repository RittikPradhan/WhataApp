package com.dhiman.whataapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdaptor extends RecyclerView.Adapter<MessageAdaptor.MessageViewHolder> {

    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    public MessageAdaptor (List<Messages> userMessagesList)
    {
        this.userMessagesList=userMessagesList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_messages_layout,parent,false);
        mAuth=FirebaseAuth.getInstance();
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position) {
        String messageSenderId=mAuth.getCurrentUser().getUid();
        Messages messages =userMessagesList.get(position);
        String fromUserID=messages.getFrom();
        String fromMessageType=messages.getType();

        userRef= FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("image"))
                {
                    String receiverImage=dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(holder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        holder.receiverMessageText.setVisibility(View.GONE);
        holder.receiverProfileImage.setVisibility(View.GONE);
        holder.senderMessageText.setVisibility(View.GONE);
        holder.messagesReceiverPicture.setVisibility(View.GONE);
        holder.messagesSenderPicture.setVisibility(View.GONE);

        holder.receiverMessageTextTime.setVisibility(View.GONE);
        holder.senderMessageTextTime.setVisibility(View.GONE);

        holder.senderMessageTextLL.setVisibility(View.GONE);
        holder.receiverMessageTextLL.setVisibility(View.GONE);


        if (fromMessageType.equals("text"))
        {



            if (fromUserID.equals(messageSenderId))
            {
                holder.senderMessageText.setVisibility(View.VISIBLE);
                holder.senderMessageTextTime.setVisibility(View.VISIBLE);
                holder.senderMessageTextLL.setVisibility(View.VISIBLE);




                holder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                holder.senderMessageText.setText(messages.getMessage());
                holder.senderMessageTextTime.setText(messages.getTime()+" - "+messages.getDate());


            }
            else
            {

                holder.receiverMessageText.setVisibility(View.VISIBLE);
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.receiverMessageTextTime.setVisibility(View.VISIBLE);
                holder.receiverMessageTextLL.setVisibility(View.VISIBLE);



                holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_message_layout);
                holder.receiverMessageText.setText(messages.getMessage());
                holder.receiverMessageTextTime.setText(messages.getTime()+" - "+messages.getDate());


            }
        }
        else if(fromMessageType.equals("image"))
        {
            if (fromUserID.equals(messageSenderId))
            {
                holder.messagesSenderPicture.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.messagesSenderPicture);
            }
            else
            {
                holder.receiverProfileImage.setVisibility(View.VISIBLE);

                holder.messagesReceiverPicture.setVisibility(View.VISIBLE);

                Picasso.get().load(messages.getMessage()).into(holder.messagesReceiverPicture);

            }
        }
        else if(fromMessageType.equals("pdf")||fromMessageType.equals("docx "))
        {
            if (fromUserID.equals(messageSenderId))
            {
                holder.messagesSenderPicture.setVisibility(View.VISIBLE);

                Picasso.get()
                        .load("https://firebasestorage.googleapis.com/v0/b/whataapp-e0c71.appspot.com/o/Image%20Files%2Ffile.png?alt=media&token=2e98cd2b-0044-4a13-974a-b640f8f87237")
                        .into(holder.messagesSenderPicture  );


            }
            else
            {
                holder.receiverProfileImage.setVisibility(View.VISIBLE);

                holder.messagesReceiverPicture.setVisibility(View.VISIBLE);
                Picasso.get()
                        .load("https://firebasestorage.googleapis.com/v0/b/whataapp-e0c71.appspot.com/o/Image%20Files%2Ffile.png?alt=media&token=2e98cd2b-0044-4a13-974a-b640f8f87237")
                        .into(holder.messagesReceiverPicture  );



            }
        }

        if (fromUserID.equals(messageSenderId))
        {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (userMessagesList.get(position).getType().equals("pdf")||userMessagesList.get(position).getType().equals("docx"))
                    {
                        CharSequence options[]=new CharSequence[]
                                {
                                        "Delete For Me",
                                        "Download and View ",
                                        "Cancel",
                                        "Delete for EveryOne"
                                };

                        AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete message ? ");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i==0)
                                {
                                    deteteSentMessages(position,holder);
                                }
                                else  if (i==1)
                                {
                                    Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                else  if (i==3)
                                {
                                    deteteMessagesForEveryone(position,holder);
                                }
                            }
                        });
                        builder.show();
                    }
                     else if (userMessagesList.get(position).getType().equals("text"))
                    {
                        CharSequence options[]=new CharSequence[]
                                {
                                        "Delete For Me",
                                        "Cancel",
                                        "Delete for EveryOne"
                                };

                        AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete messages");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i==0)
                                {
                                    deteteSentMessages(position,holder);

                                }

                                else  if (i==2)
                                {
                                    deteteMessagesForEveryone(position,holder);

                                }

                            }
                        });
                        builder.show();
                    }


                   else  if (userMessagesList.get(position).getType().equals("image"))
                    {
                        CharSequence options[]=new CharSequence[]
                                {
                                        "Delete For Me",
                                        "View it ",
                                        "Cancel",
                                        "Delete for EveryOne"
                                };

                        AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete messages");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i==0)
                                {
                                    deteteSentMessages(position,holder);
                                }
                                else  if (i==1)
                                {

                                    Intent intent=new Intent(holder.itemView.getContext(),ImageViewerActivity.class);
                                    intent.putExtra("url",userMessagesList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);
                                }


                                else  if (i==3)
                                {
                                    deteteMessagesForEveryone(position,holder);

                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        }
        else
        {

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (userMessagesList.get(position).getType().equals("pdf")||userMessagesList.get(position).getType().equals("pdf"))
                        {
                            CharSequence options[]=new CharSequence[]
                                    {
                                            "Delete For Me",
                                            "Download and View ",
                                            "Cancel",
                                    };

                            AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                            builder.setTitle("Delete messages");
                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (i==0)
                                    {
                                        deteteReceivedMessages(position,holder);

                                    }
                                    else  if (i==1)
                                    {
                                        Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                        holder.itemView.getContext().startActivity(intent);
                                    }
                                }
                            });
                            builder.show();
                        }
                        else if (userMessagesList.get(position).getType().equals("text"))
                        {
                            CharSequence options[]=new CharSequence[]
                                    {
                                            "Delete For Me",
                                            "Cancel",

                                    };

                            AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                            builder.setTitle("Delete messages");
                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (i==0)
                                    {
                                        deteteReceivedMessages(position,holder);
                                        Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class);
                                        holder.itemView.getContext().startActivity(intent);

                                    }



                                }
                            });
                            builder.show();
                        }


                       else  if (userMessagesList.get(position).getType().equals("image"))
                        {
                            CharSequence options[]=new CharSequence[]
                                    {
                                            "Delete For Me",
                                            "View it ",
                                            "Cancel",

                                    };

                            AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                            builder.setTitle("Delete messages");
                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (i==0)
                                    {
                                        deteteReceivedMessages(position,holder);

                                        Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class);
                                        holder.itemView.getContext().startActivity(intent);

                                    }
                                    else  if (i==1)
                                    {
                                        Intent intent=new Intent(holder.itemView.getContext(),ImageViewerActivity.class);
                                        intent.putExtra("url",userMessagesList.get(position).getMessage());
                                        holder.itemView.getContext().startActivity(intent);
                                    }


                                }
                            });
                            builder.show();
                        }
                    }
                });

        }


    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    private void deteteSentMessages(final int position ,final MessageViewHolder holder)
    {
        Toast.makeText(holder.itemView.getContext(), ""+position, Toast.LENGTH_SHORT).show();
        DatabaseReference rootRef=FirebaseDatabase.getInstance().getReference();
        rootRef.child("Message")
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    Toast.makeText(holder.itemView.getContext(), "Message Deleted", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(), "Some Error Occured", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    private void deteteReceivedMessages(final int position ,final MessageViewHolder holder)
    {
        DatabaseReference rootRef=FirebaseDatabase.getInstance().getReference();
        rootRef.child("Message")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    Toast.makeText(holder.itemView.getContext(), "Message Deleted", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(), "Some Error Occured", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    private void deteteMessagesForEveryone(final int position ,final MessageViewHolder holder)
    {
        final DatabaseReference rootRef=FirebaseDatabase.getInstance().getReference();
        rootRef.child("Message")
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    rootRef.child("Message")
                            .child(userMessagesList.get(position).getTo())
                            .child(userMessagesList.get(position).getFrom() )
                            .child(userMessagesList.get(position).getMessageID())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                            {
                                Toast.makeText(holder.itemView.getContext(), "Message Deleted", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                Toast.makeText(holder.itemView.getContext(), "Some Error Occured", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

            }
        });

    }


    public class MessageViewHolder extends  RecyclerView.ViewHolder
    {
        public TextView senderMessageText,receiverMessageText,receiverMessageTextTime,senderMessageTextTime;
        public CircleImageView receiverProfileImage;
        public ImageView messagesSenderPicture,messagesReceiverPicture;
        LinearLayout senderMessageTextLL,receiverMessageTextLL;

        public MessageViewHolder(@NonNull View itemView)
        {
            super(itemView);

            senderMessageText=itemView.findViewById(R.id.sender_message_text);
            receiverMessageText=itemView.findViewById(R.id.receiver_message_text);
            senderMessageTextTime=itemView.findViewById(R.id.sender_message_text_time);
            receiverMessageTextTime=itemView.findViewById(R.id.receiver_message_text_time);

            senderMessageTextLL=itemView.findViewById(R.id.LL_of_sender_message_text);
            receiverMessageTextLL=itemView.findViewById(R.id.LL_of_receiver_message_text);


            receiverProfileImage=itemView.findViewById(R.id.message_profile_image);
            messagesSenderPicture=itemView.findViewById(R.id.message_sender_image_view);
            messagesReceiverPicture=itemView.findViewById(R.id.message_receiver_image_view);
        }
    }
}
