package com.github.pradeeppappu.apdf;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.github.pradeeppappu.R;


public class MainActivity extends ActionBarActivity {
    private static final String TAG = "APDF";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            Uri uri = getIntent().getData();
            String url = Uri.decode(uri.getEncodedPath());
            Log.i(TAG, "Opening File : " + url);
            PDFFragment fragment = PDFFragment.newInstance(url, 2, true);
            getSupportFragmentManager().beginTransaction().add(R.id.pdfWrapper, fragment, "PDF").commit();
        }
    }
}
