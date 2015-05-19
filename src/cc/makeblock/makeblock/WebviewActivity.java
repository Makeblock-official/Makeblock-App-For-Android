package cc.makeblock.makeblock;

import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class WebviewActivity extends Activity {
	private WebView mWebView;  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);
		mWebView = (WebView) findViewById(R.id.webview_help);
		mWebView.setWebViewClient(new WebViewClient(){       
            public boolean shouldOverrideUrlLoading(WebView view, String url) {       
                view.loadUrl(url);       
                return true;       
            } 
         });  
		String urlStr = getIntent().getExtras().getString("url");
		mWebView.loadUrl(urlStr);    
	}
	
	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
}
