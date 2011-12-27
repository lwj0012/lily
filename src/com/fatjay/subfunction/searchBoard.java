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
		"ZhenJiang","ZhuangXiu","Zjl_Online","ZJU","ZSU", "南京大屠杀", "第七艺术", "飞越重洋", "学术讲座", "保险精算", "广告艺术"
		, "百合共创", "宇航", "代理", "人工智能", "个人写真", "算法"
		, "美利坚之窗", "淮水皖风", "站务公告栏", "反腐倡廉", "反谣言中心", "苹果电脑"
		, "古文观止", "考古", "ASCII艺术", "社团联合会主席信箱", "星月童话", "大气科学论坛"
		, "车迷世界", "羽毛球", "篮球", "BBS的安装与设置", "新手求助", "本站的各类统计列表与记录"
		, "首都北京", "台球", "比特洪流", "温馨祝福", "博客论坛", "版主监督和管理"
		, "南大战网游戏", "版主竞选与请辞", "桌面游戏", "版面开设与变更", "高矮胖瘦", "Borland世界"
		, "保龄球馆", "拳击与格斗", "男生世界", "桥牌联谊会", "行走英伦", "布袋戏"
		, "校务公告", "国际学院", "中国书法", "中国科学院", "中美中心", "礼仪中心"
		, "计算流体论坛", "锦绣常州", "聊天版", "化学版", "象棋", "中国足球"
		, "国内新闻", "声乐艺术爱好者（南大合唱团）", "理财"
		, "基督教研究", "春秋战国 ", "公务员之家", "古典诗词", "古韵悠长", "古典音乐"
		, "收藏爱好", "动漫世界", "投诉与举报", "电脑菜鸟要学飞", "咨询", "大学生竞赛"
		, "对联", "注册会计师", "C++程序设计语言", "中国药科大学", "穿越海峡", "十字绣坊"
		, "魔方", "中国地质大学", "中国矿业大学", "自行车运动", "中文系", "计算机系"
		, "地球科学与工程学院", "电子科学与工程学院", "历史系"
		, "材料科学系", "数学系", "哲学系", "物理学院", "社会保障系", "舞蹈天地"
		, "数据库系统", "辩者无敌", "侦探推理", "德文堂", "数码音乐设备", "数码世界"
		, "匡亚明学院_强化部", "神秘之旅", "碟碟不休", "白山黑水", "远古之守护", ".net技术", "戏剧春秋", "绘画艺术", "青春有梦"
		, "酒吧与咖啡馆", "DV工作室", "电子商务特区", "电子竞技", "地球科学", "经济学", "教育论坛", "电子技术版", "电子音乐"
		, "嵌入式系统", "武侠小说", "英语世界", "英语聊天版", "世说新语", "时尚男生"
		, "版务评优", "交换生天地", "一级方程式赛车", "外国文学", "七色花", "家庭生活"
		, "反哺学社", "奇幻天地", "美丽流行风", "复旦大学", "对外交流协会", "感情世界"
		, "科幻世界", "金融天下", "碧波垂钓", "健美与健身", "闪客世界", "跳蚤市场"
		, "花草园艺", "民谣及乡村音乐", "国乐飘香（民乐团）", "雅舍谈吃", "Fortran语言", "百合论坛"
		, "浪漫法兰西", "友情久久", "八闽畅怀", "天文爱好者协会", "研究生英语俱乐部", "地理科学"
		, "女生天地", "地理信息科学", "够级艺术", "高考招生信息", "电脑图形处理", "GRE&TOEFL专题讨论"
		, "脑筋急转弯", "希腊罗马", "南大环境保护协会", "粤是故乡名", "八桂大地", "罪与罚"
		, "吉它", "古琴社", "黑客的摇篮", "天涯海角", "精致手工", "硬件工作室"
		, "燕赵大地", "九州之中", "河海大学", "发烧天堂", "历史", "香港大学"
		, "游子情深", "同一片天空", "战场", "高性能计算", "淮水楚云", "荆楚大地"
		, "人类漫谈", "三湘四水", "掀起你的盖头来", "清谈雅思", "美术研究院_雕塑艺术研究所", "海外教育学院"
		, "图像世界", "信息管理技术", "塞外风情", "实习", "国际关系", "IT俱乐部"
		, "IT认证考试", "和风艺影", "日语学习", "Java语言", "爵士蓝调", "江南西道"
		, "吉林大学", "创业与求职", "就业特快", "笑话版", "新闻传播研究", "江苏体育"
		, "考研天地", "卡拉永远OK", "恋恋韩风", "法律学", "学术交流", "花果山下"
		, "生活", "生命协会", "生命科学", "百合精华", "百合站庆", "百合友情链接"
		, "小百合工作室项目反馈", "语言与语言学", "Linux和Unix", "失物招领", "情爱悠悠", "物流与供应链管理协会"
		, "兰州大学", "教务处处长信箱", "心理中心主任信箱", "工会主席信箱版", "研究生院院长信箱", "研究生会主席信箱"
		, "保卫处处长信箱", "校医院院长信箱", "就业创业指导中心主任信箱", "团委书记信箱", "图书馆馆长信箱", "后勤工作信箱"
		, "网络中心主任信箱", "学生工作处处长信箱", "学生会主席信箱", "魔术", "麻将", "管理学"
		, "模式动物研究所", "营销学社", "鬼故事", "数学版", "数学工具软件", "媒介文化研究"
		, "医学与健康", "似水流年", "电磁场与微波技术", "军事科学", "手机天地", "模型空间"
		, "现代诗歌", "露天电影院", "微软技术俱乐部", "美丽的微软窗口", "MUD人生", "音乐剧之家"
		, "神话传说", "姓名文化", "古都南京", "纳米科技", "江风海韵", "人与自然"
		, "网络资源", "网络世界", "新世纪音乐", "新手上路", "桫椤双树园", "房屋租赁"
		, "南京农业大学", "南京医科大学", "南京师范大学", "南大研究生报", "南大和园", "南京大学腾讯创新俱乐部"
		, "南大青年报", "南京大学知行社", "南大校园生活", "模拟联合国协会", "南京邮电大学", "南京工业大学"
		, "南开大学", "本本梦工厂 ", "酸甜苦辣留言版", "小说", "南京航空航天大学", "南京理工大学"
		, "南京中医药大学", "上班一族", "网络游戏", "奥林匹克运动", "南大交响乐团", "中国海洋大学"
		, "民俗民风", "百合原创", "海外游子", "涂鸦论坛", "兼职工作信息", "杀手的童话"
		, "电脑游戏", "青春伊甸园", "朋辈咨询", "人物", "个人文集", "宠物乐园"
		, "哲学与思考", "摄影艺术", "物理学", "钢琴艺术", "贴图版", "北京大学"
		, "政治科学", "流行音乐天地", "博士后之家", "程序员的休闲室", "心理健康", "浦园风景线"
		, "Python语言", "曲苑杂谈", "空中梦想家", "读书", "阳光工作室", "房地产"
		, "红十字运动", "五子连珠", "射一射老虎", "摇滚乐世界", "寝室夜话", "田径"
		, "风雪俄罗斯", "天文与空间科学学院", "大气科学学院", "商学院", "化学化工学院", "教育研究院"
		, "环境学院", "外国语学院", "地理学院", "政府管理学院", "研究生之家", "信息管理学院"
		, "新闻传播学院", "法学院", "生命科学院", "医学院", "工程管理学院", "社会学院"
		, "软件学院", "三国风云", "建筑与城市规划学院", "学生职业发展协会", "雕塑艺术", "阳光海岸（华南网友版）"
		, "欧美电视剧", "东南大学", "齐鲁青未了", "寻梦海上花", "三晋梦萦", "购物天堂"
		, "短信大家聊", "综艺大秀场", "国际大学生企业家联盟", "缥缈四国", "单身一族", "南大思源社"
		, "升级艺术", "上海交通大学", "溜冰人生", "淡烟人生", "软件工程", "软件天地"
		, "南大学生心理协会", "西班牙语", "体坛快讯", "股市风云", "石头城", "红楼逸梦"
		, "故事会", "快乐数独", "超级女声", "西楚下相", "雨渍东吴", "游泳"
		, "站长的工作室", "乒乓球", "跆拳道", "宝岛之恋", "古韵泰州", "古意中医"
		, "网球天地", "这是站内测试版", "科技文献排版", "理论计算机科学", "论文", "清华大学"
		, "南大天健社", "九河下梢天津卫 ", "雪域桑烟", "交通信息", "汽笛声声", "遍览天下"
		, "电视", "电视游戏", "城市规划", "美日研究", "中国科技大学", "校长信箱"
		, "Visual C++ 版", "素食者", "可恶可怕的病毒", "排球版", "青年志愿者协会", "本站各项投票与结果"
		, "选举版", "百年好合", "网站设计", "网页游戏", "围棋", "支教岁月"
		, "国际象棋", "武汉大学", "儒释道", "世界足球", "国际新闻", "中华武术"
		, "梁溪寄畅", "壮哉大西北", "风起西南", "新鸿基社", "我们新疆亚克西", "西安交通大学"
		, "厦门大学", "古彭汉风", "登瀛渔火", "杨式太极拳协会", "长江三角洲发展论坛", "五亭烟雨"
		, "南大瑜伽", "钱江潮", "古城镇江", "家居装修", "珠江路热线", "浙江大学"};

	
}
