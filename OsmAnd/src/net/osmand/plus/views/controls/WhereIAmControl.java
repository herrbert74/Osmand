package net.osmand.plus.views.controls;

import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;
import java.util.List;

import net.osmand.data.RotatedTileBox;
import net.osmand.plus.OsmAndConstants;
import net.osmand.plus.OsmandSettings;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.views.BaseMapLayer;
import net.osmand.plus.views.OsmandMapLayer.DrawSettings;
import net.osmand.plus.views.OsmandMapTileView;
import net.osmand.plus.views.ShadowText;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

public class WhereIAmControl extends MapControl {
	private static final int SHOW_ZOOM_LEVEL_MSG_ID = OsmAndConstants.UI_HANDLER_MAP_CONTROLS + 1;
	private static final int SHOW_ZOOM_BUTTON_MSG_ID = OsmAndConstants.UI_HANDLER_MAP_CONTROLS + 2;
	private static final int SHOW_ZOOM_LEVEL_DELAY = 1000;
	private static final int SHOW_ZOOM_LEVEL_BUTTON_DELAY = 1500;
	
	private Button whereIAmButton;
	
	private TextPaint zoomTextPaint;
	private Drawable zoomShadow;
	private Bitmap mapMagnifier;
	private Paint bitmapPaint;
	private boolean showZoomLevel = false;
	private boolean showZoomLevelButton = false;
	private OsmandMapTileView view;

	public WhereIAmControl(MapActivity mapActivity, Handler showUIHandler, float scaleCoefficient) {
		super(mapActivity, showUIHandler, scaleCoefficient);
		view = mapActivity.getMapView();
		zoomTextPaint = new TextPaint();
		zoomTextPaint.setTextSize(18 * scaleCoefficient);
		zoomTextPaint.setAntiAlias(true);
		zoomTextPaint.setFakeBoldText(true);
	}

//	public boolean onSingleTap(PointF point, RotatedTileBox tileBox) {
//		if (isShowZoomLevel() && zoomShadow.getBounds().contains((int) point.x, (int) point.y)) {
//			getOnClickMagnifierListener(view).onLongClick(null);
//			return true;
//		}
//		return false;
//	}

	
	@Override
	protected void showControl(FrameLayout parent) {
		int minimumWidth = view.getResources().getDrawable(R.drawable.back_to_loc).getMinimumWidth();
		whereIAmButton = addButton(parent, R.string.zoomIn, R.drawable.back_to_loc);
		whereIAmButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mapActivity.getMapViewTrackingUtilities().backToLocationImpl();
			}
		});
	}

	@Override
	public void initControls(FrameLayout parent) {
		zoomShadow = view.getResources().getDrawable(R.drawable.zoom_background).mutate();
		mapMagnifier = BitmapFactory.decodeResource(view.getResources(), R.drawable.map_magnifier);
		bitmapPaint = new Paint();
	}

	@Override
	public void hideControl(FrameLayout layout) {
		removeButton(layout, whereIAmButton);
	}

//	private void drawZoomLevel(Canvas canvas, RotatedTileBox tb, boolean drawZoomLevel) {
//		if (zoomShadow.getBounds().width() == 0) {
//			zoomShadow.setBounds(whereIAmButton.getLeft() - 2, whereIAmButton.getTop() - (int) (18 * scaleCoefficient),
//					whereIAmButton.getRight(), whereIAmButton.getBottom());
//		}
//		zoomShadow.draw(canvas);
//		if (drawZoomLevel) {
//			String zoomText = tb.getZoom() + "";
//			float frac = tb.getZoomScale();
//			if (frac != 0) {
//				int ifrac = (int) (frac * 10);
//				boolean pos = ifrac > 0;
//				zoomText += (pos ? "+" : "-");
//				zoomText += Math.abs(ifrac) / 10;
//				if (ifrac % 10 != 0) {
//					zoomText += "." + Math.abs(ifrac) % 10;
//				}
//			}
//			float length = zoomTextPaint.measureText(zoomText);
//
//			ShadowText.draw(zoomText, canvas, whereIAmButton.getLeft() + (whereIAmButton.getWidth() - length - 2) / 2,
//					whereIAmButton.getTop() + 4 * scaleCoefficient, zoomTextPaint, shadowColor);
//		} else {
//			int size = (int) (16 * scaleCoefficient);
//			int top = (int) (whereIAmButton.getTop() - size - 4 * scaleCoefficient);
//			int left = (int) (whereIAmButton.getLeft() + (whereIAmButton.getWidth() - mapMagnifier.getWidth() - 2 * scaleCoefficient) / 2);
//			// canvas density /2 ? size * 2
//			canvas.drawBitmap(mapMagnifier, null, new Rect(left, top, left + size * 2, top + size * 2), bitmapPaint);
//		}
//	}

	@Override
	public void drawControl(Canvas canvas, RotatedTileBox tileBox, DrawSettings nightMode) {
		BaseMapLayer mainLayer = view.getMainLayer();
		boolean zoomInEnabled = mainLayer != null && tileBox.getZoom() < mainLayer.getMaximumShownMapZoom();
		if (whereIAmButton.isEnabled() != zoomInEnabled) {
			whereIAmButton.setEnabled(zoomInEnabled);
		}
		
		if (view.isZooming()) {
			showZoomLevel =  true;
			showZoomLevelButton = false;
			showUIHandler.removeMessages(SHOW_ZOOM_LEVEL_MSG_ID);
			showUIHandler.removeMessages(SHOW_ZOOM_BUTTON_MSG_ID);
		} else {
			if (isShowZoomLevel() && view.getSettings().SHOW_RULER.get()) {
				hideZoomLevelInTime();
			}
		}
	}

	private void sendMessageToShowZoomLevel() {
		Message msg = Message.obtain(showUIHandler, new Runnable() {
			@Override
			public void run() {
				showZoomLevelButton = true;
				sendMessageToShowZoomButton();
				view.refreshMap();
			}

		});
		msg.what = SHOW_ZOOM_LEVEL_MSG_ID;
		showUIHandler.sendMessageDelayed(msg, SHOW_ZOOM_LEVEL_DELAY);
	}

	private void sendMessageToShowZoomButton() {
		Message msg = Message.obtain(showUIHandler, new Runnable() {
			@Override
			public void run() {
				showZoomLevelButton = false;
				showZoomLevel = false;
				view.refreshMap();
			}

		});
		msg.what = SHOW_ZOOM_BUTTON_MSG_ID;
		showUIHandler.sendMessageDelayed(msg, SHOW_ZOOM_LEVEL_BUTTON_DELAY);
	}

	private void hideZoomLevelInTime() {
		if (!showUIHandler.hasMessages(SHOW_ZOOM_LEVEL_MSG_ID) && !showUIHandler.hasMessages(SHOW_ZOOM_BUTTON_MSG_ID)) {
			sendMessageToShowZoomLevel();
		}
	}

	@Override
	public void updateTextColor(int textColor, int shadowColor) {
		super.updateTextColor(textColor, shadowColor);
		zoomTextPaint.setColor(textColor);
	}

	public boolean isShowZoomLevel() {
		return showZoomLevel;
	}

	public int getHeight() {
		if (height == 0) {
			Drawable buttonDrawable = view.getResources().getDrawable(R.drawable.map_zoom_in);
			height = buttonDrawable.getMinimumHeight();
		}
		return height;
	}

}