package com.foreseer.erate.SQL;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.foreseer.erate.Currency.AbstractCurrency;
import com.foreseer.erate.Activities.MainActivity;
import com.foreseer.erate.RatesUtils.CachedExchangeRateStorage;
import com.foreseer.erate.RatesUtils.RateChecker;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

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

    private CurrencyTableHandler(SQLiteDatabase database, MainActivity activity) {
        this.dbWritable = database;
        this.activity = activity;
        if (getLastUpdateTime() != 0){
            CachedExchangeRateStorage cacheStorage = CachedExchangeRateStorage.getInstance();
            cacheStorage.isSynchronized();
        }
    }

    /**
     * This method is called on startup of the program.
     * @param activity          Main activity
     * @param updateManually    Whether rate update should be performed regardless of current state of the database.
     *                          Normally the program won't re-update the rates unless they were never updated.
     */
    public void startup(MainActivity activity, boolean updateManually) {
        if (updateManually) {
            rateChecker = RateChecker.newInstance(activity);
        }

        if (!isReady()) {
            rateChecker = RateChecker.newInstance(activity);
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
     * Singleton instance factory
     */
    @NonNull
    public static CurrencyTableHandler getInstance(SQLiteDatabase database, MainActivity activity) {
        if (instance != null) {
            return instance;
        } else {
            instance = new CurrencyTableHandler(database, activity);
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

    public Map<String, Double> getAllRates(){
        String query = "SELECT * FROM " + CurrencyTableModel.TABLE_NAME;
        Cursor cursor = dbWritable.rawQuery(query, null);

        Map<String, Double> rates = new HashMap<>();

        while (cursor.moveToNext()){
            String firstCurrency = cursor.getString(cursor.getColumnIndexOrThrow(CurrencyTableModel.COLUMN_FIRST_CURRENCY));
            String secondCurrency = cursor.getString(cursor.getColumnIndexOrThrow(CurrencyTableModel.COLUMN_SECOND_CURRENCY));
            double rate = cursor.getDouble(cursor.getColumnIndexOrThrow(CurrencyTableModel.COLUMN_RATE));
            rates.put(firstCurrency + secondCurrency, rate);
        }
        cursor.close();
        return rates;
    }

    /**
     * @return Last time when rates were updated
     */
    public long getLastUpdateTime() {
        Cursor cursor = dbWritable.rawQuery("SELECT * FROM " + CurrencyTableModel.TABLE_NAME + " ORDER BY ROWID ASC LIMIT 1", null);
        long result = 0;
        if (cursor.moveToNext()) {
            result = cursor.getLong(cursor.getColumnIndexOrThrow(CurrencyTableModel.COLUMN_TIME));
        }
        cursor.close();
        return result;
    }

    /**
     * When rates are ready for updating in the DB, updates all rows.
     * In case if the table is empty, inserts rows into the table.
     * @param rates  HashMap with exchange rates.
     */
    public void updateRates(Map<String, Double> rates) {
        long currentTime = System.currentTimeMillis();

        Observable.defer(() -> Observable.just(updateRatesSync(rates, currentTime)))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.computation())
                .subscribe();
    }

    private boolean updateRatesSync(Map<String, Double> rates, long currentTime){

        boolean cleanDB = getLastUpdateTime() == 0;
        StringBuilder sampleQuery = new StringBuilder("");
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
                /*sampleQuery.append("INSERT INTO ")
                        .append(CurrencyTableModel.TABLE_NAME)
                        .append(" VALUES (")
                        .append("NULL,'")
                        .append(firstCurrency)
                        .append("','")
                        .append(secondCurrency)
                        .append("',")
                        .append(exchangeRate)
                        .append(",")
                        .append(currentTime)
                        .append(");");*/
                long newRowId = dbWritable.insert(CurrencyTableModel.TABLE_NAME, null, values);
            } else {
                values.put(CurrencyTableModel.COLUMN_RATE, exchangeRate);
                values.put(CurrencyTableModel.COLUMN_TIME, currentTime);
//                sampleQuery.append("UPDATE " + CurrencyTableModel.TABLE_NAME + " SET " + CurrencyTableModel.COLUMN_RATE + "=")
//                        .append(exchangeRate).append(", ")
//                        .append(CurrencyTableModel.COLUMN_TIME)
//                        .append("=").append(currentTime)
//                        .append(" WHERE ")
//                        .append(CurrencyTableModel.COLUMN_FIRST_CURRENCY)
//                        .append("='")
//                        .append(firstCurrency)
//                        .append("' AND ")
//                        .append(CurrencyTableModel.COLUMN_SECOND_CURRENCY)
//                        .append("='")
//                        .append(secondCurrency)
//                        .append("';");
                dbWritable.update(CurrencyTableModel.TABLE_NAME, values, CurrencyTableModel.COLUMN_FIRST_CURRENCY+"='"+firstCurrency+"'"+" AND " +
                        CurrencyTableModel.COLUMN_SECOND_CURRENCY + "='" + secondCurrency+"'", null);

            }
        }
//        if (!sampleQuery.toString().equals("")) {
//            dbWritable.execSQL(sampleQuery.toString());
//        }
        if (cleanDB){
            activity.onRatesUpdateFinished();
            activity.updateLastUpdateTime(getLastUpdateTime());
            activity.updateExistingFragments();
        }
        return true;
    }
}
