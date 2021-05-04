package com.example.bulletinboard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.util.Log;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ImageManager {
    private Context context;
    private final int MAX_IMAGE_SIZE=1920;
    private int width;
    private int height;
    private OnBitmapLoad onBitmapLoad;
    private List<Bitmap> bitmaps;
    public ImageManager(Context context,OnBitmapLoad onBitmapLoad) {
        this.context = context;
        this.onBitmapLoad=onBitmapLoad;
        bitmaps=new ArrayList<>();
    }

    /*public static Bitmap resizeImage(Bitmap bitmap, int maxSize)
    {
        int width=bitmap.getWidth();
        int height=bitmap.getHeight();
        float imgExpansion=(float)width/(float)height;
        if(imgExpansion>1)
        {
            if(width>maxSize)
            {
                width=maxSize;
                height=(int)(width/imgExpansion);
            }
        }
        else
        {
            if(height>maxSize)
            {
                height=maxSize;
                width=(int)(height*imgExpansion);
            }
        }
        return Bitmap.createScaledBitmap(bitmap,width,height,true);
    }*/
    public int[] getImageSize(String uri)
    {
        int[] ImageSize=new int [2];
        //try {
            BitmapFactory.Options options=new BitmapFactory.Options();
            options.inJustDecodeBounds =true;
            //InputStream inputStream=context.getContentResolver().openInputStream(Uri.parse(uri));
            //BitmapFactory.decodeStream(inputStream,null,options);
            BitmapFactory.decodeFile(uri,options);
            ImageSize[0]=options.outWidth;
            ImageSize[1]=options.outHeight;
        /*} catch (FileNotFoundException e) {
            e.printStackTrace();
        }*/

        return ImageSize;
    }

    public void resizeMultiLargeImages(List<String> uri)
    {
        List<int[]> sizeList=new ArrayList<>();
        List<int[]> sizeOriginList=new ArrayList<>();
        for(int i=0;i<uri.size();i++)
        {
            width=getImageSize(uri.get(i))[0];
            height= getImageSize(uri.get(i))[1];
            sizeOriginList.add(new int[]{width,height});

            float imgExpansion=(float)width/(float)height;
            if(imgExpansion>1)
            {
                if(width>MAX_IMAGE_SIZE)
                {
                    width=MAX_IMAGE_SIZE;
                    height=(int)(width/imgExpansion);
                }
            }
            else
            {
                if(height>MAX_IMAGE_SIZE)
                {
                    height=MAX_IMAGE_SIZE;
                    width=(int)(height*imgExpansion);
                }
            }
            sizeList.add(new int[]{width,height});
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    bitmaps.clear();
                    for(int i=0;i<sizeList.size();i++)
                    {
                        if(!uri.get(i).equals("empty"))
                        {
                            if(uri.get(i).startsWith("http"))
                            {
                                //Bitmap bitmap=Picasso.get().load(uri.get(i)).get();
                                Bitmap bitmap=Picasso.get().load(uri.get(i)).get();
                                bitmaps.add(bitmap);
                            }
                            else if (!uri.get(i).startsWith("http") && (sizeOriginList.get(i)[0]>MAX_IMAGE_SIZE || sizeOriginList.get(i)[1]>MAX_IMAGE_SIZE))
                            {
                               //Bitmap bitmap=Picasso.get().load(uri.get(i)).resize(sizeList.get(i)[0],sizeList.get(i)[1]).get();
                                Bitmap bitmap=Picasso.get().load(Uri.fromFile(new File(uri.get(i)))).resize(sizeList.get(i)[0],sizeList.get(i)[1]).get();
                                bitmaps.add(bitmap);
                            }
                            else
                            {
                                Bitmap bitmap=Picasso.get().load(Uri.fromFile(new File(uri.get(i)))).get();
                                bitmaps.add(bitmap);
                            }
                        }
                        else
                        {
                            bitmaps.add(null);
                        }
                    }
                    onBitmapLoad.onBitmapLoaded(bitmaps);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}
