package at.ac.tuwien.caa.docscan.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;

import java.io.File;

import at.ac.tuwien.caa.docscan.R;
import at.ac.tuwien.caa.docscan.logic.Helper;
import at.ac.tuwien.caa.docscan.rest.User;

/**
 * Created by fabian on 26.09.2017.
 */

public class DocumentActivity extends BaseNoNavigationActivity  {

    private ExpandableListView mListView;
    private Context mContext;
    private File mSelectedDir = null;
    private DocumentAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document);
        super.initToolbarTitle(R.string.document_title);

        mListView = (ExpandableListView) findViewById(R.id.document_list_view);

        mContext = this;

        FloatingActionButton addFolderButton = (FloatingActionButton) findViewById(R.id.add_folder_fab);
        addFolderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                Taken from: https://stackoverflow.com/questions/35861081/custom-popup-dialog-with-input-field

                LayoutInflater li = LayoutInflater.from(mContext);
                View promptsView = li.inflate(R.layout.create_document_view, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        mContext);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(promptsView);




                final EditText userInput = (EditText) promptsView.findViewById(R.id.document_name_edit);

                userInput.setFilters(getInputFilters());

                // set dialog message
                alertDialogBuilder
                        .setTitle(R.string.document_create_folder_title)
                        .setCancelable(true)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        onCreateDirResult(userInput.getText().toString());
                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
//                                        dialog.cancel();
                                    }
                                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
            }
        });

        FloatingActionButton confirmButton = (FloatingActionButton) findViewById(R.id.confirm_fab);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mSelectedDir != null) {
                    User.getInstance().setDocumentName(mSelectedDir.getName());
                    finish(); // Close the activity
                }
                else
                    showNoFileSelectedAlert();
            }
        });

        mListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int groupPosition, long l) {
                if (mAdapter!= null)
                    mSelectedDir = mAdapter.getGroupFile(groupPosition);
                return false;
            }
        });
    }

    private void onCreateDirResult(String result) {

        File mediaStorageDir = Helper.getMediaStorageDir(mContext.getResources().getString(R.string.app_name));
        File subDir = new File(mediaStorageDir.getAbsolutePath(), result);
        boolean dirCreated = false;
        if (subDir != null) {
            dirCreated = subDir.mkdir();

        }

        if (!dirCreated)
            showNoDirCreatedAlert();
        else {
            mAdapter = new DocumentAdapter(this);
            mListView.setAdapter(mAdapter);
        }

    }

    private void showNoDirCreatedAlert() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);

        // set dialog message
        alertDialogBuilder
                .setTitle(R.string.document_no_dir_created_title)
                .setCancelable(true)
                .setPositiveButton("OK", null)
                .setMessage(R.string.document_no_dir_created_message);

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();

    }

    private void showNoFileSelectedAlert() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);

        // set dialog message
        alertDialogBuilder
                .setTitle(R.string.document_no_dir_selected_title)
                .setCancelable(true)
                .setPositiveButton("OK", null)
                .setMessage(R.string.document_no_dir_selected_message);

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();

    }

    private InputFilter[] getInputFilters() {

        InputFilter filter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    char c = source.charAt(i);
                    if (!Character.isLetterOrDigit(c) && !Character.isSpaceChar(c)) {
                        return "";
                    }
                }
                return null;
            }
        };

        InputFilter[] filters = {filter};

        return filters;

    }


    @Override
    protected void onResume() {

        super.onResume();
        mAdapter = new DocumentAdapter(this);
        mListView.setAdapter(mAdapter);

    }


}