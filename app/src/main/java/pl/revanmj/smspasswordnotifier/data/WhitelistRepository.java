package pl.revanmj.smspasswordnotifier.data;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

public class WhitelistRepository {
    private WhitelistDao mWhitelistDao;
    private LiveData<List<WhitelistItem>> mWhitelist;

    WhitelistRepository(Application application) {
        WhitelistDb db = WhitelistDb.getDatabase(application);
        mWhitelistDao = db.getWhitelistDao();
        mWhitelist = mWhitelistDao.getAll();
    }

    LiveData<List<WhitelistItem>> getWhitelist() {
        return mWhitelist;
    }

    WhitelistItem getItemByName(String name) { return mWhitelistDao.getByName(name); }

    public void insert(WhitelistItem... items) {
        new insertAsyncTask(mWhitelistDao).execute(items);
    }

    public void delete(WhitelistItem... items) {
        new deleteAsyncTask(mWhitelistDao).execute(items);
    }

    private static class insertAsyncTask extends AsyncTask<WhitelistItem, Void, Void> {

        private WhitelistDao mAsyncTaskDao;

        insertAsyncTask(WhitelistDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final WhitelistItem... params) {
            mAsyncTaskDao.insertAll(params);
            return null;
        }
    }

    private static class deleteAsyncTask extends AsyncTask<WhitelistItem, Void, Void> {

        private WhitelistDao mAsyncTaskDao;

        deleteAsyncTask(WhitelistDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final WhitelistItem... params) {
            mAsyncTaskDao.deleteAll(params);
            return null;
        }
    }
}
