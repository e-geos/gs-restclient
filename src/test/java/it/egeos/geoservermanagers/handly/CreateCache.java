package it.egeos.geoservermanagers.handly;

import org.json.JSONArray;
import org.json.JSONObject;

import it.egeos.geoserver.restmanagers.CacheManager;

public class CreateCache {

	public static void main(String[] args) {
		CacheManager c=new CacheManager("admin", "geoserver", "http://172.22.101.124:8080/geoserver_avepa/gwc/rest");

		JSONObject json=new JSONObject(){{
			put("seedRequest",new JSONObject(){{
				put("name","avepa:ALBIIGT");
		        put("zoomStart",0);
        		put("zoomStop",14);
				put("format","image/png");
				put("type","seed");
				put("threadCount",15);
			    
				put("srs",new JSONObject(){{
					put("number",3003);
				}});

				put("bounds",new JSONObject(){{
		            put("coords",new JSONObject(){{ 
		                put("double",new JSONArray(){{
		                	put(1726485.88731075);
		                    put(5153372.65823365);
		                    put(1730465.50608842);
		                    put(5157004.30556944);		                  		                    
		                }});		                
		            }});
				}});
			}});			
		}};


		System.out.println("Fine");
	}

}




