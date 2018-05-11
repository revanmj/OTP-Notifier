package pl.revanmj.smspasswordnotifier.data;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import pl.revanmj.smspasswordnotifier.R;

/**
 * Created by revanmj on 15.03.2017.
 */

public class WhitelistAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int EMPTY_VIEW = 10;
    private List<WhitelistItem> mWhitelist;
    private ClickListener mClickListener;
    private SparseIntArray mSelectedItems = new SparseIntArray();

    public WhitelistAdapter(ClickListener listener) {
        mClickListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

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
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof WhitelistItemHolder) {
            WhitelistItem item = getItem(position);
            ((WhitelistItemHolder) holder).bind(
                    item.getSenderId(),
                    item.getName());
            holder.itemView.setOnClickListener(view -> {
                if (mClickListener != null) {
                    mClickListener.onItemClicked(position);
                }
            });
            holder.itemView.setOnLongClickListener(view -> {
                if (mClickListener != null) {
                    return mClickListener.onItemLongClicked(position);
                }
                return false;
            });
            if (isSelected(position))
                ((WhitelistItemHolder)holder).hightlightItem();
            else
                ((WhitelistItemHolder)holder).unhighlightItem();
        }
    }

    @Override
    public int getItemCount() {
        if (mWhitelist != null && mWhitelist.size() > 0)
            return mWhitelist.size();
        else
            return 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (mWhitelist == null || mWhitelist.size() == 0) {
            return EMPTY_VIEW;
        }
        return super.getItemViewType(position);
    }

    public void swapData(List<WhitelistItem> whitelist) {
        mWhitelist = whitelist;
        notifyDataSetChanged();
    }

    private WhitelistItem getItem(int pos) {
        return mWhitelist.get(pos);
    }

    public boolean isSelected(int position) {
        return mSelectedItems.get(position, -1) >= 0;
    }

    public void toggleSelection(int position) {
        if (mSelectedItems.get(position, -1) >= 0) {
            mSelectedItems.delete(position);
        } else {
            mSelectedItems.put(position, getItem(position).getSenderId());
        }
        notifyItemChanged(position);
    }

    public int getSelectedItemCount() {
        return mSelectedItems.size();
    }

    public List<WhitelistItem> getSelectedItems() {
        List<WhitelistItem> result = new ArrayList<>();
        for (int i = 0; i < mSelectedItems.size(); i++) {
            result.add(mWhitelist.get(mSelectedItems.keyAt(i)));
        }
        return result;
    }

    public void clearSelection() {
        for (int i = 0; i < mSelectedItems.size(); ++i) {
            notifyItemChanged(mSelectedItems.keyAt(i));
        }
        mSelectedItems.clear();
    }

    public class WhitelistItemHolder extends RecyclerView.ViewHolder {
        private TextView sender;
        private int _id;
        private LinearLayout linearLayout;

        WhitelistItemHolder(View itemView) {
            super(itemView);

            sender = itemView.findViewById(R.id.sender);
            linearLayout = itemView.findViewById(R.id.linear_layout);
        }

        void bind(int id, String sender) {
            this._id = id;
            this.sender.setText(sender);
        }

        public int getId() {
            return _id;
        }

        void hightlightItem() {
            linearLayout.setBackgroundResource(R.color.selected);
        }

        void unhighlightItem() {
            linearLayout.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    public class EmptyViewHolder extends RecyclerView.ViewHolder {
        EmptyViewHolder(View itemView) {
            super(itemView);
        }
    }

    public interface ClickListener {
        void onItemClicked(int position);
        boolean onItemLongClicked(int position);
    }
}
