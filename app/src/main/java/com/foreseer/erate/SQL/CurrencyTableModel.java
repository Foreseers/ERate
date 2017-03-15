package com.foreseer.erate.SQL;

import android.provider.BaseColumns;

/**
 * Model of the currency table for the DB.
 */

public class CurrencyTableModel implements BaseColumns {
    private CurrencyTableModel() {}

    public static final String TABLE_NAME = "currency_rate_table";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_FIRST_CURRENCY = "first_currency";
    public static final String COLUMN_SECOND_CURRENCY = "second_currency";
    public static final String COLUMN_RATE = "rate";
    public static final String COLUMN_TIME = "time";

    public static final int DATABASE_VERSION = 1;

    public static final String QUERY_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COLUMN_FIRST_CURRENCY +  " TEXT," +
            COLUMN_SECOND_CURRENCY + " TEXT," +
            COLUMN_RATE + " REAL," +
            COLUMN_TIME + " INTEGER)";

    public static final String QUERY_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

}
