package com.foreseer.erate.SQL;

import android.provider.BaseColumns;

/**
 * Created by Foreseer on 11/03/2017.
 */

public class FragmentTableModel implements BaseColumns{
    private FragmentTableModel() {}

    public static final String TABLE_NAME = "fragment_table";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_FIRST_CURRENCY = "first_currency";
    public static final String COLUMN_SECOND_CURRENCY = "second_currency";

    public static final int DATABASE_VERSION = 1;

    public static final String QUERY_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COLUMN_FIRST_CURRENCY +  " TEXT," +
            COLUMN_SECOND_CURRENCY + " TEXT)";

    public static final String QUERY_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
}
