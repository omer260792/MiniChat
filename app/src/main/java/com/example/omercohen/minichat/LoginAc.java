package com.example.omercohen.minichat;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.IntegerRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.local.UserIdStorageFactory;
import com.backendless.persistence.local.UserTokenStorageFactory;

import java.util.HashMap;

public class LoginAc extends AppCompatActivity {

    EditText LoginUsername,LoginUserpassword;
    private String Username,Password;
    public static String AppId="A11D8319-E3FC-51B6-FFBF-8301AE005C00",AndroidKey="183AB9FE-0D13-C8F9-FFED-6C3BE48E7300",Version="v1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);
        Backendless.initApp(this,AppId,AndroidKey,Version);

        LoginUsername=(EditText)findViewById(R.id.LoginUserName);
        LoginUserpassword=(EditText)findViewById(R.id.LoginPassword);

        PermissionManager.check(LoginAc.this, android.Manifest.permission.READ_EXTERNAL_STORAGE,1);
        PermissionManager.check(LoginAc.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE,1);
    }

    @Override
    protected void onStart() {
        super.onStart();

        String userToken = UserTokenStorageFactory.instance().getStorage().get();

        if( userToken != null && !userToken.equals( "" ) )
        {   String currentUserObjectId = UserIdStorageFactory.instance().getStorage().get();
            Backendless.Data.of( BackendlessUser.class ).findById(currentUserObjectId, new AsyncCallback<BackendlessUser>() {
                @Override
                public void handleResponse(BackendlessUser backendlessUser) {//intent to chatlist with current user login
                    Backendless.UserService.setCurrentUser(backendlessUser);
                    startActivity(new Intent("ChatListActivity"));
                }
                @Override
                public void handleFault(BackendlessFault backendlessFault) {
                    ToastMSG(LoginAc.this,"Failed To Auto Login");
                }
            });
        }}

    public void LoginBtnFunc(View v){ //login btn
        Username=LoginUsername.getText().toString();
        Password=LoginUserpassword.getText().toString();
        Login(this,Username,Password);
    }

    public static void Login(final Context c, final String username, String pass){ //static login method
        Backendless.UserService.login( username, pass, new AsyncCallback<BackendlessUser>()
        {
            public void handleResponse( BackendlessUser user ) //if login successfully
            {
                String channel = "CHN"+user.getProperty(BackendLessHelper.pUserID);//Channel name
                Backendless.Messaging.registerDevice("645218480427", channel, new AsyncCallback<Void>() {//Register Device
                    @Override
                    public void handleResponse(Void aVoid) {
                        //When Device Register Successfully
                    }
                    @Override
                    public void handleFault(BackendlessFault backendlessFault) {
                        ToastMSG(c,"Failed to Register Device "+backendlessFault.getMessage());
                    }
                });
                Intent i = new Intent("ChatListActivity");
                c.startActivity(i);
            }
            public void handleFault( BackendlessFault fault ) {ToastMSG(c,fault.getMessage());} // if login unsuccessfully
        },true);//true - stay login until logout
    }



    public void RegisterBtn(View v){
        startActivity(new Intent("RegisterActivity"));
    }

    public static void ToastMSG(Context c,String msg){
        Toast.makeText(c,msg,Toast.LENGTH_LONG).show();

    }

}
