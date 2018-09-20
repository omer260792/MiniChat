package com.example.omercohen.minichat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.BackendlessUser;
import com.backendless.exceptions.BackendlessException;
import com.backendless.persistence.BackendlessDataQuery;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.io.InputStreamReader;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.omercohen.minichat.R.id.LoginPassword;
import static com.example.omercohen.minichat.R.id.time;
import static com.example.omercohen.minichat.R.id.userImage;


public class BackendLessHelper{//Single Tone

    public static BackendLessHelper bckendLessHelper;
    public final static String pname="name",pmail="email",pUserID="UserID",pImg="UserImg";
    public SQLiteDatabase db;
    Context context;
    Integer o;


    private BackendLessHelper(Context context){
        this.context=context;
        db= DBHelper.GetDBHelper(context).getWritableDatabase();



    }

    public static void Terminate(){
        backendLessHelper = null;
    }

    public static BackendLessHelper GetBackEndLessHelper(Context context){
        if (backendLessHelper==null){backendLessHelper=new BackendLessHelper(context);}
        return backendLessHelper;
    }

    public boolean[] AddFriend(final String email){ //the array - [if success][if user doesn't exsist - true]
        final boolean [] b =new boolean[2];
        b[0]=false;
        Thread AddFR=new Thread(){
            @Override
            public void run() {
                Integer currentuser= (Integer) Backendless.UserService.CurrentUser().getProperty(pUserID);//Logged In UserID
                final String ContactTableName = DBHelper.ChatMembers+"_"+currentuser;//get contact table name in local DB Backendless

                Cursor dbcursor = db.rawQuery("SELECT * FROM "+DBHelper.ChatMembers+" WHERE email = ?",new String []{email});//check if user exist in local DB
                if(dbcursor!=null && dbcursor.getCount()>0){

                    b[0]=true;
                    return;}//break thread

                String whereClause = "email = '"+email+"'";
                final BackendlessDataQuery dataQuery = new BackendlessDataQuery();
                dataQuery.setWhereClause( whereClause );



                //sync search

                BackendlessCollection<Map> resultcl = new BackendlessCollection<Map>();
                List<Map> resultpage;//collection - the book,list map - pages,map - users
                final Map map = new HashMap();//the user I want to add
                try{resultcl = Backendless.Persistence.of( ContactTableName ).find( dataQuery );} //Check if exsist on Chatmembers_userid table and if table not exsist
                catch(BackendlessException e){}
                resultpage =  resultcl.getCurrentPage();


                if(!resultpage.isEmpty()){//if exsist user in backendless Chatmember_userId table
                    Map original =  resultpage.get(0);//first user result

                    map.put(pmail,email);
                    map.put(pname,original.get(pname));
                    map.put(pUserID,original.get(pUserID));
                    map.put(pImg,original.get(pImg));

                }else{
                    //set on server as well
                    BackendlessCollection<BackendlessUser> resultclBCUser = Backendless.Persistence.of(BackendlessUser.class ).find( dataQuery );
                    if(resultclBCUser.getCurrentPage().isEmpty()){b[1]=true;return;}//need to add User doesn't exsist msg
                    else{
                        BackendlessUser BCUser = resultclBCUser.getCurrentPage().get(0);

                        o = (Integer) map.get(pUserID);

                        map.put(pmail,email);
                        map.put(pname,BCUser.getProperty(pname));
                        map.put(pUserID,BCUser.getProperty(pUserID));
                        map.put(pImg,BCUser.getProperty(pImg));
                        Backendless.Data.of(ContactTableName).save(map);

                    }
                }
                //have user map
                SQLiteStatement stmt=db.compileStatement("INSERT INTO "+DBHelper.ChatMembers+" (email,name,UserID,UserImg,Time)" +
                        " VALUES(?,?,"+map.get(pUserID)+",?,"+System.currentTimeMillis()+")");
                stmt.bindString(1,email);
                stmt.bindString(2,(String) map.get(pname));
                stmt.bindString(3, (String) map.get(pImg));
                stmt.execute();//Insert to db
                //upload picture from server
                new Thread(){
                    @Override
                    public void run() {
                        String imgfullpath =  AddPhotoFromBackendless((String)map.get(pImg),email,true);//Create new file and update db
                        if(imgfullpath!=null){
                            String str="UPDATE " + DBHelper.ChatMembers+" SET UserImg ='"+(String) map.get(pImg)+"' WHERE email= '"+email+"'";
                            db.execSQL(str);

                        }
                    }}.start();
                // need to create user chat table
                db.execSQL("CREATE TABLE IF NOT EXISTS "+DBHelper.ChatWindow+"_"+map.get(pUserID)+" ('_id' INTEGER PRIMARY KEY AUTOINCREMENT , `name` VARCHAR," +
                        " `UserID` INTEGER NOT NULL,`message` VARCHAR,`image` VARCHAR, `Time` TIMESTAMP DEFAULT CURRENT_TIMESTAM)");



                b[0]=true;
            }};

        AddFR.start();
        try {
            AddFR.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return b;
    }

    public static String AddPhotoFromBackendless(String img, String email, boolean b){
        if ("IMG/DefaultIcon.png".equals(img))return null;
        String url="https://api.backendless.com/a11d8319-e3fc-51b6-ffbf-8301ae005c00/v1/files/"+img;
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
            return imgfile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();return null;
        }}

}
