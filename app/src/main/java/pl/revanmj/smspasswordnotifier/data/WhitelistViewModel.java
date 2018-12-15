package pl.revanmj.smspasswordnotifier.data;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class WhitelistViewModel extends AndroidViewModel {
    private WhitelistRepository mRepository;
    private LiveData<List<WhitelistItem>> mWhitelist;

    public WhitelistViewModel(Application application) {
        super(application);
        mRepository = new WhitelistRepository(application);
        mWhitelist = mRepository.getWhitelist();
    }

    public LiveData<List<WhitelistItem>> getWhitelist() { return mWhitelist; }

    public void insert(WhitelistItem... items) { mRepository.insert(items); }

    public void delete(WhitelistItem... items) { mRepository.delete(items); }

    public WhitelistItem getItemByName(String name) { return mRepository.getItemByName(name); }
}
