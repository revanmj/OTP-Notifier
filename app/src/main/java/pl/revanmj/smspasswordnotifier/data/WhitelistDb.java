package pl.revanmj.smspasswordnotifier.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {WhitelistItem.class}, version = 1, exportSchema = false)
public abstract class WhitelistDb extends RoomDatabase{
    private static final String DB_NAME = "whitelist";
    public abstract WhitelistDao getWhitelistDao();
    private static WhitelistDb INSTANCE;

    public static WhitelistDb getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (WhitelistDb.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            WhitelistDb.class, DB_NAME)
                            .build();

                }
            }
        }
        return INSTANCE;
    }
}
