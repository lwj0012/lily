package com.fatjay.main;

import com.fatjay.R;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

public class about_the_author extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_the_author);
		setTitle("");
		TextView about = (TextView)findViewById(R.id.about_the_author);
		about.setText(Html.fromHtml("Lily 0.3.0<br/>�������ѭGPL��ɣ����ҽ��ڽ�����Google Code�Ϸ���Դ���룬��������뿪����ͯЬ������ϵ��<br/>�ڱ�����Ŀ��������У��ܶ�ͯЬ�������֧�֣������ܡ����ܡ����ӡ�����κ�ܣ�û�����Ǿ�û����������<br/><br/><font color=\"red\">�ر��л�ҵ�Ů������С��ͯЬһֱ���������ҵ�֧�ֺ͹���������������׼��������ʱ����ʰ���ģ����������������������Ȼ���ʱ�����㲻���ģ����ֲ����ġ����������Ұ��㣡�Ҳ�֪��Ϊʲô��������һЩ�ǳ�ɵ�Ƶ����顣�������ţ��Ҵ��ˣ�<font><br/>лл��"));                                                  
	}

}
