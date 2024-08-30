package com.cghs.stresstest.test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cghs.stresstest.R;
import com.cghs.stresstest.test.service.CameraTestService;
import com.cghs.stresstest.test.service.VideoTestService;
import com.cghs.stresstest.util.IntentUtils;
import com.cghs.stresstest.util.OpenFileDialog;
import com.cghs.stresstest.util.OpenFileDialog.CallbackBundle;;

public class VideoTest extends StressBase implements OnClickListener {
    private final static String LOG_TAG = "VideoTestActivity";
    public static final String SERVICE_NAME = "com.cghs.stresstest.test.service.VideoTestService";

    public static final String directory = "/mnt/internal_sd/Movies";
    //private static final String PATH_PREFIX = "/mnt/internal_sd/";
    public static final Uri data = Uri.parse("file://" + directory
            + "/mama.mkv");
    public static final Uri data1 = Uri.parse("file://" + directory
            + "/nba.mkv");

    public String mFilePath = "/mnt/external_sd/test.avi";
    public String mFilePath1 = "/mnt/external_sd/Movies/mama.mkv";
    public String mFilePath2 = "/mnt/external_sd/Movies/nba.mkv";
    public final int DIALOG_ID = 1;
    private final String FILTER_SUFFIX = ".avi;.mp4;.wmv;.mkv;.mpg;";
    private final String VIDEO_PATH_CONFIG = "video.path";
    public static final int TEST_SYSTEM_VIDEO = 1;
    private String mSelectedTestTitle = null;

    private boolean isTesting = false;
    private int mMaxCount = 0;
    private int mNowCount = 0;
    private final int MSG_UPDATE_RESULT = 1;

