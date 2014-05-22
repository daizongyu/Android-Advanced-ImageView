package com.appsdk.advancedimageview.util;

import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore.Video.Thumbnails;
import android.util.Log;

public class AsyncLocalImageLoader {
	private static final String TAG = "AsyncLocalImageLoader";
	private static final boolean LOG_ENABLED = false;

	private static Context mContext;
	private static int mThumbnailWidth;
	private ThreadPoolExecutor mPoolExecutor;
	private Handler mMainThreadHandler;

	public AsyncLocalImageLoader(Context context) {
		this(context, 0);
	}

	public AsyncLocalImageLoader(Context context, int thumbnailWidth) {
		this(context, thumbnailWidth, 5, 20);
	}

	public AsyncLocalImageLoader(Context context, int thumbnailWidth, int maxPoolSize, int queueSize) {
		this(context, thumbnailWidth, 2, maxPoolSize, 3, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(queueSize), new ThreadPoolExecutor.DiscardOldestPolicy());
	}

	public AsyncLocalImageLoader(Context context, int thumbnailWidth, int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
		mContext = context;
		mThumbnailWidth = thumbnailWidth;
		mPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
		mMainThreadHandler = new Handler(Looper.getMainLooper());
	}

	public void loadDrawable(String imageFilePath, long imageFlag, LocalImageCallback localImageCallback) {
		if (localImageCallback == null)
			return;
		LoadImageTask task = new LoadImageTask(imageFilePath, imageFlag, mMainThreadHandler, localImageCallback);
		mPoolExecutor.execute(task);
	}

	public void shutdown() {
		mPoolExecutor.shutdown();
	}

	private static final class LoadImageTask implements Runnable {
		private Handler mHandler;
		private LocalImageCallback mCallback;
		private String mPath;
		private long mImageFlag;

		public LoadImageTask(String imgPath, long imageFlag, Handler handler, LocalImageCallback imageCallback) {
			if (LOG_ENABLED)
				Log.d(TAG, "start a task for load image:" + imgPath);
			this.mHandler = handler;
			this.mPath = imgPath;
			this.mImageFlag = imageFlag;
			this.mCallback = imageCallback;
		}

		@Override
		public void run() {
			try {
				final Bitmap cacheBitmap = BitmapCache.getInstance(mContext).getBitmapFromMemCache(mPath);
				if (cacheBitmap != null) {
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							mCallback.onLoaded(cacheBitmap, mImageFlag);
						}
					});
				} else {
					if (mPath.contains("mp4") || mPath.contains("wmv") || mPath.contains("avi") || mPath.contains("3gp")) {
						final Bitmap bitmap = getThumbnail(ThumbnailUtils.createVideoThumbnail(mPath, Thumbnails.MICRO_KIND));
						if (bitmap != null) {
							BitmapCache.getInstance(mContext).addBitmapToMemCache(mPath, bitmap);
							mHandler.post(new Runnable() {
								@Override
								public void run() {
									mCallback.onLoaded(bitmap, mImageFlag);
								}
							});
						}
					} else {
						final Bitmap bitmap = loadLocalFile(mPath);
						if (bitmap != null) {
							BitmapCache.getInstance(mContext).addBitmapToMemCache(mPath, bitmap);
							mHandler.post(new Runnable() {
								@Override
								public void run() {
									mCallback.onLoaded(bitmap, mImageFlag);
								}
							});
						}
					}
				}
			} catch (final Exception e) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						mCallback.onError(e, mImageFlag);
					}
				});
			} catch (OutOfMemoryError e) {
				BitmapCache.getInstance(mContext).clear();
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						mCallback.onError(new Exception("OutOfMemoryError"), mImageFlag);
					}
				});
				System.gc();
			}
		}

		private Bitmap loadLocalFile(String filePath) {
			try {
				File file = new File(filePath);
				if (!file.exists())
					return null;
				FileInputStream fis = new FileInputStream(file);
				BitmapFactory.Options opts = new BitmapFactory.Options();
				opts.inSampleSize = 2;
				Bitmap res = BitmapFactory.decodeStream(fis, null, opts);
				if (res != null)
					return getThumbnail(res);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		private Bitmap getThumbnail(Bitmap bitmap) {
			if (mThumbnailWidth > 0) {
				int width = bitmap.getWidth();
				int height = bitmap.getHeight();
				float scale = ((float) mThumbnailWidth) / width;
				Matrix matrix = new Matrix();
				matrix.postScale(scale, scale);
				return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
			} else {
				return bitmap;
			}
		}
	}

	public static interface LocalImageCallback {
		public void onLoaded(Bitmap bitmap, long imageFlag);

		public void onError(Exception e, long imageFlag);
	}
}
