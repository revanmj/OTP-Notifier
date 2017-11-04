package pl.revanmj.smspasswordnotifier.activities;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import pl.revanmj.smspasswordnotifier.R;
import pl.revanmj.smspasswordnotifier.SwipeToDelTouchCallback;
import pl.revanmj.smspasswordnotifier.data.WhitelistAdapter;
import pl.revanmj.smspasswordnotifier.data.WhitelistProvider;

public class EditWhitelistActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private WhitelistAdapter rcAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_whitelist);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        // Setting up RecyclerView
        rcAdapter = new WhitelistAdapter();
        recyclerView.setAdapter(rcAdapter);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);

        // Adding divider
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                llm.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        // Adding swipe to delete callback
        SwipeToDelTouchCallback stdcallback = new SwipeToDelTouchCallback(this);
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(stdcallback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);

        // init cursor loader
        getSupportLoaderManager().initLoader(1, null, this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {

            // All this to set up proper padding ...
            LinearLayout layout = new LinearLayout(EditWhitelistActivity.this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setGravity(Gravity.CENTER_HORIZONTAL);
            final EditText input = new EditText(EditWhitelistActivity.this);
            input.setSingleLine(true);
            layout.setPadding(pxToDp(20), 0, pxToDp(20), 0);
            input.setHint("Sender's name or number");
            layout.addView(input);

            AlertDialog.Builder builder = new AlertDialog.Builder(EditWhitelistActivity.this);
            builder.setTitle(R.string.title_add_sender);
            builder.setView(layout);
            builder.setPositiveButton(R.string.button_add, null);
            builder.setNegativeButton(R.string.button_cancel, null);

            final AlertDialog dialog = builder.create();
            dialog.setOnShowListener(dialog1 -> {

                Button button = ((AlertDialog) dialog1).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(view1 -> addSenderToWhitelist(input, dialog1));
            });

            // Add listener for Search key presses on virtual keyboard
            input.setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            addSenderToWhitelist(input, dialog);
                            final InputMethodManager imm =
                                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.toggleSoftInput(InputMethodManager.RESULT_UNCHANGED_HIDDEN, 0);
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            });

            // Show keyboard on dialog open
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            dialog.show();
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void addSenderToWhitelist(EditText input, DialogInterface dialog) {
        String sender = input.getText().toString();
        if (!sender.equals("")) {
            ContentValues cv = new ContentValues();
            cv.put(WhitelistProvider.KEY_SENDER, sender);
            EditWhitelistActivity.this.getContentResolver()
                    .insert(WhitelistProvider.CONTENT_URI, cv);
            dialog.dismiss();
        } else {
            Toast.makeText(
                    EditWhitelistActivity.this,
                    R.string.message_empty_sender,
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, WhitelistProvider.CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor data) {
        rcAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        rcAdapter.swapCursor(null);
    }

    private int pxToDp(int number) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (number * scale + 0.5f);
    }
}
