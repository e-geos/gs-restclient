package it.egeos.geoserver.restmanagers.tuples;

import it.egeos.geoserver.restmanagers.Abstracts.GenericTuple;

import java.util.ArrayList;
import java.util.List;

public class LayerTupleCache extends GenericTuple{
	public String name=null;
	public Boolean enabled=null;
	public List<String> format;
	public List<String> gridSubset;
	public Double bXmin;
	public Double bYmin;
	public Double bXmax;
	public Double bYmax;
	
	public LayerTupleCache(){
		super();
	}
	
	public LayerTupleCache(String name, Boolean enabled) {
		super();
		this.name = name;
		this.enabled = enabled;
		this.format=new ArrayList<String>();
		this.gridSubset=new ArrayList<String>();
	}
	
	public void setBoundingBox(Double bXmin,Double bYmin,Double bXmax,Double bYmax){
		this.bXmin=bXmin;
		this.bYmin=bYmin;
		this.bXmax=bXmax;
		this.bYmax=bYmax;
	}
	
	
	public void addFormat(String f){
		format.add(f);
	}
	
	public void addGridSubset(String gs){
		gridSubset.add(gs);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public List<String> getFormat() {
		return format;
	}

	public void setFormat(List<String> format) {
		this.format = format;
	}

	public List<String> getGridSubset() {
		return gridSubset;
	}

	public void setGridSubset(List<String> gridSubset) {
		this.gridSubset = gridSubset;
	}
	

	public Double getbXmin() {
		return bXmin;
	}

	public void setbXmin(Double bXmin) {
		this.bXmin = bXmin;
	}

	public Double getbYmin() {
		return bYmin;
	}

	public void setbYmin(Double bYmin) {
		this.bYmin = bYmin;
	}

	public Double getbXmax() {
		return bXmax;
	}

	public void setbXmax(Double bXmax) {
		this.bXmax = bXmax;
	}

	public Double getbYmax() {
		return bYmax;
	}

	public void setbYmax(Double bYmax) {
		this.bYmax = bYmax;
	}

	@Override
	public String toString() {
		return "LayerTupleCache [name=" + name + ", enabled=" + enabled
				+ ", format=" + format + ", gridSubset=" + gridSubset
				+ ", bXmin=" + bXmin + ", bYmin=" + bYmin + ", bXmax=" + bXmax
				+ ", bYmax=" + bYmax + "]";
	}	
}
