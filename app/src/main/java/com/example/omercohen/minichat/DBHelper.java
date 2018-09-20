package com.example.omercohen.minichat;



import android.content.Context;
import android.database.sqlite.SQLiteClosable;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


import com.backendless.Backendless;



public class DBHelper extends SQLiteOpenHelper { //Single Tone design Pattern

    private static DBHelper dbHelper;
    public static String ChatMembers="ChatMembers",ChatWindow="ChatWindow";

    public DBHelper(Context context) {
        super(context, Backendless.UserService.CurrentUser().getEmail(),null,1);
    }

    public static DBHelper GetDBHelper(Context context){
        if (dbHelper == null) {
            dbHelper=new DBHelper(context);
        }
        return dbHelper;
    }

    public static void Terminate(){
        dbHelper = null;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS "+ChatMembers+" ('_id' INTEGER PRIMARY KEY AUTOINCREMENT, `email` VARCHAR, `name` VARCHAR NOT NULL," +
                " `UserID` INTEGER NOT NULL, `UserImg` VARCHAR NOT NULL, `lastmsg` VARCHAR,`unread` INTEGER, `Time` TIMESTAMP DEFAULT CURRENT_TIMESTAM)");//can add un read num of msg

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static void deleteName(SQLiteDatabase db, int id, String name){

        String query = "DELETE FROM " + ChatMembers + " WHERE "
                + "_id" + " = '" + id + "'" +
                " AND " + BackendLessHelper.pname + " = '" + name + "'";
        db.execSQL(query);
    }




}
