package pl.revanmj.smspasswordnotifier.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = WhitelistItem.TABLE_NAME)
class WhitelistItem(
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "sender_id") var senderId: Int = -1,
        var name: String,
        var regex: String
) {
    companion object {
        @Ignore
        internal const val TABLE_NAME = "Senders"
    }
}
