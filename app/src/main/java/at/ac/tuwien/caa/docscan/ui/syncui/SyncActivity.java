package at.ac.tuwien.caa.docscan.ui.syncui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import at.ac.tuwien.caa.docscan.R;
import at.ac.tuwien.caa.docscan.rest.User;
import at.ac.tuwien.caa.docscan.sync.SyncInfo;
import at.ac.tuwien.caa.docscan.ui.BaseNavigationActivity;
import at.ac.tuwien.caa.docscan.ui.NavigationDrawer;

import static at.ac.tuwien.caa.docscan.ui.syncui.UploadingActivity.UPLOAD_FINISHED_ID;
import static at.ac.tuwien.caa.docscan.ui.syncui.UploadingActivity.UPLOAD_PROGRESS_ID;

/**
 * Created by fabian on 22.09.2017.
 */

public class SyncActivity extends BaseNavigationActivity implements SyncAdapter.SyncAdapterCallback {

    private ExpandableListView mListView;
    private Context mContext;
    private SyncAdapter mAdapter;
    private int mNumUploadJobs;
    private int mTranskribusUploadCollId;
    private ArrayList<File> mSelectedDirs;
    private TextView mSelectionTextView;
    private RelativeLayout mSelectionLayout;
    private Snackbar mSnackbar;
    private ProgressBar mProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (User.getInstance().isUploadActive()) {
            Intent intent = new Intent(getApplicationContext(), UploadingActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_sync);

        // Read the upload information:
        SyncInfo.readFromDisk(this);

        mListView = (ExpandableListView) findViewById(R.id.sync_list_view);
        mContext = this;

        addFooter();

        mAdapter = new SyncAdapter(this, SyncInfo.getInstance().getSyncList());
        mListView.setAdapter(mAdapter);

        mSelectionTextView = (TextView) findViewById(R.id.sync_selection_textview);
        mSelectionLayout = (RelativeLayout) findViewById(R.id.sync_selection_layout);

        initUploadButton();

        mProgressBar = (ProgressBar) findViewById(R.id.sync_progressbar);

        // Register to receive messages.
        // We are registering an observer (mMessageReceiver) to receive Intents
        // with actions named "PROGRESS_INTENT_NAME".
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("PROGRESS_INTENT_NAME"));

    }

    private void initUploadButton() {
        ImageButton uploadButton = (ImageButton) findViewById(R.id.start_upload_button);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                new CreateCollectionRequest(mContext, "DocScan");

                ArrayList<File> selectedDirs = mAdapter.getSelectedDirs();

                if (selectedDirs.size() == 0) {
                    showNoDirSelectedAlert();
                }
                else {
                    mSelectedDirs = selectedDirs;
                    showSnackbarInfo();
                    startUpload();
                }

            }
        });
    }

    /**
     * Adds some space to the end of the list, so that the overlapping RelativeLayout is not
     * overlapping in case the user scrolls to the end of the list.
     */
    private void addFooter() {
        View footer = new View(this);
        int footerHeight = (int) getResources().getDimension(R.dimen.sync_footer_height);
        footer.setMinimumHeight(footerHeight);
        mListView.addFooterView(footer);
    }

    private void showSnackbarInfo() {

        if (isOnline())
            showUploadingSnackbar();
        else
            showNotOnlineSnackbar();

    }

    /**
     * Shows a snackbar indicating that the device is offline.
     */
    private void showNotOnlineSnackbar() {

        String snackbarText =
                getResources().getString(R.string.sync_snackbar_offline_prefix_text) + " " +
                        getSelectionText() + " " +
                        getResources().getString(R.string.sync_snackbar_offline_postfix_text);

        closeSnackbar();
        mSnackbar = Snackbar.make(findViewById(R.id.sync_coordinatorlayout),
                snackbarText, Snackbar.LENGTH_LONG);
        mSnackbar.show();

    }

    /**
     * Shows a snackbar indicating that the upload process starts. We need this because we have
     * little control of the time when the upload starts really.
     */
    private void showUploadingSnackbar() {

        String snackbarText =
                getResources().getString(R.string.sync_snackbar_uploading_prefix_text) + " " +
                getSelectionText() +
                getResources().getString(R.string.sync_snackbar_check_notifications_text);

        closeSnackbar();
        mSnackbar = Snackbar.make(findViewById(R.id.sync_coordinatorlayout),
                snackbarText, Snackbar.LENGTH_INDEFINITE);
        mSnackbar.show();

        if (mSelectionLayout != null)
            mSelectionLayout.setVisibility(View.INVISIBLE);
        if (mListView != null)
            mListView.setVisibility(View.INVISIBLE);
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.setProgress(0);
        }
    }

    /**
     * Shows a snackbar indicating that the upload process starts. We need this because we have
     * little control of the time when the upload starts really.
     */
    private void showUploadFinishedSnackbar() {

        String snackbarText =
                getResources().getString(R.string.sync_snackbar_finished_upload_text) + " " +
                        getSelectionText() + ".";

        closeSnackbar();
        mSnackbar = Snackbar.make(findViewById(R.id.sync_coordinatorlayout),
                snackbarText, Snackbar.LENGTH_LONG);
        mSnackbar.show();


    }

    private void closeSnackbar() {

        if (mSnackbar != null) {
            if (mSnackbar.isShown())
                mSnackbar.dismiss();
        }

    }

    private boolean isOnline() {

        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }


    /**
     * This method creates simply a CollectionsRequest in order to find the ID of the DocScan Transkribus
     * upload folder.
     */
    private void startUpload() {

        SyncInfo.getInstance().setUploadDirs(mSelectedDirs);
        SyncInfo.saveToDisk(this);

        SyncInfo.startSyncJob(this);
//        new CollectionsRequest(this);

    }


    private void showNoDirSelectedAlert() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);

        // set dialog message
        alertDialogBuilder
                .setTitle(R.string.sync_no_dir_selected_title)
                .setCancelable(true)
                .setPositiveButton("OK", null)
                .setMessage(R.string.sync_no_dir_selected_message);

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();

    }


    @Override
    protected NavigationDrawer.NavigationItemEnum getSelfNavDrawerItem() {
        return NavigationDrawer.NavigationItemEnum.SYNC;
    }


    @Override
    public void onSelectionChange() {

        if (mSelectionTextView != null) {
            String text = getSelectionText();
            if (text != null)
                mSelectionTextView.setText(text);
        }
    }

    private String getSelectionText() {

        String selectionText = null;

        if (mAdapter != null) {

            int selCnt = mAdapter.getSelectedDirs().size();
            String postFix;
            if (selCnt == 1)
                postFix = getResources().getString(R.string.sync_selection_single_document_text);
            else
                postFix = getResources().getString(R.string.sync_selection_many_documents_text);
            selectionText = selCnt + " " + postFix;

        }

        return selectionText;
    }

    /**
     * Handles broadcast intents which inform about the upload progress:
      */
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            boolean finished = intent.getBooleanExtra(UPLOAD_FINISHED_ID, false);

            if (finished) {

                showUploadFinishedSnackbar();
                if (mContext != null) {
                    mAdapter = new SyncAdapter(mContext, SyncInfo.getInstance().getSyncList());
                    if (mListView != null) {
                        mListView.setAdapter(mAdapter);
                        mListView.setVisibility(View.VISIBLE);
                    }
                    if (mSelectionLayout != null)
                        mSelectionLayout.setVisibility(View.VISIBLE);
                    if (mProgressBar != null)
                        mProgressBar.setVisibility(View.INVISIBLE);
                }
                // update the selection display:
                onSelectionChange();
            }
            else {

//                if (mListView != null)
//                    mListView.setVisibility(View.INVISIBLE);
//                if (mSelectionLayout != null)
//                    mSelectionLayout.setVisibility(View.INVISIBLE);
                if (mProgressBar != null) {
                    mProgressBar.setVisibility(View.VISIBLE);
                    int progress = intent.getIntExtra(UPLOAD_PROGRESS_ID, 0);
                    mProgressBar.setProgress(progress);
                }
            }



        }
    };
}