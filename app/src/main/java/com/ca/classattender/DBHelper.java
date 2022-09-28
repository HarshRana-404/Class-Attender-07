package com.ca.classattender;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {
    public DBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, "std", factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS std_details (s_email VARCHAR(50), s_enr VARCHAR(15), s_name VARCHAR(70), s_dept VARCHAR(5))");
    }

    public void storeStudentDetails(String ema, String enr, String nm, String dpt){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO std_details VALUES('"+ema+"', '"+enr+"', '"+nm+"', '"+dpt+"')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {}
}
