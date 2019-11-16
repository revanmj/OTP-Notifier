package pl.revanmj.smspasswordnotifier.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = WhitelistItem.TABLE_NAME)
class WhitelistItem {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "sender_id")
    var senderId: Int = 0

    var name: String? = null
    var regex: String? = null

    companion object {
        @Ignore
        internal const val TABLE_NAME = "Senders"
    }
}
