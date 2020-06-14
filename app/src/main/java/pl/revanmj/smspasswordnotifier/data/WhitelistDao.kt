package pl.revanmj.smspasswordnotifier.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface WhitelistDao {
    @get:Query("SELECT * FROM Senders ORDER BY name ASC")
    val all: LiveData<List<WhitelistItem>>

    @Query("SELECT * FROM Senders WHERE sender_id == :id")
    fun getById(id: Int): WhitelistItem

    @Query("SELECT * FROM Senders WHERE name == :name")
    fun getByName(name: String): WhitelistItem

    @Insert
    fun insertAll(vararg items: WhitelistItem)

    @Insert
    fun insert(item: WhitelistItem)

    @Update
    fun update(item: WhitelistItem)

    @Update
    fun updateAll(vararg items: WhitelistItem)

    @Delete
    fun deleteAll(vararg items: WhitelistItem)

    @Delete
    fun delete(item: WhitelistItem)
}
