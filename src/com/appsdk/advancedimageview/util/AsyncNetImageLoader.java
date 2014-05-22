package com.appsdk.advancedimageview.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class AsyncNetImageLoader {
	private static final String TAG = "AsyncNetImageLoader";
	private static final boolean LOG_ENABLED = false;

	private static Context mContext;
	private static int mThumbnailWidth;
	private ThreadPoolExecutor mPoolExecutor;
	private Handler mMainThreadHandler;

	public AsyncNetImageLoader(Context context) {
		this(context, 0);
	}
	
	public AsyncNetImageLoader(Context context, int thumbnailWidth) {
		this(context, thumbnailWidth, 5, 20);
	}

	public AsyncNetImageLoader(Context context, int thumbnailWidth, int maxPoolSize, int queueSize) {
		this(context, thumbnailWidth, 0, maxPoolSize, 3, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(queueSize), new ThreadPoolExecutor.DiscardOldestPolicy());
	}

	public AsyncNetImageLoader(Context context, int thumbnailWidth, int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
		mContext = context;
		mThumbnailWidth = thumbnailWidth;
		mPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
		mMainThreadHandler = new Handler(Looper.getMainLooper());
	}

	public void loadDrawable(String imageUrl, long imageFlag, ImageCallback imageCallback) {
		if (imageCallback == null)
			return;
		LoadImageTask task = new LoadImageTask(imageUrl, imageFlag, mMainThreadHandler, imageCallback);
		mPoolExecutor.execute(task);
	}

	public void shutdown() {
		mPoolExecutor.shutdown();
	}

	private static final class LoadImageTask implements Runnable {
		private Handler mHandler;
		private ImageCallback mCallback;
		private String mUrl;
		private long mImageFlag;

		public LoadImageTask(String imageUrl, long imageFlag, Handler handler, ImageCallback imageCallback) {
			if (LOG_ENABLED)
				Log.d(TAG, "start a task for load image:" + imageUrl);
			this.mHandler = handler;
			this.mUrl = imageUrl;
			this.mImageFlag = imageFlag;
			this.mCallback = imageCallback;
		}

		@Override
		public void run() {
			try {
				final Bitmap cacheBitmap = BitmapCache.getInstance(mContext).getBitmapFromMemCache(mUrl);
				if (cacheBitmap != null) {
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							mCallback.onLoaded(cacheBitmap, mImageFlag);
						}
					});
				} else {
					URL url = new URL(mUrl);
					URLConnection conn = url.openConnection();
					conn.connect();
					InputStream is = conn.getInputStream();
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					byte[] cache = new byte[1024 * 10];
					int len = 0;
					while ((len = is.read(cache)) != -1) {
						baos.write(cache, 0, len);
					}
					ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

					BitmapFactory.Options opts = new BitmapFactory.Options();
					opts.inSampleSize = 2;
					final Bitmap bitmap = getThumbnail(BitmapFactory.decodeStream(bais, null, opts));
					if (bitmap != null) {
						BitmapCache.getInstance(mContext).addBitmapToMemCache(mUrl, bitmap);
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								mCallback.onLoaded(bitmap, mImageFlag);
							}
						});
					}
					is.close();
					baos.close();
					bais.close();
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

	public static interface ImageCallback {
		public void onLoaded(Bitmap bitmap, long imageFlag);

		public void onError(Exception e, long imageFlag);
	}
}
