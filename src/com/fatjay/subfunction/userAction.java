package com.fatjay.subfunction;

import java.io.IOException;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.fatjay.R;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.webkit.WebView;
import android.widget.TextView;

public class userAction extends Activity {
	String urlString;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.useraction);
		Bundle mBundle = getIntent().getExtras();
		urlString = mBundle.getString("url");
		String username = urlString.substring(urlString.indexOf("=") + 1);
		setTitle(username + "的个人资料");
		getUserInfo();
	}
	
	public void getUserInfo() {
		try {
			Document doc = Jsoup.connect(urlString).get();
			Element mElement = doc.select("textarea").get(0);
			String info = mElement.toString();
			WebView mTextView = (WebView)findViewById(R.id.user_info);
			/*
			info = Jsoup.parse(mElement.toString()).text();
			info = info.replaceAll("\\[m|\\[(0|[0-9]{1,2})m", "</font>");
			while (info.contains("[1;")) {
				String tempString = info.substring(info.indexOf("[1;"));
				tempString = (String) tempString.substring(0, tempString.indexOf("m")+1);
				info = info.replace(tempString, getFColorAll().get(tempString));
			}
			*/
			mTextView.loadData(info, "text/html", "gb2312");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static HashMap<String, String> getFColorAll()
	  {
	    HashMap localHashMap = new HashMap();
	    localHashMap.put("[1;31m", "</font><font color=red >");
	    localHashMap.put("[1;32m", "</font><font color=green >");
	    localHashMap.put("[1;33m", "</font><font color=#808000 >");
	    localHashMap.put("[1;34m", "</font><font color=blue >");
	    localHashMap.put("[1;35m", "</font><font color=#D000D0 >");
	    localHashMap.put("[1;36m", "</font><font color=#33A0A0 >");
	    localHashMap.put("[32m", "</font><font color=#808000 >");
	    localHashMap.put("[33m", "</font><font color=green >");
	    localHashMap.put("[37;1m", "");
	    localHashMap.put("[m", "");
	    return localHashMap;
	  }
	
}
