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

public class WhitelistAdapter extends RecyclerView.Adapter<WhitelistAdapter.WhitelistItemHolder> {
    private Cursor mCursor;

    public void swapCursor(Cursor c) {
        mCursor = c;
        notifyDataSetChanged();
    }

    @Override
    public WhitelistItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.edit_whitelist_item, parent, false);

        return new WhitelistItemHolder(itemView);
    }

    @Override
    public void onBindViewHolder(WhitelistItemHolder holder, int position) {
        Cursor item = getItem(position);
        holder.bind(item.getInt(WhitelistProvider.ID), item.getString(WhitelistProvider.SENDER));
    }

    @Override
    public int getItemCount() {
        if (mCursor != null)
            return mCursor.getCount();
        else
            return 0;
    }

    public Cursor getItem(int pos) {
        if (this.mCursor != null && !this.mCursor.isClosed())
        {
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
}
