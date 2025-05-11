package org.astral.findmaimaiultra.ui;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import org.astral.findmaimaiultra.R;

public class WebsiteActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fullscreen_webview_dialog);

        webView = findViewById(R.id.webView);

        // 获取 SharedPreferences
        SharedPreferences setting = getSharedPreferences("setting", MODE_PRIVATE);
        String sessionId = setting.getString("sessionId", "");

        // 启用 JavaScript
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // 设置 WebViewClient 在页面加载完成后注入 JS
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                // 注入 JavaScript 设置 localStorage.sessionId
                String jsCode = "localStorage.setItem('sessionId', '" + sessionId + "');";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    webView.evaluateJavascript(jsCode, null);
                } else {
                    webView.loadUrl("javascript:" + jsCode);
                }
            }
        });

        // 从 Intent 获取要加载的 URL
        String url = getIntent().getStringExtra("url");
        if (url != null && !url.isEmpty()) {
            webView.loadUrl(url);
        } else {
            finish(); // 如果没有 URL，直接关闭 Activity
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
