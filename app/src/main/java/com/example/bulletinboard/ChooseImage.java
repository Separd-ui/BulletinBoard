package com.example.bulletinboard;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChooseImage extends AppCompatActivity {
    private ImageView im_main,im_2,im_3;
    private ImageView[] imageShow=new ImageView[3];
    private String[] images=new String[3];
    private ImageManager imageManager;
    private final int MAX_IMAGE_SIZE=1920;
    private OnBitmapLoad onBitmapLoad;
    private Boolean isImageLoaded=true;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_image);
        init();
        GetIntent();

        FloatingActionButton fb_back=findViewById(R.id.fb_back);
        fb_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent();
                i.putExtra(Constans.URI_1,images[0]);
                i.putExtra(Constans.URI_2,images[1]);
                i.putExtra(Constans.URI_3,images[2]);
                setResult(RESULT_OK,i);
                finish();
            }
        });
    }
    private void GetIntent()
    {
        Intent i=getIntent();
        if(i!=null)
        {
            List<String> imageBitmaps=new ArrayList<>();
            images[0]=i.getStringExtra(Constans.IMAGEID);
            images[1]=i.getStringExtra(Constans.IMAGEID2);
            images[2]=i.getStringExtra(Constans.IMAGEID3);
            isImageLoaded=false;
            for(int j=0;j<images.length;j++)
            {
                if(!images[j].equals("empty"))
                {
                    if(images[j].startsWith("http"))
                    {
                        Picasso.get().load(images[j]).into(imageShow[j]);
                        imageBitmaps.add("empty");
                    }
                    else
                    {
                        imageBitmaps.add(images[j]);
                        //imageShow[j].setImageURI(Uri.parse(images[j]));
                    }
                }
                else
                {
                    imageBitmaps.add("empty");
                }
            }
            imageManager.resizeMultiLargeImages(imageBitmaps);
        }
    }
    private void getBitmpapFromPic()
    {
        onBitmapLoad=new OnBitmapLoad() {
            @Override
            public void onBitmapLoaded(List<Bitmap> bitmap) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        for(int i=0;i<bitmap.size();i++)
                        {
                            if(bitmap.get(i)!=null){
                                imageShow[i].setImageBitmap(bitmap.get(i));
                            }
                        }
                        isImageLoaded=true;
                    }
                });
            }
        };
    }
    private void init()
    {

        getBitmpapFromPic();
        imageManager=new ImageManager(this,onBitmapLoad);
        im_main=findViewById(R.id.main_image);
        im_2=findViewById(R.id.image_2);
        im_3=findViewById(R.id.image_3);

        imageShow[0]=im_main;
        imageShow[1]=im_2;
        imageShow[2]=im_3;
        images[0]="empty";
        images[1]="empty";
        images[2]="empty";
    }
    public void onClickImage_1(View view) {
        if(!isImageLoaded)
        {
            Toast.makeText(this, "Идёт загрузка картинок.Пожалуйста подождите.", Toast.LENGTH_SHORT).show();
            return;
        }
        getImage(1);
    }

    public void onClickImage_2(View view) {
        if(!isImageLoaded)
        {
            Toast.makeText(this, "Идёт загрузка картинок.Пожалуйста подождите.", Toast.LENGTH_SHORT).show();
            return;
        }
        getImage(2);
    }

    public void onClickImage_3(View view) {
        if(!isImageLoaded)
        {
            Toast.makeText(this, "Идёт загрузка картинок.Пожалуйста подождите.", Toast.LENGTH_SHORT).show();
            return;
        }
        getImage(3);
    }
    private void getImage(int index)
    {
        /*Intent i=new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_OPEN_DOCUMENT);
        startActivityForResult(i,index);*/
        Options options = Options.init()
                .setRequestCode(index)
                .setCount(1)
                .setFrontfacing(false)
                .setMode(Options.Mode.Picture)
                .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT);


        Pix.start(ChooseImage.this, options);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK && data!=null)
        {
            ArrayList<String> returnValue = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
            if(returnValue== null)
                return;
            switch (requestCode)
            {
                case 1:

                    images[0]=returnValue.get(0);
                    isImageLoaded=false;
                    imageManager.resizeMultiLargeImages(Arrays.asList(images));
                    break;
                case 2:
                    images[1]=returnValue.get(0);
                    isImageLoaded=false;
                    imageManager.resizeMultiLargeImages(Arrays.asList(images));
                    break;
                case 3:
                    images[2]=returnValue.get(0);
                    isImageLoaded=false;
                    imageManager.resizeMultiLargeImages(Arrays.asList(images));
                    break;

            }
        }
    }


    public void onClickDeleteImg1(View view) {
        images[0]="empty";
        im_main.setImageResource(R.drawable.archive);
    }
    public void onClickDeleteImg2(View view) {
        images[1]="empty";
        im_2.setImageResource(R.drawable.archive);
    }
    public void onClickDeleteImg3(View view) {
        im_3.setImageResource(R.drawable.archive);
        images[2]="empty";
    }
}
