package com.example.bulletinboard;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.bulletinboard.adapter.DataSend;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class DBManager {
    private Query query;
    private List<NewPost> newPosts;
    private DataSend dataSend;
    private FirebaseDatabase database;
    private FirebaseStorage firebaseStorage;
    private FirebaseAuth auth;
    private Context context;
    private int adsCount=0;
    private String[] myAds_cat={"Машины","Компьютеры","Телефоны","Бытовая техника"};
    private int DeleteItemCount=0;
    public DBManager(DataSend dataSend,Context context) {

        this.dataSend = dataSend;
        newPosts=new ArrayList<>();
        firebaseStorage=FirebaseStorage.getInstance();
        database=FirebaseDatabase.getInstance();
        this.context=context;
        auth=FirebaseAuth.getInstance();
    }

    public void getDataFromDB(String path)
    {
           if(auth.getUid()!=null)
           {
               DatabaseReference databaseReference=database.getReference(path);
               query=databaseReference.orderByChild("Ads/time");
               readDataUpdate();
           }
           else
           {
               Toast.makeText(context, "Войдите в аккаунт для того ,чтобы видеть свои объявления.", Toast.LENGTH_SHORT).show();
               newPosts.clear();
               dataSend.DataReceiver(newPosts);
           }
    }
    public void readDataUpdate()
    {
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(newPosts.size()>0)
                    newPosts.clear();
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    NewPost newPost=ds.child("Ads").getValue(NewPost.class);
                    /*NewPost newPost=ds.getChildren().iterator().next().child("Ads").getValue(NewPost.class);
                    StatusItem statusItem=ds.child("status").getValue(StatusItem.class);
                    if(newPost!=null && statusItem!=null)
                    {
                        newPost.setTotalViews(statusItem.total_views);
                    }*/
                    newPosts.add(newPost);
                }
                dataSend.DataReceiver(newPosts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void getMyDataFromDB(String uid)
    {
        if(auth.getUid()!=null)
        {
            if(newPosts.size()>0 )
                newPosts.clear();
            DatabaseReference databaseReference=database.getReference(myAds_cat[adsCount]);
            query=databaseReference.orderByChild("Ads/ui").equalTo(uid);
            readMyAdsDataUpdate(uid);
            adsCount++;

        }
        else
        {
            Toast.makeText(context, "Войдите в аккаунт для того ,чтобы видеть свои объявления.", Toast.LENGTH_SHORT).show();
            newPosts.clear();
            dataSend.DataReceiver(newPosts);
        }
    }
    public void readMyAdsDataUpdate(String uid)
    {
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    //NewPost newPost=ds.child(auth.getUid()+"/Ads").getValue(NewPost.class);
                    NewPost newPost=ds.child("Ads").getValue(NewPost.class);
                    /*StatusItem statusItem=ds.child("status").getValue(StatusItem.class);
                    if(newPost!=null && statusItem!=null)
                    {
                        newPost.setTotalViews(statusItem.total_views);
                    }*/
                    newPosts.add(newPost);
                }
                if(adsCount>3)
                {
                    dataSend.DataReceiver(newPosts);
                    newPosts.clear();
                    adsCount=0;
                }
                else
                {
                    DatabaseReference databaseReference=database.getReference(myAds_cat[adsCount]);
                    query=databaseReference.orderByChild("Ads/ui").equalTo(uid);
                    readMyAdsDataUpdate(uid);
                    adsCount++;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void ShowViews(NewPost newPost)
    {
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference(newPost.getCategory());
        int total_views;
        try
        {
            total_views=Integer.parseInt(newPost.getTotalViews());
        }
        catch (NumberFormatException e)
        {
            total_views=0;
        }
        total_views++;
        /*StatusItem statusItem=new StatusItem();
        statusItem.total_views=String.valueOf(total_views);
        databaseReference.child(newPost.getKey()).child("status").setValue(statusItem);*/
        databaseReference.child(newPost.getKey()).child("Ads/totalViews").setValue(String.valueOf(total_views));
    }
    public void DeleteItem(NewPost newPost)
    {
        StorageReference storageReference = null;
        if(DeleteItemCount==0)
        {
            if(newPost.getImageId().equals("empty"))
            {
                DeleteItemCount++;
                DeleteItem(newPost);
            }
            else
            {
                storageReference=firebaseStorage.getReferenceFromUrl(newPost.getImageId());
            }
        }
        if(DeleteItemCount==1)
        {
            if(newPost.getImageId2().equals("empty"))
            {
                DeleteItemCount++;
                DeleteItem(newPost);
            }
            else {
                storageReference=firebaseStorage.getReferenceFromUrl(newPost.getImageId2());
            }
        }
        if(DeleteItemCount==2)
        {
            if(newPost.getImageId3().equals("empty"))
            {
                DeleteItemCount++;
                DeleteItem(newPost);
            }
            else
            {
                storageReference=firebaseStorage.getReferenceFromUrl(newPost.getImageId3());
            }
        }
        if(DeleteItemCount==3)
        {
            DeleteItemCount=0;
            DeleteDBItem(newPost);
        }
        if(storageReference== null)
            return;
        storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                DeleteItemCount++;
                if(DeleteItemCount==3)
                {
                    DeleteItemCount=0;
                    DeleteDBItem(newPost);
                }
                else
                {
                    DeleteItem(newPost);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Не удалось удалить картинку.", Toast.LENGTH_SHORT).show();
            }
        });


    }
    private void DeleteDBItem(NewPost newPost)
    {
        DatabaseReference databaseReference=database.getReference(newPost.getCategory());
        databaseReference.child(newPost.getKey()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, "Удаление объявления прошло успешно.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Что-то пошло не так.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
