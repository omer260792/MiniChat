package com.example.omercohen.minichat.Services;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.backendless.messaging.PublishOptions;
import com.backendless.push.BackendlessPushService;

import java.sql.Timestamp;

import com.example.omercohen.minichat.DBHelper;
import com.example.omercohen.minichat.BackendLessHelper;


public class MyPushService extends BackendlessPushService
{
    Handler H = new Handler(Looper.getMainLooper());
    private SQLiteDatabase db;
    private BackendLessHelper backendLessHelper;
    @Override
    public void onRegistered(Context context, final String registrationId )
    {
        H.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),"device registered" + registrationId, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onUnregistered( Context context, Boolean unregistered )
    {
        H.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "device unregistered", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onMessage( Context context, Intent intent )
    {
        String SenderEmail = intent.getStringExtra("EMAIL"); //get email of sender

        backendLessHelper=BackendLessHelper.GetBackEndLessHelper(context);
        backendLessHelper.AddFriend(SenderEmail);//if friend doesn't exist add him
        db= DBHelper.GetDBHelper(context).getWritableDatabase();//get db

        final String message = intent.getStringExtra( "message" );
        final String img = intent.getStringExtra( "IMG" );
        final String name = intent.getStringExtra( "NAME" );
        final String senderid = intent.getStringExtra("BL_PUBLISHER_ID");
        final Long time = intent.getLongExtra( "google.sent_time" ,1);

        String imglocal="";
        if(img!=null){
            imglocal=BackendLessHelper.AddPhotoFromBackendless(img,SenderEmail,false);
        }

        String str = "INSERT INTO "+DBHelper.ChatWindow+"_"+senderid+" (name,UserID,message,image,Time) VALUES (?,"+senderid+",?,?,"+time+")";
        SQLiteStatement stmt = db.compileStatement(str);

        stmt.bindString(1,name);
        stmt.bindString(2,message);

        stmt.bindString(3,imglocal);
        stmt.execute();

        str = "UPDATE "+DBHelper.ChatMembers+" SET lastmsg = '"+message+" ' WHERE UserID = "+senderid+";";
        db.execSQL(str);
        str = "UPDATE "+DBHelper.ChatMembers+" SET Time = '"+time+" ' WHERE UserID = "+senderid+";";
        db.execSQL(str);

        //refresh adapter userchatwindow and users
        //notification



        return true;// When returning 'true', default Backendless onMessage implementation will be executed.
    }

    @Override
    public void onError(Context context, final String message )
    {
        H.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}