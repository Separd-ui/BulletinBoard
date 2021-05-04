package com.example.bulletinboard;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.bulletinboard.adapter.ImageAdapter;

import java.util.ArrayList;
import java.util.List;

public class ShowActivity extends AppCompatActivity {
    private TextView text_price,text_tel,text_title,text_desc;

    private List<String> imageToShow;
    private ImageAdapter imageAdapter;
    private TextView image_counter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_layout);
        init();
        GetIntent();
    }
    private void init()
    {
        text_price=findViewById(R.id.text_show_price);
        text_tel=findViewById(R.id.text_show_tel);
        text_title=findViewById(R.id.text_show_title);
        text_desc=findViewById(R.id.text_show_desc);
        image_counter=findViewById(R.id.text_counter);

        imageToShow=new ArrayList<>();
        ViewPager viewPager=findViewById(R.id.view_pager_show);
        imageAdapter=new ImageAdapter(this);
        viewPager.setAdapter(imageAdapter);
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

    }
    private void GetIntent()
    {
        Intent i=getIntent();
        if(i!=null)
        {
            text_price.setText(i.getStringExtra(Constans.PRICE));
            text_desc.setText(i.getStringExtra(Constans.DESC));
            text_title.setText(i.getStringExtra(Constans.TITLE));
            text_tel.setText(i.getStringExtra(Constans.PHONE));
            String[] show_image=new String[3];
            show_image[0]=i.getStringExtra(Constans.IMAGEID);
            show_image[1]=i.getStringExtra(Constans.IMAGEID2);
            show_image[2]=i.getStringExtra(Constans.IMAGEID3);
            for(String s:show_image)
            {
                if(!s.equals("empty"))
                    imageToShow.add(s);
            }
            imageAdapter.UpdateImages(imageToShow);
            String TextToShow = null;
            if(show_image[0].equals("empty") && show_image[1].equals("empty") && show_image[2].equals("empty"))
            {
                TextToShow=0 + "/" + 0;
            }
            else
            {
                TextToShow=1+ "/" + imageToShow.size();
            }
            image_counter.setText(TextToShow);
        }
    }

    public void onClickCall(View view) {
        String toCall="tel:"+text_tel.getText().toString();
        startActivity(new Intent(Intent.ACTION_DIAL,Uri.parse(toCall)));
    }
}
