package com.example.bulletinboard.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bulletinboard.Constans;
import com.example.bulletinboard.DBManager;
import com.example.bulletinboard.EditAct;
import com.example.bulletinboard.MainActivity;
import com.example.bulletinboard.NewPost;
import com.example.bulletinboard.R;
import com.example.bulletinboard.ShowActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolderData> {
    private List<NewPost> postList;
    private Context context;
    private AdapterOnItem adapterOnItem;
    private DBManager dbManager;
    private SharedPreferences def_s;

    public DataAdapter(List<NewPost> postList, Context context,AdapterOnItem adapterOnItem) {
        this.postList = postList;
        this.context = context;
        this.adapterOnItem=adapterOnItem;
        def_s= PreferenceManager.getDefaultSharedPreferences(context);
    }

    @NonNull
    @Override

    public ViewHolderData onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.adapter,parent,false);
        return new ViewHolderData(view,adapterOnItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderData holder, int position) {
        holder.setData(postList.get(position));
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class ViewHolderData extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView text_price,text_desc,text_title;
        private ImageView im_ads;
        private AdapterOnItem adapterOnItem;
        private LinearLayout layout_edit;
        private ImageButton b_edit,b_delete;
        private TextView text_views;
        public ViewHolderData(@NonNull View itemView,AdapterOnItem adapterOnItem) {
            super(itemView);
            text_price=itemView.findViewById(R.id.text_pricetel);
            text_title=itemView.findViewById(R.id.ad_title);
            text_desc=itemView.findViewById(R.id.text_desc);
            layout_edit=itemView.findViewById(R.id.layout_edit);
            b_delete=itemView.findViewById(R.id.b_delete);
            text_views=itemView.findViewById(R.id.text_count);
            b_edit=itemView.findViewById(R.id.b_edit);
            im_ads=itemView.findViewById(R.id.img_ads);
            this.adapterOnItem=adapterOnItem;
            itemView.setOnClickListener(this);

        }
        public void setData(NewPost newPost)
        {
            text_title.setTextColor(Color.parseColor(def_s.getString("list_preference_change_text","#FF000000")));
            text_desc.setTextColor(Color.parseColor(def_s.getString("list_preference_change_text","#FF000000")));
            text_price.setTextColor(Color.parseColor(def_s.getString("list_preference_change_text","#FF000000")));
            if(newPost.getUI()!=null)
            {
                if(newPost.getUI().equals(MainActivity.MAUTH))
                {
                    layout_edit.setVisibility(View.VISIBLE);
                }
                else
                {
                    layout_edit.setVisibility(View.GONE);
                }
            }
            Picasso.get().load(newPost.getImageId()).into(im_ads);
            String price_tel="Цена: " + newPost.getPrice() + "   Тел :" +newPost.getTel();
            text_title.setText(newPost.getTitle());
            String textDesc=null;
            if(newPost.getDesc().length()>50)
                textDesc=newPost.getDesc().substring(0,50)+"...";
            else
                textDesc=newPost.getDesc();
            text_desc.setText(textDesc);
            text_price.setText(price_tel);
            text_views.setText(newPost.getTotalViews());
            b_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertCallDelete(newPost,getAdapterPosition());
                }
            });
            b_edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i =new Intent(context, EditAct.class);
                    //i.putExtra(Constans.NEW_POST_INTENT,newPost);
                    i.putExtra(Constans.TITLE,newPost.getTitle());
                    i.putExtra(Constans.DESC,newPost.getDesc());
                    i.putExtra(Constans.KEY,newPost.getKey());
                    i.putExtra(Constans.PRICE,newPost.getPrice());
                    i.putExtra(Constans.CATEGORY,newPost.getCategory());
                    i.putExtra(Constans.IMAGEID,newPost.getImageId());
                    i.putExtra(Constans.IMAGEID2,newPost.getImageId2());
                    i.putExtra(Constans.IMAGEID3,newPost.getImageId3());
                    i.putExtra(Constans.UI,newPost.getUI());
                    i.putExtra(Constans.PHONE,newPost.getTel());
                    i.putExtra(Constans.TIME,newPost.getTime());
                    i.putExtra(Constans.TOTAL_VIEWS,newPost.getTotalViews());
                    context.startActivity(i);
                }
            });
        }

        @Override
        public void onClick(View v) {
            NewPost newPost=postList.get(getAdapterPosition());
                dbManager.ShowViews(newPost);
                Intent i =new Intent(context, ShowActivity.class);
                i.putExtra(Constans.TITLE,newPost.getTitle());
                i.putExtra(Constans.IMAGEID,newPost.getImageId());
                i.putExtra(Constans.PHONE,newPost.getTel());
                i.putExtra(Constans.DESC,newPost.getDesc());
                i.putExtra(Constans.PRICE,newPost.getPrice());
                i.putExtra(Constans.IMAGEID2,newPost.getImageId2());
                i.putExtra(Constans.IMAGEID3,newPost.getImageId3());
                context.startActivity(i);
                adapterOnItem.onAdapterClick(getAdapterPosition());
        }
    }
    private void AlertCallDelete(NewPost newPost,int position)
    {
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        builder.setTitle(R.string.delete);
        builder.setMessage(R.string.delete_mes);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dbManager.DeleteItem(newPost);
                postList.remove(position);
                notifyItemRemoved(position);
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }
    public interface  AdapterOnItem
    {
         void onAdapterClick(int position);
    }
    public void UpdateAdapter(List<NewPost> newPosts)
    {
        postList.clear();
        postList.addAll(newPosts);
        notifyDataSetChanged();

    }
    public void SetDBManager(DBManager dbManager)
    {
        this.dbManager=dbManager;
    }
}
