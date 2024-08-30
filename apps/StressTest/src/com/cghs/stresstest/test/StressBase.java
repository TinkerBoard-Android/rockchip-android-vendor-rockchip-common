package com.cghs.stresstest.test;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cghs.stresstest.R;

public abstract class StressBase extends Activity {
    public int mMaxTestCount = 0;
    public int mCurrentCount = 0;

    public Button mStartBtn = null;
    public Button mStopBtn = null;
    public Button mSetMaxBtn = null;
    public Button mExitBtn = null;

    public boolean isRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    /**
     * Used to set the start, stop and setmax Btn res.
     */
    public void setDefaultBtnId(int startid, int stopid, int exitid, int setmaxid) {
        mStartBtn = (Button) findViewById(startid);
        mStartBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                onStartClick();
            }
        });

        mStopBtn = (Button) findViewById(stopid);
        mStopBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                onStopClick();
            }
        });

        mExitBtn = (Button) findViewById(exitid);
        mExitBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        if (setmaxid != 0) {
            mSetMaxBtn = (Button) findViewById(setmaxid);
            mSetMaxBtn.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    showSetMaxDialog();
                    onSetMaxClick();
                }
            });
        }
    }

    public void updateMaxTV() {

    }

    public int incCurCount() {
        mCurrentCount = mCurrentCount + 1;
        return mCurrentCount;
    }


    public void updateBtnState() {
        if (mStartBtn != null && mStopBtn != null) {
            mStartBtn.setEnabled(!isRunning);
            mStopBtn.setEnabled(isRunning);
        }
        if (mSetMaxBtn != null)
            mSetMaxBtn.setEnabled(!isRunning);
    }

    public void showSetMaxDialog() {
        final EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        new AlertDialog.Builder(this)
                .setTitle(R.string.btn_setting)
                .setView(editText)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!editText.getText().toString().trim().equals("")) {
                            mMaxTestCount = Integer.valueOf(editText.getText().toString());
                            updateMaxTV();
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }

                }).show();
    }


    public abstract void onStartClick();

    public abstract void onStopClick();

    public abstract void onSetMaxClick();


}
