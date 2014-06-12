package com.github.pradeeppappu.apdf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PDFPage extends Fragment {

    private static final String DATA_URL = "dataUrl";

    private Bitmap mBitmap;
    private PageView mPageView;
    private TextView mTextView;
    private RelativeLayout mRelativeLayout;

    public static PDFPage newInstance(String dataUrl) {
        PDFPage fragment = new PDFPage();
        Bundle arguments = new Bundle();
        arguments.putString(DATA_URL, dataUrl);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(container == null)
            throw new RuntimeException("Container cannot be null.");
        Context context = container.getContext();
        if(context == null)
            throw new RuntimeException("Context cannot be null.");
        mRelativeLayout = new RelativeLayout(context);
        mPageView = new PageView(context);
        mPageView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        RelativeLayout.LayoutParams rlpTextView = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rlpTextView.addRule(RelativeLayout.CENTER_IN_PARENT);
        mTextView = new TextView(context);
        mTextView.setText("Loading...");
        mTextView.setLayoutParams(rlpTextView);

        mRelativeLayout.addView(mTextView);
        mRelativeLayout.addView(mPageView);

        String dataUrl = getArguments().getString(DATA_URL);
        if (dataUrl != null) {
            setBitmap(dataUrl);
            mPageView.setImageBitmap(mBitmap);
            mPageView.setVisibility(View.VISIBLE);
            mTextView.setVisibility(View.GONE);
        } else {
            mPageView.setVisibility(View.GONE);
            mTextView.setVisibility(View.VISIBLE);
        }
        return mRelativeLayout;
    }

    private void setBitmap(String dataUrl) {
        String encodedData = dataUrl.substring(dataUrl.indexOf(",") + 1);
        byte[] decodedString = Base64.decode(encodedData, Base64.DEFAULT);
        if (mBitmap != null)
            mBitmap.recycle();
        mBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBitmap != null)
            mBitmap.recycle();
    }

    public boolean isLoaded() {
        return mBitmap != null;
    }
}
