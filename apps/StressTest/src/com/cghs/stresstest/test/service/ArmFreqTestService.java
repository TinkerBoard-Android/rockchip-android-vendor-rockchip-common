package com.cghs.stresstest.test.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.cghs.stresstest.util.ArmFreqUtils;
import com.cghs.stresstest.R;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.os.SystemProperties;

public class ArmFreqTestService extends Service {

	private static final String TAG = "ArmFreqTestService";
	private static final boolean DEBUG = true;

	private static void LOG(String str) {
		if (DEBUG) {
			Log.v(TAG, str);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private static final int VIEW_WIDTH = 500;
	private static final int VIEW_HEIGHT = 300;
	public  static final int GPU_MODE_3168 = 0;
	public  static final int GPU_MODE_3188 = 1;
	
	private Context mContext = null;
	private View mView = null;
	private WindowManager wm = null;
	private WindowManager.LayoutParams wmParams = null;

	private int mCurrentX = 0;
	private int mCurrentY = 0;
	private int mStartX = 0;
	private int mStartY = 0;

	protected ArrayAdapter<String> mAdapter;
	private List<String> freqs = null;
	private List<String> freqs_bigCore = null;
	private List<String> gpu_freqs_3188 = null;
	private String[] gpu_freqs_3168 = { "1","5" };
	private String[] ddr_freqs = { "c:200M", "c:300M", "c:456M" };
	private int curFreq;
	private int gpu_mode = GPU_MODE_3188;

	private boolean isTest = false;
	private int mTestMode = FIX_TEST_MODE;
	private CheckBox fix_checkBox, fix_ddr_checkBox;
	private CheckBox random_checkBox, random_gpu_checkBox;
	private Spinner freq_spinner, ddr_spinner;
	private Spinner update_time_spinner, update_gputime_spinner;
	private TextView cur_frequency_text, cur_gpufreq_text, cur_ddrfreq_text;
	private Button test_btn;
	private Button cancel_btn;
	private Button exit_btn;

	private static final int FIX_TEST_MODE = 1;
	private static final int RANDOM_TEST_MODE = 2;
	private static final int GPU_TEST_MODE = 3;
	private static final int DDR_TEST_MODE = 4;
	private static int[] updateTimeList = null;
	private static int[] updateDDRList = null;
	private static String[] update_ddr_labels = null;
	private String uptateDDR;
	private String defDDR = "c:300M";
	private int uptateTime = 5;// 默认5s
	private int uptateTimeDDR = 36000;// 默认5s
        private static boolean hasbigCore = ("rk3399".equals(SystemProperties.get("ro.board.platform")));

	@Override
	public void onCreate() {
		LOG("~~~~~~~~~~~~~~~~~~onCreate()~~~~~~~~~~~~~~~~~~~~~~~");
		super.onCreate();
		mContext = this;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		LOG("~~~~~~~~~~~~~~~~~~onStart()~~~~~~~~~~~~~~~~~~~~~~~");
		super.onStart(intent, startId);
		isTest = false;
		mTestMode = FIX_TEST_MODE;
		this.createFloatingView(LayoutInflater.from(this),
				(ActivityManager) getSystemService(Context.ACTIVITY_SERVICE));
		updateTimeList = this.getResources().getIntArray(
				R.array.update_time_values);
		updateDDRList = this.getResources().getIntArray(
				R.array.update_timeddr_values);
		update_ddr_labels = this.getResources().getStringArray(
				R.array.update_ddr_labels);
	}

	private void createFloatingView(LayoutInflater inflater, ActivityManager am) {
		View view = inflater.inflate(R.layout.armfreq_view, null);
		mView = view.findViewById(R.id.root);
		wm = (WindowManager) mContext.getApplicationContext().getSystemService(
				"window");
		mView.setOnTouchListener(mTouchListener);
		initwmparams();
		wm.addView(mView, wmParams);
		setupViews();
	}

	private void setupViews() {
		freqs = ArmFreqUtils.getAvailableFrequencies();
               LOG("--------hasbigCore:"+hasbigCore);
               if(hasbigCore)
		freqs_bigCore = ArmFreqUtils.getAvailableFrequencies_bigCore();
		gpu_freqs_3188 = ArmFreqUtils.getAvailableGpuFreqs();

		mAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, freqs);
		mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		freq_spinner = (Spinner) mView.findViewById(R.id.freq_spinner);
		freq_spinner.setAdapter(mAdapter);
		fix_checkBox = (CheckBox) mView.findViewById(R.id.fix_checkBox);
		random_checkBox = (CheckBox) mView.findViewById(R.id.random_checkBox);
		random_gpu_checkBox = (CheckBox) mView
				.findViewById(R.id.random_gpu_checkBox);
		fix_ddr_checkBox = (CheckBox) mView.findViewById(R.id.fix_ddr_checkBox);
		update_time_spinner = (Spinner) mView
				.findViewById(R.id.update_time_spinner);
		update_gputime_spinner = (Spinner) mView
				.findViewById(R.id.update_gputime_spinner);
		ddr_spinner = (Spinner) mView.findViewById(R.id.ddr_spinner);
		cur_frequency_text = (TextView) mView
				.findViewById(R.id.cur_frequency_text);
		cur_gpufreq_text = (TextView) mView.findViewById(R.id.cur_gpufreq_text);
		cur_ddrfreq_text = (TextView) mView.findViewById(R.id.cur_ddrfreq_text);
		// cur_frequency_text.setText(ArmFreqUtils.getCurFrequencies()/1000+"M");
		test_btn = (Button) mView.findViewById(R.id.test_btn);
		cancel_btn = (Button) mView.findViewById(R.id.cancel_btn);
		exit_btn = (Button) mView.findViewById(R.id.exit_btn);
		fix_checkBox.setChecked(true);
		random_checkBox.setChecked(false);
		update_time_spinner.setEnabled(false);
		random_gpu_checkBox.setChecked(false);
		update_gputime_spinner.setEnabled(false);
		fix_ddr_checkBox.setChecked(false);
		ddr_spinner.setEnabled(false);
		freq_spinner.setEnabled(true);
		fix_checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isTest) {
					if (mTestMode == FIX_TEST_MODE) {
						showNoSelectAlert(R.string.fixed_frequency_text);
					} else if (mTestMode == RANDOM_TEST_MODE) {
						showNoSelectAlert(R.string.random_frequency_text);
					} else if (mTestMode == GPU_TEST_MODE) {
						showNoSelectAlert(R.string.random_gpufreq_text);
					} else {
						showNoSelectAlert(R.string.fixed_ddrfreq_text);
					}
				} else {
					if (isChecked) {
						mTestMode = FIX_TEST_MODE;
						random_checkBox.setChecked(false);
						update_time_spinner.setEnabled(false);
						random_gpu_checkBox.setChecked(false);
						update_gputime_spinner.setEnabled(false);
						fix_ddr_checkBox.setChecked(false);
						ddr_spinner.setEnabled(false);
						freq_spinner.setEnabled(true);
					}
				}
			}
		});
		random_checkBox
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isTest) {
							if (mTestMode == FIX_TEST_MODE) {
								showNoSelectAlert(R.string.fixed_frequency_text);
							} else if (mTestMode == RANDOM_TEST_MODE) {
								showNoSelectAlert(R.string.random_frequency_text);
							} else if (mTestMode == GPU_TEST_MODE) {
								showNoSelectAlert(R.string.random_gpufreq_text);
							} else {
								showNoSelectAlert(R.string.fixed_ddrfreq_text);
							}
						} else {
							if (isChecked) {
								mTestMode = RANDOM_TEST_MODE;
								fix_checkBox.setChecked(false);
								update_time_spinner.setEnabled(true);
								freq_spinner.setEnabled(false);
								random_gpu_checkBox.setChecked(false);
								update_gputime_spinner.setEnabled(false);
								fix_ddr_checkBox.setChecked(false);
								ddr_spinner.setEnabled(false);
							}
						}
					}
				});
		random_gpu_checkBox
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isTest) {
							if (mTestMode == FIX_TEST_MODE) {
								showNoSelectAlert(R.string.fixed_frequency_text);
							} else if (mTestMode == RANDOM_TEST_MODE) {
								showNoSelectAlert(R.string.random_frequency_text);
							} else if (mTestMode == GPU_TEST_MODE) {
								showNoSelectAlert(R.string.random_gpufreq_text);
							} else {
								showNoSelectAlert(R.string.fixed_ddrfreq_text);
							}
						} else {
							if (isChecked) {
								mTestMode = GPU_TEST_MODE;
								fix_checkBox.setChecked(false);
								update_time_spinner.setEnabled(false);
								freq_spinner.setEnabled(false);
								random_checkBox.setChecked(false);
								fix_ddr_checkBox.setChecked(false);
								ddr_spinner.setEnabled(false);
								update_gputime_spinner.setEnabled(true);
							}
						}
					}
				});
		fix_ddr_checkBox
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isTest) {
							if (mTestMode == FIX_TEST_MODE) {
								showNoSelectAlert(R.string.fixed_frequency_text);
							} else if (mTestMode == RANDOM_TEST_MODE) {
								showNoSelectAlert(R.string.random_frequency_text);
							} else if (mTestMode == GPU_TEST_MODE) {
								showNoSelectAlert(R.string.random_gpufreq_text);
							} else {
								showNoSelectAlert(R.string.fixed_ddrfreq_text);
							}
						} else {
							if (isChecked) {
								mTestMode = DDR_TEST_MODE;
								fix_checkBox.setChecked(false);
								update_time_spinner.setEnabled(false);
								freq_spinner.setEnabled(false);
								random_checkBox.setChecked(false);
								random_gpu_checkBox.setChecked(false);
								update_gputime_spinner.setEnabled(false);
								ddr_spinner.setEnabled(true);
							}
						}
					}
				});
		test_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isTest) {

				} else if (!fix_checkBox.isChecked()
						&& !random_checkBox.isChecked()
						&& !random_gpu_checkBox.isChecked()
						&& !fix_ddr_checkBox.isChecked()) {
					Toast.makeText(mContext, R.string.select_mode_alert,
							Toast.LENGTH_SHORT).show();
				} else if (random_gpu_checkBox.isChecked()) {
					LOG("----------------start test----------------mTestMode:"
							+ mTestMode);
					isTest = true;
					try {
						if (gpu_mode == GPU_MODE_3168)
							ArmFreqUtils.openGpuEcho();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
						// showAlertView();
					}
					int index = update_gputime_spinner
							.getSelectedItemPosition();
					update_gputime_spinner.setEnabled(false);
					if (index < 0 || index >= updateTimeList.length) {
						return;
					}
					uptateTime = updateTimeList[index];
					LOG("------------GPU_RANDOM_TEST_MODE--------------"
							+ uptateTime);
					if (task_gpu != null) {
						task_gpu.cancel();
						task_gpu = null;
					}
					
					if (gpu_mode == GPU_MODE_3188 && !setGpuRandomValueFor3188()) {
						return;
					} else if (gpu_mode == GPU_MODE_3168 && !setGpuRandomValue()) {
						return;
					}
					isExit = false;
					task_gpu = new TimerTask() {
						@Override
						public void run() {
							Message message = new Message();
							message.what = GPU_MSG;
							handler.sendMessage(message);
						}
					};
					timer_gpu.schedule(task_gpu, uptateTime, uptateTime);
				} else if (fix_ddr_checkBox.isChecked()) {
					LOG("----------------start test----------------mTestMode:"
							+ mTestMode);
					isTest = true;
					int index = ddr_spinner.getSelectedItemPosition();
					ddr_spinner.setEnabled(false);
					if (index < 0 || index >= updateDDRList.length) {
						return;
					}
					uptateTimeDDR = updateDDRList[index];
					LOG("-----------DDR_TEST_MODE--------------"
							+ uptateTimeDDR);
					/*
					 * try { ArmFreqUtils.setDDRFreq(uptateDDR); //
					 * cur_ddrfreq_text.setText(ArmFreqUtils.getCurDDR()); } catch
					 * (FileNotFoundException e) { e.printStackTrace(); } catch
					 * (IOException e) { e.printStackTrace(); //
					 * showAlertView(); }
					 * cur_ddrfreq_text.setText(ArmFreqUtils.getCurDDR());
					 */

					if (task_ddr != null) {
						task_ddr.cancel();
						task_ddr = null;
					}
					if (!setDDRRandomValue()) {
						return;
					}
					isExit = false;
					task_ddr = new TimerTask() {
						@Override
						public void run() {
							Message message = new Message();
							message.what = DDR_MSG;
							handler.sendMessage(message);
						}
					};
					timer_ddr.schedule(task_ddr, uptateTimeDDR, uptateTimeDDR);
				} else {
					LOG("----------------start test----------------mTestMode:"
							+ mTestMode);
					isTest = true;
					try {
						ArmFreqUtils.setGovernorMode(ArmFreqUtils.USERSPACE_MODE);
					     if(hasbigCore)
                                                ArmFreqUtils.setGovernorMode_bigCore(ArmFreqUtils.USERSPACE_MODE);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
						showAlertView();
						exitTest();
					} catch (IOException e) {
						e.printStackTrace();
						showAlertView();
						exitTest();
					}
					if (mTestMode == FIX_TEST_MODE) {
						freq_spinner.setEnabled(false);
						String str = freqs.get(freq_spinner
								.getSelectedItemPosition());
						int value = Integer.valueOf(str.split("M")[0]) * 1000;
						try {
							ArmFreqUtils.setSpeedFreq(value);
						} catch (FileNotFoundException e) {
							e.printStackTrace();
							showAlertView();
							exitTest();
						} catch (IOException e) {
							e.printStackTrace();
							showAlertView();
							exitTest();
						}
					} else if (mTestMode == RANDOM_TEST_MODE) {
						int index = update_time_spinner
								.getSelectedItemPosition();
						update_time_spinner.setEnabled(false);
						if (index < 0 || index >= updateTimeList.length) {
							return;
						}
						uptateTime = updateTimeList[index];
						LOG("------------RANDOM_TEST_MODE--------------"
								+ uptateTime);
						if (task != null) {
							task.cancel();
							task = null;
						}
						if (!setRandomValue()) {
							return;
						}
						isExit = false;
						task = new TimerTask() {
							@Override
							public void run() {
								Message message = new Message();
								message.what = TASK_MSG;
								handler.sendMessage(message);
							}
						};
						timer.schedule(task, uptateTime, uptateTime);
					}
				}
			}
		});
		cancel_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isTest) {
					if (mTestMode == FIX_TEST_MODE) {
						showNoTestAlertDialog(R.string.no_test_msg,
								R.string.fixed_frequency_text);
					} else if (mTestMode == RANDOM_TEST_MODE) {
						showNoTestAlertDialog(R.string.no_test_msg,
								R.string.random_frequency_text);
					} else if (mTestMode == GPU_TEST_MODE) {
						showNoTestAlertDialog(R.string.no_test_msg,
								R.string.random_gpufreq_text);
					} else {
						showNoTestAlertDialog(R.string.no_test_msg,
								R.string.fixed_ddrfreq_text);
					}
				}
			}
		});
		exit_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isTest) {
					if (mTestMode == FIX_TEST_MODE) {
						showNoTestAlertDialog(R.string.exit_msg,
								R.string.fixed_frequency_text);
					} else if (mTestMode == RANDOM_TEST_MODE) {
						showNoTestAlertDialog(R.string.exit_msg,
								R.string.random_frequency_text);
					} else if (mTestMode == GPU_TEST_MODE) {
						showNoTestAlertDialog(R.string.exit_msg,
								R.string.random_gpufreq_text);
					} else {
						showNoTestAlertDialog(R.string.exit_msg,
								R.string.fixed_ddrfreq_text);
					}
				} else {
					LOG("----------------stopService-------------------");
					wm.removeView(mView);
					if (alert_View != null) {
						wm.removeView(alert_View);
						alert_View = null;
					}
					Intent i = new Intent(mContext,ArmFreqTestService.class);
					mContext.stopService(i);
					// android.os.Process.killProcess(android.os.Process.myPid());
				}
			}
		});
	}

	private boolean setRandomValue() {
		int size = freqs.size();
		int randomIndex = new Random().nextInt(size);
		String str = freqs.get(randomIndex);
		int value = Integer.valueOf(str.split("M")[0]) * 1000;
		LOG("---------set freq--------------------value:" + value);

                //for rk3399
               String str_bigCore=null;
               int value_bigCore =0;
               if(hasbigCore){
                int size_bigCore = freqs_bigCore.size();
                int randomIndex_bigCore = new Random().nextInt(size_bigCore);
                 str_bigCore = freqs_bigCore.get(randomIndex_bigCore);
                value_bigCore = Integer.valueOf(str_bigCore.split("M")[0]) * 1000;
                LOG("---------set bigCore freq--------------------value:" + value_bigCore);
               }               
		try {
			ArmFreqUtils.setSpeedFreq(value);
		//for rk3399
                       if(hasbigCore){
                        ArmFreqUtils.setSpeedFreq_bigCore(value_bigCore);
                        str=str+"/"+str_bigCore;
                        }

			cur_frequency_text.setText(str);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			showAlertView();
			exitTest();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			// showAlertView();
			exitTest();
			return false;
		}
		return true;
	}

	private int indexgpu = 0;

	private boolean setGpuRandomValue() {
		int randomIndex = indexgpu;
		String str = gpu_freqs_3168[randomIndex];
		indexgpu++;
		if (indexgpu > 1) {
			indexgpu = 0;
		}
		LOG("---------set freq--------------------value:" + str);
		try {
			ArmFreqUtils.setGpuFreq(str);
			cur_gpufreq_text.setText(str);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			showAlertView();
			exitTest();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			// showAlertView();
			exitTest();
			return false;
		}
		return true;
	}
	
	private boolean setGpuRandomValueFor3188() {
		int randomIndex = Math.abs(new Random(System.currentTimeMillis()).nextInt());
		String str = gpu_freqs_3188.get(randomIndex%ArmFreqUtils.GPU_AVAILABLE_FREQ_COUNT);
		LOG("---------set freq--------------------value:" + str);
		try {
			ArmFreqUtils.setGpuFreqFor3188(str);
			cur_gpufreq_text.setText(str);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			showAlertView();
			exitTest();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			// showAlertView();
			exitTest();
			return false;
		}
		return true;
		
	}
	
	

	private int index = 0;

	private boolean setDDRRandomValue() {
		// int randomIndex = new Random().nextInt(size);
		int randomIndex = index;
		String str = ddr_freqs[randomIndex];
		index++;
		if (index > 2) {
			index = 0;
		}
		LOG("---------set freq--------------------value:" + str);
		try {
			ArmFreqUtils.setDDRFreq(str);
			cur_ddrfreq_text.setText(str);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			showAlertView();
			exitTest();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			// showAlertView();
			exitTest();
			return false;
		}
		return true;
	}

	private final Timer timer = new Timer();
	private TimerTask task = null;
	private static final int TASK_MSG = 0x01;
	private final Timer timer_gpu = new Timer();
	private TimerTask task_gpu = null;
	private static final int GPU_MSG = 0x02;
	private final Timer timer_ddr = new Timer();
	private TimerTask task_ddr = null;
	private static final int DDR_MSG = 0x03;
	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case TASK_MSG:
				if (!isExit)
					setRandomValue();
				break;
			case GPU_MSG:
				if (!isExit) {
					if (gpu_mode == GPU_MODE_3188) {
						setGpuRandomValueFor3188();
					} else if (gpu_mode == GPU_MODE_3168) {
						setGpuRandomValue();
					}
				}
				break;
			case DDR_MSG:
				if (!isExit)
					setDDRRandomValue();
				break;
			}
		}

	};

	private boolean isExit = false;

	private void exitTest() {
		if (isTest) {
			isExit = true;
			cur_frequency_text.setText("");
			cur_gpufreq_text.setText("");
			cur_ddrfreq_text.setText("");
			cur_ddrfreq_text.setText("");
			if (mTestMode == FIX_TEST_MODE) {
				freq_spinner.setEnabled(true);
			} else if (mTestMode == RANDOM_TEST_MODE) {
				update_time_spinner.setEnabled(true);
				if (task != null) {
					task.cancel();
				}
			} else if (mTestMode == GPU_TEST_MODE) {
				update_gputime_spinner.setEnabled(true);
				if (task_gpu != null) {
					task_gpu.cancel();
				}
			} else if (mTestMode == DDR_TEST_MODE) {
				ddr_spinner.setEnabled(true);
				if (task_ddr != null) {
					task_ddr.cancel();
				}
			}
			if (alert_View != null) {
				wm.removeView(alert_View);
				alert_View = null;
			}
			try {
				ArmFreqUtils.setGovernorMode(ArmFreqUtils.INTERACTIVE_MODE);
			    if(hasbigCore)	
                                ArmFreqUtils.setGovernorMode_bigCore(ArmFreqUtils.INTERACTIVE_MODE);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				//showAlertView();
			} catch (IOException e) {
				e.printStackTrace();
				//showAlertView();
			}
			try {
				if(gpu_mode == GPU_MODE_3168) {
					//for 3168 test
					ArmFreqUtils.openGpuEcho();
					ArmFreqUtils.setGpuFreq("0");
				} else if (gpu_mode == GPU_MODE_3188){
					ArmFreqUtils.setGpuFreqFor3188("0");
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				//showAlertView();
			} catch (IOException e) {
				e.printStackTrace();
				//showAlertView();
			}
			isTest = false;
		}
	}

	private View alert_View = null;

	private void showAlertView() {
		View view = LayoutInflater.from(this)
				.inflate(R.layout.alert_view, null);
		if (alert_View == null) {
			alert_View = view.findViewById(R.id.alert_root);
		} else {
			return;
		}
		String format = mContext.getResources().getString(
				R.string.alert_message);
		TextView tv = (TextView) alert_View.findViewById(R.id.messge);
		tv.setText(format);
		Button cancel_btn = (Button) alert_View.findViewById(R.id.cancel_btn);
		cancel_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				wm.removeView(alert_View);
				alert_View = null;
			}
		});
		WindowManager.LayoutParams params = null;
		if (params == null) {
			params = new WindowManager.LayoutParams();
			params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
					| WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;// 2002|WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
			// ;
			params.format = PixelFormat.TRANSLUCENT;
			params.flags |= 8;
			params.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
			params.setTitle("MediaVideo");
			params.gravity = Gravity.LEFT | Gravity.TOP;
			DisplayMetrics d = mContext.getResources().getDisplayMetrics();
			int mWidth = d.widthPixels/3;
			int mHeight = d.heightPixels/3;
			params.x = (d.widthPixels - mWidth) / 2;
			params.y = (d.heightPixels - mHeight) / 2;

			params.width = mWidth;
			params.height = mHeight;
		}
		wm.addView(alert_View, params);
	}

	private View exit_testView = null;

	private void showNoTestAlertDialog(final int msg, int res) {
		View view = LayoutInflater.from(this).inflate(R.layout.exit_test_view,
				null);
		if (exit_testView == null) {
			exit_testView = view.findViewById(R.id.exit_test_root);
		} else {
			return;
		}
		String format = mContext.getResources().getString(msg);
		String str = String.format(format,
				mContext.getResources().getString(res));
		TextView tv = (TextView) exit_testView.findViewById(R.id.messge);
		tv.setText(str);
		Button ok_btn = (Button) exit_testView.findViewById(R.id.ok_btn);
		Button cancel_btn = (Button) exit_testView
				.findViewById(R.id.cancel_btn);
		ok_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				exitTest();
				wm.removeView(exit_testView);
				exit_testView = null;
				if (msg == R.string.exit_msg) {
					wm.removeView(mView);
					Intent i = new Intent(mContext,ArmFreqTestService.class);
					mContext.stopService(i);
				}
			}
		});
		cancel_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				wm.removeView(exit_testView);
				exit_testView = null;
			}
		});
		WindowManager.LayoutParams params = null;
		if (params == null) {
			params = new WindowManager.LayoutParams();
			params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
					| WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;// 2002|WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
			// ;
			params.format = PixelFormat.TRANSLUCENT;
			params.flags |= 8;
			params.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
			params.setTitle("MediaVideo");
			params.gravity = Gravity.LEFT | Gravity.TOP;
			DisplayMetrics d = mContext.getResources().getDisplayMetrics();
			int mWidth = d.widthPixels/3;
			int mHeight = d.heightPixels/5;
			params.x = (d.widthPixels - mWidth) / 2;
			params.y = (d.heightPixels - mHeight) / 2;

			params.width = mWidth;
			params.height = mHeight;
		}
		wm.addView(exit_testView, params);
	}

	private void showNoSelectAlert(int res) {
		String format = mContext.getResources().getString(
				R.string.not_select_alert);
		String str = String.format(format,
				mContext.getResources().getString(res));
		Toast.makeText(mContext, str, Toast.LENGTH_SHORT).show();
		if (mTestMode == FIX_TEST_MODE) {
			fix_checkBox.setChecked(true);
			random_checkBox.setChecked(false);
		} else {
			fix_checkBox.setChecked(false);
			random_checkBox.setChecked(true);
		}
	}

	private void initwmparams() {
		if (wmParams == null) {
			wmParams = new WindowManager.LayoutParams();
			wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
					| WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;// 2002|WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
			// ;
			wmParams.format = PixelFormat.TRANSLUCENT;
			wmParams.flags |= 8;
			//wmParams.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
			wmParams.setTitle("MediaVideo");
			wmParams.gravity = Gravity.LEFT | Gravity.TOP;
			DisplayMetrics d = mContext.getResources().getDisplayMetrics();
			//int mWidth = 
			//int mHeight = VIEW_HEIGHT;
			int minPix = (int) Math.min(d.widthPixels,d.heightPixels);
			int mWidth = (int)((4*minPix)/5);//Integer.valueOf(mContext.getResources().getString(R.string.freq_width));
			int mHeight = (int)((1*minPix)/2); //Integer.valueOf(mContext.getResources().getString(R.string.freq_high));
			// wmParams.x = (d.widthPixels - mWidth) / 2;
			// wmParams.y = (d.heightPixels - mHeight) / 2;
			wmParams.x = 0;
			wmParams.y = 0;

			wmParams.width = mWidth;
			wmParams.height = mHeight;
		}
	}

	private boolean isDoubleClick = false;
	OnTouchListener mTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				isDoubleClick = false;
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				isDoubleClick = true;
				break;
			case MotionEvent.ACTION_MOVE:
				break;
			case MotionEvent.ACTION_POINTER_UP:
				break;
			case MotionEvent.ACTION_UP:
				break;
			}
			if (!isDoubleClick) {
				tarckFlinger(event);
				return true;
			}
			return true;
		}
	};

	private boolean tarckFlinger(MotionEvent event) {
		/*
		 * wmParams.width+=50; wmParams.height+=50;
		 * mVideoView.setVideoMeasure(wmParams.width, wmParams.height);
		 * wm.updateViewLayout(mRootView, wmParams);
		 */
		mCurrentX = (int) event.getRawX();
		mCurrentY = (int) event.getRawY();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mStartX = (int) event.getX();
			mStartY = (int) event.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			updateWindowParams();
			break;
		case MotionEvent.ACTION_UP:
			mStartX = 0;
			mStartY = 0;
			break;
		}
		return true;
	}

	private void updateWindowParams() {
		wmParams.x = mCurrentX - mStartX;
		wmParams.y = mCurrentY - mStartY;
		wm.updateViewLayout(mView, wmParams);
	}

	class Dict {

	}
}
