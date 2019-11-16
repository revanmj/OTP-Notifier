package pl.revanmj.smspasswordnotifier.data

import android.graphics.Color
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView

import java.util.ArrayList

import pl.revanmj.smspasswordnotifier.R

/**
 * Created by revanmj on 15.03.2017.
 */

class WhitelistAdapter(private val mClickListener: ClickListener?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val EMPTY_VIEW = 10
    }

    private var mWhitelist: List<WhitelistItem>? = null
    private val mSelectedItems = SparseIntArray()

    val selectedItemCount: Int
        get() = mSelectedItems.size()

    val selectedItems: List<WhitelistItem>
        get() {
            val result = ArrayList<WhitelistItem>()
            for (i in 0 until mSelectedItems.size()) {
                result.add(mWhitelist!![mSelectedItems.keyAt(i)])
            }
            return result
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == EMPTY_VIEW) {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.empty_view, parent, false)
            return EmptyViewHolder(v)
        }

        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.edit_whitelist_item, parent, false)
        return WhitelistItemHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is WhitelistItemHolder) {
            val item = getItem(position)
            holder.bind(
                    item.senderId,
                    item.name)
            holder.itemView.setOnClickListener {
                mClickListener?.onItemClicked(position)
            }
            holder.itemView.setOnLongClickListener {
                if (mClickListener != null)
                    return@setOnLongClickListener mClickListener.onItemLongClicked(position)
                false
            }
            if (isSelected(position))
                holder.highlightItem()
            else
                holder.unhighlightItem()
        }
    }

    override fun getItemCount(): Int {
        return if (mWhitelist != null && mWhitelist!!.isNotEmpty())
            mWhitelist!!.size
        else
            1
    }

    override fun getItemViewType(position: Int): Int {
        return if (mWhitelist == null || mWhitelist!!.isEmpty()) {
            EMPTY_VIEW
        } else super.getItemViewType(position)
    }

    fun swapData(whitelist: List<WhitelistItem>) {
        mWhitelist = whitelist
        notifyDataSetChanged()
    }

    private fun getItem(pos: Int): WhitelistItem {
        return mWhitelist!![pos]
    }

    private fun isSelected(position: Int): Boolean {
        return mSelectedItems.get(position, -1) >= 0
    }

    fun toggleSelection(position: Int) {
        if (mSelectedItems.get(position, -1) >= 0) {
            mSelectedItems.delete(position)
        } else {
            mSelectedItems.put(position, getItem(position).senderId)
        }
        notifyItemChanged(position)
    }

    fun clearSelection() {
        selectedItems.forEachIndexed {
            index, _ -> notifyItemChanged(mSelectedItems.keyAt(index))
        }
        mSelectedItems.clear()
    }

    inner class WhitelistItemHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val sender: TextView = itemView.findViewById(R.id.sender)
        private val linearLayout: LinearLayout = itemView.findViewById(R.id.linear_layout)
        var id: Int = 0
            private set

        internal fun bind(id: Int, sender: String?) {
            this.id = id
            this.sender.text = sender
        }

        internal fun highlightItem() {
            linearLayout.setBackgroundResource(R.color.selected)
        }

        internal fun unhighlightItem() {
            linearLayout.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    inner class EmptyViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface ClickListener {
        fun onItemClicked(position: Int)
        fun onItemLongClicked(position: Int): Boolean
    }
}
