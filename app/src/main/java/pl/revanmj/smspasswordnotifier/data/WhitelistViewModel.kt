package pl.revanmj.smspasswordnotifier.data

import android.app.Application

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

class WhitelistViewModel(application: Application) : AndroidViewModel(application) {
    private val mRepository: WhitelistRepository = WhitelistRepository(application)
    val whitelist: LiveData<List<WhitelistItem>>

    init {
        whitelist = mRepository.whitelist
    }

    fun insert(vararg items: WhitelistItem) {
        mRepository.insert(*items)
    }

    fun update(vararg items: WhitelistItem) {
        mRepository.update(*items)
    }

    fun delete(vararg items: WhitelistItem) {
        mRepository.delete(*items)
    }

    fun getItemByName(name: String): WhitelistItem {
        return mRepository.getItemByName(name)
    }
}
