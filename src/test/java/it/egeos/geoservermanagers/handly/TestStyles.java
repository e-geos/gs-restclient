package it.egeos.geoservermanagers.handly;

import it.egeos.geoserver.restmanagers.GeoserverManager;
import it.egeos.geoserver.restmanagers.tuples.StyleTuple;

public class TestStyles {

	public static void main(String[] args) {
		String usr="admin";
		String pwd="geoserver";
		String url="http://tardis.cip:8086/geoserver/rest";
		
		GeoserverManager g=new GeoserverManager(usr,pwd,url);
				
		System.out.println("Getting all styles");
		
		for(StyleTuple s:g.getAllStyles())
			System.out.println("\t"+s);
	
		String layer="egs_comuni_abruzzo";
		
		System.out.println("Getting styles of "+layer);
		System.out.println("def: "+g.getDefaultStyle(layer));
		
		for(StyleTuple s:g.getLayerStyles(layer)){
			System.out.println("\t"+s);
		}
		
		g.assignStyle("geo", layer, "grass");
		g.assignOptStyle("geo", layer, "line");
		g.assignOptStyle("geo", layer, "poi");
		g.assignOptStyle("geo", layer, "grass");
		g.removeStyle(layer, "green");
	
		System.out.println("fine");
	}
	
}