    private TextView mFilePathTV;
    private Button mSelectBtn;
    private Button mDeleteBtn;
    private CheckBox mSelectBox;
    private TextView mMaxTV;
    private TextView mResultTV;
    private Button mMaxCountBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_test);
        init();
        initDataFromIntent();
        registerReceiver(mReceiver, new IntentFilter(IntentUtils.INTENT_ACTION_STOP_AND_UPDATE_RESULT));
    }

    private void init() {
        getVideoPathConfig(this);

        setDefaultBtnId(R.id.start_btn, R.id.stop_btn, R.id.exit_btn, 0);

        mFilePathTV = (TextView) findViewById(R.id.filePath_tv);
        mFilePathTV.setText(getString(R.string.video_path) + "Automatically copy test video when start!");
        //mFilePathTV.setText(getString(R.string.video_path) + mFilePath);

        mSelectBtn = (Button) findViewById(R.id.select_btn);
        mSelectBtn.setOnClickListener(this);

        mMaxTV = (TextView) findViewById(R.id.maxvideotime_tv);
        mMaxTV.setText(getString(R.string.max_time) + mMaxCount + "H");
        mResultTV = (TextView) findViewById(R.id.videoresult);
        mResultTV.setText(getString(R.string.already_test_time) + mNowCount + "H");

        mMaxCountBtn = (Button) findViewById(R.id.maxvideotime_btn);
        mMaxCountBtn.setOnClickListener(this);

        mDeleteBtn = (Button) findViewById(R.id.delete_btn);
        mDeleteBtn.setOnClickListener(this);

        mSelectBox = (CheckBox) findViewById(R.id.system_video);
        mSelectBox.setChecked(true);
        mSelectBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                if (isChecked) {
                    mSelectBtn.setVisibility(View.GONE);
                    mDeleteBtn.setVisibility(View.GONE);
                    mMaxTV.setVisibility(View.VISIBLE);
                    mMaxCountBtn.setVisibility(View.VISIBLE);
                    mFilePathTV.setText(getString(R.string.video_path) + "Automatically copy test video when start!");
                } else {
                    mSelectBtn.setVisibility(View.VISIBLE);
                    mDeleteBtn.setVisibility(View.VISIBLE);
                    mMaxTV.setVisibility(View.GONE);
                    mMaxCountBtn.setVisibility(View.GONE);
                    mFilePathTV.setText(getString(R.string.video_path) + mFilePath);
                }
            }
        });

    }

    private void initDataFromIntent() {
        mNowCount = getIntent().getIntExtra(IntentUtils.INTENT_EXTRA_COUNT, 0);
    }

    private void updateView() {
        mFilePathTV.setText(getString(R.string.video_path) + mFilePath);
    }


    @Override
    public void onStartClick() {
        if (!mSelectBox.isChecked()) {
            if (!isVideoFileExist()) {
                Log.e(LOG_TAG, "video file (" + mFilePath + ") isn't exist");
                Toast.makeText(this, R.string.error_video, Toast.LENGTH_LONG)
                        .show();
                return;
            }

            Intent intent = new Intent(this, VideoPlayActivity.class);
            intent.putExtra("path", mFilePath);
            startActivity(intent);
        } else {
            copyVideoToSdcard(directory);
            copyVideoToSdcard1(directory);
            sendBroadcast(new Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, data));
            sendBroadcast(new Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    data1));
            testSystemVideo();
        }
    }

    @Override
    public void onStopClick() {
        Intent intent = new Intent(this, VideoTestService.class);
        if (isServiceRunning()) {
            stopService(intent);
        }
        finish();

    }

    @Override
    public void onSetMaxClick() {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.select_btn:
                showFileSelectDialog();
                break;
            case R.id.delete_btn:
                deleteVideoFile();
                break;
            case R.id.maxvideotime_btn:
                onSetClick();
                break;
            default:
                break;
        }
    }

    private void onSetClick() {
        final EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_title)
                .setView(editText)
                .setPositiveButton(R.string.dialog_ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                if (!editText.getText().toString().trim()
                                        .equals("")) {
                                    mMaxCount = Integer.valueOf(editText
                                            .getText().toString());
                                    mMaxTV.setText(getString(R.string.max_time)
                                            + mMaxCount + "H");
                                }
                            }
                        })
                .setNegativeButton(R.string.dialog_cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.cancel();
                            }

                        }).show();
    }

    @Override
    @Deprecated
    protected Dialog onCreateDialog(int id) {
        if (id == DIALOG_ID) {
            Map<String, Integer> images = new HashMap<String, Integer>();
            // 下面几句设置各文件类型的图标， 需要你先把图标添加到资源文件夹
            images.put(OpenFileDialog.sRoot, R.drawable.filedialog_root);    // 根目录图标
            images.put(OpenFileDialog.sParent, R.drawable.filedialog_folder_up);    //返回上一层的图标
            images.put(OpenFileDialog.sFolder, R.drawable.filedialog_folder);    //文件夹图标
            images.put("mkv", R.drawable.filedialog_videofile);    //wav文件图标
            images.put("avi", R.drawable.filedialog_videofile); //视频
            images.put("mp4", R.drawable.filedialog_videofile); //视频
            images.put("wmv", R.drawable.filedialog_videofile); //视频
            images.put("mpg", R.drawable.filedialog_videofile); //视频
            images.put(OpenFileDialog.sEmpty, R.drawable.filedialog_root);
            Dialog dialog = OpenFileDialog.createDialog(DIALOG_ID, this, "打开文件", new CallbackBundle() {
                        @Override
                        public void callback(Bundle bundle) {
                            String filepath = bundle.getString("path");
                            mFilePath = filepath;
                            updateView();
                        }
                    },
                    FILTER_SUFFIX,
                    images);
            return dialog;
        }


        return super.onCreateDialog(id);
    }


    private void showFileSelectDialog() {
        showDialog(DIALOG_ID);
    }

    private boolean isVideoFileExist() {
        File file = new File(mFilePath);
        return file.exists();
    }

    private void deleteVideoFile() {
        if (isVideoFileExist()) {
            File file = new File(mFilePath);
            boolean result = file.delete();
            Toast.makeText(this, getString(R.string.delete_video) + " " + result, Toast.LENGTH_LONG).show();
        } else {

        }
    }

    public void getVideoPathConfig(Context c) {
        String temp;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(c.getAssets().open(VIDEO_PATH_CONFIG)));

            while ((temp = br.readLine()) != null) {
                String[] path = temp.split(":");
                mFilePath = path[1];
            }

            if (br != null) {
                br.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    private void testSystemVideo() {
        Intent intent = new Intent(this, VideoTestService.class);
        if (isServiceRunning()) {
            stopService(intent);
        }

        if (mMaxCount != 0)
            intent.putExtra("max", mMaxCount);
        intent.putExtra(IntentUtils.INTENT_EXTRA_VIDEO_TEST, TEST_SYSTEM_VIDEO);
        intent.putExtra(IntentUtils.INTENT_EXTRA_VIDEO_TEST_TITLE, mSelectedTestTitle);

        startService(intent);
    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager
                .getRunningServices(Integer.MAX_VALUE)) {
            if (SERVICE_NAME.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private boolean isVideoFilesExist() {
        File file1 = new File(mFilePath1);
        File file2 = new File(mFilePath2);
        return file1.exists() && file2.exists();
    }

    private void deleteVideoFiles() {
        if (isVideoFilesExist()) {
            File file1 = new File(mFilePath1);
            File file2 = new File(mFilePath2);
            boolean result = file1.delete() && file2.delete();
            Toast.makeText(this, getString(R.string.delete_video) + " " + result, Toast.LENGTH_LONG).show();
        } else {

        }
    }

    public void copyVideoToSdcard(String name) {// name为sd卡下制定的路径

        try {
            // System.out.println("R.raw." + r.getName());
            int id = R.raw.mama;
            // Log.d(TAG, "----------->");
            String path = name + "/mama.mkv";
            BufferedOutputStream bufEcrivain = new BufferedOutputStream(
                    (new FileOutputStream(new File(path))));
            BufferedInputStream VideoReader = new BufferedInputStream(
                    getResources().openRawResource(id));
            byte[] buff = new byte[10 * 1024];
            int len;
            while ((len = VideoReader.read(buff)) > 0) {
                bufEcrivain.write(buff, 0, len);
                // Log.d(TAG, "----------->11111");
            }
            bufEcrivain.flush();
            bufEcrivain.close();
            VideoReader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void copyVideoToSdcard1(String name) {// name为sd卡下制定的路径

        try {
            // System.out.println("R.raw." + r.getName());
            int id = R.raw.nba;
            // Log.d(TAG, "----------->");
            String path = name + "/nba.mkv";
            BufferedOutputStream bufEcrivain = new BufferedOutputStream(
                    (new FileOutputStream(new File(path))));
            BufferedInputStream VideoReader = new BufferedInputStream(
                    getResources().openRawResource(id));
            byte[] buff = new byte[10 * 1024];
            int len;
            while ((len = VideoReader.read(buff)) > 0) {
                bufEcrivain.write(buff, 0, len);
                // Log.d(TAG, "----------->11111");
            }
            bufEcrivain.flush();
            bufEcrivain.close();
            VideoReader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_RESULT:
                    mResultTV.setText(getString(R.string.already_test_time)
                            + mNowCount + "H");
                    mResultTV.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }

        ;
    };
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(
                    IntentUtils.INTENT_ACTION_STOP_AND_UPDATE_RESULT)) {
                isTesting = false;
                mNowCount = intent.getIntExtra(IntentUtils.INTENT_EXTRA_TIME,
                        0);
                mHandler.sendEmptyMessage(MSG_UPDATE_RESULT);
            }
        }
    };

}
