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
		about.setText(Html.fromHtml("Lily 0.3.0<br/>本软件遵循GPL许可，并且将于近期在Google Code上发布源代码，请有意参与开发的童鞋和我联系。<br/>在本软件的开发过程中，很多童鞋给予大力支持，如鑫总、苏总、涛子、奎球、魏总，没有你们就没有这款软件。<br/><br/><font color=\"red\">特别感谢我的女朋友吴小茜童鞋一直以来给予我的支持和鼓励，是你让我在准备放弃的时候重拾信心，鼓励我完成了这款软件！虽然这段时间让你不开心，各种不开心。。。但是我爱你！我不知道为什么会总是做一些非常傻逼的事情。。。老婆！我错了！<font><br/>谢谢！"));                                                  
	}

}
