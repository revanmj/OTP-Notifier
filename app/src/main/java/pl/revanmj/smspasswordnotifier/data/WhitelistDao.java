package pl.revanmj.smspasswordnotifier.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface WhitelistDao {
    @Query("SELECT * FROM Senders ORDER BY name ASC")
    LiveData<List<WhitelistItem>> getAll();

    @Query("SELECT * FROM Senders WHERE sender_id == :id")
    WhitelistItem getById(int id);

    @Query("SELECT * FROM Senders WHERE name == :name")
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
