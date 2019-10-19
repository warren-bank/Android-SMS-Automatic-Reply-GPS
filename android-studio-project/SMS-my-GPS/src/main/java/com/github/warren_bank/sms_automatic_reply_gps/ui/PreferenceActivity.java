package com.github.warren_bank.sms_automatic_reply_gps.ui;

import com.github.warren_bank.sms_automatic_reply_gps.R;
import com.github.warren_bank.sms_automatic_reply_gps.data_model.ListItem;
import com.github.warren_bank.sms_automatic_reply_gps.data_model.Preferences;
import com.github.warren_bank.sms_automatic_reply_gps.event.SMSReceiver;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import java.util.ArrayList;

public class PreferenceActivity extends Activity {
    private CheckBox inputEnable;
    private ListView listView;

    private ArrayList<ListItem>     listItems;
    private ArrayAdapter<ListItem>  listAdapter;

    // ---------------------------------------------------------------------------------------------
    // Lifecycle Events:
    // ---------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);

        inputEnable = (CheckBox) findViewById(R.id.input_enable);
        listView    = (ListView) findViewById(R.id.listview);

        inputEnable.setChecked(Preferences.isEnabled(PreferenceActivity.this));

        listItems   = Preferences.getListItems(PreferenceActivity.this);
        listAdapter = new ArrayAdapter<ListItem>(PreferenceActivity.this, android.R.layout.simple_list_item_1, listItems);
        listView.setAdapter(listAdapter);

        inputEnable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Preferences.setEnabled(PreferenceActivity.this, inputEnable.isChecked());
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showEditDialog(position);
                return;
            }
        });
    }

    // ---------------------------------------------------------------------------------------------
    // ActionBar:
    // ---------------------------------------------------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getActionBar().setDisplayShowHomeEnabled(false);
        getMenuInflater().inflate(R.menu.activity_preference, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch(menuItem.getItemId()) {
            case R.id.menu_add: {
                showEditDialog(-1);
                return true;
            }
            default: {
                return super.onOptionsItemSelected(menuItem);
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Add/Edit Dialog:
    // ---------------------------------------------------------------------------------------------

    private void showEditDialog(final int position) {
        final boolean isAdd = (position < 0);

        final ListItem listItem = (isAdd)
          ? new ListItem("", "")
          : listItems.get(position)
        ;

        final Dialog dialog = new Dialog(PreferenceActivity.this, R.style.app_theme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_listitem);

        final EditText inputSender  = (EditText) dialog.findViewById(R.id.input_sender);
        final EditText inputMessage = (EditText) dialog.findViewById(R.id.input_message);

        final Button buttonDelete = (Button) dialog.findViewById(R.id.button_delete);
        final Button buttonSave   = (Button) dialog.findViewById(R.id.button_save);

        inputSender.setText(listItem.sender);
        inputMessage.setText(listItem.message_prefix);

        if (isAdd) {
            buttonDelete.setText(R.string.label_button_cancel);
        }

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isAdd) {
                  listItems.remove(position);
                  listAdapter.notifyDataSetChanged();
                }
                dialog.dismiss();
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String new_sender           = inputSender.getText().toString().trim();
                final String new_message_prefix   = inputMessage.getText().toString().trim();

                final boolean same_sender         = new_sender.equals(listItem.sender);
                final boolean same_message_prefix = new_message_prefix.equals(listItem.message_prefix);

                if (new_sender.equals("") || new_message_prefix.equals("")) {
                    Toast.makeText(PreferenceActivity.this, getResources().getString(R.string.error_missing_required_value), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (same_sender && same_message_prefix) {
                    // no change
                    dialog.dismiss();
                    return;
                }

                if (!same_sender) {
                    listItem.sender = new_sender;
                }
                if (!same_message_prefix) {
                    listItem.message_prefix = new_message_prefix;
                }

                if (isAdd) {
                    if (!listItems.add(listItem)) {
                        Toast.makeText(PreferenceActivity.this, getResources().getString(R.string.error_add_listitem), Toast.LENGTH_SHORT).show();
                        return;
                    }                    
                }

                listAdapter.notifyDataSetChanged();
                dialog.dismiss();

                Preferences.setListItems(PreferenceActivity.this, listItems);
            }
        });

        dialog.show();
    }
}
