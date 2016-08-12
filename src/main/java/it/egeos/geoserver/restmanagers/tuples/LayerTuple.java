package it.egeos.geoserver.restmanagers.tuples;

import it.egeos.geoserver.restmanagers.Abstracts.GenericTuple;
import it.egeos.geoserver.restmanagers.types.StoreTypes;

import org.json.JSONObject;

public class LayerTuple extends GenericTuple{
	public String name=null;
	public String nativeName=null;
	public String title=null;
	public StoreTuple store=null;
    public double maxX;
    public double maxY;
    public double minX;
    public double minY;
    public String crs;
	
	public LayerTuple(){
		super();
	}

	public LayerTuple(JSONObject json){
		super();
		this.name = getStringOrNull(json,"name");
		this.title = getStringOrNull(json,"title");
		JSONObject store_info = getJSONObjectOrNull(json,"store");
		if (store_info!=null)
			this.store = new StoreTuple(store_info);
	}
	
	public LayerTuple(String name, String title, StoreTuple store) {
		super();
		this.name = name;
		this.title = title;
		this.store = store;
	}
	
    public boolean isLayerGroup(){
		return store!=null && store.type.equals(StoreTypes.LAYERGROUP);
	}
	
	public String getCompleteName(){
		String res=null;
		try{
			if(name!=null)
				res=store.workspace.name+":"+name;
		}
		catch(NullPointerException e){
			//we try to reach workspace name, if it fails, we return short name			
		}		
		return res;
	}

    @Override
    public String toString() {
        return "LayerTuple [name=" + name + ", title=" + title + ", store=" + store + ", maxX=" + maxX + ", maxY="
                + maxY + ", minX=" + minX + ", minY=" + minY + ", crs=" + crs + "]";
    }

}
