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
		about.setText(Html.fromHtml("Lily 0.3.0<br/>本软件遵循GPL许可，并且将于近期在Google Code上发布源代码，请有意参与开发的童鞋和我联系。<br/>在本软件的开发过程中，不少人给予巨大的帮助，在这里就不一一谢过了，没有你们就没有这款软件。<br/><br/><font color=\"red\">特别感谢我的女朋友吴小茜童鞋一直以来给予我的支持和鼓励，是你让我在准备放弃的时候重拾信心，鼓励我完成了这款软件！<font><br/>谢谢！"));                                                  
	}

}
