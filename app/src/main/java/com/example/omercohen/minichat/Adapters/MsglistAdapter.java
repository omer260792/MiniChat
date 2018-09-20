package com.example.omercohen.minichat.Adapters;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.DateFormat;
import android.os.Environment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import com.backendless.BackendlessCollection;
import com.backendless.BackendlessUser;
import com.backendless.persistence.BackendlessDataQuery;
import com.example.omercohen.minichat.R;
import com.example.omercohen.minichat.DBHelper;
import com.example.omercohen.minichat.Chat_Activity;
import com.example.omercohen.minichat.BackendLessHelper;

import me.himanshusoni.chatmessageview.ChatMessageView;

import static android.R.attr.name;


/**
 * Created by Ido Talmor on 15/07/2017.
 */

public class MsglistAdapter extends CursorAdapter {
    final Context context;
    int currentid, SENDER_ID = 0;
    SQLiteDatabase db;
    String v   ="storage/emulated/0/Android/data/com.example.omercohen.minichat/files/Pictures/";
    Bitmap bm;
    String nameUser = "";
    String nameUser1 = "";
    String uemail;
    private ChatMessageView mChatMessageView;
    Chat_Activity chat_activity;
    ImageView imageView;

    public MsglistAdapter(Context context, Cursor c){
        super(context,c);
        this.context=context;




        db = DBHelper.GetDBHelper(context).getWritableDatabase();

        getNameSender(nameUser1);

    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.message_layout, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        RelativeLayout relativelayout=(RelativeLayout)view.findViewById(R.id.relative_msg);
        mChatMessageView=(ChatMessageView)view.findViewById(R.id.relativemsglayout);
        RelativeLayout relativeLayoutTime = (RelativeLayout)view.findViewById(R.id.relativeLayoutTime);

        //RelativeLayout relativelayout1=(RelativeLayout)view.findViewById(R.id.imageLayout);
        imageView = (ImageView)view.findViewById(R.id.imgViewCamera);

       // ImageView imageView = (ImageView)view.findViewById(R.id.imgViewCamera);



        TextView username = (TextView)view.findViewById(R.id.userttlmsgtextview);
        TextView lastmsg = (TextView)view.findViewById(R.id.msgtextview);
        TextView msgtime = (TextView) view.findViewById(R.id.msgtimewindow);
        ImageView img = (ImageView)view.findViewById(R.id.msgimg);

        final int id = cursor.getInt(cursor.getColumnIndexOrThrow("_id")); //1
        String name=cursor.getString(cursor.getColumnIndexOrThrow("name")); // dan
        String msg=cursor.getString(cursor.getColumnIndexOrThrow("message"));
        String image=cursor.getString(cursor.getColumnIndexOrThrow("image"));
        String utime=cursor.getString(cursor.getColumnIndexOrThrow("Time"));
        final int UserID = cursor.getInt(cursor.getColumnIndexOrThrow(BackendLessHelper.pUserID)); //47



        if(nameUser1 == name){
            mChatMessageView.setBackgroundResource(R.drawable.msgbackuser);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mChatMessageView.getLayoutParams();
            RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams)relativeLayoutTime.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params1.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            mChatMessageView.setLayoutParams(params);

            //maybe need to change back to left when from user.
          }else {
            mChatMessageView.setBackgroundResource(R.drawable.rounded_corner);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mChatMessageView.getLayoutParams();
            RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams)relativeLayoutTime.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT );
            params1.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            mChatMessageView.setLayoutParams(params);
            mChatMessageView.setArrowPosition(ChatMessageView.ArrowPosition.LEFT);

            if (Chat_Activity.camBool == 2) {
                RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams)imageView.getLayoutParams();
                params2.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                params.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                params1.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                imageView.setLayoutParams(params2);
                imageView.setImageBitmap(Chat_Activity.bm);
                Chat_Activity.camBool = 1;

            }
        }
          //maybe need to change back to left when from user.

        username.setText(name);
        lastmsg.setText(msg);





        Date date = new Date(Long.valueOf(utime)); // *1000 is to convert seconds to milliseconds
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+3")); // give a timezone reference for formating (see comment at the bottom
        String formattedDate = sdf.format(date);
        msgtime.setText(formattedDate);
        if("IMG/DefaultIcon.png".equals(image)){img.setImageResource(R.drawable.usericon);}
        else{
            //userImg.setImageResource(R.drawable.usericon);
            String [] str = image.split("/");
            final String ostr = str[str.length - 1];

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 3;

            bm = BitmapFactory.decodeFile(v + ostr);
            if(bm !=null){
                img.setImageBitmap(bm);
            }else {
                uemail = (String)Backendless.UserService.CurrentUser().getProperty(BackendLessHelper.pmail);
                String Directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)+"/IMG/"+uemail;
                bm = BitmapFactory.decodeFile(Directory+"/Profile.jpg",options);
                img.setImageBitmap(bm);//create directory for images

            }
        }




        relativelayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder alrt = new AlertDialog.Builder(context);
                alrt.setTitle("Do You Want To Delete This Message?");
                alrt.setNegativeButton("No",null);
                alrt.setPositiveButton("Yes!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String query = "UPDATE "+DBHelper.ChatWindow+"_"+UserID+" SET message = null WHERE _id = '"+id+"'";
                        db.execSQL(query);
                        query = "UPDATE "+DBHelper.ChatWindow+"_"+UserID+" SET image = null WHERE _id = '"+id+"'";
                        db.execSQL(query);
                        ((Chat_Activity)context).getmsg();
                    }
                });
                alrt.show();

                return false;
            }
        });


    }


    public String getNameSender (final String name ){
        Thread checkUser =new Thread(){
            @Override
            public void run() {
                super.run();

                Integer currentuser = (Integer) Backendless.UserService.CurrentUser().getProperty(BackendLessHelper.pUserID);//Logged In UserID

                final Map map = new HashMap();//the user I want to add



                String whereClause = "UserID = '"+currentuser+"'";
                final BackendlessDataQuery dataQuery = new BackendlessDataQuery();
                dataQuery.setWhereClause( whereClause );


                BackendlessCollection<BackendlessUser> resultclBCUser1 = Backendless.Persistence.of(BackendlessUser.class ).find( dataQuery );
                if(resultclBCUser1.getCurrentPage().isEmpty()){
                    return;

                }else {


                    BackendlessUser BCUser = resultclBCUser1.getCurrentPage().get(0);

                    map.put(nameUser,BCUser.getProperty(BackendLessHelper.pname));



                    nameUser1 = (String) map.get(nameUser);


                }
            }
        };
        checkUser.start();
        return nameUser1;

    }

    public static Boolean CheckImage (Boolean Bool){



        return Bool;

    }


}
