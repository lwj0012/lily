package com.fatjay.subfunction;

import com.fatjay.R;
import com.fatjay.main.LilyActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

public class searchBoard extends Activity implements OnClickListener {
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.searchboard);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, boards);
        AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.search_edit);
        textView.setAdapter(adapter);
        Button mButton = (Button)findViewById(R.id.search_go);
        mButton.setOnClickListener(this);
    }
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.search_edit);
		String boardname = textView.getText().toString();
		Intent mIntent = new Intent();
		Bundle mBundle = new Bundle();
		mBundle.putString("url", "http://bbs.nju.edu.cn/bbstdoc?board=" + boardname);
		mIntent.putExtras(mBundle);
		mIntent.setClass(searchBoard.this, threadList.class);
		searchBoard.this.startActivity(mIntent);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	        if(keyCode == KeyEvent.KEYCODE_BACK){
	        	LilyActivity.instance.switchActivity(0);
	            return true;
	        }else{
	            return super.onKeyDown(keyCode, event);
	        }
	    }
	
	static final String[] boards = new String[] {"1937_12_13","7th_Art","Abroad","AcademicReport","Actuary",
		"AD_Art","Advice","Aerospace","Agent","AI","AlbumShow","Algorithm","America","AnHui","Announce","AntiMalfeasant",
		"AntiRumor","Apple","Archaism","Archeology","ASCIIArt","Association_Union","Astrology","AtmosphereSci","AutoSpeed",
		"Badminton","Basketball","BBSDev","BBSHelp","bbslists","BeiJing","Billiards","BitTorrent","Bless","Blog","BMManager",
		"BNGames","Board","BoardGame","BoardManage","BodilyForm","Borland","Bowling","Boxing_Fight","Boys","Bridge","Britain",
		"Budaixi","bulletin","C_Inter","Calligraphy","CAS","CCAS","CCP","CFD","ChangZhou","Chat","Chemistry","Chess","ChinaFootball",
		"ChinaNews","Chorus","Chrematistics","Christianity","ChunQiu_ZhanGuo","Civil_Servant","Classical_Poem","ClassicalCulture",
		"ClassicalMusic","Collections","Comic","Complain","Computer_ABC","Consultant","Contest","Couplet","CPA","CPlusPlus","CPU",
		"Cross_Strait","CrossShow","Cube","CUG","CUMT","Cycling","D_Chinese","D_Computer","D_EarthScience","D_EE","D_History",
		"D_Materials","D_Maths","D_Philosophy","D_Physics","D_SocialSec","Dance","Database","Debate","Detective","Deutsch","DigiMusic",
		"DigitalWorld","DII","Discovery","DiscZone","DongBei","DotaAllstars","DotNet","Drama","Drawing","Dream","Drink","DV_Studio",
		"E_Business","E_Sports","EarthSciences","Economics","Education","EEtechnology","ElectronicMusic","Embedded","Emprise","English",
		"EnglishCorner","Esperanto","Esquire","ExcellentBM","ExchangeStudent","F1","F_Literature","FairyTale","FamilyLife","FanBu",
		"Fantasy","Fashion","FDU","FEA","Feelings","Fiction","Finance","Fishing","Fitness","Flash","FleaMarket","Flowers","Folk_Country",
		"Folk_Music","FOOD","Fortran","Forum","French","Friendship","FuJian","GAFA","GEC","Geography","Girls","GIS","GJ","GoToUniversity",
		"Graphics","GRE_TOEFL","GreatTurn","GreeceRome","GreenEarth","GuangDong","GuangXi","Guilt","Guitar","GuQin","Hacker","HaiNan",
		"HandiCraft","Hardware","HeBei","HeNan","HHU","HiFi","History","HKU","Hometown","HomoSky","HotZone","HPC","HuaiAn","HuBei","Human",
		"HuNan","ID","IELTS","IFA_IS","IFIS","Image","Info_Manage","Inner_Mongolia","Intern","IR","ITClub","ITExam","J_Ent","Japanese",
		"Java","Jazz_Blues","JiangXi","JLU","JobAndWork","JobExpress","Joke","Journalism","JSSports","KaoYan","KaraOK","Korea","Law",
		"LectureHall","LianYunGang","Life","LifeLeague","LifeScience","LilyDigest","LilyFestival","LilyLinks","LilyStudio","Linguistics",
		"LinuxUnix","LostToFind","Love","LSCMA","LZU","M_Academic","M_CMHER","M_Gonghui","M_Graduate","M_GraduateUnion","M_Guard",
		"M_Hospital","M_Job","M_League","M_Library","M_Logistic","M_NIC","M_Student","M_StudentUnion","Magic","MaJiang","Management",
		"MARC","Marketing_Zone","Marvel","Mathematics","MathTools","Mediastudy","Medicine","Memory","Microwave","Military","Mobile",
		"Model_Space","Modern_Poem","Movies","MSTClub","MSWindows","MudLife","Musical","Mythlegend","Names","NanJing","NanoST","NanTong",
		"Nature","NetResources","Network","NewAge","newcomers","Nirvana","NJ_HOUSE","NJAU","NJMU","NJNU","NJU_Graduate","NJU_HOME",
		"NJU_TIC","NJU_Youth","NJU_zhixing","NJUExpress","NJUMUN","NJUPT","NJUT","NKU","NoteBook","notepad","Novel","NUAA","NUST","NZY",
		"OfficeStaff","OLGames","Olympics","Orchestra","OUC","OurCustom","Ourselves","Overseas","Paint","PartTimeJob","Party_of_Killer",
		"PCGames","Peer_Edu","PeerCounseling","People","PersonalCorpus","PetsEden","Philosophy","Photography","Physics","Piano","Pictures",
		"PKU","Politics","PopMusic","Postdoc","Program","Psychology","PuKouCampus","Python","QuYi","Radio","Reading","ReadyForJob",
		"RealEstate","RedCross","Renju","Riddle","RockMusic","RoomChating","RunForEver","Russia","S_Astronomy","S_Atmosphere","S_Business",
		"S_Chemistry","S_Education","S_Environment","S_ForeignLang","S_Geography","S_GOV","S_Graduate","S_Information","S_Journalism",
		"S_Law","S_LifeScience","S_Medicine","S_MSE","S_Sociology","S_Software","SanGuo","SAU","SCDA","Sculpture","SE_Association",
		"Seasons","SEU","ShanDong","ShangHai","ShanXi","Shopping","ShortMessage","Shows","SIFE_NJU","SiGuo","Single","SiYuan","SJ",
		"SJTU","Skating","Smoking","SoftEng","Software","SPA","Spanish","SportsNews","Stock","StoneCity","StoneStory","Story","Sudoku",
		"SuperGirls","SuQian","SuZhou","Swimming","sysop","TableTennis","Taekwondo","Taiwan","TaiZhou","TCM","Tennis","test","TeX",
		"Theoretical_CS","Thesis","THU","TianJian","TianJin","Tibet","Traffic_Info","Train","Travel","TV","TVGames","UrbanPlan",
		"US_JP_Research","USTC","V_Suggestions","VC","Vegetarian","Virus","Volleyball","Volunteer","vote","VoteBoard","WarAndPeace",
		"WebDesign","WebGames","WeiQi","West_Volunteer","WesternstyleChess","WHU","Wisdom","WorldFootball","WorldNews","WuShu","WuXi",
		"XiBei","XiNan","xinhongji","XinJiang","XJTU","XMU","XuZhou","YanCheng","YangTaiChi","YangtzeDelta","YangZhou","YOGA","ZheJiang",
		"ZhenJiang","ZhuangXiu","Zjl_Online","ZJU","ZSU", "�Ͼ�����ɱ", "��������", "��Խ����", "ѧ������", "���վ���", "�������"
		, "�ٺϹ���", "�", "����", "�˹�����", "����д��", "�㷨"
		, "������֮��", "��ˮ���", "վ�񹫸���", "��������", "��ҥ������", "ƻ������"
		, "���Ĺ�ֹ", "����", "ASCII����", "�������ϻ���ϯ����", "����ͯ��", "������ѧ��̳"
		, "��������", "��ë��", "����", "BBS�İ�װ������", "��������", "��վ�ĸ���ͳ���б����¼"
		, "�׶�����", "̨��", "���غ���", "��ܰף��", "������̳", "�����ල�͹���"
		, "�ϴ�ս����Ϸ", "������ѡ�����", "������Ϸ", "���濪������", "�߰�����", "Borland����"
		, "�������", "ȭ�����", "��������", "���������", "����Ӣ��", "����Ϸ"
		, "У�񹫸�", "����ѧԺ", "�й��鷨", "�й���ѧԺ", "��������", "��������"
		, "����������̳", "���峣��", "�����", "��ѧ��", "����", "�й�����"
		, "��������", "�������������ߣ��ϴ�ϳ��ţ�", "���"
		, "�������о�", "����ս�� ", "����Ա֮��", "�ŵ�ʫ��", "�����Ƴ�", "�ŵ�����"
		, "�ղذ���", "��������", "Ͷ����ٱ�", "���Բ���Ҫѧ��", "��ѯ", "��ѧ������"
		, "����", "ע����ʦ", "C++�����������", "�й�ҩ�ƴ�ѧ", "��Խ��Ͽ", "ʮ���巻"
		, "ħ��", "�й����ʴ�ѧ", "�й���ҵ��ѧ", "���г��˶�", "����ϵ", "�����ϵ"
		, "�����ѧ�빤��ѧԺ", "���ӿ�ѧ�빤��ѧԺ", "��ʷϵ"
		, "���Ͽ�ѧϵ", "��ѧϵ", "��ѧϵ", "����ѧԺ", "��ᱣ��ϵ", "�赸���"
		, "���ݿ�ϵͳ", "�����޵�", "��̽����", "������", "���������豸", "��������"
		, "������ѧԺ_ǿ����", "����֮��", "��������", "��ɽ��ˮ", "Զ��֮�ػ�", ".net����", "Ϸ�紺��", "�滭����", "�ഺ����"
		, "�ư��뿧�ȹ�", "DV������", "������������", "���Ӿ���", "�����ѧ", "����ѧ", "������̳", "���Ӽ�����", "��������"
		, "Ƕ��ʽϵͳ", "����С˵", "Ӣ������", "Ӣ�������", "��˵����", "ʱ������"
		, "��������", "���������", "һ������ʽ����", "�����ѧ", "��ɫ��", "��ͥ����"
		, "����ѧ��", "������", "�������з�", "������ѧ", "���⽻��Э��", "��������"
		, "�ƻ�����", "��������", "�̲�����", "�����뽡��", "��������", "�����г�"
		, "����԰��", "��ҥ���������", "����Ʈ�㣨�����ţ�", "����̸��", "Fortran����", "�ٺ���̳"
		, "����������", "����þ�", "��������", "���İ�����Э��", "�о���Ӣ����ֲ�", "�����ѧ"
		, "Ů�����", "������Ϣ��ѧ", "��������", "�߿�������Ϣ", "����ͼ�δ���", "GRE&TOEFLר������"
		, "�Խת��", "ϣ������", "�ϴ󻷾�����Э��", "���ǹ�����", "�˹���", "���뷣"
		, "����", "������", "�ڿ͵�ҡ��", "���ĺ���", "�����ֹ�", "Ӳ��������"
		, "���Դ��", "����֮��", "�Ӻ���ѧ", "��������", "��ʷ", "��۴�ѧ"
		, "��������", "ͬһƬ���", "ս��", "�����ܼ���", "��ˮ����", "�������"
		, "������̸", "������ˮ", "������ĸ�ͷ��", "��̸��˼", "�����о�Ժ_���������о���", "�������ѧԺ"
		, "ͼ������", "��Ϣ������", "�������", "ʵϰ", "���ʹ�ϵ", "IT���ֲ�"
		, "IT��֤����", "�ͷ���Ӱ", "����ѧϰ", "Java����", "��ʿ����", "��������"
		, "���ִ�ѧ", "��ҵ����ְ", "��ҵ�ؿ�", "Ц����", "���Ŵ����о�", "��������"
		, "�������", "������ԶOK", "��������", "����ѧ", "ѧ������", "����ɽ��"
		, "����", "����Э��", "������ѧ", "�ٺϾ���", "�ٺ�վ��", "�ٺ���������"
		, "С�ٺϹ�������Ŀ����", "����������ѧ", "Linux��Unix", "ʧ������", "�鰮����", "�����빩Ӧ������Э��"
		, "���ݴ�ѧ", "���񴦴�������", "����������������", "������ϯ�����", "�о���ԺԺ������", "�о�������ϯ����"
		, "��������������", "УҽԺԺ������", "��ҵ��ҵָ��������������", "��ί�������", "ͼ��ݹݳ�����", "���ڹ�������"
		, "����������������", "ѧ����������������", "ѧ������ϯ����", "ħ��", "�齫", "����ѧ"
		, "ģʽ�����о���", "Ӫ��ѧ��", "�����", "��ѧ��", "��ѧ�������", "ý���Ļ��о�"
		, "ҽѧ�뽡��", "��ˮ����", "��ų���΢������", "���¿�ѧ", "�ֻ����", "ģ�Ϳռ�"
		, "�ִ�ʫ��", "¶���ӰԺ", "΢�������ֲ�", "������΢����", "MUD����", "���־�֮��"
		, "�񻰴�˵", "�����Ļ�", "�Ŷ��Ͼ�", "���׿Ƽ�", "���纣��", "������Ȼ"
		, "������Դ", "��������", "����������", "������·", "���˫��԰", "��������"
		, "�Ͼ�ũҵ��ѧ", "�Ͼ�ҽ�ƴ�ѧ", "�Ͼ�ʦ����ѧ", "�ϴ��о�����", "�ϴ��԰", "�Ͼ���ѧ��Ѷ���¾��ֲ�"
		, "�ϴ����걨", "�Ͼ���ѧ֪����", "�ϴ�У԰����", "ģ�����Ϲ�Э��", "�Ͼ��ʵ��ѧ", "�Ͼ���ҵ��ѧ"
		, "�Ͽ���ѧ", "�����ι��� ", "����������԰�", "С˵", "�Ͼ����պ����ѧ", "�Ͼ�����ѧ"
		, "�Ͼ���ҽҩ��ѧ", "�ϰ�һ��", "������Ϸ", "����ƥ���˶�", "�ϴ�������", "�й������ѧ"
		, "�������", "�ٺ�ԭ��", "��������", "Ϳѻ��̳", "��ְ������Ϣ", "ɱ�ֵ�ͯ��"
		, "������Ϸ", "�ഺ����԰", "����ѯ", "����", "�����ļ�", "������԰"
		, "��ѧ��˼��", "��Ӱ����", "����ѧ", "��������", "��ͼ��", "������ѧ"
		, "���ο�ѧ", "�����������", "��ʿ��֮��", "����Ա��������", "������", "��԰�羰��"
		, "Python����", "��Է��̸", "���������", "����", "���⹤����", "���ز�"
		, "��ʮ���˶�", "��������", "��һ���ϻ�", "ҡ��������", "����ҹ��", "�ﾶ"
		, "��ѩ����˹", "������ռ��ѧѧԺ", "������ѧѧԺ", "��ѧԺ", "��ѧ����ѧԺ", "�����о�Ժ"
		, "����ѧԺ", "�����ѧԺ", "����ѧԺ", "��������ѧԺ", "�о���֮��", "��Ϣ����ѧԺ"
		, "���Ŵ���ѧԺ", "��ѧԺ", "������ѧԺ", "ҽѧԺ", "���̹���ѧԺ", "���ѧԺ"
		, "���ѧԺ", "��������", "��������й滮ѧԺ", "ѧ��ְҵ��չЭ��", "��������", "���⺣�����������Ѱ棩"
		, "ŷ�����Ӿ�", "���ϴ�ѧ", "��³��δ��", "Ѱ�κ��ϻ�", "��������", "��������"
		, "���Ŵ����", "���մ��㳡", "���ʴ�ѧ����ҵ������", "����Ĺ�", "����һ��", "�ϴ�˼Դ��"
		, "��������", "�Ϻ���ͨ��ѧ", "�������", "��������", "�������", "������"
		, "�ϴ�ѧ������Э��", "��������", "��̳��Ѷ", "���з���", "ʯͷ��", "��¥����"
		, "���»�", "��������", "����Ů��", "��������", "���ն���", "��Ӿ"
		, "վ���Ĺ�����", "ƹ����", "��ȭ��", "����֮��", "����̩��", "������ҽ"
		, "�������", "����վ�ڲ��԰�", "�Ƽ������Ű�", "���ۼ������ѧ", "����", "�廪��ѧ"
		, "�ϴ��콡��", "�ź���������� ", "ѩ��ɣ��", "��ͨ��Ϣ", "��������", "��������"
		, "����", "������Ϸ", "���й滮", "�����о�", "�й��Ƽ���ѧ", "У������"
		, "Visual C++ ��", "��ʳ��", "�ɶ���µĲ���", "�����", "����־Ը��Э��", "��վ����ͶƱ����"
		, "ѡ�ٰ�", "����ú�", "��վ���", "��ҳ��Ϸ", "Χ��", "֧������"
		, "��������", "�人��ѧ", "���͵�", "��������", "��������", "�л�����"
		, "��Ϫ�ĳ�", "׳�մ�����", "��������", "�º����", "�����½��ǿ���", "������ͨ��ѧ"
		, "���Ŵ�ѧ", "������", "������", "��ʽ̫��ȭЭ��", "���������޷�չ��̳", "��ͤ����"
		, "�ϴ��٤", "Ǯ����", "�ų���", "�Ҿ�װ��", "�齭·����", "�㽭��ѧ"};

	
}
