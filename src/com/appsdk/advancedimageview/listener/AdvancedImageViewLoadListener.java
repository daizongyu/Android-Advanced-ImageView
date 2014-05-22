package com.appsdk.advancedimageview.listener;

import android.graphics.Bitmap;

import com.appsdk.advancedimageview.AdvancedImageView;

/**
 * Get onFinish callback when image load finished
 */
public interface AdvancedImageViewLoadListener {
	/**
	 * Listen AdvancedImageView load event, when image load finish, this event
	 * is triggered
	 * 
	 * @param view
	 * @param result
	 * @param bitmap
	 */
	void onFinish(AdvancedImageView view, boolean result, Bitmap bitmap);
}
