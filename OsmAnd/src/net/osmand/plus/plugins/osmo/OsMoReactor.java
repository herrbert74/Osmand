package net.osmand.plus.plugins.osmo;

import org.json.JSONObject;

public interface OsMoReactor {

	public boolean acceptCommand(String command, String data, JSONObject obj, OsMoThread tread);

}