package at.ac.tuwien.caa.docscan.ui.syncui;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.widget.CircularProgressDrawable;
import android.support.v7.content.res.AppCompatResources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.util.Comparator;
import java.util.List;

import at.ac.tuwien.caa.docscan.R;
import at.ac.tuwien.caa.docscan.glidemodule.GlideApp;
import at.ac.tuwien.caa.docscan.logic.Document;
import at.ac.tuwien.caa.docscan.ui.gallery.GalleryActivity;

/**
 * Created by fabian on 4/5/2018.
 */

public class DocumentUploadAdapter extends DocumentAdapter {

    protected Context mContext;
    private List<Document> mDocuments;
    private DocumentAdapterCallback mCallback;

    public DocumentUploadAdapter(@NonNull Context context, int resource, @NonNull List<Document> documents) {
        super(context, resource, documents);

        mContext = context;
        mDocuments = documents;
//        mCallback = (DocumentAdapterCallback) context;

    }


//    public DocumentAdapter(@NonNull Context context, int resource) {
//        super(context, resource);
//
//        mContext = context;
//
//        fillList();
//    }

    @Override
    public boolean isEnabled (int position) {

        Document document = mDocuments.get(position);

        if ((document != null) && (document.isCropped()))
            return false;
        else
            return true;

    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = super.getView(position, convertView, parent);

        Document document = mDocuments.get(position);
        if (document == null)
            return convertView;

//        Show the upload status in the icon:
        ImageView iconView = convertView.findViewById(R.id.layout_listview_row_icon);
        if (iconView != null) {
            if (document.isUploaded())
                iconView.setImageResource(R.drawable.ic_cloud_done_black_24dp);
            else if (document.isAwaitingUpload()) {
                iconView.setImageResource(R.drawable.ic_cloud_upload_black_24dp);
                TextView textView = convertView.findViewById(R.id.layout_listview_row_description);
//                Write that the upload is pending:
                if (textView != null)
                    textView.append(" " + mContext.getResources().getString(R.string.sync_dir_pending_text));
            }
            else if (document.isCropped()) {

                iconView.setVisibility(View.INVISIBLE);
                ProgressBar p = convertView.findViewById(R.id.layout_listview_progress_bar);
                p.setVisibility(View.VISIBLE);

                TextView textView = convertView.findViewById(R.id.layout_listview_row_description);
//                Write that the upload is pending:
                if (textView != null)
                    textView.append(" " + mContext.getResources().getString(R.string.sync_dir_cropping_text));

            }
            else
                iconView.setImageResource(R.drawable.ic_cloud_queue_black_24dp);

        }

        return convertView;

    }


}
