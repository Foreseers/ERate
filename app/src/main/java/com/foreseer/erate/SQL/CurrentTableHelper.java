package com.foreseer.erate.SQL;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Helper for the DB, used by handler classes.
 */

public class CurrentTableHelper extends SQLiteOpenHelper {

    public CurrentTableHelper(Context context){
        super(context, Database.DATABASE_NAME, null, CurrencyTableModel.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CurrencyTableModel.QUERY_CREATE_TABLE);
        db.execSQL(FragmentTableModel.QUERY_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(CurrencyTableModel.QUERY_DELETE_TABLE);
        db.execSQL(FragmentTableModel.QUERY_DELETE_TABLE);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
