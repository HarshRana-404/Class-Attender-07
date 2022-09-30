package com.ca.classattender;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {
    Context cn;
    public DBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, "std", factory, version);
        cn = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS std_details (s_email VARCHAR(50), s_enr VARCHAR(15), s_name VARCHAR(70), s_dept VARCHAR(5))");
        db.execSQL("CREATE TABLE IF NOT EXISTS recent_tb (rec int(2))");
    }

    public void storeStudentDetails(String ema, String enr, String nm, String dpt){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM std_details");
        db.execSQL("INSERT INTO std_details VALUES('"+ema+"', '"+enr+"', '"+nm+"', '"+dpt+"')");
    }

    public Cursor getStudentDetails(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor csr;
        csr = db.rawQuery("SELECT * FROM std_details", null);
        return csr;
    }
    public Boolean isUserRecentRegistered(){
        SQLiteDatabase db = getWritableDatabase();
        Cursor csr;
        csr = db.rawQuery("SELECT * FROM std_details", null);
        int n=-1;
        while(csr.moveToNext()){
            n = csr.getInt(0);
        }
        if(n==0){
            return false;
        }
        return true;
    }
    public void writeUserRecentRegister(int n){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM recent_tb");
        db.execSQL("INSERT INTO recent_tb VALUES("+n+")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {}
}
