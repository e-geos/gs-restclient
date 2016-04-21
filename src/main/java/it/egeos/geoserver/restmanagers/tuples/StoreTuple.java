package it.egeos.geoserver.restmanagers.tuples;

import it.egeos.geoserver.restmanagers.Abstracts.GenericTuple;
import it.egeos.geoserver.restmanagers.types.StoreTypes;

import org.json.JSONObject;

public class StoreTuple extends GenericTuple{
	public String name=null;
	public String type=null;
	public WorkspaceTuple workspace=null;
	
	public StoreTuple(){
		super();
	}

	public StoreTuple(JSONObject json) {
		super();
		this.name=getStringOrNull(json, "name");
		this.type=getTypeByClass(json.optString("@class"));
		
		JSONObject ws = getJSONObjectOrNull(json, "workspace");
		if (ws!=null)
			this.workspace = new WorkspaceTuple(ws);
	}

	public StoreTuple(String name, String type, WorkspaceTuple workspace) {
		super();
		this.name = name;
		this.type = type;
		this.workspace = workspace;
	}

	public String getShortName(){
		String res=name;
		try {
			res=name.split(":")[1];			
		} 
		catch (ArrayIndexOutOfBoundsException|NullPointerException e) {
			//name is null so no split is needed or no contains :
		}		
		return res;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StoreTuple other = (StoreTuple) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} 
		else if (!name.equals(other.name))
			return false;		
		if (type == null) {
			if (other.type != null)
				return false;
		} 
		else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "StoreTuple("+name+","+type+","+workspace+")";
	}
	
	protected String getTypeByClass(String cls){
		if (cls.equals("wmsStore"))
			return StoreTypes.WMS;
		return null;
	}
}
