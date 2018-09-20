package com.example.omercohen.minichat;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.files.BackendlessFile;
import com.backendless.messaging.PublishOptions;
import com.backendless.persistence.BackendlessDataQuery;
import com.backendless.services.messaging.MessageStatus;

import com.example.omercohen.minichat.Adapters.MsglistAdapter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.example.omercohen.minichat.R;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.omercohen.minichat.BackendLessHelper.pUserID;


/**
 * Created by omercohen on 18/08/2017.
 */

public class Chat_Activity extends AppCompatActivity {

    public static final int CAMERA = 1, GALLERY = 2, WRITE_STORAGE = 2;
    File ImgFileat, ImgFile;
    int userimgInt, senderID;
    ImageView userimg;
    TextView username;
    ImageButton btnSend;
    ListView lv;
    String v = "storage/emulated/0/Android/data/com.example.omercohen.minichat/files/Pictures/";
    String dcim = "storage/emulated/0/DCIM/IMG/";
    EditText msgtxt;
    String uname, msgtxtstr, imgpath = "", uemail, uImg;
    public static Bitmap bm;
    PublishOptions publishOptions;
    SharedPreferences prefs;
    SQLiteDatabase db = DBHelper.GetDBHelper(this).getReadableDatabase();
    Integer UserIDMsg;
    ImageView imageView;
    public static int camBool = 1;

    String nameUser = "";
    String nameUser1 = "";

    private RecyclerView mRecyclerView;
    private ImageButton mButtonSend;
    private EditText mEditTextMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_layout);


        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mButtonSend = (ImageButton) findViewById(R.id.imgBtnSendMsg);
        mEditTextMessage = (EditText) findViewById(R.id.edittxtMsg);
        imageView = (ImageView) findViewById(R.id.imgViewCamera);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        username = (TextView) findViewById(R.id.tvFriendUser);
        lv = (ListView) findViewById(R.id.listViewMsg);
        msgtxt = (EditText) findViewById(R.id.edittxtMsg);
        btnSend = (ImageButton) findViewById(R.id.imgBtnSendMsg);

        nameSender(nameUser1);


        uname = getIntent().getStringExtra("name");

        prefs = getSharedPreferences(RegisterAc.SHPRF, MODE_PRIVATE);

        String query = "SELECT * FROM " + DBHelper.ChatMembers + " WHERE name = '" + uname + "'"; //get user to chat


        userimgInt = getIntent().getIntExtra("UserID", userimgInt);
        senderID = getIntent().getIntExtra("SenderID", senderID);


        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();

        UserIDMsg = cursor.getInt(cursor.getColumnIndexOrThrow(pUserID));
        uImg = cursor.getString(cursor.getColumnIndexOrThrow(BackendLessHelper.pImg));
        uemail = cursor.getString(cursor.getColumnIndexOrThrow(BackendLessHelper.pmail));

        username.setText(uname);
        userimg = (CircleImageView) findViewById(R.id.imgUser);

        if ("IMG/DefaultIcon.png".equals(uImg)) {
            userimg.setImageResource(R.drawable.usericon);
        } else {
            String[] str = uImg.split("/");
            final String ostr = str[str.length - 1];
            BitmapFactory.Options
                    options = new BitmapFactory.Options();
            options.inSampleSize = 3;

            bm = BitmapFactory.decodeFile(v + ostr);
            if (bm != null) userimg.setImageBitmap(bm);
            else {
                //create directory for images
                String Directory = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM));
                Log.e("ddddd", Directory);

                bm = BitmapFactory.decodeFile(dcim + uemail + "/Profile.jpg", options);
                userimg.setImageBitmap(bm);

            }
        }
        getmsg();
        //**** change the path Image


    }


    public void getmsg() {

        String query = "SELECT * FROM " + DBHelper.ChatWindow + "_" + UserIDMsg + " WHERE message IS NOT NULL"; //get table query - each user have msg table
        String querySenderID = "SELECT * FROM " + DBHelper.ChatWindow + "_" + senderID + " WHERE message IS NOT NULL"; //get table query - each user have msg table
        Cursor cursor = db.rawQuery(query, null);//get table cursor
        lv.setAdapter(new MsglistAdapter(this, cursor));

    }


    public void sendMsg(View view) {
        msgtxtstr = msgtxt.getText().toString();
        publishOptions = new PublishOptions();
        publishOptions.setPublisherId(String.valueOf(Backendless.UserService.CurrentUser().getProperty(pUserID)));
        if (userimg != null) {
            String[] imgarray = imgpath.split("/");
            String imgname = imgarray[imgarray.length - 1];
            Backendless.Files.Android.upload(bm, Bitmap.CompressFormat.PNG, 10, imgname, "IMG/" + uemail, false, new AsyncCallback<BackendlessFile>() {
                @Override
                public void handleResponse(BackendlessFile backendlessFile) {
                    msgtxt.setText("");
                }

                @Override
                public void handleFault(BackendlessFault backendlessFault) {

                }
            });

            publishOptions.putHeader("IMG", "IMG/" + uemail + "/" + imgname);
        }
        msgtxt.setText("");
        publishOptions.putHeader("NAME", uname);
        publishOptions.putHeader("EMAIL", uemail);
        publishOptions.putHeader("android-ticker-text", "You just got a push notification!");
        publishOptions.putHeader("android-content-title", uname + " Send You A Message!");
        publishOptions.putHeader("android-content-text", msgtxtstr);
        Backendless.Messaging.publish("CHN" + UserIDMsg, (Object) msgtxtstr, publishOptions, new AsyncCallback<MessageStatus>() {
            @Override
            public void handleResponse(MessageStatus messageStatus) {
                String str = "INSERT INTO " + DBHelper.ChatWindow + "_" + UserIDMsg + " (name,UserID,message,image,Time) VALUES (?," + UserIDMsg + ",?,?," + System.currentTimeMillis() + ")";
                SQLiteStatement stmt = db.compileStatement(str);

                stmt.bindString(1, nameUser1);
                stmt.bindString(2, msgtxtstr);
                if (uImg == null) uImg = "";
                stmt.bindString(3, uImg);
                stmt.execute();


                String str1 = "INSERT INTO " + DBHelper.ChatWindow + "_" + senderID + " (name,UserID,message,image,Time) VALUES (?," + senderID + ",?,?," + System.currentTimeMillis() + ")";
                SQLiteStatement stmtSender = db.compileStatement(str1);

                stmtSender.bindString(1, nameUser1);
                stmtSender.bindString(2, msgtxtstr);
                if (uImg == null) uImg = "";
                stmtSender.bindString(3, uImg);
                stmtSender.execute();
                getmsg();
                imgpath = "";
                ImgFile = null;
            }


            @Override
            public void handleFault(BackendlessFault backendlessFault) {

            }
        });
    }


    public void loadImagebtn(View view) {
        PermissionManager.check(Chat_Activity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE, GALLERY);
        PermissionManager.check(Chat_Activity.this, android.Manifest.permission.CAMERA, CAMERA);
        PermissionManager.check(Chat_Activity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, WRITE_STORAGE);
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();

        alertDialog.setTitle("Profile Photo");

        alertDialog.setMessage("Please Pick A Method");

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Camera ", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                camera();
            }
        });

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Gallery", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                gallery();

            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Cancel", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {

                return;

            }
        });
        alertDialog.show();


    }


    private void camera() {
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//implicit Intent - for image picker from camera
        generateImageFile();//create File object
        i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(ImgFile));//pass Uri to File object - for storage
        startActivityForResult(i, CAMERA);//go to camera
    }

    private void gallery() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);//implicit Intent - for image picker from gallery
        startActivityForResult(i, GALLERY);//open gallery to pick image
    }

    private void generateImageFile() {
        String fileName = "IMG_" + System.currentTimeMillis() + ".jpg";
        ImgFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + fileName);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent i) {

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case GALLERY: {
                    imgpath = getRealPathFromUrl(i.getData());
                    ImgFile = new File(imgpath);
                    bm = BitmapFactory.decodeFile(imgpath);
                }
                case CAMERA:
                    imgpath = ImgFile.getAbsolutePath();
                    bm = BitmapFactory.decodeFile(imgpath);
//                    imageView.setImageBitmap(bm);
                    if (!imgpath.isEmpty()) {
                        camBool = 2;
                        saveImg();


                    }
                    pictureAlert(bm);

            }
        }
    }

    public String getRealPathFromUrl(Uri contentUri) {//get full string path from uri
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            cursor.moveToNext();
            int idImg = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(idImg);
        }


    }

    private void pictureAlert(final Bitmap bitmap) {
        AlertDialog.Builder pic = new AlertDialog.Builder(this);
        pic.setMessage("Tell something about the picture");

        ImageView imgFinalCam = new ImageView(this);
        // imgFinalCam.setImageResource(R.mipmap.ic_launcher);
        imgFinalCam.setImageBitmap(bitmap);


        pic.setView(imgFinalCam);

        pic.setPositiveButton("send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

//                sendPhoto(uemail, uname, bitmap);
            }
        });

        pic.create().show();
    }

