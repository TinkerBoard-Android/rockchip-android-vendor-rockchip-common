package com.cghs.stresstest.test;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.R.integer;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.widget.EditText;
import android.widget.Toast;

import com.cghs.stresstest.R;

public class SmsSendTest extends StressBase {
	public static final String TAG = "SmsSendTest";

	private final String DEFAULT_NUM = "10086";
	private final String DEFAULT_CONTENT = "hi 10086";
	private final int DEFAULT_COUNT = 5;

	private EditText mPhoneEt;
	private EditText mContentEt;
	private EditText mSendCountEt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sms_send_test);
		setDefaultBtnId(R.id.start_btn, R.id.stop_btn, R.id.exit_btn, 0);
		initRes();

	}

	private void initRes() {
		mPhoneEt = (EditText) findViewById(R.id.et_phone);
		mPhoneEt.setText(DEFAULT_NUM);
		mContentEt = (EditText) findViewById(R.id.et_content);
		mContentEt.setText(DEFAULT_CONTENT);
		mSendCountEt = (EditText) findViewById(R.id.et_scount);
		mSendCountEt.setText(DEFAULT_COUNT + "");
	}

	@Override
	public void onStartClick() {

		String mobile = mPhoneEt.getText().toString();
		String content = mContentEt.getText().toString();
		String countStr = mSendCountEt.getText().toString();

		SmsManager smsManager = SmsManager.getDefault();

		if (mobile.length() <= 0) {

			Toast.makeText(SmsSendTest.this, "请输入号码", Toast.LENGTH_LONG).show();
		} else {
			if (countStr.length() <= 0) {
				Toast.makeText(SmsSendTest.this, "请输入发送次数", Toast.LENGTH_LONG)
						.show();

			} else {
				int count = Integer.parseInt(countStr);

				while (count-- > 0) {
					/* 检查收件人电话格式与简讯字数是否超过70字符 */
					if (PhoneNumberUtils.isGlobalPhoneNumber(mobile)) {
						try {
							/*
							 * 两个条件都检查通过的情况下,发送简讯 *
							 * 先建构一PendingIntent对象并使用getBroadcast
							 * ()方法进行Broadcast *
							 * 将PendingIntent,电话,简讯文字等参数传入sendTextMessage
							 * ()方法发送简讯
							 */
							// SmsManager manager =
							// SmsManager.getDefault();
							// smsManager.sendTextMessage("10086",null,"hi,this is sms",null,null);
							PendingIntent mPI = PendingIntent.getBroadcast(
									SmsSendTest.this, 0, new Intent(), 0);

							if (content.length() >= 70 || content.length() == 0) {
								if (content.length() >= 70) {

									// 短信字数大于70，自动分条
									List<String> ms = smsManager
											.divideMessage(content);

									for (String str : ms) {
										// 短信发送

										smsManager.sendTextMessage(mobile,
												null, content, mPI, null);
									}
								}
								if (content.length() == 0) {
									Toast.makeText(SmsSendTest.this, "请输入发送内容",
											Toast.LENGTH_LONG).show();
								}
							} else {
								smsManager.sendTextMessage(mobile, null,
										content, mPI, null);
							}

							Toast.makeText(SmsSendTest.this, "发送成功！",
									Toast.LENGTH_LONG).show();

						} catch (Exception e) {
							e.printStackTrace();
						}
						Toast.makeText(SmsSendTest.this, "短信成功!!",
								Toast.LENGTH_SHORT).show();
						// mEditText1.setText("");
						// mEditText2.setText("");
					} else {
						/* 电话格式不符 */
						if (isPhoneNumberValid(mobile) == false) {
							Toast.makeText(SmsSendTest.this,
									"电话号码格式错误或没有内容,请检查!!", Toast.LENGTH_SHORT)
									.show();

						}

					}
				}
			}
		}

	}

	@Override
	public void onStopClick() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSetMaxClick() {
		// TODO Auto-generated method stub

	}

	/* 检查字符串是否为电话号码的方法,并回传true or false的判断值 */
	public static boolean isPhoneNumberValid(String phoneNumber) {
		boolean isValid = false;
		/*
		 * 可接受的电话格式有：
		 */
		String expression = "^\\(?(\\d{3})\\)?[- ]?(\\d{3})[- ]?(\\d{5})$";
		/*
		 * 可接受的电话格式有：
		 */
		String expression2 = "^\\(?(\\d{3})\\)?[- ]?(\\d{4})[- ]?(\\d{4})$";
		CharSequence inputStr = phoneNumber;
		Pattern pattern = Pattern.compile(expression);
		Matcher matcher = pattern.matcher(inputStr);

		Pattern pattern2 = Pattern.compile(expression2);
		Matcher matcher2 = pattern2.matcher(inputStr);
		if (matcher.matches() || matcher2.matches()) {
			isValid = true;
		}
		if (phoneNumber == "10086" || phoneNumber == "10010") {
			isValid = true;
		}
		return isValid;
	}

}
