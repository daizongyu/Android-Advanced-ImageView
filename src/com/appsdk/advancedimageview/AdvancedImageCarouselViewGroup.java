package com.appsdk.advancedimageview;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.appsdk.advancedimageview.listener.AdvancedImageViewLoadListener;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView.ScaleType;
import android.widget.Scroller;

public class AdvancedImageCarouselViewGroup extends ViewGroup implements AdvancedImageViewLoadListener {
	private static final int SNAP_VELOCITY = 600;
	private static final int TOUCH_STATE_REST = 0;
	private static final int TOUCH_STATE_SCROLLING = 1;
	private static final int DEFAULT_DURATION_TIME = 500;

	private Scroller mScroller;
	private VelocityTracker mVelocityTracker;
	private ArrayList<AdvancedImageView> mImageViewList;
	private int mWidth;
	private int mHeight;
	private int mPadding;
	private int mTouchState = TOUCH_STATE_REST;
	private int mTouchSlop;
	private float mLastMotionX;
	private int mPosition = 0;
	private int mCount = 0;
	private OnImageSwitchListener mSwitchListener = null;
	private OnImageClickListener mClickListener = null;
	private int mIntervalTime = 0;
	private int mDurationTime = DEFAULT_DURATION_TIME;
	private Timer mSwitchTimer;
	private SwitchTimerTask mSwitchTask;
	private Handler mSwitchHandler;
	private ScaleType mScaleType = ScaleType.FIT_XY;
	private float mAspectRatio = 0.0f;

	protected interface OnImageSwitchListener {
		void onImageSwitch(int position);
	}

	protected interface OnImageClickListener {
		void onImageClick(int position);
	}

	public AdvancedImageCarouselViewGroup(Context context) {
		this(context, null);
	}

	public AdvancedImageCarouselViewGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
		mScroller = new Scroller(context);
		mImageViewList = new ArrayList<AdvancedImageView>();
		mSwitchHandler = new SwitchHandler(this);
		startSwitchTimerTask();
	}

	protected void addAdvancedImageView(AdvancedImageView imageView) {
		addClickListener(imageView);
		mImageViewList.add(imageView);
		imageView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		imageView.setScaleType(mScaleType);
		addView(imageView);
		mCount = mImageViewList.size();
	}

	protected void addAdvancedImageView(AdvancedImageView imageView, int index) {
		addClickListener(imageView);
		mImageViewList.add(imageView);
		imageView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		imageView.setScaleType(mScaleType);
		addView(imageView, index);
		mCount = mImageViewList.size();
	}

	protected AdvancedImageView getAdvancedImageView(int index) {
		if (index < 0 || index >= mCount) {
			return null;
		}
		return mImageViewList.get(index);
	}

	protected void removeAdvancedImageView(int index) {
		if (index < 0 || index >= mCount) {
			return;
		}
		mImageViewList.remove(index);
		removeViewAt(index);
		mCount = mImageViewList.size();
	}

	protected void removeAllAdvancedImageView() {
		mImageViewList.clear();
		removeAllViews();
		mCount = mImageViewList.size();
	}

	protected int size() {
		return mCount;
	}

	protected int getPosition() {
		return mPosition;
	}

	protected void setOnImageSwitchListener(OnImageSwitchListener listener) {
		mSwitchListener = listener;
	}

	protected void setOnImageClickListener(OnImageClickListener listener) {
		mClickListener = listener;
	}

	protected void refreshLayout() {
		requestLayout();
	}

	protected void setAspectRatio(float ratio) {
		mAspectRatio = ratio;
		refreshLayout();
	}

	protected void setScaleType(ScaleType type) {
		mScaleType = type;
	}

	protected void setPadding(int padding) {
		mPadding = padding;
	}

	protected void scrollToScreen(int position) {
		if (position < 0 || position >= mCount) {
			return;
		}
		mPosition = position;
		int dx = mPosition * getWidth() - getScrollX();
		mScroller.startScroll(getScrollX(), 0, dx, 0, mDurationTime);
		invalidate();
		if (mSwitchListener != null) {
			mSwitchListener.onImageSwitch(mPosition);
		}
	}

	protected void setIntervalTime(int millisecond) {
		mIntervalTime = millisecond;
		startSwitchTimerTask();
	}

	protected void setDurationTime(int millisecond) {
		if (millisecond > 0) {
			mDurationTime = millisecond;
		} else {
			mDurationTime = DEFAULT_DURATION_TIME;
		}
	}

	@Override
	public void onFinish(AdvancedImageView view, boolean result, Bitmap bitmap) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (mCount < 1) {
			return;
		}
		mWidth = getMeasuredWidth();
		mHeight = getMeasuredHeight();
		if (mPosition >= 0 && mPosition < mCount) {
			mScroller.abortAnimation();
			int left = 0;
			for (int i = 0; i < mCount; i++) {
				AdvancedImageView childView = (AdvancedImageView) getChildAt(i);
				childView.layout(left + mPadding, 0, left + mWidth - mPadding, mHeight);
				left = left + mWidth;
			}
		}
	}

	private static class SwitchHandler extends Handler {
		AdvancedImageCarouselViewGroup mCarousel;

		public SwitchHandler(AdvancedImageCarouselViewGroup carousel) {
			mCarousel = carousel;
		}

		@Override
		public void handleMessage(Message msg) {
			mCarousel.scrollToScreen(msg.what);
			super.handleMessage(msg);
		}
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate();
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		int action = ev.getAction();
		if ((action == MotionEvent.ACTION_MOVE) && (mTouchState != TOUCH_STATE_REST)) {
			return true;
		}
		float x = ev.getX();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mLastMotionX = x;
			mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;
			break;
		case MotionEvent.ACTION_MOVE:
			int xDiff = (int) Math.abs(mLastMotionX - x);
			if (xDiff > mTouchSlop) {
				mTouchState = TOUCH_STATE_SCROLLING;
			}
			break;
		case MotionEvent.ACTION_UP:
			mTouchState = TOUCH_STATE_REST;
			break;
		case MotionEvent.ACTION_CANCEL:
			mTouchState = TOUCH_STATE_REST;
			break;
		}
		return mTouchState != TOUCH_STATE_REST;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);
		int action = event.getAction();
		float x = event.getX();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			cancelSwitchTimerTask();
			if (mScroller != null) {
				if (!mScroller.isFinished()) {
					mScroller.abortAnimation();
				}
			}
			mLastMotionX = x;
			break;
		case MotionEvent.ACTION_MOVE:
			cancelSwitchTimerTask();
			int disX = (int) (mLastMotionX - x);
			mLastMotionX = x;
			if (mPosition == 0 && disX < 0) {
				disX = 0;
			} else if (mPosition == size() - 1 && disX > 0) {
				disX = 0;
			}
			scrollBy(disX, 0);
			break;
		case MotionEvent.ACTION_UP:
			mVelocityTracker.computeCurrentVelocity(1000);
			int velocityX = (int) mVelocityTracker.getXVelocity();
			if (velocityX < -SNAP_VELOCITY && mPosition < (mCount - 1)) {
				scrollToScreen(mPosition + 1);
			} else if (velocityX > SNAP_VELOCITY && mPosition > 0) {
				scrollToScreen(mPosition - 1);
			} else {
				scrollBack();
			}
			if (mVelocityTracker != null) {
				mVelocityTracker.recycle();
				mVelocityTracker = null;
			}
			startSwitchTimerTask();
			break;
		case MotionEvent.ACTION_CANCEL:
			mTouchState = TOUCH_STATE_REST;
			startSwitchTimerTask();
			break;
		}
		return true;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (mAspectRatio != 0) {
			int width = MeasureSpec.getSize(widthMeasureSpec);
			int height = MeasureSpec.getSize(heightMeasureSpec);
			height = (int) (width / mAspectRatio);
			this.setMeasuredDimension(width, height);
			heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	private void addClickListener(AdvancedImageView imageView) {
		imageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mClickListener != null) {
					mClickListener.onImageClick(mPosition);
				}
			}
		});
	}

	private void scrollBack() {
		int destScreen = (getScrollX() + getWidth() / 2) / getWidth();
		scrollToScreen(destScreen);
	}

	private class SwitchTimerTask extends TimerTask {
		@Override
		public void run() {
			if (mIntervalTime > 0) {
				if (mPosition >= mCount - 1) {
					mSwitchHandler.sendEmptyMessage(0);
				} else {
					mSwitchHandler.sendEmptyMessage(mPosition + 1);
				}
			}
		}
	}

	private void startSwitchTimerTask() {
		try {
			if (mSwitchTimer == null) {
				mSwitchTimer = new Timer();
			}
			if (mSwitchTask != null) {
				cancelSwitchTimerTask();
			}
			if (mIntervalTime > 0) {
				mSwitchTask = new SwitchTimerTask();
				mSwitchTimer.schedule(mSwitchTask, mIntervalTime, mIntervalTime);
			}
		} catch (Exception e) {
			cancelSwitchTimerTask();
		}
	}

	private void cancelSwitchTimerTask() {
		try {
			if (mSwitchTask != null) {
				mSwitchTask.cancel();
				mSwitchTask = null;
			}
		} catch (Exception e) {
			mSwitchTask = null;
		}
	}

}