//    private void sendPhoto(final String senderId, final String chatName, Bitmap bitmap){
//
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        String imageFileName = "JPEG_" + timeStamp + "_.jpg";
//        String imageDirectory = "sentPics";
//        File file = bitmapConvert(bitmap, imageFileName);
//
//    }

    private File bitmapConvert(Bitmap bitmap, String imageFileName) {
        File f = new File(this.getApplicationContext().getCacheDir(), imageFileName);
        try {
            f.createNewFile();

        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 0, bos);
        byte[] bitmapArry = bos.toByteArray();

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
            fos.write(bitmapArry);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return f;
    }


    public String nameSender(final String name) {
        Thread checkUser = new Thread() {
            @Override
            public void run() {
                super.run();

                Integer currentuser = (Integer) Backendless.UserService.CurrentUser().getProperty(pUserID);//Logged In UserID

                final Map map = new HashMap();//the user I want to add


                String whereClause = "UserID = '" + currentuser + "'";
                final BackendlessDataQuery dataQuery = new BackendlessDataQuery();
                dataQuery.setWhereClause(whereClause);


                BackendlessCollection<BackendlessUser> resultclBCUser1 = Backendless.Persistence.of(BackendlessUser.class).find(dataQuery);
                if (resultclBCUser1.getCurrentPage().isEmpty()) {
                    return;

                } else {


                    BackendlessUser BCUser = resultclBCUser1.getCurrentPage().get(0);

                    map.put(nameUser, BCUser.getProperty(BackendLessHelper.pname));


                    nameUser1 = (String) map.get(nameUser);


                }
            }
        };
        checkUser.start();
        return nameUser1;

    }

    public void saveImg() {


//        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
//        bm.compress(Bitmap.CompressFormat.PNG,100, baos);
//        byte [] b=baos.toByteArray();
//        String temp= Base64.encodeToString(b, Base64.DEFAULT);


        String[] str = imgpath.split("/");
        final String ostr = str[str.length - 1];

        String str1 = "INSERT INTO " + DBHelper.ChatWindow + "_" + senderID + " (name,UserID,message,image,Time) VALUES (?," + senderID + ",?,?," + System.currentTimeMillis() + ")";
        SQLiteStatement stmt = db.compileStatement(str1);

        stmt.bindString(1, "");
        stmt.bindString(2, "");
        stmt.bindString(3, ostr);
        stmt.execute();
    }


}

