package com.appsdk.advancedimageview;

import com.appsdk.advancedimageview.AdvancedImageCarouselViewGroup.OnImageClickListener;
import com.appsdk.advancedimageview.AdvancedImageCarouselViewGroup.OnImageSwitchListener;
import com.appsdk.advancedimageview.listener.AdvancedImageCarouselClickListener;
import com.appsdk.advancedimageview.listener.AdvancedImageCarouselSwitchListener;
import com.appsdk.advancedimageview.util.AssetsManager;
import com.appsdk.advancedimageview.util.mConfig;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ImageView.ScaleType;

/**
 * AdvancedImageCarousel is a widget can automatic scroll images. It takes
 * bitmap cache management, add individual floating layer in bottom, and can set
 * many attributes to display as you want. For example aspect ratio, default
 * image, loading image, error image, dot image etc.</br><uses-permission
 * android:name="android.permission.INTERNET" />
 */
public class AdvancedImageCarousel extends RelativeLayout {

	private Context mContext;
	private AdvancedImageCarouselViewGroup mViewGroup;
	private ViewGroup mDotViewGroup;
	private View mBottomView;
	private OnImageSwitchListener mImageSwitchListener;
	private AdvancedImageCarouselSwitchListener mCarouselSwitchListener;
	private OnImageClickListener mImageClickListener;
	private AdvancedImageCarouselClickListener mCarouselClickListener;

	private float mAspectRatio = 0.0f;
	private Drawable mDefaultDrawable = null;
	private int mDefaultResId = 0;
	private Drawable mLoadingDrawable = null;
	private int mLoadingResId = 0;
	private Drawable mErrorDrawable = null;
	private int mErrorResId = 0;
	private ImageView[] mDotImageViews;
	private Drawable mDotNormalDrawable = null;
	private int mDotNormalResId = 0;
	private Drawable mDotFocusDrawable = null;
	private int mDotFocusResId = 0;
	private int mDotViewMarginTop = 10;
	private int mDotViewMarginRight = 10;
	private int mDotViewMarginBottom = 10;
	private int mDotViewMarginLeft = 10;
	private int mDotMargin = 4;

	public AdvancedImageCarousel(Context context) {
		this(context, null);
	}

