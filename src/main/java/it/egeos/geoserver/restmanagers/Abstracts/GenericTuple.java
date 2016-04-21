package it.egeos.geoserver.restmanagers.Abstracts;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class GenericTuple {

	protected String getStringOrNull(JSONObject json,String attr){
		try {
			return json.getString(attr);
		} 
		catch (JSONException e) {
			return null;
		}
	}

	protected JSONObject getJSONObjectOrNull(JSONObject json,String attr){
		try {
			return json.getJSONObject(attr);
		} 
		catch (JSONException e) {
			return null;
		}
	}

}
