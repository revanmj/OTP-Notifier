package pl.revanmj.smspasswordnotifier.data;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.support.annotation.NonNull;

@Database(entities = {WhitelistItem.class}, version = 3, exportSchema = false)
public abstract class WhitelistDb extends RoomDatabase{
    private static final String DB_NAME = "whitelist.db";
    private static WhitelistDb INSTANCE;

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.beginTransaction();
            database.execSQL("ALTER TABLE Senders RENAME TO temp_table;");
            database.execSQL(
                    "CREATE TABLE Senders(\n" +
                    "   sender_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    "   name TEXT, " +
                    "   regex TEXT );");
            database.execSQL(
                    "INSERT INTO Senders (name, regex)\n" +
                    "  SELECT name, regex\n" +
                    "  FROM temp_table;");
            database.execSQL("DROP TABLE temp_table;");
            database.setTransactionSuccessful();
            database.endTransaction();
        }
    };

    public abstract WhitelistDao getWhitelistDao();

    public static WhitelistDb getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (WhitelistDb.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(), WhitelistDb.class, DB_NAME)
                            .addMigrations(MIGRATION_2_3)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
