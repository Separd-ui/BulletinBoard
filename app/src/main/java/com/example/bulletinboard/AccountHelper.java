package com.example.bulletinboard;

import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.concurrent.Executor;

public class AccountHelper {
    private FirebaseAuth auth;
    private MainActivity activity;
    private GoogleSignInClient googleSignInClient;
    public static final int GOOGLE_REQ=35;
    public static final int GOOGLE_REQ_LINK=40;
    private String temp_email,temp_password;

    public AccountHelper(FirebaseAuth auth, MainActivity activity) {
        this.auth = auth;
        this.activity = activity;
        GoogleAccountManager();
    }

    public void SendEmail()
    {
        FirebaseUser user=auth.getCurrentUser();
        assert user != null;
        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(activity, "Мы отправили вам письмо для подтверждения.", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(activity, "Что-то пошло не так.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public void linkAccounts(String email,String password)
    {
        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
        if(auth.getCurrentUser()!=null)
        {
            auth.getCurrentUser().linkWithCredential(credential)
                    .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                if(task.getResult()==null)
                                    return;
                                activity.dialog.dismiss();
                                Toast.makeText(activity, "Ваши аккаунты были успешно связаны.", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(activity, "Произошла ошибка.", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
        }
        else
        {
            temp_email=email;
            temp_password=password;
            DialogSignWithLink(R.string.alert,R.string.alert_message);
        }

    }
    public   void SignIn(String email,String password)
    {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            activity.getUserForHeader();
                            activity.dialog.dismiss();
                            activity.fb.show();
                            activity.dbManager.getDataFromDB("Машины");
                            activity.toolbar.setTitle(R.string.cars_ads);
                        }
                        else
                        {
                            Toast.makeText(activity, "Вход провалился", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    public void DialogSendAgainLink()
    {
        AlertDialog.Builder builder=new AlertDialog.Builder(activity);
        builder.setMessage(R.string.link_message);
        builder.setTitle(R.string.link_title);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SendEmail();
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SignOut();
                activity.getUserForHeader();
            }
        });
    }
    public void DialogSignWithLink(int message,int title)
    {
        AlertDialog.Builder builder=new AlertDialog.Builder(activity);
        builder.setMessage(title);
        builder.setTitle(message);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SignIn_Google(GOOGLE_REQ_LINK);
            }
        });
        builder.show();

    }
    public void SignUp(String email,String password)
    {
        if(!email.equals("") && !password.equals(""))
        {
            auth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener( activity, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                                SendEmail();
                                activity.getUserForHeader();
                                activity.dialog.dismiss();
                            }
                            else
                            {
                                FirebaseAuthUserCollisionException exception=(FirebaseAuthUserCollisionException) task.getException();
                                if(exception==null)
                                    return;
                                if(exception.getErrorCode().equals("ERROR_EMAIL_ALREADY_IN_USE"))
                                {
                                    linkAccounts(email,password);
                                }
                            }
                        }
                    });
        }
    }
    private void SignOut()
    {
        auth.signOut();
        activity.fb.hide();
        googleSignInClient.signOut();
        activity.getUserForHeader();
    }
    public void SignIn_Google(int req)
    {
        Intent i =googleSignInClient.getSignInIntent();
        activity.startActivityForResult(i,req);
    }
    private void GoogleAccountManager()
    {
        GoogleSignInOptions googleSignInOptions=new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getString(R.string.default_web_client_id)).requestEmail().build();
        googleSignInClient= GoogleSignIn.getClient(activity,googleSignInOptions);
    }
    public void SignInGoogleResult(String idToken,int index)
    {
        AuthCredential googleAuthCredential= GoogleAuthProvider.getCredential(idToken,null);
        auth.signInWithCredential(googleAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    if(index==1)
                    {
                        linkAccounts(temp_email,temp_password);
                        activity.getUserForHeader();
                        activity.dbManager.getDataFromDB("Машины");
                        activity.toolbar.setTitle(R.string.cars_ads);
                        activity.OnClickAddNewPost();
                    }
                    if(index==0)
                    {
                        activity.dialog.dismiss();
                        activity.getUserForHeader();
                        activity.dbManager.getDataFromDB("Машины");
                        activity.toolbar.setTitle(R.string.cars_ads);
                        activity.OnClickAddNewPost();
                    }

                }
                else
                {
                    Toast.makeText(activity, "Что-то пошло не так.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public void CallSure()
    {
        AlertDialog.Builder builder=new AlertDialog.Builder(activity);
        builder.setTitle(R.string.sign_out);
        builder.setMessage(R.string.sure);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SignOut();
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }
}
