package pl.revanmj.smspasswordnotifier.data

import android.content.Context

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration

@Database(entities = [WhitelistItem::class], version = 3, exportSchema = false)
abstract class WhitelistDb : RoomDatabase() {

    abstract val whitelistDao: WhitelistDao

    companion object {
        private const val DB_NAME = "whitelist.db"
        private var INSTANCE: WhitelistDb? = null

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.beginTransaction()
                database.execSQL("ALTER TABLE Senders RENAME TO temp_table;")
                database.execSQL(
                        "CREATE TABLE Senders(\n" +
                                "   sender_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                                "   name TEXT, " +
                                "   regex TEXT );")
                database.execSQL(
                        "INSERT INTO Senders (name, regex)\n" +
                                "  SELECT name, regex\n" +
                                "  FROM temp_table;")
                database.execSQL("DROP TABLE temp_table;")
                database.setTransactionSuccessful()
                database.endTransaction()
            }
        }

        fun getDatabase(context: Context): WhitelistDb {
            if (INSTANCE == null) {
                synchronized(WhitelistDb::class) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(
                                context.applicationContext, WhitelistDb::class.java, DB_NAME)
                                .addMigrations(MIGRATION_2_3)
                                .build()
                    }
                }
            }
            return INSTANCE!!
        }
    }
}
