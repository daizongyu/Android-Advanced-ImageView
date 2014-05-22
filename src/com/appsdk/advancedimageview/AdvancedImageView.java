package com.appsdk.advancedimageview;

import com.appsdk.advancedimageview.listener.AdvancedImageViewLoadListener;
import com.appsdk.advancedimageview.util.AssetsManager;
import com.appsdk.advancedimageview.util.AsyncLocalImageLoader;
import com.appsdk.advancedimageview.util.AsyncNetImageLoader;
import com.appsdk.advancedimageview.util.BitmapCache;
import com.appsdk.advancedimageview.util.AsyncLocalImageLoader.LocalImageCallback;
import com.appsdk.advancedimageview.util.AsyncNetImageLoader.ImageCallback;
import com.appsdk.advancedimageview.util.DataCleanManager;
import com.appsdk.advancedimageview.util.mConfig;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

/**
 * The Advanced ImageView, can auto load image from url, local image path and
 * local movie path(mp4, wmv, avi, 3gp), use bitmap cache to ensure display
 * speed. There's many attributes you can set to determine display mode.
 * </br><uses-permission android:name="android.permission.INTERNET" />
 */
public class AdvancedImageView extends ImageView {

	private static String TAG = "AdvancedImageView";

	private static Context mContext;

	private Drawable mDefaultDrawable = null;
	private int mDefaultResId = 0;
	private Drawable mLoadingDrawable = null;
	private int mLoadingResId = 0;
	private Drawable mErrorDrawable = null;
	private int mErrorResId = 0;
	private AdvancedImageViewLoadListener mListener = null;
	private boolean mFitHeight = false;
	private boolean mFitWidth = false;
	private boolean mCenterCrop = false;
	private int mThumbnailWidth = 0;
	private float mAspectRatio = 0.0f;
	private int mRoundRadius = 0;
	private long mImageFlag;

	private AsyncLocalImageLoader mLocalImageLoader;
	private AsyncNetImageLoader mNetImageLoader;

	public AdvancedImageView(Context context) {
		this(context, null);
	}

	public AdvancedImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AdvancedImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;

		mDefaultDrawable = new BitmapDrawable(mContext.getResources(), AssetsManager.getImageFromAssetsFile(mContext, "appsdk_advancedimageview_default_image.jpg"));
		mLoadingDrawable = new BitmapDrawable(mContext.getResources(), AssetsManager.getImageFromAssetsFile(mContext, "appsdk_advancedimageview_loading_image.jpg"));
		mErrorDrawable = new BitmapDrawable(mContext.getResources(), AssetsManager.getImageFromAssetsFile(mContext, "appsdk_advancedimageview_error_image.jpg"));

