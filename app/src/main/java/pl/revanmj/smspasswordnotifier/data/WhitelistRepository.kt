package pl.revanmj.smspasswordnotifier.data

import android.app.Application

import androidx.lifecycle.LiveData

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class WhitelistRepository internal constructor(application: Application) {
    private val mWhitelistDao: WhitelistDao
    internal val whitelist: LiveData<List<WhitelistItem>>

    init {
        val db = WhitelistDb.getDatabase(application)
        mWhitelistDao = db.whitelistDao
        whitelist = mWhitelistDao.all
    }

    internal fun getItemByName(name: String): WhitelistItem {
        return mWhitelistDao.getByName(name)
    }

    fun insert(vararg items: WhitelistItem) {
        GlobalScope.launch(Dispatchers.IO) {
            mWhitelistDao.insertAll(*items)
        }
    }

    fun update(vararg items: WhitelistItem) {
        GlobalScope.launch(Dispatchers.IO) {
            mWhitelistDao.updateAll(*items)
        }
    }

    fun delete(vararg items: WhitelistItem) {
        GlobalScope.launch(Dispatchers.IO) {
            mWhitelistDao.deleteAll(*items)
        }
    }
}
