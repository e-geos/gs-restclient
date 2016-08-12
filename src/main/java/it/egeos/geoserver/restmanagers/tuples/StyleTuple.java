package it.egeos.geoserver.restmanagers.tuples;

import it.egeos.geoserver.restmanagers.Abstracts.GenericTuple;

import org.json.JSONObject;

public class StyleTuple extends GenericTuple{
	public String name=null;
	public String format=null;
	public String filename=null;
	public WorkspaceTuple workspace=null;

	public StyleTuple() {
		super();
	}
	
	public StyleTuple(JSONObject json) {
		super();
		this.name=getStringOrNull(json,"name");
		this.format=getStringOrNull(json,"format");
		this.filename=getStringOrNull(json,"filename");		
		JSONObject ws = getJSONObjectOrNull(json, "workspace");
		if (ws!=null)
			this.workspace= new WorkspaceTuple(ws);
	}

	public StyleTuple(String name, String format, String filename,WorkspaceTuple workspace) {
		super();
		this.name = name;
		this.format = format;
		this.filename = filename;
		this.workspace = workspace;
	}
	
	public String getWorkspaceName(){
		String ws=null;
		try{
			ws=workspace.name;
		}
		catch (NullPointerException e){
		}
		return ws;
	}
	
	public String toString(){
		return (workspace!=null?workspace.name+":":"")+name;
	}
}
