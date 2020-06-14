package pl.revanmj.smspasswordnotifier.activities

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.Observer

import kotlinx.android.synthetic.main.activity_edit_whitelist.*
import kotlinx.android.synthetic.main.content_edit_whitelist.*

import pl.revanmj.smspasswordnotifier.R
import pl.revanmj.smspasswordnotifier.data.WhitelistAdapter
import pl.revanmj.smspasswordnotifier.data.WhitelistItem
import pl.revanmj.smspasswordnotifier.data.WhitelistViewModel
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

class EditWhitelistActivity : AppCompatActivity() {
    private var mActionMode: ActionMode? = null
    private lateinit var mRvAdapter: WhitelistAdapter
    private lateinit var mViewModel: WhitelistViewModel

    private var mActionModeCallback: ActionMode.Callback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.menu_whitelist_action, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.action_delete -> {
                    val itemsToDelete = mRvAdapter.selectedItems
                    mViewModel.delete(*itemsToDelete.toTypedArray())
                    Toast.makeText(this@EditWhitelistActivity, "Deleted " + itemsToDelete.size + " items",
                            Toast.LENGTH_SHORT).show()
                    mode.finish()
                    true
                }
                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            mRvAdapter.clearSelection()
            mActionMode = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_whitelist)
        setSupportActionBar(toolbar)

        // Setting up RecyclerView
        mRvAdapter = WhitelistAdapter(object : WhitelistAdapter.ClickListener {
            override fun onItemClicked(position: Int, whitelistItem: WhitelistItem) {
                if (mActionMode != null) {
                    toggleSelection(position)
                } else {
                    showEditDialog(whitelistItem)
                }
            }

            override fun onItemLongClicked(position: Int): Boolean {
                if (mActionMode == null) {
                    mActionMode = startSupportActionMode(mActionModeCallback)
                }
                toggleSelection(position)
                return true
            }
        })
        recyclerView.adapter = mRvAdapter
        recyclerView.setHasFixedSize(true)
        val llm = LinearLayoutManager(this)
        llm.orientation = RecyclerView.VERTICAL
        recyclerView.layoutManager = llm

        // Adding divider
        val dividerItemDecoration = DividerItemDecoration(recyclerView.context,
                llm.orientation)
        recyclerView.addItemDecoration(dividerItemDecoration)

        // init data
        mViewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
                .create(WhitelistViewModel::class.java)
        mViewModel.whitelist.observe(this, Observer{ words ->
            // Update the cached copy of the words in the adapter.
            mRvAdapter.swapData(words)
        })

        fab.setOnClickListener { showEditDialog() }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun showEditDialog(item: WhitelistItem? = null) {
        // All this to set up proper padding ...
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER_HORIZONTAL
        layout.setPadding(pxToDp(20), pxToDp(4), pxToDp(20), 0)

        val senderField = EditText(this)
        senderField.isSingleLine = true
        senderField.hint = "Sender's name or number"
        layout.addView(senderField)

        val regexField = EditText(this)
        regexField.isSingleLine = false
        regexField.hint = "Regular expression for extracting code (leave empty for default)"
        layout.addView(regexField)

        var itemTmp = item
        if (itemTmp != null) {
            senderField.text.append(itemTmp.name)
            regexField.text.append(itemTmp.regex)
        } else {
            itemTmp = WhitelistItem(name = "", regex = "")
        }

        val isAddMode = itemTmp.senderId == -1
        val builder = AlertDialog.Builder(this)
        builder.setTitle(if (isAddMode) R.string.title_add_sender else R.string.title_update_sender)
        builder.setView(layout)
        builder.setPositiveButton(if (isAddMode) R.string.button_add else R.string.button_update, null)
        builder.setNegativeButton(R.string.button_cancel, null)

        val dialog = builder.create()
        dialog.setOnShowListener { dialog1 ->
            val button = (dialog1 as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                itemTmp.name = senderField.text.toString()
                itemTmp.regex = regexField.text.toString()
                persist(itemTmp, dialog)
            }
        }

        senderField.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN
                    && (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER)) {
                itemTmp.name = senderField.text.toString()
                itemTmp.regex = regexField.text.toString()
                persist(itemTmp, dialog)
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.toggleSoftInput(InputMethodManager.RESULT_UNCHANGED_HIDDEN, 0)
                return@setOnKeyListener true
            }
            false
        }

        // Show keyboard on dialog open
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        dialog.show()
    }

    private fun persist(item: WhitelistItem, dialog: DialogInterface) {
        if (item.name.isNotEmpty()) {
            if (item.regex.isNotEmpty()) {
                try {
                    Pattern.compile(item.regex)
                } catch (e: PatternSyntaxException) {
                    Toast.makeText(this,
                            this.getString(R.string.message_wrong_regex, e.localizedMessage),
                            Toast.LENGTH_SHORT).show()
                    return
                }
            }
            if (item.senderId == -1)
                mViewModel.insert(item)
            else
                mViewModel.update(item)
            dialog.dismiss()
        } else {
            Toast.makeText(this,
                    R.string.message_empty_sender,
                    Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleSelection(position: Int) {
        mRvAdapter.toggleSelection(position)
        val count = mRvAdapter.selectedItemCount

        if (count == 0) {
            mActionMode?.finish()
        } else {
            mActionMode!!.title = count.toString()
            mActionMode!!.invalidate()
        }
    }

    private fun pxToDp(number: Int): Int {
        val scale = resources.displayMetrics.density
        return (number * scale + 0.5f).toInt()
    }
}