	public AdvancedImageCarousel(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AdvancedImageCarousel(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		mDotNormalDrawable = new BitmapDrawable(mContext.getResources(), AssetsManager.getImageFromAssetsFile(mContext, "appsdk_advancedimagecarousel_dot_normal.png"));
		mDotFocusDrawable = new BitmapDrawable(mContext.getResources(), AssetsManager.getImageFromAssetsFile(mContext, "appsdk_advancedimagecarousel_dot_focus.png"));
		if (attrs != null) {
			mAspectRatio = attrs.getAttributeFloatValue(mConfig.XMLNS, "aspectRatio", 0.0f);
			if (attrs.getAttributeResourceValue(mConfig.XMLNS, "imageDefault", 0) != 0) {
				mDefaultResId = attrs.getAttributeResourceValue(mConfig.XMLNS, "imageDefault", 0);
				mDefaultDrawable = null;
			}
			if (attrs.getAttributeResourceValue(mConfig.XMLNS, "imageLoading", 0) != 0) {
				mLoadingResId = attrs.getAttributeResourceValue(mConfig.XMLNS, "imageLoading", 0);
				mLoadingDrawable = null;
			}
			if (attrs.getAttributeResourceValue(mConfig.XMLNS, "imageError", 0) != 0) {
				mErrorResId = attrs.getAttributeResourceValue(mConfig.XMLNS, "imageError", 0);
				mErrorDrawable = null;
			}
			if (attrs.getAttributeResourceValue(mConfig.XMLNS, "dotNormal", 0) != 0) {
				mDotNormalResId = attrs.getAttributeResourceValue(mConfig.XMLNS, "dotNormal", 0);
				mDotNormalDrawable = null;
			}
			if (attrs.getAttributeResourceValue(mConfig.XMLNS, "dotFocus", 0) != 0) {
				mDotFocusResId = attrs.getAttributeResourceValue(mConfig.XMLNS, "dotFocus", 0);
				mDotFocusDrawable = null;
			}
		}
		initCarousel();
		if (mAspectRatio != 0) {
			setAspectRatio(mAspectRatio);
			requestLayout();
		}
	}

	/**
	 * Add AdvancedImageView to Image Carousel Notice: You should
	 * setDefaultImage, setLoadingImage, setErrorImage to AdvancedImageView
	 * before add it to Image Carousel
	 * 
	 * @param imageView
	 */
	public void addCarouselView(AdvancedImageView imageView) {
		setDefaultImage(imageView);
		mViewGroup.addAdvancedImageView(imageView);
		refreshCarouselLayout();
	}

	/**
	 * Add AdvancedImageView to Image Carousel at index position
	 * 
	 * @param imageView
	 * @param index
	 */
	public void addCarouselView(AdvancedImageView imageView, int index) {
		setDefaultImage(imageView);
		mViewGroup.addAdvancedImageView(imageView, index);
		refreshCarouselLayout();
	}

	/**
	 * Add view to Image Carousel by image's url
	 * 
	 * @param url
	 */
	public void addCarouselViewByUrl(String url) {
		AdvancedImageView imageView = new AdvancedImageView(mContext);
		setDefaultImage(imageView);
		imageView.setNetImage(url);
		mViewGroup.addAdvancedImageView(imageView);
		refreshCarouselLayout();
	}

	/**
	 * Add view to Image Carousel by image's url at index position
	 * 
	 * @param url
	 * @param index
	 */
	public void addCarouselViewByUrl(String url, int index) {
		AdvancedImageView imageView = new AdvancedImageView(mContext);
		setDefaultImage(imageView);
		imageView.setNetImage(url);
		mViewGroup.addAdvancedImageView(imageView, index);
		refreshCarouselLayout();
	}

	/**
	 * Add view to Image Carousel by image's path
	 * 
	 * @param filePath
	 */
	public void addCarouselViewByPath(String filePath) {
		AdvancedImageView imageView = new AdvancedImageView(mContext);
		setDefaultImage(imageView);
		imageView.setLocalImage(filePath);
		mViewGroup.addAdvancedImageView(imageView);
		refreshCarouselLayout();
	}

	/**
	 * Add view to Image Carousel by image's path at index position
	 * 
	 * @param filePath
	 * @param index
	 */
	public void addCarouselViewByPath(String filePath, int index) {
		AdvancedImageView imageView = new AdvancedImageView(mContext);
		setDefaultImage(imageView);
		imageView.setLocalImage(filePath);
		mViewGroup.addAdvancedImageView(imageView, index);
		refreshCarouselLayout();
	}

	/**
	 * Get AdvancedImageView from Carousel Image at index position
	 * 
	 * @param index
	 * @return If exist return AdvancedImageView, nor return null
	 */
	public AdvancedImageView getCarouselView(int index) {
		return mViewGroup.getAdvancedImageView(index);
	}

	/**
	 * Remove the view of Image Carousel at index position
	 * 
	 * @param index
	 */
	public void removeCarouselView(int index) {
		mViewGroup.removeAdvancedImageView(index);
		refreshCarouselLayout();
	}

	/**
	 * Remove all the view of Image Carousel
	 */
	public void removeAllCarouselView() {
		mViewGroup.removeAllAdvancedImageView();
		refreshCarouselLayout();
	}

	/**
	 * Add individual view at bottom of Image Carousel
	 * 
	 * @param view
	 */
	public void addBottomView(View view) {
		mBottomView = view;
		initCarousel();
	}

	/**
	 * Refresh bottom view of Image Carousel
	 * 
	 * @param view
	 */
	public void refreshBottomView(View view) {
		mBottomView = view;
	}

	/**
	 * Remove bottom view of Image Carousel
	 */
	public void removeBottomView() {
		mBottomView = null;
		initCarousel();
	}

	/**
	 * Get current position of Image Carousel
	 * 
	 * @return position
	 */
	public int getPosition() {
		return mViewGroup.getPosition();
	}

	/**
	 * Get Image Carousel's view number
	 * 
	 * @return view number
	 */
	public int size() {
		return mViewGroup.size();
	}

	/**
	 * Set fix aspect ratio of image (width/height) Notice: fitHeight, fitWidth,
	 * centerCrop, aspectRatio can only set one attribute
	 * 
	 * @param ratio
	 */
	public void setAspectRatio(float ratio) {
		mAspectRatio = ratio;
		mViewGroup.setAspectRatio(ratio);
	}

	/**
	 * Set default image drawable
	 * 
	 * @param drawable
	 */
	public void setDefaultImage(Drawable drawable) {
		mDefaultDrawable = drawable;
		mDefaultResId = 0;
		for (int i = 0; i < mViewGroup.size(); i++) {
			mViewGroup.getAdvancedImageView(i).setDefaultImage(drawable);
		}
	}

	/**
	 * Set default image from resource id
	 * 
	 * @param resId
	 */
	public void setDefaultImage(int resId) {
		mDefaultDrawable = null;
		mDefaultResId = resId;
		for (int i = 0; i < mViewGroup.size(); i++) {
			mViewGroup.getAdvancedImageView(i).setDefaultImage(resId);
		}
	}

	/**
	 * Set loading image drawable
	 * 
	 * @param drawable
	 */
	public void setLoadingImage(Drawable drawable) {
		mLoadingDrawable = drawable;
		mLoadingResId = 0;
		for (int i = 0; i < mViewGroup.size(); i++) {
			mViewGroup.getAdvancedImageView(i).setLoadingImage(drawable);
		}
	}

	/**
	 * Set loading image from resource id
	 * 
	 * @param resId
	 */
	public void setLoadingImage(int resId) {
		mLoadingDrawable = null;
		mLoadingResId = resId;
		for (int i = 0; i < mViewGroup.size(); i++) {
			mViewGroup.getAdvancedImageView(i).setLoadingImage(resId);
		}
	}

	/**
	 * Set load fail image drawable
	 * 
	 * @param drawable
	 */
	public void setErrorImage(Drawable drawable) {
		mErrorDrawable = drawable;
		mErrorResId = 0;
		for (int i = 0; i < mViewGroup.size(); i++) {
			mViewGroup.getAdvancedImageView(i).setErrorImage(drawable);
		}
	}

	/**
	 * Set load fail image from resource id
	 * 
	 * @param resId
	 */
	public void setErrorImage(int resId) {
		mErrorDrawable = null;
		mErrorResId = resId;
		for (int i = 0; i < mViewGroup.size(); i++) {
			mViewGroup.getAdvancedImageView(i).setErrorImage(resId);
		}
	}

	/**
	 * Whether show switch dot in Image Carousel
	 * 
	 * @param show
	 */
	public void setSwitchDotShow(boolean show) {
		if (show) {
			mDotViewGroup.setVisibility(View.VISIBLE);
		} else {
			mDotViewGroup.setVisibility(View.GONE);
		}
	}

	/**
	 * Set individual normal dot drawable
	 * 
	 * @param drawable
	 */
	public void setDotNormal(Drawable drawable) {
		mDotNormalResId = 0;
		mDotNormalDrawable = drawable;
		refreshLayout();
	}

	/**
	 * Set individual normal dot resource id
	 * 
	 * @param resId
	 */
	public void setDotNormal(int resId) {
		mDotNormalResId = resId;
		mDotNormalDrawable = null;
		refreshLayout();
	}

	/**
	 * Set individual focus dot drawable
	 * 
	 * @param drawable
	 */
	public void setDotFocus(Drawable drawable) {
		mDotFocusResId = 0;
		mDotFocusDrawable = drawable;
		refreshLayout();
	}

	/**
	 * Set individual focus dot resource id
	 * 
	 * @param resId
	 */
	public void setDotFocus(int resId) {
		mDotFocusResId = resId;
		mDotFocusDrawable = null;
		refreshLayout();
	}

	/**
	 * Set dot view's margin to edge
	 * 
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 */
	public void setDotViewMargin(int left, int top, int right, int bottom) {
		mDotViewMarginLeft = left;
		mDotViewMarginTop = top;
		mDotViewMarginRight = right;
		mDotViewMarginBottom = bottom;
		initCarousel();
	}

	/**
	 * Set dot's margin to other dots
	 * 
	 * @param margin
	 */
	public void setDotMargin(int margin) {
		mDotMargin = margin;
		refreshLayout();
	}

	/**
	 * Set on image switch listener to Image Carousel
	 * 
	 * @param listener
	 */
	public void setOnCarouselSwitchListener(AdvancedImageCarouselSwitchListener listener) {
		mCarouselSwitchListener = listener;
	}

	/**
	 * Set on image click listener to Image Carousel
	 * 
	 * @param listener
	 */
	public void setOnCarouselClickListener(AdvancedImageCarouselClickListener listener) {
		mCarouselClickListener = listener;
	}

	/**
	 * Refresh Layout
	 */
	public void refreshLayout() {
		mViewGroup.refreshLayout();
		refreshCarouselLayout();
	}

	/**
	 * Set scale type, default FIT_XY
	 * 
	 * @param type
	 */
	public void setScaleType(ScaleType type) {
		mViewGroup.setScaleType(type);
	}

	/**
	 * Set padding of image
	 * 
	 * @param padding
	 */
	public void setPadding(int padding) {
		mViewGroup.setPadding(padding);
	}

	/**
	 * Scroll to screen at position
	 * 
	 * @param position
	 */
	public void scrollToScreen(int position) {
		mViewGroup.scrollToScreen(position);
	}

	/**
	 * Set auto scroll interval time, default stop
	 * 
	 * @param millisecond
	 *            >0: Auto scroll; =0: Stop
	 */
	public void setIntervalTime(int millisecond) {
		mViewGroup.setIntervalTime(millisecond);
	}

	/**
	 * Set scroll duration time
	 * 
	 * @param millisecond
	 *            >0: Set success; =0: Use default duration time
	 */
	public void setDurationTime(int millisecond) {
		mViewGroup.setDurationTime(millisecond);
	}

	private void initCarousel() {
		removeAllViews();
		if (mViewGroup == null) {
			mViewGroup = new AdvancedImageCarouselViewGroup(mContext);
		}
		addView(mViewGroup, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		if (mBottomView != null) {
			LayoutParams params1 = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			params1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			addView(mBottomView, params1);
		}

		if (mDotViewGroup == null) {
			mDotViewGroup = new LinearLayout(mContext);
		}
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		params.setMargins(mDotViewMarginLeft, mDotViewMarginTop, mDotViewMarginRight, mDotViewMarginBottom);
		addView(mDotViewGroup, params);

		mImageSwitchListener = new OnImageSwitchListener() {
			@Override
			public void onImageSwitch(int position) {
				if (mCarouselSwitchListener != null) {
					mCarouselSwitchListener.onImageSwitch(position);
				}
				for (int i = 0; i < mDotImageViews.length; i++) {
					if (i == position) {
						if (mDotFocusDrawable != null)
							mDotImageViews[i].setImageDrawable(mDotFocusDrawable);
						else
							mDotImageViews[i].setImageResource(mDotFocusResId);
					} else {
						if (mDotNormalDrawable != null)
							mDotImageViews[i].setImageDrawable(mDotNormalDrawable);
						else
							mDotImageViews[i].setImageResource(mDotNormalResId);
					}
				}
			}
		};
		mViewGroup.setOnImageSwitchListener(mImageSwitchListener);

		mImageClickListener = new OnImageClickListener() {
			@Override
			public void onImageClick(int position) {
				if (mCarouselClickListener != null) {
					mCarouselClickListener.onImageClick(position);
				}
			}
		};
		mViewGroup.setOnImageClickListener(mImageClickListener);

		refreshCarouselLayout();
	}

	private void refreshCarouselLayout() {
		mDotViewGroup.removeAllViews();
		mDotImageViews = new ImageView[mViewGroup.size()];
		for (int i = 0; i < mViewGroup.size(); i++) {
			ImageView imageView = new ImageView(mContext);
			imageView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			imageView.setPadding(mDotMargin, mDotMargin, mDotMargin, mDotMargin);
			mDotImageViews[i] = imageView;
			if (i == mViewGroup.getPosition()) {
				if (mDotFocusDrawable != null)
					mDotImageViews[i].setImageDrawable(mDotFocusDrawable);
				else
					mDotImageViews[i].setImageResource(mDotFocusResId);
			} else {
				if (mDotNormalDrawable != null)
					mDotImageViews[i].setImageDrawable(mDotNormalDrawable);
				else
					mDotImageViews[i].setImageResource(mDotNormalResId);
			}
			mDotImageViews[i].setOnClickListener(new OnDotClickListener(i));
			mDotViewGroup.addView(mDotImageViews[i]);
		}
	}

	private void setDefaultImage(AdvancedImageView imageView) {
		if (mDefaultDrawable != null)
			imageView.setDefaultImage(mDefaultDrawable);
		if (mDefaultResId != 0)
			imageView.setDefaultImage(mDefaultResId);
		if (mLoadingDrawable != null)
			imageView.setLoadingImage(mLoadingDrawable);
		if (mLoadingResId != 0)
			imageView.setLoadingImage(mLoadingResId);
		if (mErrorDrawable != null)
			imageView.setErrorImage(mErrorDrawable);
		if (mErrorResId != 0)
			imageView.setErrorImage(mErrorResId);
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

	private class OnDotClickListener implements OnClickListener {
		private int mPosition;

		public OnDotClickListener(int position) {
			mPosition = position;
		}

		@Override
		public void onClick(View v) {
			mViewGroup.scrollToScreen(mPosition);
		}
	}
}
