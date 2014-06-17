package com.github.pradeeppappu.apdf;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PDFPage extends Fragment {

    private static final String DATA_URL = "dataUrl";
    private static final String BITMAP_QUALITy = "bitmapQuality";

    private Bitmap mBitmap;
    private PageView mPageView;
    private TextView mTextView;
    private RelativeLayout mRelativeLayout;

    public static PDFPage newInstance(String dataUrl,  int bitmapQuality) {
        PDFPage fragment = new PDFPage();
        Bundle arguments = new Bundle();
        arguments.putString(DATA_URL, dataUrl);
        arguments.putInt(BITMAP_QUALITy, bitmapQuality);
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
        int bitmapQuality = getArguments().getInt(BITMAP_QUALITy);
        if (dataUrl != null) {
            setBitmap(dataUrl, context, bitmapQuality);
            mPageView.setImageBitmap(mBitmap);
            mPageView.setVisibility(View.VISIBLE);
            mTextView.setVisibility(View.GONE);
        } else {
            mPageView.setVisibility(View.GONE);
            mTextView.setVisibility(View.VISIBLE);
        }
        return mRelativeLayout;
    }

    private void setBitmap(String dataUrl, Context context, int bitmapQuality) {
       if (mBitmap != null)
            mBitmap.recycle();
        mBitmap = Utils.getBitmap(dataUrl, context, bitmapQuality);
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
