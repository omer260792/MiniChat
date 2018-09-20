package com.example.omercohen.minichat;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by omercohen on 06/10/2017.
 */

public class ProfilePage extends AppCompatActivity{

    BackendlessUser CurrentUser;
    SQLiteDatabase db;
    TextView nameField, emailFieldReg, textPassword, phoneNumET, birthDayET;
    ImageView imageView;
    String uname, uImg, uemail;
    Bitmap bm;
    String v ="storage/emulated/0/Android/data/com.example.omercohen.minichat/files/Pictures/";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_layout);

        CurrentUser = Backendless.UserService.CurrentUser();
        db = DBHelper.GetDBHelper(this).getWritableDatabase();

        nameField = (TextView) findViewById(R.id.nameField);
        emailFieldReg = (TextView) findViewById(R.id.emailFieldReg);
        nameField.setText((String) CurrentUser.getProperty("name"));
        emailFieldReg.setText((String) CurrentUser.getProperty(BackendLessHelper.pmail));
        imageView = (ImageView)findViewById(R.id.buttonTakeImageRegister);

        uname = getIntent().getStringExtra("name");
        String query = "SELECT * FROM " + DBHelper.ChatMembers; //get user to chat
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        uImg = (String) CurrentUser.getProperty(BackendLessHelper.pImg);
        uemail = (String) CurrentUser.getProperty(BackendLessHelper.pmail);

        getBitmapFromURL(uImg);

        new Thread(){
            @Override
            public void run() {
                super.run();
                AddPhotoFromBackendless(uImg,uemail,true);

            }
        }.start();

        if("IMG/DefaultIcon.png".equals(uImg)){imageView.setImageResource(R.drawable.usericon);}
        else {
            String[] str = uImg.split("/");
            final String ostr = str[str.length - 1];
            BitmapFactory.Options
                    options = new BitmapFactory.Options();
            options.inSampleSize = 3;

            bm = BitmapFactory.decodeFile(v + ostr);
            if (bm != null) imageView.setImageBitmap(bm);
            else {
                    imageView.setImageResource(R.drawable.usericon);

            }
        }
        imageView.setImageBitmap(bm);
    }
    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }


    public static String AddPhotoFromBackendless(String img, String email, boolean b){
        if ("IMG/DefaultIcon.png".equals(img))return null;
        String url="https://api.backendless.com/A11D8319-E3FC-51B6-FFBF-8301AE005C00/v1/files/"+img;
        InputStream in = null;
        try {
            in = new URL(url).openStream();
            Bitmap bitmap = BitmapFactory.decodeStream(in);//get bitmap from input stream

            String Directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)+"/IMG/"+email; //create directory for images
            File directory = new File(Directory);
            if(!directory.exists())directory.mkdirs();

            File imgfile;
            if(b==true){imgfile = new File(Directory,"Profile.jpg");}//if need to download profile picture
            else{
                String [] imgname = img.split("/");//if not profile img take image name
                imgfile = new File(Directory,imgname[imgname.length-1]);//create img file container
            }

            if(!imgfile.exists()){imgfile.createNewFile();}
            else{return imgfile.getAbsolutePath();}


            FileOutputStream fos = new FileOutputStream(imgfile);//download image from backendless to container
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,fos);
            fos.flush();
            fos.close();
            in.close();
            Log.e("ddddd",imgfile.getAbsolutePath().toString());

            return imgfile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();return null;
        }}


}
