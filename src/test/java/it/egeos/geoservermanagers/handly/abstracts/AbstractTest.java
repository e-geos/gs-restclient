package it.egeos.geoservermanagers.handly.abstracts;

import it.egeos.geoserver.restmanagers.GeoserverManager;

public abstract class AbstractTest {
	protected static String usr="admin";
	protected static String pwd="geoserver";
	protected static String url="http://tardis.cip:8086/geoserver/rest";
	
	protected static GeoserverManager g=new GeoserverManager(usr,pwd,url);

}
