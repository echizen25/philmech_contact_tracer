package com.example.philmechlocator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Date;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "DtrDBase";
    //Initialize Database name and Table Name

    DatabaseHelper(Context context){
        super(context,DATABASE_NAME,null,1);

    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        //create Table
        db.execSQL("CREATE TABLE location_logs(loc_id int,"+
                " memcode TEXT," +
                "log_date_time datetime , " +
                "log_out_date_time datetime," +
                "[specify_location] TEXT, " +
                "[current_status] TEXT, " +
                "[status] TINYINT " +
                ")");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS location_logs");
        onCreate(db);
    }
    public boolean adduserdata(String memcode, Date log_date_time,
                               String specify_location, String current_status,int stat){
        //get writable db
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        //Create Contentvalues
        ContentValues contentValues = new ContentValues();
        contentValues.put("memcode" , memcode);
        contentValues.put("log_date_time" , log_date_time.toString());
        contentValues.put("specify_location", specify_location);
        contentValues.put("current_status", current_status);
        contentValues.put("stat", stat);
        long result = sqLiteDatabase.insert("location_logs",null,contentValues);
        if (result == 1){
            return false;
        }
        else {
            return true;
        }
    }
    public boolean updateuserdata(String memcode,String log_out_date_time,
                                  String current_status) {
        //get writable db
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        //Create Contentvalues
        ContentValues contentValues = new ContentValues();

        contentValues.put("log_out_date_time", log_out_date_time.toString());
        contentValues.put("current_status", current_status);
        long result = sqLiteDatabase.update("location_logs", contentValues, "memcode=? AND current_status = 'True' ", new String[]{memcode} );
        if (result == 1) {
            return false;
        } else {
            return true;
        }


    }



}
