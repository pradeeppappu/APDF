package com.github.pradeeppappu.apdf;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;

/**
 * Created by pappu on 11/06/2014.
 */
public class PDFFragment extends Fragment {
    public static final String ARG_URL = "argUrl";
    public static final String ARG_SCALE = "argScale";
    public static final String STATE_PAGES = "statePages";
    private static final String TAG = PDFFragment.class.getName();
    private String mUrl;
    private float mScale;
    private String[] mPages = new String[1]; // Images of all the pages.

    private WebView mWebView;
    private PDFPager mPager;
    private PDFJavascriptInterface mJSInterface;
    private PDFPagerAdapter mPageAdapter;
    private ViewPager.OnPageChangeListener mPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            Log.d(TAG, "Page selected : " + position);
            if(mPages[position] == null)
                mJSInterface.getPage(position + 1);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    public static PDFFragment newInstance(String url, float scale) {
        PDFFragment fragment = new PDFFragment();
        Bundle arguments = new Bundle();
        arguments.putString(ARG_URL, url);
        arguments.putFloat(ARG_SCALE, scale);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
            setHasOptionsMenu(true);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null) {
            Log.e(TAG, "Container is null. Add the fragment using the below syntax.\n getSupportFragmentManager().beginTransaction().add(android.R.id.content, fragment, \"PDF\").commit();");
            throw new RuntimeException("Container is null");
        }

        Context context = container.getContext();
        mWebView = new WebView(context);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        WebSettings settings = mWebView.getSettings();
        settings.setAllowFileAccess(true);
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            settings.setAllowFileAccessFromFileURLs(true);
        mJSInterface = new PDFJavascriptInterface();
        mWebView.addJavascriptInterface(mJSInterface, "PDFAND");
        mPager = new PDFPager(context);
        mPager.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mPager.setId(android.R.id.custom);
        mPageAdapter = new PDFPagerAdapter(getChildFragmentManager());
        mPager.setAdapter(mPageAdapter);

        mPager.setOnPageChangeListener(mPageChangeListener);
        return mPager;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            setHasOptionsMenu(true);
        }
        if(savedInstanceState == null) {
            mUrl = getArguments().getString(ARG_URL);
            mScale = getArguments().getFloat(ARG_SCALE);
            mWebView.loadUrl("file:///android_asset/pdfjs/viewer.html?file=" + mUrl + "&scale=" + mScale);
        } else {
            mWebView.restoreState(savedInstanceState);
            mPages = savedInstanceState.getStringArray(STATE_PAGES);
            notifyDataSetChanged();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mWebView.saveState(outState);
        outState.putStringArray(STATE_PAGES, mPages);
        super.onSaveInstanceState(outState);
    }

    public Bitmap getPageAt(int pageNum) {
        if(pageNum < mPages.length && !TextUtils.isEmpty(mPages[pageNum]))
            return Utils.getBitmap(mPages[pageNum]);
        return null;
    }

    public int getPageCount() {
        if(mPages == null)
            return 0;
        return mPages.length;
    }

    public String[] getPages() {
        return mPages;
    }

    private void notifyDataSetChanged() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPageAdapter.notifyDataSetChanged();
            }
        });
    }

    private void loadUrl(final String url) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mWebView != null)
                    mWebView.loadUrl(url);
            }
        });
    }

    public class PDFJavascriptInterface {
        private static final String JS_CALL_GET_PAGE = "javascript:getPage(%1$s)";
        private static final String JS_CALL_GET_ALL_PAGES = "javascript:getAllPages()";

        public void getPage(int index) {
            loadUrl(String.format(JS_CALL_GET_PAGE, index));
        }

        public void getAllPages() {
            loadUrl(JS_CALL_GET_ALL_PAGES);
        }

        @JavascriptInterface
        public void setNumOfPages(int numOfPages) {
            Log.d(TAG, "Number of pages : " + numOfPages);
            if(mPages.length < numOfPages) { // Eliminating a re-initalisation after a pause/resume
                mPages = new String[numOfPages];
                notifyDataSetChanged();
            }
        }

        @JavascriptInterface
        public void setPage(int index, String pageImg) {
            Log.d(TAG, "setPage(" + index + ", " + pageImg + ")");
            mPages[index] = pageImg;
            notifyDataSetChanged();
        }

        @JavascriptInterface
        public void setPages(String[] pages) {
            mPages = pages;
            notifyDataSetChanged();
        }
    }

    private class PDFPager extends VerticalViewPager {
        public PDFPager(Context context) {
            super(context);
        }

        public PDFPager(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        protected boolean canScroll(View v, boolean checkV, int dy, int x, int y) {
            if (v instanceof PageView) {
                return ((PageView) v).canScrollVerticallyFroyo(-dy);
            }
            return super.canScroll(v, checkV, dy, x, y);
        }
    }

    private class PDFPagerAdapter extends FragmentStatePagerAdapter {
        public PDFPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PDFPage.newInstance(mPages[position]);
        }

        @Override
        public int getCount() {
            return mPages.length;
        }

        @Override
        public int getItemPosition(Object object) {
            PDFPage page = (PDFPage) object;
            if(page.isLoaded())
                return super.getItemPosition(object);
            return POSITION_NONE;
        }
    }
}