		if (attrs != null) {
			mFitWidth = attrs.getAttributeBooleanValue(mConfig.XMLNS, "fitWidth", false);
			mFitHeight = attrs.getAttributeBooleanValue(mConfig.XMLNS, "fitHeight", false);
			mCenterCrop = attrs.getAttributeBooleanValue(mConfig.XMLNS, "centerCrop", false);
			mThumbnailWidth = attrs.getAttributeIntValue(mConfig.XMLNS, "thumbnailWidth", 0);
			mAspectRatio = attrs.getAttributeFloatValue(mConfig.XMLNS, "aspectRatio", 0.0f);
			if (attrs.getAttributeResourceValue(mConfig.XMLNS, "imageDefault", 0) != 0) {
				mDefaultResId = attrs.getAttributeResourceValue(mConfig.XMLNS, "imageDefault", 0);
				mDefaultDrawable = null;
				Log.i(TAG, "Set default image");
			}
			if (attrs.getAttributeResourceValue(mConfig.XMLNS, "imageLoading", 0) != 0) {
				mLoadingResId = attrs.getAttributeResourceValue(mConfig.XMLNS, "imageLoading", 0);
				mLoadingDrawable = null;
				Log.i(TAG, "Set loading image");
			}
			if (attrs.getAttributeResourceValue(mConfig.XMLNS, "imageError", 0) != 0) {
				mErrorResId = attrs.getAttributeResourceValue(mConfig.XMLNS, "imageError", 0);
				mErrorDrawable = null;
				Log.i(TAG, "Set error image");
			}
			mRoundRadius = attrs.getAttributeIntValue(mConfig.XMLNS, "roundRadius", 0);
			Log.i(TAG, "fitWidth:" + mFitWidth + "; fitHeight:" + mFitHeight + "; centerCrop:" + mCenterCrop + "; thumbnailWidth:" + mThumbnailWidth + "; mAspectRatio:" + mAspectRatio + "; mRoundRadius:" + mRoundRadius);
			initRoundRadius();
		}
	}

	/**
	 * Set default image drawable
	 * 
	 * @param drawable
	 */
	public void setDefaultImage(Drawable drawable) {
		mDefaultDrawable = drawable;
		mDefaultResId = 0;
	}

	/**
	 * Set default image from resource id
	 * 
	 * @param resId
	 */
	public void setDefaultImage(int resId) {
		mDefaultDrawable = null;
		mDefaultResId = resId;
	}

	/**
	 * Set loading image drawable
	 * 
	 * @param drawable
	 */
	public void setLoadingImage(Drawable drawable) {
		mLoadingDrawable = drawable;
		mLoadingResId = 0;
	}

	/**
	 * Set loading image from resource id
	 * 
	 * @param resId
	 */
	public void setLoadingImage(int resId) {
		mLoadingDrawable = null;
		mLoadingResId = resId;
	}

	/**
	 * Set load fail image drawable
	 * 
	 * @param drawable
	 */
	public void setErrorImage(Drawable drawable) {
		mErrorDrawable = drawable;
		mErrorResId = 0;
	}

	/**
	 * Set load fail image from resource id
	 * 
	 * @param resId
	 */
	public void setErrorImage(int resId) {
		mErrorDrawable = null;
		mErrorResId = resId;
	}

	/**
	 * Set AdvancedImageViewLoadListener to get callback
	 * 
	 * @param listener
	 */
	public void setOnloadListener(AdvancedImageViewLoadListener listener) {
		mListener = listener;
	}

	/**
	 * Set fitheight mode, the height is fixed Notice: fitHeight, fitWidth,
	 * centerCrop, aspectRatio can only set one attribute
	 * 
	 * @param fit
	 */
	public void setFitHeight(boolean fit) {
		mFitHeight = fit;
	}

	/**
	 * Set fitwidth mode, the width is fixed Notice: fitHeight, fitWidth,
	 * centerCrop, aspectRatio can only set one attribute
	 * 
	 * @param fit
	 */
	public void setFitWidth(boolean fit) {
		mFitWidth = fit;
	}

	/**
	 * Set center crop mode Notice: fitHeight, fitWidth, centerCrop, aspectRatio
	 * can only set one attribute
	 * 
	 * @param fit
	 */
	public void setCenterCrop(boolean fit) {
		mCenterCrop = fit;
	}

	/**
	 * Whether this AdvancedImageView is thumbnail
	 * 
	 * @param thumbnailWidth
	 *            if > 0: AdvancedImageView show a thumbnail with thumbnailWidth
	 *            if = 0: AdvancedImageView show the original image
	 */
	public void setThumbnailWidth(int thumbnailWidth) {
		mThumbnailWidth = thumbnailWidth;
	}

	/**
	 * Set fix aspect ratio of image (width/height) Notice: fitHeight, fitWidth,
	 * centerCrop, aspectRatio can only set one attribute
	 * 
	 * @param ratio
	 */
	public void setAspectRatio(float ratio) {
		mAspectRatio = ratio;
	}

	/**
	 * Set image radius<br>
	 * 
	 * @param radius
	 *            radius > 1
	 */
	public void setRondRadius(int radius) {
		mRoundRadius = radius;
		initRoundRadius();
	}

	/**
	 * Set image from filePath in local storage
	 * 
	 * @param filePath
	 */
	public void setLocalImage(String filePath) {
		mImageFlag = System.currentTimeMillis();
		mLocalImageLoader = new AsyncLocalImageLoader(mContext, mThumbnailWidth);
		if (filePath == null || filePath.length() < 1) {
			if (mDefaultDrawable != null)
				setImageDrawable(mDefaultDrawable);
			else
				setImageResource(mDefaultResId);
		} else {
			if (mLoadingDrawable != null)
				setImageDrawable(mLoadingDrawable);
			else
				setImageResource(mLoadingResId);
			mLocalImageLoader.loadDrawable(filePath, mImageFlag, new LocalImageCallback() {
				@Override
				public void onLoaded(Bitmap bitmap, long imageFlag) {
					if (imageFlag == mImageFlag) {
						setImageBitmap(bitmap);
					}
					if (mListener != null)
						mListener.onFinish(AdvancedImageView.this, true, bitmap);
				}

				@Override
				public void onError(Exception e, long imageFlag) {
					if (mErrorDrawable != null) {
						if (imageFlag == mImageFlag) {
							setImageDrawable(mErrorDrawable);
						}
						if (mListener != null)
							mListener.onFinish(AdvancedImageView.this, true, ((BitmapDrawable) mErrorDrawable).getBitmap());
					} else {
						if (imageFlag == mImageFlag) {
							setImageResource(mErrorResId);
						}
						Bitmap bitmap = mErrorResId > 0 ? ((BitmapDrawable) getResources().getDrawable(mErrorResId)).getBitmap() : null;
						if (mListener != null)
							mListener.onFinish(AdvancedImageView.this, true, bitmap);
					}
				}
			});
		}
	}

	/**
	 * Set image from url on internet <br>
	 * Notice: Must set <uses-permission
	 * android:name="android.permission.INTERNET" /> in AndroidManifest.xml
	 * 
	 * @param url
	 */
	public void setNetImage(String url) {
		mImageFlag = System.currentTimeMillis();
		mNetImageLoader = new AsyncNetImageLoader(mContext, mThumbnailWidth);
		if (url == null || url.length() < 1 || mNetImageLoader == null) {
			if (mDefaultDrawable != null)
				setImageDrawable(mDefaultDrawable);
			else
				setImageResource(mDefaultResId);
		} else {
			if (mLoadingDrawable != null)
				setImageDrawable(mLoadingDrawable);
			else
				setImageResource(mLoadingResId);
			mNetImageLoader.loadDrawable(url, mImageFlag, new ImageCallback() {
				@Override
				public void onError(Exception e, long imageFlag) {
					if (mErrorDrawable != null) {
						if (imageFlag == mImageFlag) {
							setImageDrawable(mErrorDrawable);
						}
						if (mListener != null)
							mListener.onFinish(AdvancedImageView.this, true, ((BitmapDrawable) mErrorDrawable).getBitmap());
					} else {
						if (imageFlag == mImageFlag) {
							setImageResource(mErrorResId);
						}
						Bitmap bitmap = mErrorResId > 0 ? ((BitmapDrawable) getResources().getDrawable(mErrorResId)).getBitmap() : null;
						if (mListener != null)
							mListener.onFinish(AdvancedImageView.this, true, bitmap);
					}
				}

				@Override
				public void onLoaded(Bitmap bitmap, long imageFlag) {
					if (imageFlag == mImageFlag) {
						setImageBitmap(bitmap);
					}
					if (mListener != null)
						mListener.onFinish(AdvancedImageView.this, true, bitmap);
				}
			});
		}
	}

	/**
	 * Clear displayed image, recover default image
	 */
	public void clear() {
		if (mDefaultDrawable != null)
			setImageDrawable(mDefaultDrawable);
		else
			setImageResource(mDefaultResId);
	}

	/**
	 * Clear all the cache of AdvancedImageView, call this function before
	 * destory application
	 */
	public static void destory() {
		try {
			BitmapCache.getInstance(mContext).destroy();
			DataCleanManager.cleanApplicationData(mContext, Environment.getDownloadCacheDirectory().getAbsolutePath() + "/image/");
			String state = Environment.getExternalStorageState();
			if (state.equals(Environment.MEDIA_MOUNTED)) {
				String external = Environment.getExternalStorageDirectory().getAbsolutePath();
				PackageManager pm = mContext.getPackageManager();
				PackageInfo info = pm.getPackageInfo(mContext.getPackageName(), 0);
				external += "/android/data/";
				external += info.packageName;
				external += "/image/";
				DataCleanManager.cleanApplicationData(mContext, external);
			}
		} catch (Exception e) {
		} finally {
			mContext = null;
		}
	}

	private final RectF roundRect = new RectF();
	private float rectadius = 0;
	private final Paint maskPaint = new Paint();
	private final Paint zonePaint = new Paint();

	private void initRoundRadius() {
		if (mRoundRadius < 1)
			return;
		maskPaint.setAntiAlias(true);
		maskPaint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		zonePaint.setAntiAlias(true);
		zonePaint.setColor(Color.WHITE);
		float density = getResources().getDisplayMetrics().density;
		rectadius = mRoundRadius * density;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		int w = getWidth();
		int h = getHeight();
		roundRect.set(0, 0, w, h);
	}

	@Override
	public void draw(Canvas canvas) {
		if (mRoundRadius < 1) {
			super.draw(canvas);
		} else {
			canvas.saveLayer(roundRect, zonePaint, Canvas.ALL_SAVE_FLAG);
			canvas.drawRoundRect(roundRect, rectadius, rectadius, zonePaint);
			canvas.saveLayer(roundRect, maskPaint, Canvas.ALL_SAVE_FLAG);
			super.draw(canvas);
			canvas.restore();
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (mFitHeight) {
			mFitWidth = false;
			mCenterCrop = false;
			mAspectRatio = 0.0f;
			int width = MeasureSpec.getSize(widthMeasureSpec);
			int height = MeasureSpec.getSize(heightMeasureSpec);
			if (getDrawable() != null && ((BitmapDrawable) getDrawable()).getBitmap() != null) {
				Bitmap bitmap = ((BitmapDrawable) getDrawable()).getBitmap();
				width = height * bitmap.getWidth() / bitmap.getHeight();
			}
			this.setMeasuredDimension(width, height);
		} else if (mFitWidth) {
			mFitHeight = false;
			mCenterCrop = false;
			mAspectRatio = 0.0f;
			int width = MeasureSpec.getSize(widthMeasureSpec);
			int height = MeasureSpec.getSize(heightMeasureSpec);
			if (getDrawable() != null && ((BitmapDrawable) getDrawable()).getBitmap() != null) {
				Bitmap bitmap = ((BitmapDrawable) getDrawable()).getBitmap();
				height = width * bitmap.getHeight() / bitmap.getWidth();
			}
			this.setMeasuredDimension(width, height);
		} else if (mCenterCrop) {
			mFitWidth = false;
			mFitHeight = false;
			mAspectRatio = 0.0f;
			int width = MeasureSpec.getSize(widthMeasureSpec);
			int height = MeasureSpec.getSize(heightMeasureSpec);
			if (getDrawable() != null && ((BitmapDrawable) getDrawable()).getBitmap() != null) {
				Bitmap bitmap = ((BitmapDrawable) getDrawable()).getBitmap();
				int bitmapWidth = bitmap.getWidth();
				int bitmapHeight = bitmap.getHeight();
				if ((bitmapWidth * height) > (bitmapHeight * width)) {
					width = height * bitmap.getWidth() / bitmap.getHeight();
				} else {
					height = width * bitmap.getHeight() / bitmap.getWidth();
				}
			}
			this.setMeasuredDimension(width, height);
		} else if (mAspectRatio != 0) {
			mFitWidth = false;
			mFitHeight = false;
			mCenterCrop = false;
			int width = MeasureSpec.getSize(widthMeasureSpec);
			int height = MeasureSpec.getSize(heightMeasureSpec);
			if (getDrawable() != null && ((BitmapDrawable) getDrawable()).getBitmap() != null) {
				height = (int) (width / mAspectRatio);
			}
			this.setMeasuredDimension(width, height);
		} else {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}
}
