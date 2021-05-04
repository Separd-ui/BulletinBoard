package com.example.bulletinboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class LogoActivity extends AppCompatActivity {
    private ImageView imageView,img_load;
    private Animation anim_img,anim_load;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logo);
        init();
        AutoStart();
    }
    private void init()
    {
        imageView=findViewById(R.id.image_logo);
        img_load=findViewById(R.id.img_loading);
        anim_img=AnimationUtils.loadAnimation(getApplicationContext(),R.anim.anim_image);
        anim_load=AnimationUtils.loadAnimation(getApplicationContext(),R.anim.anim_load);
        img_load.startAnimation(anim_load);
        imageView.startAnimation(anim_img);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

    private void AutoStart()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                    Intent i =new Intent(LogoActivity.this,MainActivity.class);
                    startActivity(i);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
