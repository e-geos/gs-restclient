package it.egeos.geoservermanagers.handly;

import it.egeos.geoserver.restmanagers.tuples.LayerTuple;
import it.egeos.geoserver.restmanagers.tuples.StyleTuple;
import it.egeos.geoservermanagers.handly.abstracts.AbstractTest;

import java.util.LinkedHashMap;

public class TestLayerGroups extends AbstractTest{

	public static void main(String[] args) {
		String lg_name="lg003";
		String ws="geo";
		
		LinkedHashMap<LayerTuple, StyleTuple> subs = g.getSubLayers(ws, lg_name);
		
		for(LayerTuple s:subs.keySet()){
		   System.out.println(s+" "+subs.get(s));
		    
		}
		
		System.out.println("Fine");
	}

	
	
	
}
