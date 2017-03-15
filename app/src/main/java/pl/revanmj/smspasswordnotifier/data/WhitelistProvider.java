package pl.revanmj.smspasswordnotifier.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by revanmj on 15.03.2017.
 */

public class WhitelistProvider extends ContentProvider {
    public static final int ID = 0;
    public static final int SENDER = 1;

    public static final String AUTHORITY = "pl.revanmj.provider.sms_whitelist";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/senders");

    static final int URI_SENDERS = 1;
    static final int URI_SENDERID = 2;

    static final UriMatcher uriMatcher;


    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, "senders", URI_SENDERS);
        uriMatcher.addURI(AUTHORITY, "senders/*", URI_SENDERID); // * is for strings, # is for ids
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        WhitelistSqlHelper dbHelper = new WhitelistSqlHelper(context);

        db = dbHelper.getWritableDatabase();
        return (db != null);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_SENDERS);

        switch (uriMatcher.match(uri)) {
            case URI_SENDERS:
                break;

            case URI_SENDERID:
                qb.appendWhere(KEY_SENDER + "='" + uri.getPathSegments().get(1) + "'");
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if (sortOrder == null || sortOrder == ""){
            // By default sort on sender names
            sortOrder = KEY_SENDER;
        }
        Cursor c = qb.query(db,	projection,	selection, selectionArgs, null, null, sortOrder);

        // register to watch a content URI for changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)){
            // Get all senders
            case URI_SENDERS:
                return "vnd.android.cursor.dir/vnd.revanmj.senders";

            // Get specific sender
            case URI_SENDERID:
                return "vnd.android.cursor.item/vnd.revanmj.senders";

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        long rowId = db.insert(TABLE_SENDERS, null, contentValues);

        if (rowId > 0) {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }

        throw new SQLException("Failed to add a sender into " + uri);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;

        switch (uriMatcher.match(uri)){
            case URI_SENDERS:
                count = db.delete(TABLE_SENDERS, selection, selectionArgs);
                break;

            case URI_SENDERID:
                count = db.delete(TABLE_SENDERS, KEY_SENDER +  " = '" + uri.getPathSegments().get(1) + "' " +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;

        switch (uriMatcher.match(uri)){
            case URI_SENDERS:
                count = db.update(TABLE_SENDERS, values, selection, selectionArgs);
                break;

            case URI_SENDERID:
                count = db.update(TABLE_SENDERS, values, KEY_SENDER + " = '" + uri.getPathSegments().get(1) + "' " +
                        (!TextUtils.isEmpty(selection) ? " AND (" +selection + ')' : ""), selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri );
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    private SQLiteDatabase db;
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "whitelist.db";
    private static final String TABLE_SENDERS = "Senders";
    public static final String KEY_ID = "sender_id";
    public static final String KEY_SENDER = "name";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_SENDERS + " ( " +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY_SENDER + " TEXT )";

    public static class WhitelistSqlHelper extends SQLiteOpenHelper {

        public WhitelistSqlHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(TABLE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            List<String> senders = new ArrayList<>();

            String query = "SELECT  * FROM " + TABLE_SENDERS;

            Cursor cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    senders.add(cursor.getString(SENDER));
                    Log.d("WhitelistProvider", cursor.getString(SENDER));
                } while (cursor.moveToNext());
            }

            // Drop older table if existed
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SENDERS);

            // create fresh table
            this.onCreate(db);

            for (int i = 0; i < senders.size(); i++) {
                ContentValues values = new ContentValues();
                values.put(KEY_SENDER, senders.get(i));

                // 3. insert
                db.insert(TABLE_SENDERS, null, values);
            }
        }
    }
}
