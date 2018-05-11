package pl.revanmj.smspasswordnotifier.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface WhitelistDao {
    @Query("SELECT * FROM Whitelist ORDER BY name ASC")
    LiveData<List<WhitelistItem>> getAll();

    @Query("SELECT * FROM Whitelist WHERE senderId == :id")
    WhitelistItem getById(int id);

    @Query("SELECT * FROM Whitelist WHERE name == :name")
    WhitelistItem getByName(String name);

    @Insert
    void insertAll(WhitelistItem... items);

    @Insert
    void insert(WhitelistItem item);

    @Delete
    void deleteAll(WhitelistItem... items);

    @Delete
    void delete(WhitelistItem item);
}
