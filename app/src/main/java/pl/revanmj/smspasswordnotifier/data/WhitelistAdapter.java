package pl.revanmj.smspasswordnotifier.data;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import pl.revanmj.smspasswordnotifier.R;

/**
 * Created by revanmj on 15.03.2017.
 */

public class WhitelistAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int EMPTY_VIEW = 10;
    private Cursor mCursor;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == EMPTY_VIEW) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.empty_view, parent, false);
            return new EmptyViewHolder(v);
        }

        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.edit_whitelist_item, parent, false);

        return new WhitelistItemHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Cursor item = getItem(position);
        if (holder instanceof WhitelistItemHolder)
            ((WhitelistItemHolder)holder).bind(
                    item.getInt(WhitelistProvider.ID),
                    item.getString(WhitelistProvider.SENDER));
    }

    @Override
    public int getItemCount() {
        if (mCursor != null && mCursor.getCount() > 0)
            return mCursor.getCount();
        else
            return 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (mCursor == null || mCursor.getCount() == 0) {
            return EMPTY_VIEW;
        }
        return super.getItemViewType(position);
    }

    public void swapCursor(Cursor c) {
        mCursor = c;
        notifyDataSetChanged();
    }

    public Cursor getItem(int pos) {
        if (this.mCursor != null && !this.mCursor.isClosed()) {
            this.mCursor.moveToPosition(pos);
        }

        return this.mCursor;
    }

    public static class WhitelistItemHolder extends RecyclerView.ViewHolder {
        private TextView sender;
        private int _id;


        public WhitelistItemHolder(View itemView) {
            super(itemView);

            sender = (TextView) itemView.findViewById(R.id.sender);
        }

        public void bind(int id, String sender) {
            this._id = id;
            this.sender.setText(sender);
        }

        public int getId() {
            return _id;
        }
    }

    public class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(View itemView) {
            super(itemView);
        }
    }
}
