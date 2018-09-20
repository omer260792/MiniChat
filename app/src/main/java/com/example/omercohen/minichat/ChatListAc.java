package com.example.omercohen.minichat;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.files.BackendlessFile;
import com.example.omercohen.minichat.Adapters.ChatlistAdapter;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.omercohen.minichat.DBHelper.ChatMembers;


/**
 * Created by Ido Talmor on 01/07/2017.
 */

public class ChatListAc extends AppCompatActivity  {

    Bitmap bitmapProfile;
    TextView name;
    CircleImageView profileImg;
    BackendLessHelper BC;
    ListView lv;
    SQLiteDatabase db;
    BackendlessUser CurrentUser;
    Handler handler;
    private File ImgFile;
    private String imgpath;
    Bitmap bm;
    String userEmail;
    String userid;
    SharedPreferences prefs;
    private TextView TVProfile, TVSettings, TVPhone;
    View view;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatlist_layout);

        CurrentUser = Backendless.UserService.CurrentUser();
        BC=BackendLessHelper.GetBackEndLessHelper(this);
        db=DBHelper.GetDBHelper(this).getWritableDatabase();

        profileImg = (CircleImageView) findViewById(R.id.ImgVProfile);

        prefs = getSharedPreferences(RegisterAc.SHPRF,MODE_PRIVATE);

        name=(TextView)findViewById(R.id.TextName);
        lv=(ListView)findViewById(R.id.chatList);

        name.setText((String)CurrentUser.getProperty("name"));
        userid = String.valueOf(CurrentUser.getProperty(BackendLessHelper.pUserID));
        userEmail = String.valueOf(CurrentUser.getProperty(BackendLessHelper.pmail));
        String imgpath = prefs.getString(userid,null);
        addTableUser();

        //buttom navigation view
        TVProfile = (TextView) findViewById(R.id.textview_profile);
        TVSettings = (TextView) findViewById(R.id.textview_settings);
        TVPhone = (TextView) findViewById(R.id.textview_phone);


        if(imgpath!=null){//if imgpath from device transferred on intent
            bitmapProfile = BitmapFactory.decodeFile(imgpath);
            profileImg.setImageBitmap(bitmapProfile);


        }


        else{
           // profileImg.setImageBitmap(R.drawable.usericon);
            handler = new Handler(Looper.getMainLooper());
            new Thread(){
                @Override
                public void run() {
                    PermissionManager.check(ChatListAc.this, android.Manifest.permission.READ_EXTERNAL_STORAGE,1);
                    PermissionManager.check(ChatListAc.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE,1);
                    final String img = BC.AddPhotoFromBackendless((String)CurrentUser.getProperty(BackendLessHelper.pImg),CurrentUser.getEmail(),true);

                    bitmapProfile = BitmapFactory.decodeFile(img);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(bitmapProfile!=null){
                                profileImg.setImageBitmap(bitmapProfile);
                                prefs.edit().putString(userid,img).apply();}
                        }
                    });
                    super.run();
                }
            }.start();
        }
        getcontacts();


       BottomNavigationView bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener(){
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.profileItem:
                        TVPhone.setVisibility(View.GONE);
                        TVSettings.setVisibility(View.GONE);
                        TVProfile.setVisibility(View.GONE);
                        Intent intent1 = new Intent(getBaseContext(), ProfilePage.class);

                        startActivity(intent1);
                        finish();
                        break;
                    case R.id.addPhone:
                        TVPhone.setVisibility(View.GONE);
                        TVSettings.setVisibility(View.GONE);
                        TVProfile.setVisibility(View.GONE);
                        AddDialog(view);


                        break;
                    case R.id.btnSettings:
                        TVPhone.setVisibility(View.GONE);
                        TVSettings.setVisibility(View.GONE);
                        TVProfile.setVisibility(View.GONE);
                        Intent intent2 = new Intent(getBaseContext(), SettingsAc.class);
                        startActivity(intent2);
                        finish();
                        break;
                }
                return true;
            }
            });
        }


    @Override
    protected void onStart() {
        super.onStart();

    }

    public void AddDialog(View view) {
        AlertDialog.Builder ag= new AlertDialog.Builder(this);
        final EditText ET=new EditText(this);
        ET.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        ag.setTitle("Add Friend").setMessage("Please Enter Your Friend Email Address").setView(ET).setNegativeButton("Cancel",null).setPositiveButton("Add Friend", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String friend=((EditText)ET).getText().toString();
                if(BC.AddFriend(friend)==new boolean[]{false,true}){//need to set the equal right
                    LoginAc.ToastMSG(ChatListAc.this,"Failed to Add "+friend);
                }else {onStart();}
            }
        }).show();
        onRestart();

    }

    public void getcontacts(){
        String query = "SELECT * FROM "+ ChatMembers;
        Cursor cursor = db.rawQuery(query, null);
        lv.setAdapter(new ChatlistAdapter(this,cursor));

    }

    private void camera() {
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//implicit Intent - for image picker from camera
        generateImageFile();//create File object
        i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(ImgFile));//pass Uri to File object - for storage
        startActivityForResult(i, RegisterAc.CAMERA);//go to camera
    }

    private void gallery(){
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);//implicit Intent - for image picker from gallery
        startActivityForResult(i,RegisterAc.GALLERY);//open gallery to pick image
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
            return cursor.getString(idImg);
        }}


    public void onImgChatList (View view) {
        PermissionManager.check(ChatListAc.this, android.Manifest.permission.READ_EXTERNAL_STORAGE,RegisterAc.GALLERY);
        PermissionManager.check(ChatListAc.this, android.Manifest.permission.CAMERA,RegisterAc.CAMERA);
        PermissionManager.check(ChatListAc.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE,RegisterAc.WRITE_STORAGE);

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
                case RegisterAc.GALLERY:
                    {
                    imgpath=getRealPathFromUrl(i.getData());
                    ImgFile = new File(imgpath);
                    bm = BitmapFactory.decodeFile(imgpath);
                    profileImg.setImageBitmap(bm);
                    saveImgBacknless();
                   }
                case RegisterAc.CAMERA:
                    imgpath = ImgFile.getAbsolutePath();
                    bm = BitmapFactory.decodeFile(imgpath);
                    //getCroppedBitmap(bm);

                    Bitmap circleBitmap = Bitmap.createBitmap(bm
                            .getWidth(), bm.getHeight(), Bitmap.Config.ARGB_8888);

                    BitmapShader shader = new BitmapShader(bm,  Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                    Paint paint = new Paint();
                    paint.setShader(shader);

                    Canvas c = new Canvas(circleBitmap);
                    c.drawCircle(bm.getWidth()/2, bm.getHeight()/2, bm.getWidth()/2, paint);


                    profileImg.setImageBitmap(circleBitmap);
                    if (circleBitmap!=null){
                        Log.e("oeoeoeo","otmtmtmrr");
                    }
                    saveImgBacknless();

            }}
    }

    public void saveImgBacknless (){
        String [] str = imgpath.split("/");
        final String ostr = str[str.length - 1];
        Backendless.Files.Android.upload(bm, Bitmap.CompressFormat.PNG, 100, ostr, "IMG/"+CurrentUser.getEmail(), false, new AsyncCallback<BackendlessFile>() {
            @Override
            public void handleResponse(BackendlessFile backendlessFile) {
                CurrentUser.setProperty(BackendLessHelper.pImg,"IMG/"+CurrentUser.getEmail()+"/"+ostr);
                Backendless.Persistence.of(BackendlessUser.class).save(CurrentUser, new AsyncCallback<BackendlessUser>() {
                    @Override
                    public void handleResponse(BackendlessUser backendlessUser) {

                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(userid,imgpath);
                        editor.commit();
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


    public void btnSettings(View view) {
        Intent i = new Intent(this, SettingsAc.class);
        i.putExtra(userid, userid);
        startActivity(i);

    }


    public void addTableUser(){

        db.execSQL("CREATE TABLE IF NOT EXISTS "+DBHelper.ChatWindow+"_"+userid+" ('_id' INTEGER PRIMARY KEY AUTOINCREMENT , `name` VARCHAR," +
                " `UserID` INTEGER NOT NULL,`message` VARCHAR,`image` VARCHAR, `Time` TIMESTAMP DEFAULT CURRENT_TIMESTAM)");

    }

}



