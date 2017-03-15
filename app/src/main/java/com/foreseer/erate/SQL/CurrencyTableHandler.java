package com.foreseer.erate.SQL;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Button;

import com.foreseer.erate.Currency.AbstractCurrency;
import com.foreseer.erate.MainActivity;
import com.foreseer.erate.R;
import com.foreseer.erate.RateChecker;

import java.util.HashMap;
import java.util.Map;

/**
 * Handler for currency table in DB, providing basic functions for interaction with that table.
 */

public class CurrencyTableHandler {
    //Singleton class
    private static CurrencyTableHandler instance;

    //Writable DB
    private SQLiteDatabase dbWritable;

    //Current RateChecker
    private RateChecker rateChecker;

    //Main activity of the app
    private MainActivity activity;

    //Variables for updating, preventing certain elements of program from working while the rates are being updated.
    private boolean startedUpdate = false;
    private boolean finished = true;

    private CurrencyTableHandler(Context context, SQLiteDatabase database, MainActivity activity, boolean updateManually) {
        this.dbWritable = database;
        this.activity = activity;
    }

    /**
     * This method is called on startup of the program.
     * @param activity          Main activity
     * @param updateManually    Whether rate update should be performed regardless of current state of the database.
     *                          Normally the program won't re-update the rates unless they were never updated.
     */
    public void startup(MainActivity activity, boolean updateManually) {
        if (updateManually) {
            rateChecker = new RateChecker(false);
            updateRates();
        }

        if (!isReady()) {
            rateChecker = new RateChecker(false);
            updateRates();
        } else {
            activity.updateLastUpdateTime(getLastUpdateTime());
            activity.updateExistingFragments();
        }

}

    /**
     * This method returns True if DB is ready for usage - it's not currently being updated AND
     * it's not empty.
     *
     * @return true if DB is ready
     */
    public boolean isReady() {
        if (!startedUpdate) {
            return getLastUpdateTime() != 0;
        } else {
            return finished && getLastUpdateTime() != 0;
        }
    }


    /**
     * This method pulls actual rates from ratechecker if it's done and starts updating the DB.
     * @param rateChecker
     */
    public void updateRates(RateChecker rateChecker){
        if (rateChecker != null){
            if (rateChecker.isDone()){
                this.rateChecker = rateChecker;
                updateRates();
            }
        }
    }

    /**
     * Singleton instance factory
     */
    public static CurrencyTableHandler getInstance(Context context, SQLiteDatabase database, MainActivity activity, boolean updateManually) {
        if (instance != null) {
            return instance;
        } else {
            instance = new CurrencyTableHandler(context, database, activity, updateManually);
            return instance;
        }
    }

    /**
     * Singleton instance factory
     */
    public static CurrencyTableHandler getInstance(){
        if (instance != null){
            return instance;
        }
        return null;
    }

    /**
     * This method returns exchange rate from first currency to second one.
     * @param baseCurrency  First currency
     * @param endCurrency   Second currency
     * @return              Exchange rate
     */
    public double getExchangeRate(AbstractCurrency baseCurrency, AbstractCurrency endCurrency) {
        String query = "SELECT * FROM " + CurrencyTableModel.TABLE_NAME + " WHERE " + CurrencyTableModel.COLUMN_FIRST_CURRENCY +
                " = '" + baseCurrency.getCurrencyCode() + "'" + " AND " + CurrencyTableModel.COLUMN_SECOND_CURRENCY + " = '" + endCurrency.getCurrencyCode() + "'";
        Cursor cursor = dbWritable.rawQuery(query, null);

        double rate = 0;
        if (cursor.moveToNext()) {
            rate = cursor.getDouble(cursor.getColumnIndexOrThrow(CurrencyTableModel.COLUMN_RATE));
        }
        cursor.close();
        return rate;
    }

    /**
     * @return Last time when rates were updated
     */
    private long getLastUpdateTime() {
        Cursor cursor = dbWritable.rawQuery("SELECT * FROM " + CurrencyTableModel.TABLE_NAME + " ORDER BY ROWID ASC LIMIT 1", null);
        long result = 0;
        if (cursor.moveToNext()) {
            result = cursor.getLong(cursor.getColumnIndexOrThrow(CurrencyTableModel.COLUMN_TIME));
        }
        cursor.close();
        return result;
    }

    /**
     * This method starts updating rates.
     */
    private void updateRates() {
        startedUpdate = true;
        finished = false;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (!rateChecker.isDone()) {
                        try {
                            Thread.sleep(150);
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

    /**
     * When rates are ready for updating in the DB, updates all rows.
     * In case if the table is empty, inserts rows into the table.
     * @param rates  HashMap with exchange rates.
     */
    private void updateRates(HashMap<String, Double> rates) {
        long currentTime = System.currentTimeMillis();

        //dbWritable.execSQL("DELETE FROM " + CurrencyTableModel.TABLE_NAME);
        boolean cleanDB = getLastUpdateTime() == 0;
        for (Map.Entry<String, Double> rate : rates.entrySet()) {
            String firstCurrency = rate.getKey().substring(0, 3);
            String secondCurrency = rate.getKey().substring(3);
            Double exchangeRate = rate.getValue();

            ContentValues values = new ContentValues();
            if (cleanDB) {
                values.put(CurrencyTableModel.COLUMN_FIRST_CURRENCY, firstCurrency);
                values.put(CurrencyTableModel.COLUMN_SECOND_CURRENCY, secondCurrency);
                values.put(CurrencyTableModel.COLUMN_RATE, exchangeRate);
                values.put(CurrencyTableModel.COLUMN_TIME, currentTime);
                long newRowId = dbWritable.insert(CurrencyTableModel.TABLE_NAME, null, values);
            } else {
                values.put(CurrencyTableModel.COLUMN_RATE, exchangeRate);
                values.put(CurrencyTableModel.COLUMN_TIME, currentTime);
                dbWritable.update(CurrencyTableModel.TABLE_NAME, values, CurrencyTableModel.COLUMN_FIRST_CURRENCY+"='"+firstCurrency+"'"+" AND " +
                        CurrencyTableModel.COLUMN_SECOND_CURRENCY + "='" + secondCurrency+"'", null);
            }

        }
        finished = true;

        activity.onRatesUpdateFinished();
        activity.updateLastUpdateTime(getLastUpdateTime());
        activity.updateExistingFragments();
    }
}
