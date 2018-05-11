package pl.revanmj.smspasswordnotifier.data;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = WhitelistItem.TABLE_NAME)
public class WhitelistItem {
    @Ignore
    public static final String TABLE_NAME = "Whitelist";

    @PrimaryKey(autoGenerate = true)
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
