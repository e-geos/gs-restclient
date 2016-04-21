package it.egeos.geoserver.restmanagers.Abstracts;

import java.net.URL;
import java.util.ArrayList;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.log4j.Logger;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 
 * @author Federico C. Guizzardi - cippinofg <at> gmail.com
 * 
 * RestManager is a (abstract) class to comunicate with Geoserver Rest API
 * 
 */

public abstract class RestManager {
	protected Logger log = Logger.getLogger(this.getClass());
		
	//Mediatypes, only json at this time 
	protected String MEDIATYPE=MediaType.APPLICATION_JSON;
	protected MediaType MEDIATYPE_POST=MediaType.APPLICATION_JSON_TYPE;
		
	//Geoserver location, base and rest urls
	protected static String SERVER_REST_URL="http://localhost/geoserver/rest";
	
    private ClientConfig cc=new ClientConfig(){{            
        connectorProvider(new ApacheConnectorProvider());
        property(ApacheClientProperties.PREEMPTIVE_BASIC_AUTHENTICATION,true);                       
    }};
	
	protected JerseyClient c= null;
	protected WebTarget t;

	/*
	 * Constructor with login/password and geoserver on url
	 */
	public RestManager(String login,String password,String url) {
		SERVER_REST_URL=url;
		log.info("gs-restclient: "+login+":"+((password!=null && !password.isEmpty())?"*******":"null")+"@"+SERVER_REST_URL);
		c = JerseyClientBuilder.createClient(cc);
		if (login!=null && password!=null && !login.trim().isEmpty() && !password.trim().isEmpty())
			c.register(HttpAuthenticationFeature.basic(login,password));
		
        if (SERVER_REST_URL!=null && !SERVER_REST_URL.trim().isEmpty())
        	t = c.target(SERVER_REST_URL);
        else
        	log.error("created with no URL");
	}
	
	/*
	 * GET call who returns a plain text (parsable json) 
	 */
	protected String makeRequest(URL url){
		return makeRequest(url.toString().replace(SERVER_REST_URL,""));
	}
	
	/*
	 * GET call who returns a plain text (parsable json) 
	 */
	protected String makeRequest(String service){	    
		log.info("GET "+SERVER_REST_URL+service);
		String res=t.path(service).request().accept(MEDIATYPE).get(String.class);
		log.debug("Done GET "+SERVER_REST_URL+service+": "+res);
		return res;
	}
	
	/*
	 * GET call who returns a JSONArray
	 */
	protected JSONArray makeRequestJSONArray(String service){
		return new JSONArray(makeRequest(service));
	}
	
	/*
	 * POST call to service with f params. Returns a resp object
	 */
	protected <T> T makePost(String service,Form f,Class<T> resp){
		return makePost(service,Entity.entity(f,MEDIATYPE),resp);		
	}

	/*
	 * POST call to service with json (org.json.JSONObject) params. Returns a resp object
	 */
	protected <T> T makePost(String service,JSONObject json,Class<T> resp){
		return makePost(service,Entity.entity(json.toString(),MEDIATYPE),resp);
	}

	/*
	 * POST call to service with et params. Returns a resp object
	 */
	protected <T> T makePost(String service,Entity<?> et,Class<T> resp){
		log.info("POST "+SERVER_REST_URL+service);
		T res = t.path(service).request(MEDIATYPE_POST).post(et,resp);
		log.debug("Done POST "+SERVER_REST_URL+service+": "+res);
		return res;
	}

	/*
	 * PUT call to service with json params. Returns a resp object
	 */
	protected <T> T makePut(String service,JSONObject payload,Class<T> resp){
		return makePut(service, payload.toString(), resp);
	}
	
	/*
	 * PUT call to service with json params. Returns a resp object
	 */
	protected <T> T makePut(String service,String payload,Class<T> resp){
		log.info("PUT "+SERVER_REST_URL+service);
		T res=t.path(service).request(MEDIATYPE_POST).put(Entity.entity(payload,MediaType.APPLICATION_JSON),resp);
		log.debug("Done PUT "+SERVER_REST_URL+service+": "+res);
		return res;
	}
	
	/*
	 * DELETE call to service with params. Returns a resp object
	 */	
	protected <T> T makeDelete(String service,MultivaluedMap<String, String> attrs, Class<T> resp){
		log.info("DELETE "+SERVER_REST_URL+service);
		WebTarget tmp = t.path(service);
		if (attrs!=null)
			for(String k:attrs.keySet())
				tmp=tmp.queryParam(k, attrs.get(k));
		
		T res=tmp.request(MEDIATYPE_POST).delete(resp);
		log.debug("Done DELETE "+SERVER_REST_URL+service+": "+res);
		return res;
	}

	/*
	 * Utility to convert JSONArray to ArrayList
	 */
	@SuppressWarnings({ "serial", "unchecked" })
	protected <T> ArrayList<T> toArrayList(final JSONArray ja,Class<T> cls){
		return new ArrayList<T>(){{
	        for(int i=0;i<ja.length();i++)
	        	add((T)ja.get(i));
		}};
	}

	public void shutdown(){	    
        if (c!=null && !c.isClosed())
	        c.close();
	}
	
	@Override
	protected void finalize() throws Throwable {
	    shutdown();
	    super.finalize();
	}
	
	
}

