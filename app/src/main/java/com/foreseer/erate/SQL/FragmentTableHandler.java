package com.foreseer.erate.SQL;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.foreseer.erate.Currency.CurrencyHelper;
import com.foreseer.erate.Fragments.AbstractRateFragment;

import java.util.ArrayList;

/**
 * Handler for fragment table in DB, providing basic functions for interaction with that table.
 */

public class FragmentTableHandler {

    //This is a singleton
    private static FragmentTableHandler instance;

    //Writable DB
    private SQLiteDatabase dbWritable;

    private FragmentTableHandler(Context context) {
        dbWritable = new CurrentTableHelper(context).getWritableDatabase();
    }

    /**
     * This method returns singleton instance of handler
     * @param context Context of the application
     * @return        Instance of itself or make a new one
     */
    public static FragmentTableHandler getInstance (Context context){
       if (instance != null){
           return instance;
       } else {
           instance = new FragmentTableHandler(context);
           return instance;
       }
    }

    /**
     * This method creates a new fragment in the database and returns its row id.
     * @param firstCurrency     First currency of the fragment
     * @param secondCurrency    Second currency of the fragment
     * @return                  Row id in the table
     */
    public long createFragment(String firstCurrency, String secondCurrency){
        ContentValues values = new ContentValues();
        values.put(FragmentTableModel.COLUMN_FIRST_CURRENCY, firstCurrency);
        values.put(FragmentTableModel.COLUMN_SECOND_CURRENCY, secondCurrency);

        return dbWritable.insert(FragmentTableModel.TABLE_NAME, null, values);
    }

    /**
     * This method removes a row with specified ID.
     * @param id    Row id
     */
    public void removeFragment(int id){
        dbWritable.delete(FragmentTableModel.TABLE_NAME, FragmentTableModel.COLUMN_ID + " = " + id, null);
    }

    /**
     * This method swaps order of currencies in the table for the specified fragment
     * @param id    Row id
     */
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
            cursor.close();
            return;
        }
        cursor.close();

        dbWritable.execSQL("UPDATE " + FragmentTableModel.TABLE_NAME + " SET " + FragmentTableModel.COLUMN_FIRST_CURRENCY + " = '" + secondCurrency + "', " +
        FragmentTableModel.COLUMN_SECOND_CURRENCY + " = '" + firstCurrency + "' WHERE id = " + id);
    }

    /**
     * Gives the latest SQL row id of the fragment.
     * @return  Max SQL row id
     */
    public int getCount(){
        Cursor cursor = dbWritable.rawQuery("SELECT MAX(" + FragmentTableModel.COLUMN_ID + ") FROM " + FragmentTableModel.TABLE_NAME, null);
        String[] results = cursor.getColumnNames();
        int count = 0;

        if (cursor.moveToNext()) {
            count = cursor.getInt(0);
        }
        cursor.close();

        return count;
    }

    /**
     * This method returns of list of existing fragments from the database.
     * @return List of existing fragments.
     */
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
