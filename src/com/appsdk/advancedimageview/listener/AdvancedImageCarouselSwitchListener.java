package com.appsdk.advancedimageview.listener;

/**
 * Listen AdvancedImageCarousel image switch event
 */
public interface AdvancedImageCarouselSwitchListener {
	/**
	 * Listen AdvancedImageCarousel image switch event, return current image
	 * position
	 * 
	 * @param position
	 */
	void onImageSwitch(int position);
}
