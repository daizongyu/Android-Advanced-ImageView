package com.appsdk.advancedimageview.util;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;

public class BitmapCache {
	public static final String TAG = "BitmapCache";
	private static final boolean LOG_ENABLED = false;

	private static BitmapCache mInstance;
	private final MyLruCache<String, Bitmap> mMemCache;

	public static BitmapCache getInstance(Context context) {
		if (mInstance == null)
			mInstance = new BitmapCache(context);
		return mInstance;
	}

	private BitmapCache(Context context) {
		// Get memory class of this device, exceeding this amount will throw an
		// OutOfMemory exception.
		final int memClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
		// Use 1/5th of the available memory for this memory cache.
		final int cacheSize = 1024 * 1024 * memClass / 5;

		if (LOG_ENABLED)
			Log.d(TAG, "LRUCache size sets to " + cacheSize);

		mMemCache = new MyLruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap value) {
				return value.getRowBytes() * value.getHeight();
			}
		};
	}

	public Bitmap getBitmapFromMemCache(String key) {
		final Bitmap b = mMemCache.get(key);
		if (LOG_ENABLED)
			Log.d(TAG, (b == null) ? "Cache miss" : "Cache found");
		if (b != null && b.isRecycled()) {
			/* A recycled bitmap cannot be used again */
			mMemCache.remove(key);
			return null;
		}
		return b;
	}

	public void addBitmapToMemCache(String key, Bitmap bitmap) {
		if (key != null && bitmap != null && getBitmapFromMemCache(key) == null)
			mMemCache.put(key, bitmap);
	}
	
	public void removeBitmapFromMemCache(String key) {
		mMemCache.remove(key);
	}

	private Bitmap getBitmapFromMemCache(int resId) {
		return getBitmapFromMemCache("res:" + resId);
	}

	private void addBitmapToMemCache(int resId, Bitmap bitmap) {
		addBitmapToMemCache("res:" + resId, bitmap);
	}

	public static Bitmap GetFromResource(Context context, View v, int resId) {
		BitmapCache cache = BitmapCache.getInstance(context);
		Bitmap bitmap = cache.getBitmapFromMemCache(resId);
		if (bitmap == null) {
			bitmap = BitmapFactory.decodeResource(v.getResources(), resId);
			cache.addBitmapToMemCache(resId, bitmap);
		}
		return bitmap;
	}

	public void clear() {
		mMemCache.evictAll();
	}

	public void destroy() {
		clear();
		mInstance = null;
	}
}
