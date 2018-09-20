package com.example.omercohen.minichat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.files.BackendlessFile;

import java.io.File;


public class RegisterAc extends AppCompatActivity {

    public static final int CAMERA = 1,GALLERY=2,WRITE_STORAGE=2;
    ImageView img;
    private EditText emailFieldR, passwordFieldR, nameField;
    private String imgpath;
    BackendlessUser user;
    private File ImgFile;
    SharedPreferences prefs;
    Bitmap bm;
    static String SHPRF = "WhatPref";


    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        Backendless.initApp(this,LoginAc.AppId, LoginAc.AndroidKey, LoginAc.Version );

        nameField = (EditText) findViewById(R.id.nameField);
        emailFieldR = (EditText) findViewById(R.id.emailFieldReg);
        passwordFieldR = (EditText) findViewById(R.id.textPasswordReg);
        img = (ImageView) findViewById(R.id.buttonTakeImageRegister);

        prefs = getSharedPreferences(SHPRF,MODE_PRIVATE);
    }

    public void RegisterBtn(View v){
        final String name=nameField.getText().toString();
        final String email=emailFieldR.getText().toString();
        final String password=passwordFieldR.getText().toString();

        final Long[] counterValue = new Long[1];
        Thread t=new Thread(){
            @Override
            public void run() {
                counterValue[0] = Backendless.Counters.incrementAndGet( "my counter" );
            }
        };t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            LoginAc.ToastMSG(RegisterAc.this,"Please Try Again Later");
        }

        final int userid = Math.round(counterValue[0]);
        user = new BackendlessUser();
        user.setProperty(BackendLessHelper.pname,name);
        user.setProperty(BackendLessHelper.pmail, email );
        user.setProperty(BackendLessHelper.pUserID,userid);
        user.setPassword( password );



        Backendless.UserService.register( user, new AsyncCallback<BackendlessUser>()
        {
            public void handleResponse(final BackendlessUser registeredUser )
            {
                LoginAc.Login(RegisterAc.this,email,password);//login after register
                if(imgpath!=null&&ImgFile!=null){//if register successfully - upload and save photo

                        String [] str = imgpath.split("/");
                        final String ostr = str[str.length - 1];
                    Backendless.Files.Android.upload(bm, Bitmap.CompressFormat.PNG, 100, ostr, "IMG/"+registeredUser.getEmail(), false, new AsyncCallback<BackendlessFile>() {
                        @Override
                        public void handleResponse(BackendlessFile backendlessFile) {
                            registeredUser.setProperty(BackendLessHelper.pImg,"IMG/"+registeredUser.getEmail()+"/"+ostr);
                            Backendless.Persistence.of(BackendlessUser.class).save(registeredUser, new AsyncCallback<BackendlessUser>() {
                                @Override
                                public void handleResponse(BackendlessUser backendlessUser) {
                                    prefs.edit().putString(String.valueOf(userid),imgpath).apply();
                                }

                                @Override
                                public void handleFault(BackendlessFault backendlessFault) {

                                }
                            });
                        }

                        @Override
                        public void handleFault(BackendlessFault backendlessFault) {

                        }
                    });
                }

            }
            public void handleFault( BackendlessFault fault )
            {
                LoginAc.ToastMSG(RegisterAc.this,fault.getMessage());
            }
        } );

    }

    public void backPage(View view) {
        Intent i = new Intent(RegisterAc.this,LoginAc.class);
        startActivity(i);
    }

    public void onImg(View view) {
        PermissionManager.check(RegisterAc.this, android.Manifest.permission.READ_EXTERNAL_STORAGE,GALLERY);
        PermissionManager.check(RegisterAc.this, android.Manifest.permission.CAMERA,CAMERA);
        PermissionManager.check(RegisterAc.this, Manifest.permission.WRITE_EXTERNAL_STORAGE,WRITE_STORAGE);

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();

        alertDialog.setTitle("Profile Photo");
        alertDialog.setMessage("Please Pick A Method");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Camera ", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                camera();
            }});
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Gallery", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {

                gallery();

            }});
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Cancel", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {

                return;

            }});

        alertDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent i) {

        if(resultCode==RESULT_OK){
            switch (requestCode){
                case GALLERY:{
                    imgpath=getRealPathFromUrl(i.getData());
                    ImgFile = new File(imgpath);
                    bm = BitmapFactory.decodeFile(imgpath);
                    img.setImageBitmap(bm);}
                case CAMERA:
                    imgpath = ImgFile.getAbsolutePath();
                    bm = BitmapFactory.decodeFile(imgpath);
                    img.setImageBitmap(bm);
            }}
    }
    private void camera() {
    Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//implicit Intent - for image picker from camera
    generateImageFile();//create File object
    i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(ImgFile));//pass Uri to File object - for storage
    startActivityForResult(i, CAMERA);//go to camera
}

    private void gallery(){
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);//implicit Intent - for image picker from gallery
        startActivityForResult(i,GALLERY);//open gallery to pick image
    }

    private void generateImageFile(){
        String fileName="IMG_"+System.currentTimeMillis()+".jpg";
        ImgFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES)+"/"+fileName);
    }


    public String getRealPathFromUrl(Uri contentUri){//get full string path from uri
        Cursor cursor = getContentResolver().query(contentUri,null,null,null,null);
        if(cursor == null){
            return contentUri.getPath();
        }else {
            cursor.moveToNext();
            int idImg = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            Log.e("ooeoe", cursor.getString(idImg));
            return cursor.getString(idImg);
        }}


}
