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
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer

import com.google.android.material.floatingactionbutton.FloatingActionButton

import pl.revanmj.smspasswordnotifier.R
import pl.revanmj.smspasswordnotifier.data.WhitelistAdapter
import pl.revanmj.smspasswordnotifier.data.WhitelistItem
import pl.revanmj.smspasswordnotifier.data.WhitelistViewModel

class EditWhitelistActivity : AppCompatActivity() {
    private var mActionMode: ActionMode? = null
    private var mRvAdapter: WhitelistAdapter? = null
    private var mViewModel: WhitelistViewModel? = null

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
                    val itemsToDelete = mRvAdapter!!.selectedItems
                    mViewModel!!.delete(*itemsToDelete.toTypedArray())
                    Toast.makeText(this@EditWhitelistActivity, "Deleted " + itemsToDelete.size + " items",
                            Toast.LENGTH_SHORT).show()
                    mode.finish()
                    true
                }
                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            mRvAdapter!!.clearSelection()
            mActionMode = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_whitelist)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Setting up RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        mRvAdapter = WhitelistAdapter(object : WhitelistAdapter.ClickListener {
            override fun onItemClicked(position: Int) {
                if (mActionMode != null) {
                    toggleSelection(position)
                } else {
                    // TODO: Add opening edit dialog
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
        mViewModel?.whitelist?.observe(this, Observer{ words ->
            // Update the cached copy of the words in the adapter.
            mRvAdapter?.swapData(words)
        })

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener { showEditDialog() }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun showEditDialog() {
        // All this to set up proper padding ...
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER_HORIZONTAL
        layout.setPadding(pxToDp(20), 0, pxToDp(20), 0)
        val input = EditText(this)
        input.isSingleLine = true
        input.hint = "Sender's name or number"
        layout.addView(input)

        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.title_add_sender)
        builder.setView(layout)
        builder.setPositiveButton(R.string.button_add, null)
        builder.setNegativeButton(R.string.button_cancel, null)

        val dialog = builder.create()
        dialog.setOnShowListener { dialog1 ->
            val button = (dialog1 as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener { addSenderToWhitelist(input, dialog1) }
        }

        // Add listener for Search key presses on virtual keyboard
        input.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN
                    && (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER)) {
                        addSenderToWhitelist(input, dialog)
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

    private fun addSenderToWhitelist(input: EditText, dialog: DialogInterface) {
        val sender = input.text.toString()
        if (sender != "") {
            val item = WhitelistItem(name=sender, regex = "")
            mViewModel!!.insert(item)
            dialog.dismiss()
        } else {
            Toast.makeText(this,
                    R.string.message_empty_sender,
                    Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleSelection(position: Int) {
        mRvAdapter!!.toggleSelection(position)
        val count = mRvAdapter!!.selectedItemCount

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
