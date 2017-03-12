package com.foreseer.erate.SQL;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.foreseer.erate.Currency.CurrencyHelper;
import com.foreseer.erate.Fragments.AbstractRateFragment;

import java.util.ArrayList;

/**
 * Created by Foreseer on 11/03/2017.
 */

public class FragmentTableHandler {

    private CurrentTableHelper helper;
    private static FragmentTableHandler instance;

    private SQLiteDatabase dbWritable;

    private FragmentTableHandler(Context context) {
        helper = new CurrentTableHelper(context);

        dbWritable = helper.getWritableDatabase();
    }

    public static FragmentTableHandler getInstance (Context context){
       if (instance != null){
           return instance;
       } else {
           instance = new FragmentTableHandler(context);
           return instance;
       }
    }

    public long createFragment(String firstCurrency, String secondCurrency){
        ContentValues values = new ContentValues();
        values.put(FragmentTableModel.COLUMN_FIRST_CURRENCY, firstCurrency);
        values.put(FragmentTableModel.COLUMN_SECOND_CURRENCY, secondCurrency);

        return dbWritable.insert(FragmentTableModel.TABLE_NAME, null, values);
    }

    public void removeFragment(int id){
        dbWritable.delete(FragmentTableModel.TABLE_NAME, FragmentTableModel.COLUMN_ID + " = " + id, null);
    }

    public void swapFragment(int id){
        String selectFragmentQuery = "SELECT * FROM " + FragmentTableModel.TABLE_NAME + " WHERE " + FragmentTableModel.COLUMN_ID +
                " = " + id;
        Cursor cursor = dbWritable.rawQuery(selectFragmentQuery, null);

        String firstCurrency = "";
        String secondCurrency = "";
        while (cursor.moveToNext()){
            firstCurrency = cursor.getString(cursor.getColumnIndexOrThrow(FragmentTableModel.COLUMN_FIRST_CURRENCY));
            secondCurrency = cursor.getString(cursor.getColumnIndexOrThrow(FragmentTableModel.COLUMN_SECOND_CURRENCY));
        }
        if (firstCurrency.equals("") || secondCurrency.equals("")){
            return;
        }

        dbWritable.execSQL("UPDATE " + FragmentTableModel.TABLE_NAME + " SET " + FragmentTableModel.COLUMN_FIRST_CURRENCY + " = '" + secondCurrency + "', " +
        FragmentTableModel.COLUMN_SECOND_CURRENCY + " = '" + firstCurrency + "' WHERE id = " + id);
    }

    public int getCount(){
        Cursor cursor = dbWritable.rawQuery("SELECT MAX(" + FragmentTableModel.COLUMN_ID + ") FROM " + FragmentTableModel.TABLE_NAME, null);
        String[] results = cursor.getColumnNames();
        int count = 0;

        if (cursor.moveToNext()) {
            try {
                count = cursor.getInt(0);
            } finally {
                cursor.close();
            }
            return count;
        }
        return 0;
    }

    public ArrayList<AbstractRateFragment> getExistingFragments(){
        Cursor cursor = dbWritable.rawQuery("SELECT * FROM " + FragmentTableModel.TABLE_NAME, null);
        ArrayList<AbstractRateFragment> fragments = new ArrayList<>();
        while (cursor.moveToNext()){
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(FragmentTableModel.COLUMN_ID));
            String firstCurrency = cursor.getString(cursor.getColumnIndexOrThrow(FragmentTableModel.COLUMN_FIRST_CURRENCY));
            String secondCurrency = cursor.getString(cursor.getColumnIndexOrThrow(FragmentTableModel.COLUMN_SECOND_CURRENCY));

            fragments.add(new AbstractRateFragment(id, CurrencyHelper.getCurrency(firstCurrency), CurrencyHelper.getCurrency(secondCurrency)));
        }
        cursor.close();
        if (fragments.size() > 0){
            return fragments;
        } else {
            return null;
        }
    }

    public String[] getColumnNames(){
        return null;
    }

    public SQLiteDatabase getDbWritable() {
        return dbWritable;
    }

    public void close(){
        dbWritable.close();
    }
}
