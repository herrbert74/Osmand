package net.osmand.plus.views.controls;

import net.osmand.data.RotatedTileBox;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.utils.OsmAndFormatter;
import net.osmand.plus.views.OsmandMapLayer.DrawSettings;
import net.osmand.plus.views.OsmandMapTileView;
import net.osmand.plus.views.ShadowText;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextPaint;
import android.widget.FrameLayout;

public class RulerControl extends MapControl {

		ShadowText cacheRulerText = null;
		float cacheRulerZoom = 0;
		double cacheRulerTileX = 0;
		double cacheRulerTileY = 0;
		float cacheRulerTextLen = 0;
		MapZoomControls zoomControls;
		Drawable rulerDrawable;
		TextPaint rulerTextPaint;
		final static double screenRulerPercent = 0.25;
		
		public RulerControl(MapZoomControls zoomControls, MapActivity mapActivity, Handler showUIHandler, float scaleCoefficient) {
			super(mapActivity, showUIHandler, scaleCoefficient);
			this.zoomControls = zoomControls;
			rulerTextPaint = new TextPaint();
			rulerTextPaint.setTextSize(20 * scaleCoefficient);
			rulerTextPaint.setAntiAlias(true);
			rulerDrawable = mapActivity.getResources().getDrawable(R.drawable.ruler);
		}
		
		@Override
		protected void hideControl(FrameLayout layout) {
		}
		
		@Override
		public void updateTextColor(int textColor, int shadowColor) {
			super.updateTextColor(textColor, shadowColor);
			rulerTextPaint.setColor(textColor);
		}

		@Override
		protected void showControl(FrameLayout layout) {
		}

		@Override
		public void drawControl(Canvas canvas, RotatedTileBox tb, DrawSettings nightMode) {
			if( (zoomControls.isVisible() && zoomControls.isShowZoomLevel()) || !mapActivity.getMyApplication().getSettings().SHOW_RULER.get()){
				return;
			}
			OsmandMapTileView view = mapActivity.getMapView();
			// update cache
			if (view.isZooming()) {
				cacheRulerText = null;
			} 
			//Drawable needs to be redrawn
			else if((tb.getZoom() + tb.getZoomScale()) != cacheRulerZoom ||
					Math.abs(tb.getCenterTileX() - cacheRulerTileX) +  Math.abs(tb.getCenterTileY() - cacheRulerTileY) > 1){
				cacheRulerZoom = (tb.getZoom() + tb.getZoomScale());
				cacheRulerTileX = tb.getCenterTileX();
				cacheRulerTileY = tb.getCenterTileY();
				
				final double screenWidthInMeters = tb.getDistance(0, tb.getPixHeight() / 2, tb.getPixWidth(), tb.getPixHeight() / 2);
				double pixToMeterRatio = tb.getPixWidth() / screenWidthInMeters;
				
				double roundedRulerLengthInMeters = OsmAndFormatter.calculateRoundedDist(screenWidthInMeters * screenRulerPercent, view.getApplication());
				
				int rulerLengthInPx = (int) (pixToMeterRatio * roundedRulerLengthInMeters);
				cacheRulerText = ShadowText.create(OsmAndFormatter.getFormattedDistance((float) roundedRulerLengthInMeters, view.getApplication()));
				cacheRulerTextLen = rulerTextPaint.measureText(cacheRulerText.getText());
				Rect bounds = rulerDrawable.getBounds();
				/*bounds.right = (int) (view.getWidth() - 7 * scaleCoefficient);
				bounds.left = bounds.right - cacheRulerDistPix;*/
				bounds.left = (int) (7 * scaleCoefficient);
				bounds.right = bounds.left + rulerLengthInPx;
				rulerDrawable.setBounds(bounds);
				rulerDrawable.invalidateSelf();
			} 
			
			if (cacheRulerText != null) {
				Rect bounds = rulerDrawable.getBounds();
				int bottom = (int) (view.getHeight() - vmargin);
				if(bounds.bottom != bottom) {
					bounds.bottom = bottom;
					bounds.top = bounds.bottom - rulerDrawable.getMinimumHeight();
					rulerDrawable.setBounds(bounds);
					rulerDrawable.invalidateSelf();
				}
				rulerDrawable.draw(canvas);
				cacheRulerText.draw(canvas, bounds.left + (bounds.width() - cacheRulerTextLen) / 2, bounds.bottom - 8 * scaleCoefficient,
						rulerTextPaint, shadowColor);
			}
		}
	}