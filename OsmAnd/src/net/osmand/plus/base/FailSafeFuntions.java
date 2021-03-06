package net.osmand.plus.base;

import java.io.File;
import java.util.ArrayList;

import net.osmand.PlatformUtil;
import net.osmand.access.AccessibleAlertBuilder;
import net.osmand.data.LatLon;
import net.osmand.plus.GPXUtilities;
import net.osmand.plus.GPXUtilities.GPXFile;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.OsmandSettings;
import net.osmand.plus.R;
import net.osmand.plus.TargetPointsHelper;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.routing.RoutingHelper;
import net.osmand.plus.routing.RouteProvider.GPXRouteParamsBuilder;

import org.apache.commons.logging.Log;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.TextView;

public class FailSafeFuntions {
	private static boolean quitRouteRestoreDialog = false;
	private static Log log = PlatformUtil.getLog(FailSafeFuntions.class);
	
	public static void restoreRoutingMode(final MapActivity ma) {
		final OsmandApplication app = ma.getMyApplication();
		final OsmandSettings settings = app.getSettings();
		final Handler uiHandler = new Handler();
		final String gpxPath = settings.FOLLOW_THE_GPX_ROUTE.get();
		final TargetPointsHelper targetPoints = app.getTargetPointsHelper();
		final LatLon pointToNavigate = targetPoints.getPointToNavigate();
		if (pointToNavigate == null && gpxPath == null) {
			notRestoreRoutingMode(ma, app);
		} else {
			quitRouteRestoreDialog = false;
			Runnable encapsulate = new Runnable() {
				int delay = 7;
				Runnable delayDisplay = null;

				@Override
				public void run() {
					Builder builder = new AccessibleAlertBuilder(ma);
					final TextView tv = new TextView(ma);
					tv.setText(ma.getString(R.string.continue_follow_previous_route_auto, delay + ""));
					tv.setPadding(7, 5, 7, 5);
					builder.setView(tv);
					builder.setPositiveButton(R.string.default_buttons_yes, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							quitRouteRestoreDialog = true;
							restoreRoutingModeInner();

						}
					});
					builder.setNegativeButton(R.string.default_buttons_no, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							quitRouteRestoreDialog = true;
							notRestoreRoutingMode(ma, app);
						}
					});
					final AlertDialog dlg = builder.show();
					dlg.setOnDismissListener(new OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
							quitRouteRestoreDialog = true;
						}
					});
					dlg.setOnCancelListener(new OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							quitRouteRestoreDialog = true;
						}
					});
					delayDisplay = new Runnable() {
						@Override
						public void run() {
							if(!quitRouteRestoreDialog) {
								delay --;
								tv.setText(ma.getString(R.string.continue_follow_previous_route_auto, delay + ""));
								if(delay <= 0) {
									try {
										if (dlg.isShowing() && !quitRouteRestoreDialog) {
											dlg.dismiss();
										}
										quitRouteRestoreDialog = true;
										restoreRoutingModeInner();
									} catch(Exception e) {
										// swalow view not attached exception
										log.error(e.getMessage()+"", e);
									}
								} else {
									uiHandler.postDelayed(delayDisplay, 1000);
								}
							}
						}
					};
					delayDisplay.run();
				}

				private void restoreRoutingModeInner() {
					AsyncTask<String, Void, GPXFile> task = new AsyncTask<String, Void, GPXFile>() {
						@Override
						protected GPXFile doInBackground(String... params) {
							if (gpxPath != null) {
								// Reverse also should be stored ?
								GPXFile f = GPXUtilities.loadGPXFile(app, new File(gpxPath));
								if (f.warning != null) {
									return null;
								}
								return f;
							} else {
								return null;
							}
						}

						@Override
						protected void onPostExecute(GPXFile result) {
							final GPXRouteParamsBuilder gpxRoute;
							if (result != null) {
								gpxRoute = new GPXRouteParamsBuilder(result, settings);
								if (settings.GPX_SPEAK_WPT.get()) {
									gpxRoute.setAnnounceWaypoints(true);
								}
								if (settings.GPX_ROUTE_CALC_OSMAND_PARTS.get()) {
									gpxRoute.setCalculateOsmAndRouteParts(true);
								}
								if (settings.GPX_CALCULATE_RTEPT.get()) {
									gpxRoute.setUseIntermediatePointsRTE(true);
								}
								if(settings.GPX_ROUTE_CALC.get()) {
									gpxRoute.setCalculateOsmAndRoute(true);
								}
							} else {
								gpxRoute = null;
							}
							LatLon endPoint = pointToNavigate;
							if (endPoint == null) {
								notRestoreRoutingMode(ma, app);
							} else {
								enterRoutingMode(ma, gpxRoute);
							}
						}

						
					};
					task.execute(gpxPath);

				}
			};
			encapsulate.run();
		}

	}
	
	public static void enterRoutingMode(MapActivity ma, 
			GPXRouteParamsBuilder gpxRoute) {
		OsmandApplication app = ma.getMyApplication();
		ma.getMapViewTrackingUtilities().backToLocationImpl();
		RoutingHelper routingHelper = app.getRoutingHelper();
		if(gpxRoute == null) {
			app.getSettings().FOLLOW_THE_GPX_ROUTE.set(null);
		}
		routingHelper.setGpxParams(gpxRoute);
		app.getTargetPointsHelper().setStartPoint(null, false, null);
		app.getSettings().FOLLOW_THE_ROUTE.set(true);
		routingHelper.setFollowingMode(true);
		app.getTargetPointsHelper().updateRouteAndReferesh(true);
		app.initVoiceCommandPlayer(ma);
	}
	
	private static void notRestoreRoutingMode(MapActivity ma, OsmandApplication app){
		ma.updateApplicationModeSettings();
		app.getRoutingHelper().clearCurrentRoute(null, new ArrayList<LatLon>());
		ma.refreshMap();
	}

	public static void quitRouteRestoreDialog() {
		quitRouteRestoreDialog = true;
	}
}
