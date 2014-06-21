package com.github.pradeeppappu.apdf;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PDFPage extends Fragment {
    private static final String TAG = PDFPage.class.getName();
    public static final String DATA_URL = "dataUrl";
    private static final String SCALE = "scale";
    private static final String PAGE_NUM = "pageNum";

    private Bitmap mBitmap;
    private PageView mPageView;
    private TextView mTextView;
    private RelativeLayout mRelativeLayout;
    private int pageNumber;

    public static PDFPage newInstance(int pageNum, float mScale) {
        PDFPage fragment = new PDFPage();
        Bundle arguments = new Bundle();
        arguments.putInt(PAGE_NUM, pageNum);
        arguments.putFloat(SCALE, mScale);
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
        pageNumber = getArguments().getInt(PAGE_NUM);
        if(dataUrl != null) {
            drawBitmap(context);
            dataUrl = null;
        }
        return mRelativeLayout;
    }

    public void drawBitmap(final Context context) {
        Log.d(TAG, "drawing Bitmap for page " + (pageNumber + 1));
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                String dataUrl = getArguments().getString(DATA_URL);
                if (dataUrl != null) {
                    float scale = getArguments().getFloat(SCALE);
                    setBitmap(dataUrl, context == null ? getActivity() : context, scale);
                    dataUrl = null;
                    mPageView.setImageBitmap(mBitmap);
                    mPageView.setVisibility(View.VISIBLE);
                    mTextView.setVisibility(View.GONE);
                } else {
                    mPageView.setVisibility(View.GONE);
                    mTextView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public int getPageNumber() {
        return pageNumber;
    }

    private void setBitmap(String dataUrl, Context context, float scale) {
       if (mBitmap != null)
            mBitmap.recycle();
        if(dataUrl != null) {
            mBitmap = Utils.getBitmap(dataUrl, context, Math.round(scale));
            dataUrl = null;
        }
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
