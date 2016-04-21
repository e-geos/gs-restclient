package it.egeos.geoserver.restmanagers.tuples;

import org.json.JSONObject;

public class WmsLayerTuple extends LayerTuple {
	public String nativeName=null;
	
	public WmsLayerTuple(){
			super();
	}
		
	public WmsLayerTuple(JSONObject json){
		super(json);
		this.nativeName = getStringOrNull(json, "nativeName");
	}
	
	public WmsLayerTuple(String name, String title, StoreTuple store,String nativeName) {
		super(name,title,store);
		this.nativeName = nativeName;
	}

	@Override
	public String toString() {
		return "WmsLayerTuple("+name+","+nativeName+","+title+","+store+")";
	}	
}
