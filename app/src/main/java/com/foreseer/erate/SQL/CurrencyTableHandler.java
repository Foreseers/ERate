package com.foreseer.erate.SQL;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.foreseer.erate.Currency.AbstractCurrency;
import com.foreseer.erate.MainActivity;
import com.foreseer.erate.RateChecker;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Foreseer on 12/03/2017.
 */

public class CurrencyTableHandler {
    private static CurrencyTableHandler instance;

    private SQLiteDatabase dbWritable;
    private RateChecker rateChecker;

    private MainActivity activity;

    private CurrencyTableHandler(Context context, SQLiteDatabase database, MainActivity activity, boolean updateManually){
        this.dbWritable = database;
        this.activity = activity;
    }

    public void startup(MainActivity activity, boolean updateManually){
        if (updateManually) {
            rateChecker = new RateChecker();
            updateRates();
        } else {
            if (!isReady()){
                rateChecker = new RateChecker();
                updateRates();
            } else {
                activity.updateExistingFragments();
            }
        }
    }

    public boolean isReady(){
        if (getLastUpdateTime() == 0){
            return false;
        } else {
            return true;
        }
    }

    public static CurrencyTableHandler getInstance (Context context, SQLiteDatabase database, MainActivity activity, boolean updateManually){
        if (instance != null){
            return instance;
        } else {
            instance = new CurrencyTableHandler(context, database, activity, updateManually);
            return instance;
        }
    }

    public double getExchangeRate(AbstractCurrency baseCurrency, AbstractCurrency endCurrency){
        String query = "SELECT * FROM " + CurrencyTableModel.TABLE_NAME + " WHERE " + CurrencyTableModel.COLUMN_FIRST_CURRENCY +
                " = '" + baseCurrency.getCurrencyCode() + "'" + " AND " + CurrencyTableModel.COLUMN_SECOND_CURRENCY + " = '" + endCurrency.getCurrencyCode() + "'";
        Cursor cursor = dbWritable.rawQuery(query, null);

        while (cursor.moveToNext()){
            double rate = cursor.getDouble(cursor.getColumnIndexOrThrow(CurrencyTableModel.COLUMN_RATE));

            cursor.close();
            return rate;
        }
        cursor.close();
        return 0;
    }

    public long getLastUpdateTime(){
        Cursor cursor = dbWritable.rawQuery("SELECT * FROM " + CurrencyTableModel.TABLE_NAME + " ORDER BY ROWID ASC LIMIT 1", null);

        while (cursor.moveToNext()){
            long result = cursor.getLong(cursor.getColumnIndexOrThrow(CurrencyTableModel.COLUMN_TIME));
            cursor.close();
            return result;
        }
        cursor.close();
        return 0;
    }

    private void updateRates(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (!rateChecker.isDone()) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (rateChecker.isDone()) {
                        break;
                    }
                }
                updateRates(rateChecker.getRates());
            }
        });

        thread.start();
    }

    private void updateRates(HashMap<String, Double> rates){
        long currentTime = System.currentTimeMillis();

        for (Map.Entry<String, Double> rate : rates.entrySet()){
            String firstCurrency = rate.getKey().substring(0, 3);
            String secondCurrency = rate.getKey().substring(3);
            Double exchangeRate = rate.getValue();

            ContentValues values = new ContentValues();
            values.put(CurrencyTableModel.COLUMN_FIRST_CURRENCY, firstCurrency);
            values.put(CurrencyTableModel.COLUMN_SECOND_CURRENCY, secondCurrency);
            values.put(CurrencyTableModel.COLUMN_RATE, exchangeRate);
            values.put(CurrencyTableModel.COLUMN_TIME, currentTime);

            long newRowId = dbWritable.insert(CurrencyTableModel.TABLE_NAME, null, values);
        }

        activity.updateExistingFragments();
    }
}
