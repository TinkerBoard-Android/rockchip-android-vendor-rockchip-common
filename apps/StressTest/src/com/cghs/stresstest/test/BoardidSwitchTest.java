package com.cghs.stresstest.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.cghs.stresstest.R;
import com.cghs.stresstest.util.StresstestUtil;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class BoardidSwitchTest extends StressBase implements OnClickListener {
	public final static String LOG_TAG = "BoardidSwitchTest";
	public final static String BOARD_ID_SWITCH_PATH = "/mnt/external_sd/boardid_test.state";

	private static final int MAX_AREA_ID = 109;
	private String BID_SAMPLE = "0000004414000000000202020b010b000000000002070001010003010000010100010100000000000000000000";

	private TextView mMaxView;
	private TextView mTestTimeTv;
	private TextView mCountdownTv;
	private TextView mBidTv;
	private TextView mResultTv;
	private Button mGetBidBtn;
	private Button mSetBidBtn;

	private int mAreaId = 0;
	private int mNowAreaId = 0;
	private int mStartTest = 0;

	private String mComapreErrMsg;
	private boolean mCompareResult = false;

	private CountDownTimer mCountDownTimer;
	private CustXmlHelper mCustXmlHelper;

	private HashMap<String, String> mStateMap = new HashMap<String, String>();
	private ArrayList<String> mRmAppList = new ArrayList<String>();
	private ArrayList<String> mCpAppList = new ArrayList<String>();
	private ArrayList<String> mCpLibList = new ArrayList<String>();
	private ArrayList<String> mSystemAppList = new ArrayList<String>();
	private ArrayList<String> mSystemLibList = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_boardid_switch_test);
		initRes();
		initData();

	}

	private void initRes() {
		setDefaultBtnId(R.id.start_btn, R.id.stop_btn, R.id.exit_btn,
				R.id.maxtime_btn);
		mMaxView = (TextView) findViewById(R.id.maxtime_tv);
		mTestTimeTv = (TextView) findViewById(R.id.testtime_tv);
		mCountdownTv = (TextView) findViewById(R.id.countdown_tv);
		mResultTv = (TextView) findViewById(R.id.result_tv);
		mBidTv = (TextView) findViewById(R.id.boardid_tv);
		mGetBidBtn = (Button) findViewById(R.id.getboardid_btn);
		mGetBidBtn.setOnClickListener(this);
		mSetBidBtn = (Button) findViewById(R.id.setboardid_btn);
		mSetBidBtn.setOnClickListener(this);
		
//		mSetBidBtn.setVisibility(View.GONE);
		mMaxView.setVisibility(View.GONE);
		((Button) findViewById(R.id.maxtime_btn)).setVisibility(View.GONE);

	}

	private void initData() {
		listDir(new File("/system/app/"), mSystemAppList);
		listDir(new File("/system/lib/"), mSystemLibList);
		mAreaId = Integer.parseInt(getAreaidFromBid(getBoardId()), 16);

		try {
			mCustXmlHelper = new CustXmlHelper("/mnt/external_sd/cust.xml");
			mCustXmlHelper.parseXml("" + mAreaId);
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		StresstestUtil.readState(BOARD_ID_SWITCH_PATH, mStateMap);
		String value = mStateMap.get("areaid");
		if (value != null)
			mNowAreaId = Integer.valueOf(value);
		value = mStateMap.get("enable");
		if (value != null)
			mStartTest = Integer.valueOf(value);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (mStartTest == 1) {
			mCompareResult = compareInfo();
			if (mCompareResult) {
				mResultTv.setText("PASS!");
				mResultTv.setVisibility(View.VISIBLE);
			} else {
				mResultTv.setText("FAIL! Reason:" + mComapreErrMsg);
				mResultTv.setVisibility(View.VISIBLE);
				StresstestUtil.writeState(formatCompareErrLog(),
						"/mnt/external_sd/boardid_test_err.log", true);
			}
			preStartTest();
		}
	}

	@Override
	public void updateMaxTV() {
		super.updateMaxTV();
		mMaxView.setText(getString(R.string.max_test_time) + mMaxTestCount);
	}

	public void updateTestTimeTV() {
		mTestTimeTv.setText(getString(R.string.already_test_time)
				+ mCurrentCount);
		mTestTimeTv.setVisibility(View.VISIBLE);
	}

	@Override
	public void onStartClick() {
		preStartTest();
	}

	@Override
	public void onStopClick() {
		stopTest();
	}

	@Override
	public void onSetMaxClick() {

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.getboardid_btn:
			mBidTv.setText(getString(R.string.boardid_default) + getBoardId());
			break;

		case R.id.setboardid_btn:
			setBoardId(BID_SAMPLE);
			//setBoardIdByAreaid(++mAreaId);
			break;
		default:
			break;
		}
	}

	public void preStartTest() {
		isRunning = true;
		mStartTest = 1;
		mNowAreaId++;
		if (mNowAreaId > MAX_AREA_ID)
			mNowAreaId = 1;
		mCountDownTimer = new CountDownTimer(30000, 1000) {

			@Override
			public void onTick(long millisUntilFinished) {
				mCountdownTv.setText((millisUntilFinished / 1000) + "");
				mCountdownTv.setVisibility(View.VISIBLE);
			}

			@Override
			public void onFinish() {
				mCountdownTv.setVisibility(View.INVISIBLE);
				startTest();
			}
		}.start();
	}

	public void startTest() {
		incCurCount();
		StresstestUtil.writeState(formatStateContent(), BOARD_ID_SWITCH_PATH);
		setBoardIdByAreaid(mNowAreaId);

		try {
			bootCommand(this, "--wipe_all");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void stopTest() {
		isRunning = false;
		mStartTest = 0;
		mCurrentCount = 0;
		mMaxTestCount = 0;
		mNowAreaId = 0;
		mCountDownTimer.cancel();
		mCountdownTv.setVisibility(View.INVISIBLE);
		StresstestUtil.writeState(formatStateContent(), BOARD_ID_SWITCH_PATH);

	}
 

	// ===========================for A10 test===========================//
	/**
	 * Reboot into the recovery system with the supplied argument.
	 * 
	 * @param arg
	 *            to pass to the recovery utility.
	 * @throws IOException
	 *             if something goes wrong.
	 */
	private static File RECOVERY_DIR = new File("/cache/recovery");
	private static File COMMAND_FILE = new File(RECOVERY_DIR, "command");

	private static void bootCommand(Context context, String arg)
			throws IOException {
		RECOVERY_DIR.mkdirs(); // In case we need it
		COMMAND_FILE.delete(); // In case it's not writable

		FileWriter command = new FileWriter(COMMAND_FILE);
		try {
			command.write(arg);
			command.write("\n");
		} finally {
			command.close();
		}

		// Having written the command file, go ahead and reboot
		PowerManager pm = (PowerManager) context
				.getSystemService(Context.POWER_SERVICE);
		pm.reboot("recovery");

		throw new IOException("Reboot failed (no permissions?)");
	}

	static {
		System.loadLibrary("rkinfo");
	}

	public native String getBoardId();

	public native String setBoardId(String bid);

	public native int setBoardIdByAreaid(int areaid);

	public class CustXmlHelper {
		private XmlPullParser mParser;

		public CustXmlHelper(String path) {
			mParser = Xml.newPullParser();
			FileInputStream fin;
			try {
				fin = new FileInputStream(new File(path));
				mParser.setInput(fin, "UTF-8");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				e.printStackTrace();
			}
		}

		public void parseXml(String area_id) throws XmlPullParserException,
				IOException {
			mRmAppList.clear();
			mCpAppList.clear();
			boolean isFind = false;
			int eventType = mParser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				String tag = mParser.getName();
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					if (tag.equals("if")) {
						if (mParser.getAttributeName(1).equals("bid")
								&& mParser.getAttributeValue(1).equals(area_id)) {
							isFind = true;
						}
					} else if (tag.equals("set") && isFind) {

					} else if (tag.equals("rm") && isFind) {
						String appPath = mParser.getAttributeValue(0);
						String[] result = appPath.split("/");
						if (result.length == 3) {
							mRmAppList.add(result[2]);
						}
					} else if (tag.equals("cp") && isFind) {
						String appPath = mParser.getAttributeValue(1);
						String[] result = appPath.split("/");
						if (result.length == 3) {
							if (result[1].equals("app"))
								mCpAppList.add(result[2]);
							if (result[1].equals("lib"))
								mCpLibList.add(result[2]);
						}
					}
					break;
				case XmlPullParser.END_TAG:
					if (tag.equals("if") && isFind) {
						Log.e("cghs", "return ... ");
						return;
					}
					break;
				default:
					break;
				}
				eventType = mParser.next();
			}

		}
	}

	/**
	 * list the file name.
	 * 
	 * @param file
	 * @param list
	 */
	public static void listDir(File file, ArrayList<String> list) {
		if (file == null)
			return;

		if (file.isFile()) {
			list.add(file.getName());
		} else if (file.isDirectory() && file.listFiles() != null) {
			for (File tfile : file.listFiles()) {
				if (file.isFile()) {
					list.add(file.getName());
				} else {
					listDir(tfile, list);
				}
			}
		}
	}

	private String getAreaidFromBid(String bid) {
		return bid.subSequence(8, 10).toString();
	}

	public boolean compareInfo() {
		for (String rmapp : mRmAppList) {
			if (mSystemAppList.contains(rmapp)) {
				mComapreErrMsg = "Not rm " + rmapp;
				return false;
			}
		}

		for (String cpapp : mCpAppList) {
			if (!mSystemAppList.contains(cpapp)) {
				mComapreErrMsg = "Not cp" + cpapp;
				return false;
			}
		}

		for (String cplib : mCpLibList) {
			if (!mSystemLibList.contains(cplib)) {
				mComapreErrMsg = "Not cp " + cplib;
				return false;
			}
		}
		return true;
	}

	private String formatStateContent() {
		StringBuilder sb = new StringBuilder();
		sb.append("enable:").append(mStartTest).append("\n");
		sb.append("currenttime:").append(mCurrentCount).append("\n");
		sb.append("maxtime:").append(mMaxTestCount).append("\n");
		sb.append("areaid:").append(mNowAreaId).append("\n");
		return sb.toString();
	}

	private String formatCompareErrLog() {
		StringBuilder sb = new StringBuilder();
		sb.append(mNowAreaId + ":");
		sb.append(mComapreErrMsg).append("\n");
		return sb.toString();
	}

}
