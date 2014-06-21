package com.github.pradeeppappu.apdf;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

/**
 * Created by pappu on 11/06/2014.
 */
public class PDFFragment extends Fragment {
    public static final String ARG_URL = "argUrl";
    public static final String ARG_SCALE = "argScale";
    public static final String ARG_DEBUG_JS = "argDebugJS";

    public static final String STATE_PAGE_COUNT = "statePageCount";
    public static final String TAG = "PDFFragment";
    private String mUrl;
    private float mScale;
    private int mPageCount = 1;
    private boolean isBelowHCMR2 = false;
    private WebView mWebView;
    private PDFPager mPager;
    private PDFJavascriptInterface mJSInterface;
    private PDFPagerAdapter mPageAdapter;

    public static PDFFragment newInstance(String url, float scale, boolean debugJs) {
        PDFFragment fragment = new PDFFragment();
        Bundle arguments = new Bundle();
        arguments.putString(ARG_URL, url);
        arguments.putFloat(ARG_SCALE, scale);
        arguments.putBoolean(ARG_DEBUG_JS, debugJs);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "Enter: onCreateView");
        Log.d(TAG, "OS version " + Build.VERSION.SDK_INT);
        if (container == null) {
            Log.e(TAG, "Container is null. Add the fragment using the below syntax.\n getSupportFragmentManager().beginTransaction().add(android.R.id.content, fragment, \"PDF\").commit();");
            throw new RuntimeException("Container is null");
        }

        Context context = container.getContext();
        mWebView = new WebView(context);
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                String msg = consoleMessage.sourceId() + "(" + consoleMessage.lineNumber() + ")\n" + consoleMessage.message();
                Log.d(TAG, msg);
                return true;
            }
        });
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.d(TAG, "WebView loading url : " + url);
                super.onPageStarted(view, url, favicon);
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            setupWebViewLayer();

        WebSettings settings = mWebView.getSettings();
        settings.setAllowFileAccess(true);
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            setupWebViewFileAccess();

        mJSInterface = new PDFJavascriptInterface();
        mWebView.addJavascriptInterface(mJSInterface, "PDFAND");
        mPager = new PDFPager(context);
        mPager.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mPager.setId(android.R.id.custom);
        mPageAdapter = new PDFPagerAdapter(getChildFragmentManager());
        mPager.setAdapter(mPageAdapter);
        mPager.setOffscreenPageLimit(1);
        Log.d(TAG, "Exit: onCreateView");
        return mPager;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupWebViewLayer() {
        Log.d(TAG, "setupWebViewLayer");
        mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setupWebViewFileAccess() {
        Log.d(TAG, "setupWebViewFileAccess");
        mWebView.getSettings().setAllowFileAccessFromFileURLs(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");
        if (savedInstanceState == null) {
            mUrl = getArguments().getString(ARG_URL);
            mScale = getArguments().getFloat(ARG_SCALE);
            boolean debug = getArguments().getBoolean(ARG_DEBUG_JS);
            Log.d(TAG, "loading viewer.html");
            isBelowHCMR2 = Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR1;
            String url;
            if (isBelowHCMR2)
                url = "file:///android_asset/pdfjs/viewer.2.3.3.html";
            else
                url = "file:///android_asset/pdfjs/viewer.html";
            mWebView.loadUrl(url + "?file=" + mUrl + "&scale=" + mScale + "&debug=" + debug);
        } else {
            mWebView.restoreState(savedInstanceState);
            mPageCount = savedInstanceState.getInt(STATE_PAGE_COUNT);
            notifyDataSetChanged();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mWebView.saveState(outState);
        outState.putInt(STATE_PAGE_COUNT, mPageCount);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mWebView != null) {
            mWebView.stopLoading();
            mWebView.destroy();
            mWebView = null;
        }
    }

    public void getPageAt(int pageNum, OnPageLoadedListener pageLoadedListener) {
        if (pageNum >= mPageCount)
            throw new IndexOutOfBoundsException("" + pageNum + " is invalid. Max. noOfPages is " + mPageCount);
        mJSInterface.getPage(pageNum + 1, pageLoadedListener);
    }

    public int getPageCount() {
        return mPageCount;
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

    public interface OnPageLoadedListener {
        void onPageLoaded(int index, String dataUri);
    }

    public class PDFJavascriptInterface {
        private static final String JS_CALL_GET_PAGE = "javascript:if(typeof getPage !== 'undefined') getPage(%1$s, %2$s)";
        private static final String JS_CALL_LOAD_ALL_PAGES = "javascript:if(typeof loadAllPages !== 'undefined') loadAllPages(function(){})";
        private OnPageLoadedListener[] pageLoadedListeners;

        public void getPage(int index, OnPageLoadedListener pageLoadedListener) {
            Log.d(TAG, "getPage " + index);
            if (pageLoadedListeners == null)
                pageLoadedListeners = new OnPageLoadedListener[index];
            pageLoadedListeners[index - 1] = pageLoadedListener;
            Log.d(TAG, "Setting pageLoadedListener for page " + index);
            loadUrl(String.format(JS_CALL_GET_PAGE, index, "function(index, page){ PDFAND.setPage(index-1, page, true); }"));
        }

        public void loadAllPages() {
            loadUrl(JS_CALL_LOAD_ALL_PAGES);
        }

        @JavascriptInterface
        public void setNumOfPages(int numOfPages) {
            Log.d(TAG, "Number of pages : " + numOfPages);
            OnPageLoadedListener[] currentPageLoadedListeners = pageLoadedListeners == null ? null : pageLoadedListeners.clone();
            pageLoadedListeners = new OnPageLoadedListener[numOfPages];
            if (currentPageLoadedListeners != null) {
                Log.d(TAG, "Reloading listeners " + currentPageLoadedListeners.length);
                int i = 0;
                for (OnPageLoadedListener listener : currentPageLoadedListeners) {
                    Log.d(TAG, "Reloading listener for page " + (i + 1));
                    pageLoadedListeners[i++] = listener;
                }
            }
            mPageCount = numOfPages;
            notifyDataSetChanged();
        }

        @JavascriptInterface
        public void setPage(int index, String pageImg, boolean callOnPageLoadedListener) {
            Log.d(TAG, "setPage " + (index + 1));
            if (callOnPageLoadedListener && pageLoadedListeners != null && pageLoadedListeners[index] != null) {
                Log.d(TAG, "setPage " + (index + 1) + " invoking pageLoadedListener");
                pageLoadedListeners[index].onPageLoaded(index, pageImg);
            }
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
            Log.d(TAG, "Creating page " + (position + 1));
            final PDFPage page = PDFPage.newInstance(position, mScale);
            mJSInterface.getPage(position + 1, new OnPageLoadedListener() {
                @Override
                public void onPageLoaded(int index, String dataURI) {
                    Log.d(TAG, "Loaded Page " + (index + 1));
                    page.getArguments().putString(PDFPage.DATA_URL, dataURI);
                    page.drawBitmap(page.getActivity());
                }
            });
            return page;
        }

        @Override
        public int getCount() {
            return mPageCount;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            Log.d(TAG, "Destroying page " + (position + 1));
            super.destroyItem(container, position, object);
        }

        @Override
        public int getItemPosition(Object object) {
            PDFPage page = (PDFPage) object;
            Log.d(TAG, "Is Page Loaded " + page.isLoaded());
            if (page.isLoaded())
                return super.getItemPosition(object);
            return POSITION_NONE;
        }
    }
}
