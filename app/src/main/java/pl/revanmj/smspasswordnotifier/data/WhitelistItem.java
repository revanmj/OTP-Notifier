package pl.revanmj.smspasswordnotifier.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = WhitelistItem.TABLE_NAME)
public class WhitelistItem {
    @Ignore
    static final String TABLE_NAME = "Senders";

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "sender_id")
    private int senderId;

    private String name;
    private String regex;

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }
}
