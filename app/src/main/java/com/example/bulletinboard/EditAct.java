package com.example.bulletinboard;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.bulletinboard.adapter.ImageAdapter;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EditAct extends AppCompatActivity implements OnBitmapLoad {
    private StorageReference storageReference;
    private EditText ed_name,ed_tel,ed_desc,ed_price;
    private Spinner spinner;
    private FloatingActionButton fab;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private Boolean isEdit=false,isLoaded=false;
    private String temp_cat="";
    private String temp_key="";
    private String temp_ui="";
    private String temp_time="";
    private String temp_total_views="";
    private Boolean isImageUpdate=false;
    private ProgressDialog progressDialog;
    private Animation anim_load;
    private String[] UriArray = new String[3];
    private int load_count=0;
    private String[] uploadUri=new String[3];
    private String[] uploadNewUri=new String[3];
    private List<String> imageToShow;
    private ImageAdapter imageAdapter;
    private TextView image_counter;
    private ViewPager viewPager;
    private final int MAX_IMAGE_SIZE=1920;
    private List<Bitmap> bitmapList;
    private ImageManager imageManager;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_layout);
        init();
        GetIntent();
    }

    public void onClickImage(View view) {

        Intent i =new Intent(EditAct.this,ChooseImage.class);
        i.putExtra(Constans.IMAGEID,UriArray[0]);
        i.putExtra(Constans.IMAGEID2,UriArray[1]);
        i.putExtra(Constans.IMAGEID3,UriArray[2]);
        startActivityForResult(i,20);
        isImageUpdate=true;
        isLoaded=false;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK && data!=null)
        {
            if(requestCode==20)
            {

                imageToShow.clear();
                String[] pager_show=getUrisFromChoose(data);
                for (String s : pager_show) {
                    if (!s.equals("empty")) {
                        imageToShow.add(s);
                    }
                }
                imageAdapter.UpdateImages(imageToShow);
                isLoaded=false;
                imageManager.resizeMultiLargeImages(Arrays.asList(pager_show));
                String TextToShow = null;
                if(imageToShow.size()==0)
                {
                     TextToShow=0 + "/" + 0;
                }
                else
                {
                    TextToShow=viewPager.getCurrentItem() + 1 + "/" + imageToShow.size();
                }
                image_counter.setText(TextToShow);

            }
        }
    }
    private  void init()
    {
        imageToShow=new ArrayList<>();
        viewPager=findViewById(R.id.view_pager);
        imageAdapter=new ImageAdapter(this);
        viewPager.setAdapter(imageAdapter);
        image_counter=findViewById(R.id.image_counter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                String TextToShow=position + 1 + "/" + imageToShow.size();
                image_counter.setText(TextToShow);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        uploadUri[0]="empty";
        uploadUri[1]="empty";
        uploadUri[2]="empty";

        UriArray[0]="empty";
        UriArray[1]="empty";
        UriArray[2]="empty";
        spinner=findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter=ArrayAdapter.createFromResource(this,R.array.category_spinner,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        imageManager=new ImageManager(this,this);
        bitmapList=new ArrayList<>();
        mAuth=FirebaseAuth.getInstance();
        fab=findViewById(R.id.fab);
        ed_desc=findViewById(R.id.ed_desc);
        ed_name=findViewById(R.id.ed_name);
        ed_price=findViewById(R.id.ed_price);
        ed_tel=findViewById(R.id.ed_tel);
        storageReference= FirebaseStorage.getInstance().getReference(mAuth.getUid()+"Images");
        progressDialog=new ProgressDialog(this);
        anim_load=AnimationUtils.loadAnimation(getApplicationContext(),R.anim.load_animation);
    }
    private  void GetIntent()
    {
        Intent i =getIntent();
        if(i!=null)
        {
            if(i.getStringExtra(Constans.TITLE)!=null)
            {
                isEdit=true;
                //NewPost newPost=(NewPost)i.getSerializableExtra(Constans.NEW_POST_INTENT);

                ed_name.setText(i.getStringExtra(Constans.TITLE));
                ed_tel.setText(i.getStringExtra(Constans.PHONE));
                ed_price.setText(i.getStringExtra(Constans.PRICE));
                ed_desc.setText(i.getStringExtra(Constans.DESC));
                spinner.setVisibility(View.GONE);
                //Picasso.get().load(i.getStringExtra(Constans.IMAGEID)).into(imageView);
                UriArray[0]=i.getStringExtra(Constans.IMAGEID);
                UriArray[1]=i.getStringExtra(Constans.IMAGEID2);
                UriArray[2]=i.getStringExtra(Constans.IMAGEID3);
                for(int j=0;j<UriArray.length;j++)
                    uploadUri[j]=UriArray[j];

                if(imageToShow.size()>0)
                    imageToShow.clear();
                for(String s :UriArray)
                {
                    if(!s.equals("empty"))
                        imageToShow.add(s);
                }
                imageAdapter.UpdateImages(imageToShow);

                temp_cat=i.getStringExtra(Constans.CATEGORY);
                temp_key=i.getStringExtra(Constans.KEY);
                temp_ui=i.getStringExtra(Constans.UI);
                temp_time=i.getStringExtra(Constans.TIME);
                temp_total_views=i.getStringExtra(Constans.TOTAL_VIEWS);
                String TextToShow = null;
                if(imageToShow.size()==0)
                {
                    TextToShow=0 + "/" + 0;
                }
                else
                {
                    TextToShow=1+ "/" + imageToShow.size();
                }
                image_counter.setText(TextToShow);
                isLoaded=true;
            }
            else
            {
                isLoaded=true;
                isEdit=false;
            }
        }
    }
    private String[] getUrisFromChoose(Intent data)
    {
        if(isEdit)
        {
            uploadNewUri[0]=data.getStringExtra(Constans.URI_1);
            uploadNewUri[1]=data.getStringExtra(Constans.URI_2);
            uploadNewUri[2]=data.getStringExtra(Constans.URI_3);
            return uploadNewUri;
        }
        else
        {
            UriArray[0]=data.getStringExtra(Constans.URI_1);
            UriArray[1]=data.getStringExtra(Constans.URI_2);
            UriArray[2]=data.getStringExtra(Constans.URI_3);
            return UriArray;
        }

    }
    private void LoadAnim()
    {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        LayoutInflater layoutInflater=this.getLayoutInflater();
        View view=layoutInflater.inflate(R.layout.progress_dialog,null);
        builder.setView(view);


        ImageView img_load=view.findViewById(R.id.img_load);
        img_load.startAnimation(anim_load);

        AlertDialog dialog=builder.create();
        dialog.show();

    }
    /*private void uploadImage()
    {
        Bitmap bitmap=((BitmapDrawable)imageView.getDrawable()).getBitmap();
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,out);
        byte[] byteArray=out.toByteArray();
        final StorageReference mRef=storageReference.child(System.currentTimeMillis()+"_image");
        UploadTask load=mRef.putBytes(byteArray);
        Task<Uri> task=load.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                return mRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                uploadUri=task.getResult();
                SavePost();
                Toast.makeText(EditAct.this, "Загрузка прошла успешно.", Toast.LENGTH_SHORT).show();
                finish();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }*/

    private void uploadImage() {
        if (load_count < UriArray.length) {
            if (!UriArray[load_count].equals("empty")) {
                Bitmap bitmap = bitmapList.get(load_count);
                /*try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(UriArray[load_count]));
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, out);
                byte[] byteArray = out.toByteArray();
                final StorageReference mRef = storageReference.child(System.currentTimeMillis() + "_image");
                UploadTask load = mRef.putBytes(byteArray);
                Task<Uri> task = load.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        return mRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.getResult()== null)
                            return;
                        uploadUri[load_count] = task.getResult().toString();
                        load_count++;
                        if (load_count < UriArray.length) {
                            uploadImage();
                        } else {
                            SavePost();
                            Toast.makeText(EditAct.this, "Загрузка прошла успешно.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
            }
            else {
                load_count++;
                uploadImage();
            }
        }
        else
        {
            SavePost();
            Toast.makeText(EditAct.this, "Загрузка прошла успешно.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void onClickSave(View view) {
        if(isLoaded)
        {
            LoadAnim();
            if(isEdit)
            {
                if(isImageUpdate)
                {
                    uploadUpdateImage();
                }
                else
                {
                    UpdatePost();
                }
            }
            else
            {
                uploadImage();
            }
        }
        else {
            Toast.makeText(this, "Идёт загрузка картинок.Пожалуйста подождите...", Toast.LENGTH_SHORT).show();
        }
    }
    private void SavePost()
    {
        databaseReference= FirebaseDatabase.getInstance().getReference(spinner.getSelectedItem().toString());
        //mAuth=FirebaseAuth.getInstance();
        if(mAuth.getUid()!=null)
        {
            String key =databaseReference.push().getKey();
            NewPost post=new NewPost();
            post.setImageId(uploadUri[0]);
            post.setImageId2(uploadUri[1]);
            post.setImageId3(uploadUri[2]);
            post.setTitle(ed_name.getText().toString());
            post.setDesc(ed_desc.getText().toString());
            post.setPrice(ed_price.getText().toString());
            post.setTel(ed_tel.getText().toString());
            post.setUI(mAuth.getUid());
            post.setTime(String.valueOf(System.currentTimeMillis()));
            post.setKey(key);
            post.setTotalViews("0");
            post.setCategory(spinner.getSelectedItem().toString());
            assert key != null;
            databaseReference.child(key).child("Ads").setValue(post);
            Intent i =new Intent();
            i.putExtra("cat",spinner.getSelectedItem().toString());
            setResult(RESULT_OK,i);
        }
    }
    private void UpdatePost()
    {
        if(mAuth!=null)
        {
            databaseReference= FirebaseDatabase.getInstance().getReference(temp_cat);
            NewPost post=new NewPost();
            post.setImageId(uploadUri[0]);
            post.setImageId2(uploadUri[1]);
            post.setImageId3(uploadUri[2]);
            post.setTitle(ed_name.getText().toString());
            post.setDesc(ed_desc.getText().toString());
            post.setPrice(ed_price.getText().toString());
            post.setTel(ed_tel.getText().toString());
            post.setUI(temp_ui);
            post.setTime(temp_time);
            post.setKey(temp_key);
            post.setCategory(temp_cat);
            post.setTotalViews(temp_total_views);
            databaseReference.child(temp_key).child("Ads").setValue(post).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(EditAct.this, "Редактирование объявления прошло успешно.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            });
        }
    }
    private void uploadUpdateImage()
    {
        Bitmap bitmap=null;
        if(load_count<UriArray.length)
        {
            if(UriArray[load_count].equals(uploadNewUri[load_count]))
            {
                uploadUri[load_count]=UriArray[load_count];
                load_count++;
                uploadUpdateImage();
            }
            else if(!UriArray[load_count].equals(uploadNewUri[load_count]) && !uploadNewUri[load_count].equals("empty"))
            {
                /*try {
                    bitmap= MediaStore.Images.Media.getBitmap(getContentResolver(),Uri.parse(uploadNewUri[load_count]));
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                bitmap=bitmapList.get(load_count);
            }
            else if(!UriArray[load_count].equals(uploadNewUri[load_count]) && uploadNewUri[load_count].equals("empty"))
            {
                StorageReference storage_delete_img=FirebaseStorage.getInstance().getReferenceFromUrl(UriArray[load_count]);
                storage_delete_img.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        uploadUri[load_count]="empty";
                        load_count++;
                        if(load_count<UriArray.length)
                        {
                            uploadUpdateImage();
                        }
                        else
                        {
                            UpdatePost();
                        }
                    }
                });
            }
            if(bitmap==null)
                return;
            ByteArrayOutputStream out=new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,20,out);
            byte[] byteArray=out.toByteArray();
            final StorageReference mRef;
            if(!UriArray[load_count].equals("empty"))
            {
                 mRef=FirebaseStorage.getInstance().getReferenceFromUrl(UriArray[load_count]);
            }
            else
            {
                 mRef=storageReference.child(System.currentTimeMillis()+"_image");
            }

            UploadTask load=mRef.putBytes(byteArray);
            Task<Uri> task=load.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    return mRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    uploadUri[load_count]=task.getResult().toString();
                    load_count++;
                    if(load_count<UriArray.length)
                    {
                        uploadUpdateImage();
                    }
                    else
                    {
                        UpdatePost();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }
        else
        {
            UpdatePost();
        }

    }

    @Override
    public void onBitmapLoaded(List<Bitmap> bitmap) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bitmapList.clear();
                bitmapList.addAll(bitmap);
                isLoaded=true;
            }
        });
    }
    /*private void uploadUpdateImage()
    {
        Bitmap bitmap=((BitmapDrawable)imageView.getDrawable()).getBitmap();
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,out);
        byte[] byteArray=out.toByteArray();
        final StorageReference mRef=FirebaseStorage.getInstance().getReferenceFromUrl(temp_image_url);
        UploadTask load=mRef.putBytes(byteArray);
        Task<Uri> task=load.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                return mRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                uploadUri=task.getResult();
                assert uploadUri != null;
                temp_image_url=uploadUri.toString();
                Toast.makeText(EditAct.this, "Загрузка прошла успешно.", Toast.LENGTH_SHORT).show();
                UpdatePost();
                finish();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }*/

}
