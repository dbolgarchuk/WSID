package com.bobomonkey.whatshouldido;

import java.util.Random;

import com.bobomonkey.whatshouldido.R;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	private Button mQuestion;
	private TextView mAnswerText;
	private String[] mStrings;
	private Random mGenerator;
	private int mDurationMs;
	private Handler mHandler;
	private SetTextTask mSetTextTask;
	private String mAnswerString;
	private OnClickListener mQuestionClickListener;
	private boolean mIsRated;
	private boolean mIsReferToGooglePlay;
	private SharedPreferences mPrefs;
	private int mAskTimes;

	public final static String IS_RATED_KEY = "com.bobomonkey.whatshouldido.israted";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mQuestion = (Button) findViewById(R.id.question);
		mAnswerText = (TextView) findViewById(R.id.answer);

		mGenerator = new Random();
		mStrings = getResources().getStringArray(R.array.answersArray);
		mHandler = new Handler();

		mPrefs = this.getSharedPreferences("com.bobomonkey.whatshouldido",
				Context.MODE_PRIVATE);
		mIsRated = mPrefs.getBoolean(IS_RATED_KEY, false);

		mQuestionClickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				mQuestion.setOnClickListener(null);
				mAnswerText.setText(R.string.thinking);
				mAnswerText.setVisibility(View.VISIBLE);
				final int hintNumber = mGenerator.nextInt(mStrings.length);
				if (hintNumber % 15 == 0 && !mIsRated && mAskTimes > 5) {
					mAnswerString = getResources().getString(R.string.rate_app);
					mIsReferToGooglePlay = true;
				} else {
					mAnswerString = mStrings[hintNumber];
					mAnswerText.setOnClickListener(null);
				}
				mDurationMs = 0;
				mHandler.removeCallbacks(mSetTextTask);
				mSetTextTask = new SetTextTask();
				mHandler.postDelayed(mSetTextTask, 100);
				mAskTimes++;
			}
		};
		mQuestion.setOnClickListener(mQuestionClickListener);
		mQuestion.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					mQuestion.setBackgroundColor(getResources().getColor(
							R.color.pressed_button));
					return false;
				case MotionEvent.ACTION_UP:
					mQuestion.setBackgroundColor(getResources().getColor(
							R.color.normal_button));
					return false;
				default:
					return false;
				}
			}
		});
	}

	private class SetTextTask implements Runnable {

		@Override
		public void run() {
			if (mDurationMs < 800) {
				mDurationMs += 100;
				mHandler.postDelayed(this, 100);
			} else {
				mAnswerText.setText(mAnswerString);
				mQuestion.setOnClickListener(mQuestionClickListener);
				if (mIsReferToGooglePlay) {
					mAnswerText.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							Uri uri = Uri.parse("market://details?id="
									+ MainActivity.this.getPackageName());
							Intent goToMarket = new Intent(Intent.ACTION_VIEW,
									uri);
							try {
								startActivity(goToMarket);
								mPrefs.edit().putBoolean(IS_RATED_KEY, true);
							} catch (ActivityNotFoundException e) {
								startActivity(new Intent(
										Intent.ACTION_VIEW,
										Uri.parse("http://play.google.com/store/apps/details?id="
												+ MainActivity.this
														.getPackageName())));
							}
						}
					});
				}
			}
		}
	}
}
