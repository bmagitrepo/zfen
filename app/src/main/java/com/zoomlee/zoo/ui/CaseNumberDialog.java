package com.zoomlee.zoo.ui;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.ui.view.LoadingView;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @since 5/26/15
 */
public class CaseNumberDialog extends Dialog implements View.OnClickListener {
    private LoadingView loadingView;


    public CaseNumberDialog(Context context, String caseNumber) {
        super(context, R.style.AppTheme);
        setContentView(R.layout.dialog_case_number);
        String url = String.format("http://zoomle.demo.alterplay.com:80/v3/case-number/?receiptNum=%s", caseNumber);

        WebView webView = (WebView) findViewById(R.id.webView);
        loadingView = (LoadingView) findViewById(R.id.loading);
        webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                if (loadingView.getVisibility() == View.GONE)
                    loadingView.show();

                if (progress == 100)
                    loadingView.hide();
            }
        });

        findViewById(R.id.backBtn).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }
}