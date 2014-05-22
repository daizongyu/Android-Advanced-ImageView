package com.appsdk.advancedimageview.util;

import java.io.InputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class AssetsManager {
	public static Bitmap getImageFromAssetsFile(Context context, String fileName) {
		Bitmap bitmap = null;
		AssetManager assetManager = context.getResources().getAssets();
		try {
			InputStream is = assetManager.open(fileName);
			bitmap = BitmapFactory.decodeStream(is, null, null);
			is.close();
		} catch (Exception e) {
			return null;
		}
		return bitmap;
	}
}
