package com.example.bulletinboard.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.example.bulletinboard.ImageManager;
import com.example.bulletinboard.OnBitmapLoad;
import com.example.bulletinboard.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends PagerAdapter implements OnBitmapLoad{
    private Activity context;
    private LayoutInflater layoutInflater;
    private List<String> imageToShow;
    private List<Bitmap> bitmaps;
    private ImageManager imageManager;
    private Boolean isUri=false;

    public ImageAdapter(Activity context) {
        this.context = context;
        layoutInflater=LayoutInflater.from(context);
        imageToShow=new ArrayList<>();
        bitmaps=new ArrayList<>();
        imageManager=new ImageManager(context,this);
    }

    @Override
    public int getCount() {
        return bitmaps.size();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view=layoutInflater.inflate(R.layout.page_viewer,container,false);
        ImageView image_pager;
        image_pager=view.findViewById(R.id.imageViewPager);

        image_pager.setImageBitmap(bitmaps.get(position));
        /*if(uri.startsWith("http"))
        {
            Picasso.get().load(uri).into(image_pager);
        }
        else
        {
            image_pager.setImageURI(Uri.parse(imageToShow.get(position)));
        }*/

        container.addView(view);
        return view;

    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view==object;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((LinearLayout)object);
    }
    public void UpdateImages(List<String> images)
    {
        imageManager.resizeMultiLargeImages(images);
    }

    @Override
    public void onBitmapLoaded(List<Bitmap> bitmap) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bitmaps.clear();
                bitmaps.addAll(bitmap);
                notifyDataSetChanged();
            }
        });
    }

}
