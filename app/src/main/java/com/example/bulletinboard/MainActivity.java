package com.example.bulletinboard;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bulletinboard.adapter.DataAdapter;
import com.example.bulletinboard.adapter.DataSend;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private NavigationView navigationView;
    private DrawerLayout drawer;
    private FirebaseAuth auth;
    private TextView text_header;
    public AlertDialog dialog;
    public Toolbar toolbar;
    private DataAdapter.AdapterOnItem adapterOnItem;
    private DataAdapter adapter;
    private RecyclerView recyclerView;
    private List<NewPost> newPostList;
    private DataSend dataSend;
    public DBManager dbManager;
    private final int REQ=15;
    public static  String MAUTH="";
    private String current_cat="Машины";
    private AdView adView;
    public FloatingActionButton fb;
    private AccountHelper accountHelper;
    private ImageView im_nav_header;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addAds();

        adapterOnItem=new DataAdapter.AdapterOnItem() {
            @Override
            public void onAdapterClick(int position) {
            }
        };
        init();
        OnClickAddNewPost();

        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(auth.getCurrentUser()!=null)
                {
                    if (auth.getCurrentUser().isEmailVerified())
                    {
                        Intent i=new Intent(MainActivity.this,EditAct.class);
                        startActivityForResult(i,REQ);
                    }
                    else
                    {
                        accountHelper.DialogSendAgainLink();
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK && data!=null && requestCode==REQ)
        {
            current_cat=data.getStringExtra("cat");
        }
        if(requestCode==AccountHelper.GOOGLE_REQ)
        {
            Task<GoogleSignInAccount> accountTask=GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account=accountTask.getResult(ApiException.class);
                if(account!=null)
                {
                    Picasso.get().load(account.getPhotoUrl()).into(im_nav_header);
                    accountHelper.SignInGoogleResult(account.getIdToken(),0);
                }
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
        if(requestCode==AccountHelper.GOOGLE_REQ_LINK)
        {
            Task<GoogleSignInAccount> accountTask=GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account=accountTask.getResult(ApiException.class);
                if(account!=null)
                {
                    Picasso.get().load(account.getPhotoUrl()).into(im_nav_header);
                    accountHelper.SignInGoogleResult(account.getIdToken(),1);
                }
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
    }
    public void OnClickAddNewPost()
    {
        FirebaseUser user=auth.getCurrentUser();
        if(user!=null && user.isEmailVerified() )
        {
            fb.show();
        }
        else
        {
            fb.hide();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        adView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adView.destroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        GoogleSignInAccount account=GoogleSignIn.getLastSignedInAccount(this);
        if(account!=null)
        {
            Picasso.get().load(account.getPhotoUrl()).into(im_nav_header);
        }

        if(adView!=null)
        {
            adView.resume();
        }
        if(current_cat.equals("my_ads"))
        {
            toolbar.setTitle(R.string.my_ads);
            dbManager.getMyDataFromDB(auth.getUid());
        }
        else {
            toolbar.setTitle(current_cat);
            dbManager.getDataFromDB(current_cat);
        }

    }

    private void init()
    {
        fb=findViewById(R.id.fb_add);
        navigationView=findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Menu menu=navigationView.getMenu();
        MenuItem menuItem=menu.findItem(R.id.ads_id);
        MenuItem menuItem1=menu.findItem(R.id.acc_id);
        SpannableString sp1=new SpannableString(menuItem1.getTitle());
        SpannableString sp=new SpannableString(menuItem.getTitle());
        sp1.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.black)),0,sp1.length(),0);
        sp.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.black)),0,sp.length(),0);
        menuItem.setTitle(sp);
        menuItem1.setTitle(sp1);

        drawer=findViewById(R.id.drawer_layout);
        text_header=navigationView.getHeaderView(0).findViewById(R.id.text_header);
        im_nav_header=navigationView.getHeaderView(0).findViewById(R.id.image_header);
        auth=FirebaseAuth.getInstance();

        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle=new ActionBarDrawerToggle(this,drawer,toolbar,R.string.open,R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        recyclerView=findViewById(R.id.rec_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        newPostList=new ArrayList<>();
        adapter=new DataAdapter(newPostList,this,adapterOnItem);
        recyclerView.setAdapter(adapter);

        GetData();
        dbManager=new DBManager(dataSend,this);
        adapter.SetDBManager(dbManager);
        accountHelper=new AccountHelper(auth,this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getUserForHeader();

    }
    private void GetData()
    {
        dataSend=new DataSend() {
            @Override
            public void DataReceiver(List<NewPost> newPosts) {
                Collections.reverse(newPosts);
                adapter.UpdateAdapter(newPosts);
            }
        };
    }
    public void getUserForHeader()
    {
        FirebaseUser currentUser=auth.getCurrentUser();
        if(currentUser!=null && currentUser.isEmailVerified())
        {
            String text_enter="Приветствуем: "+ currentUser.getEmail();
            text_header.setText(text_enter);
            MAUTH=auth.getUid();
        }
        else
        {
            text_header.setText(R.string.enter_header);
            MAUTH="";
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_settings,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        if(id==R.id.settings_main)
        {
            Intent i=new Intent(MainActivity.this,SettingsActivity.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        if(id==R.id.my_ads)
        {
            toolbar.setTitle(R.string.my_ads);
            dbManager.getMyDataFromDB(auth.getUid());
            current_cat="my_ads";
        }
        if(id==R.id.cars_ads)
        {
            toolbar.setTitle(R.string.cars_ads);
            dbManager.getDataFromDB("Машины");
            current_cat="Машины";
        }
        if(id==R.id.smartphone_ads)
        {
            toolbar.setTitle(R.string.smartphone_ads);
            dbManager.getDataFromDB("Телефоны");
            current_cat="Телефоны";
        }
        if(id==R.id.electronics_ads)
        {
            toolbar.setTitle(R.string.electronic_ads);
            dbManager.getDataFromDB("Бытовая техника");
            current_cat="Бытовая техника";
        }
        if(id==R.id.pc_ads)
        {
            toolbar.setTitle(R.string.pc_ads);
            dbManager.getDataFromDB("Компьютеры");
            current_cat="Компьютеры";
        }
        if(id==R.id.register)
        {
            signDialog(R.string.sign_up,R.string.register,0,R.string.google_sign_up);
        }
        if(id==R.id.sign_in)
        {
            signDialog(R.string.enter_acc,R.string.enter,1,R.string.google_sign_in);

        }
        if(id==R.id.sign_out)
        {
            accountHelper.CallSure();
            im_nav_header.setImageResource(R.drawable.nav_header);
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void signDialog(int title,int button,int index,int button_google_name)
    {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        LayoutInflater layoutInflater=this.getLayoutInflater();
        View dialogView= layoutInflater.inflate(R.layout.sign_layout,null);
        builder.setView(dialogView);


        TextView text_dialog=dialogView.findViewById(R.id.text_title);
        TextView text_restore_pas=dialogView.findViewById(R.id.restore_pas);
        Button button_dialog=dialogView.findViewById(R.id.b_sign_up);
        SignInButton button_google=dialogView.findViewById(R.id.b_sign_google);
        Button button_send_again=dialogView.findViewById(R.id.b_send_again);
        CheckBox checkBox=dialogView.findViewById(R.id.ch_pas);
        EditText ed_mail,ed_pas;
        ed_mail=dialogView.findViewById(R.id.ed_mail);
        ed_pas=dialogView.findViewById(R.id.ed_password);
        if(index==1)
        {
            text_restore_pas.setVisibility(View.VISIBLE);
        }
        else
        {
            text_restore_pas.setVisibility(View.GONE);
        }

        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkBox.isChecked())
                {
                    ed_pas.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
                else
                {
                    ed_pas.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        text_dialog.setText(title);
        button_dialog.setText(button);
        text_restore_pas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button_dialog.setVisibility(View.GONE);
                button_google.setVisibility(View.GONE);
                checkBox.setVisibility(View.GONE);
                ed_pas.setVisibility(View.GONE);
                text_dialog.setText(R.string.enter_email);
                button_send_again.setVisibility(View.VISIBLE);
                text_restore_pas.setVisibility(View.GONE);
            }
        });
        button_send_again.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!ed_mail.getText().toString().equals(""))
                {
                    auth.sendPasswordResetEmail(ed_mail.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(MainActivity.this, "Мы  отправили вам письмо для восстановления.", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                            else
                            {
                                Toast.makeText(MainActivity.this, "Произошла ошибка.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else
                {
                    Toast.makeText(MainActivity.this, "Заполните поле.", Toast.LENGTH_SHORT).show();
                }

            }
        });
        button_dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(ed_mail.getText().toString()) && !TextUtils.isEmpty(ed_pas.getText().toString()))
                {
                    if(index==0)
                    {
                        accountHelper.SignUp(ed_mail.getText().toString(),ed_pas.getText().toString());
                    }
                    else if(index==1)
                    {
                        accountHelper.SignIn(ed_mail.getText().toString(),ed_pas.getText().toString());
                    }
                }
                else
                {
                    Toast.makeText(MainActivity.this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                }
            }
        });
        button_google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(auth.getCurrentUser()!=null)
                {
                    Toast.makeText(MainActivity.this, "Вы уже зарегестрировались.", Toast.LENGTH_SHORT).show();
                    return;
                }
                else
                {
                    accountHelper.SignIn_Google(AccountHelper.GOOGLE_REQ);
                }
            }
        });
        dialog=builder.create();
        if(dialog.getWindow()!=null)
        {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();
    }

    private void addAds()
    {
        MobileAds.initialize(this);
        adView=findViewById(R.id.adView);
        AdRequest adRequest=new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }


}