package com.example.omercohen.minichat.Adapters;

import android.content.Context;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


import com.backendless.Backendless;
import com.example.omercohen.minichat.DBHelper;
import com.example.omercohen.minichat.PermissionManager;
import com.example.omercohen.minichat.R;
import com.example.omercohen.minichat.Chat_Activity;
import com.example.omercohen.minichat.BackendLessHelper;

import static android.R.attr.name;

/**
 * Created by Ido Talmor on 15/07/2017.
 */

public class ChatlistAdapter extends CursorAdapter {
    final Context context;
    Bitmap bm;
    public SQLiteDatabase db;
    int UserID;
    String uttl, uemail;
    String uimg;
    AlertDialog alertDialog;
    String v   ="storage/emulated/0/Android/data/com.example.omercohen.minichat/files/Pictures/";
    String dcim   ="storage/emulated/0/DCIM/IMG/";
    public ChatlistAdapter(Context context,Cursor c){
        super(context,c);
        this.context=context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.member_object, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        RelativeLayout relativelayout=(RelativeLayout)view.findViewById(R.id.relative_layout);
        ImageView userImg = (ImageView)view.findViewById(R.id.userImage);
        final TextView userttl = (TextView)view.findViewById(R.id.userttl);
        TextView lastmsg = (TextView)view.findViewById(R.id.lastmsg);
        TextView msgtime = (TextView) view.findViewById(R.id.msgtime);

        uimg=cursor.getString(cursor.getColumnIndexOrThrow(BackendLessHelper.pImg));
        uttl=cursor.getString(cursor.getColumnIndexOrThrow(BackendLessHelper.pname));
        String umsg=cursor.getString(cursor.getColumnIndexOrThrow("lastmsg"));
        final String utime=cursor.getString(cursor.getColumnIndexOrThrow("Time"));
        UserID = cursor.getInt(cursor.getColumnIndexOrThrow("UserID"));
        uemail = cursor.getString(cursor.getColumnIndexOrThrow("email"));


        if("IMG/DefaultIcon.png".equals(uimg)){userImg.setImageResource(R.drawable.usericon);}
        else{
            String [] str = uimg.split("/");
            final String ostr = str[str.length - 1];

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 3;

            bm = BitmapFactory.decodeFile(v + ostr,options);
            if(bm !=null)userImg.setImageBitmap(bm);
            else {

                bm = BitmapFactory.decodeFile(dcim+uemail+"/Profile.jpg",options);
                userImg.setImageBitmap(bm);

            }
        }
        userttl.setText(uttl);
        lastmsg.setText(umsg);

        Date date = new Date(Long.valueOf(utime)); // *1000 is to convert seconds to milliseconds
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z"); // the format of your date
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+3")); // give a timezone reference for formating (see comment at the bottom
        String formattedDate = sdf.format(date);
        msgtime.setText(formattedDate);
        relativelayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer currentuser= (Integer) Backendless.UserService.CurrentUser().getProperty(BackendLessHelper.pUserID);//Logged In UserID
                String omer = userttl.getText().toString();
                Intent i=new Intent(context,Chat_Activity.class);
                i.putExtra("name",omer);
                i.putExtra("UserID",UserID);
                i.putExtra("SenderID",currentuser);
                context.startActivity(i);
            }
        });
        relativelayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                contactDeletBtn(view);
                return false;
            }
        });

    }
    public void contactDeletBtn(View view) {

        AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        alertDialog.setTitle("Contact");

        alertDialog.setMessage("Do you want to delete a contact");

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "cancel ", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                return;
            }
        });

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Delete contact", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {

            }
        });

        alertDialog.show();


    }

}
