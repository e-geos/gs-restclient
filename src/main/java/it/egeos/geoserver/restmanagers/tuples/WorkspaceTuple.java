package it.egeos.geoserver.restmanagers.tuples;

import it.egeos.geoserver.restmanagers.Abstracts.GenericTuple;

import org.json.JSONObject;

public class WorkspaceTuple extends GenericTuple{
	public String name=null;
	
	public WorkspaceTuple() {
		super();
	}

	public WorkspaceTuple(JSONObject json){
		super();
		this.name = getStringOrNull(json,"name");
	}
	
	public WorkspaceTuple(String name) {
		super();
		this.name = name;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WorkspaceTuple other = (WorkspaceTuple) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "WorkspaceTuple(" + name + ")";
	}	
}
