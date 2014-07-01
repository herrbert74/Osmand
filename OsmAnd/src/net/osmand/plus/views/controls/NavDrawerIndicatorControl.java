package net.osmand.plus.views.controls;

import net.osmand.data.RotatedTileBox;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.views.OsmandMapLayer.DrawSettings;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

public class NavDrawerIndicatorControl extends MapControl {
	private Button navigateButton;
	private MapActivity mapActivity;

	public NavDrawerIndicatorControl(MapActivity mapActivity, Handler showUIHandler,
			float scaleCoefficient) {
		super(mapActivity, showUIHandler, scaleCoefficient);
		this.mapActivity = mapActivity;
		
	}

	@Override
	public void showControl(final FrameLayout parent) {
		navigateButton = addButton(parent, R.string.get_directions, R.drawable.map_btn_hmenu);
		navigateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mapActivity.toggleDrawer();
		/*		OsmandApplication app = mapActivity.getMyApplication();
				RoutingHelper routingHelper = app.getRoutingHelper();
				if (routingHelper.isFollowingMode()) {
					routingHelper.setRoutePlanningMode(false);
					mapActivity.getMapViewTrackingUtilities().switchToRoutePlanningMode();
				} else {
					OsmandApplication ctx = mapActivity.getMyApplication();
					if (!ctx.getTargetPointsHelper().checkPointToNavigateShort()) {
						ri.showDialog();
					} else {
						mapActivity.getMapViewTrackingUtilities().backToLocationImpl();
						app.getSettings().FOLLOW_THE_ROUTE.set(true);
						routingHelper.setFollowingMode(true);
						routingHelper.setRoutePlanningMode(false);
						mapActivity.getMapViewTrackingUtilities().switchToRoutePlanningMode();
						routingHelper.setCurrentLocation(app.getLocationProvider().getLastKnownLocation(), false);
						app.getRoutingHelper().notifyIfRouteIsCalculated();
					}
				}*/
			}
		});
	}

	@Override
	public void hideControl(FrameLayout layout) {
		removeButton(layout, navigateButton);
	}

	@Override
	public void drawControl(Canvas canvas, RotatedTileBox tileBox, DrawSettings nightMode) {
	}

	public int getWidth() {
		if (width == 0) {
			Drawable buttonDrawable = mapActivity.getResources().getDrawable(R.drawable.map_btn_hmenu);
			width = buttonDrawable.getMinimumWidth();
		}
		return width;
	}
}
