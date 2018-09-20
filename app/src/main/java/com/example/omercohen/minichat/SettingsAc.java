package com.example.omercohen.minichat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

public class SettingsAc extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_layout);
    }

    public void SignOut(View view) {
        Backendless.UserService.logout(new AsyncCallback<Void>() {
            @Override
            public void handleResponse(Void aVoid) {
                BackendLessHelper.Terminate();
                DBHelper.Terminate();
                Intent i = new Intent(SettingsAc.this,LoginAc.class);
                startActivity(i);
            }

            @Override
            public void handleFault(BackendlessFault backendlessFault) {

            }
        });
    }

    public void changeProfile(View view) {
        Intent i = new Intent(SettingsAc.this,ProfilePage.class);
        startActivity(i);
    }
}
