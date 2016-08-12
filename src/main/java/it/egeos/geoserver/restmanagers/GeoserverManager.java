package it.egeos.geoserver.restmanagers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Form;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.egeos.geoserver.restmanagers.Abstracts.RestManager;
import it.egeos.geoserver.restmanagers.interfaces.GeoserverManagerAPI;
import it.egeos.geoserver.restmanagers.tuples.LayerGroupTuple;
import it.egeos.geoserver.restmanagers.tuples.LayerTuple;
import it.egeos.geoserver.restmanagers.tuples.SqlLayerTuple;
import it.egeos.geoserver.restmanagers.tuples.StoreTuple;
import it.egeos.geoserver.restmanagers.tuples.StyleTuple;
import it.egeos.geoserver.restmanagers.tuples.VTGeometryTuple;
import it.egeos.geoserver.restmanagers.tuples.VTParameterTuple;
import it.egeos.geoserver.restmanagers.tuples.WmsStoreTuple;
import it.egeos.geoserver.restmanagers.tuples.WorkspaceTuple;
import it.egeos.geoserver.restmanagers.types.StoreTypes;
import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import it.geosolutions.geoserver.rest.GeoServerRESTReader;
import it.geosolutions.geoserver.rest.decoder.RESTBoundingBox;
import it.geosolutions.geoserver.rest.decoder.RESTCoverageList;
import it.geosolutions.geoserver.rest.decoder.RESTDataStore;
import it.geosolutions.geoserver.rest.decoder.RESTDataStore.DBType;
import it.geosolutions.geoserver.rest.decoder.RESTDataStoreList;
import it.geosolutions.geoserver.rest.decoder.RESTFeatureTypeList;
import it.geosolutions.geoserver.rest.decoder.RESTLayer;
import it.geosolutions.geoserver.rest.decoder.RESTLayerGroup;
import it.geosolutions.geoserver.rest.decoder.RESTLayerGroupList;
import it.geosolutions.geoserver.rest.decoder.RESTPublished;
import it.geosolutions.geoserver.rest.decoder.RESTPublishedList;
import it.geosolutions.geoserver.rest.decoder.RESTResource;
import it.geosolutions.geoserver.rest.decoder.RESTStyleList;
import it.geosolutions.geoserver.rest.decoder.RESTWmsList;
import it.geosolutions.geoserver.rest.decoder.RESTWmsStore;
import it.geosolutions.geoserver.rest.decoder.utils.NameLinkElem;
import it.geosolutions.geoserver.rest.encoder.GSLayerEncoder;
import it.geosolutions.geoserver.rest.encoder.GSLayerGroupEncoder23;
import it.geosolutions.geoserver.rest.encoder.datastore.GSPostGISDatastoreEncoder;
import it.geosolutions.geoserver.rest.encoder.feature.GSFeatureTypeEncoder;
import it.geosolutions.geoserver.rest.encoder.metadata.virtualtable.GSVirtualTableEncoder;
import it.geosolutions.geoserver.rest.encoder.metadata.virtualtable.VTGeometryEncoder;
import it.geosolutions.geoserver.rest.encoder.metadata.virtualtable.VTParameterEncoder;
import it.geosolutions.geoserver.rest.manager.GeoServerRESTStoreManager;

/**
 * 
 * @author Federico C. Guizzardi - cippinofg <at> gmail.com
 * 
 * RestManager implementation to control a Geoserver
 *
 */

public class GeoserverManager extends RestManager implements GeoserverManagerAPI {
    //Rest API for User/Group Management
    protected static String USERS_SERVICE="/users.json";
    protected static String GROUPS_SERVICE="/groups.json";	
    protected static String MEMBERS_SERVICE="/members.json";
    
    //Rest API for Rules Management
    protected static String RULES_SERVICE="/rules.json";

    //Rest API for Roles Management
    protected static String ROLES_SERVICE="/roles.json"; 
    protected static String USERROLES_SERVICE="/userrolerefs.json";
    protected static String GROUPROLES_SERVICE="/grouprolerefs.json";
    
    //Rest API for Stores Management (physical)
    protected static String BASE_STORES_SERVICE="/stores.json";
    
    //Rest API for standard Geoserver 
    protected static String WORKSPACES_SERVICE="/workspaces.json";
    protected static String DATASTORES_SERVICE="/workspaces/%s/datastores.json";
    protected static String COVERAGESTORES_SERVICE="/workspaces/%s/coveragestores.json";
    protected static String WMSSTORES_SERVICE="/workspaces/%s/wmsstores.json";
    protected static String WMSSTORE_SERVICE="/workspaces/%s/wmsstores/%s.json";
    protected static String LAYERS_BY_DATASTORE_SERVICE="/workspaces/%s/datastores/%s/featuretypes.json";
    protected static String LAYERS_BY_COVERAGESTORE_SERVICE="/workspaces/%s/coveragestores/%s/coverages.json";
    protected static String LAYERS_BY_WMSSTORE_SERVICE="/workspaces/%s/wmsstores/%s/wmslayers.json";
    protected static String LAYERSTREE_SERVICE="/layertree.json";
    protected static String LAYERS_SERVICE="/layers.json";
    protected static String LAYER_SERVICE="/layers/%s";
    protected static String LAYER_STYLES_SERVICE="/layers/%s/styles.json";
    protected static String LAYERGROUPS_SERVICE="/layergroups.json";
    protected static String WS_LAYERGROUPS_SERVICE="/workspaces/%s/layergroups.json"; 
    protected static String WS_LAYERGROUP_SERVICE="/workspaces/%s/layergroups/%s.json"; 
    protected static String FEATURETYPE_SERVICE="/workspaces/%s/datastores/%s/featuretypes/%s.json";
    protected static String COVERAGE_SERVICE="/workspaces/%s/coveragestores/%s/coverages/%s.json";
    protected static String WMSLAYER_SERVICE="/workspaces/%s/wmsstores/%s/wmslayers/%s.json";
    protected static String STYLES_SERVICE="/styles.json";
    protected static String STYLE_SERVICE="/styles/%s.json";
    protected static String WS_STYLES_SERVICE="/workspaces/%s/styles.json";
    protected static String WS_STYLE_SERVICE="/workspaces/%s/styles/%s.json";
    
    //Common separator
    protected static String VSEPARATOR=";";
    protected static String RULES_VSEPARATOR=",";
    
    //Patterns
    protected Pattern WORKSPACE_IN_HREF=Pattern.compile(".*/workspaces/(.*?)/((datastores)|(coveragestores)|(wmsstores))/.*");
    protected Pattern SUBPATH = Pattern.compile("http://.*/rest(/.*)$");
    
    //Comunication managers: GeoServerRESTReader/GeoServerRESTPublisher from geoserver-manager
    protected GeoServerRESTReader reader =null;
    protected GeoServerRESTPublisher publisher = null;
    protected GeoServerRESTStoreManager storemanager=null;

    //Available access mode
    public final static char ADMIN='a'; 
    public final static char READ='r';
    public final static char WRITE='w';
	
    public GeoserverManager(String login, String password, String url) {
        super(login,password,url);

        String base=SERVER_REST_URL.replaceAll("/rest", "");
        try {
            reader=new GeoServerRESTReader(base, login, password);
        } 
        catch (IllegalArgumentException|MalformedURLException e) {
            //no connection is possible
            log.error("URL '"+url+"' is bad!",e);
        }
        try {
            publisher = new GeoServerRESTPublisher(base, login, password);
        }
        catch (IllegalArgumentException e) {
            //no connection is possible
            log.error("Generic exception creating GeoServerRESTPublisher",e);
        } 
        try {
            storemanager=new GeoServerRESTStoreManager(new URL(base), login, password);
        } 
        catch (IllegalArgumentException|MalformedURLException e) {
            //no connection is possible
            log.error("Generic exception creating GeoServerRESTStoreManager",e);
        } 
    }
	
    /* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#getAllLayers()
     */
    @Override
    @SuppressWarnings("serial")
	public Map<String,List<LayerTuple>> getAllLayers(){
    	return new HashMap<String,List<LayerTuple>>(){{
    		try {
				JSONObject p = new JSONObject(makeRequest(LAYERSTREE_SERVICE)).getJSONObject("layers");								
				put(StoreTypes.DATA,new ArrayList<LayerTuple>(){{
					try {
						JSONArray fts = p.getJSONArray("featureTypes");
						for(int i=0;i<fts.length();i++){
							JSONObject l = fts.getJSONObject(i);
							add(new LayerTuple(
								l.getString("name"),
								l.getString("title"), 
								new StoreTuple(
									l.getJSONObject("store").getString("name"),
									StoreTypes.DATA, 
									new WorkspaceTuple(l.getJSONObject("workspace"))
								)
							));
						}
					} 
					catch (JSONException |NullPointerException e) {
		    			log.debug("Element 'layers'.'featureTypes' not found in response: "+e.getMessage(),e);
		    		}    						
				}});
				
				put(StoreTypes.COVERAGE,new ArrayList<LayerTuple>(){{
					try {
						JSONArray cs = p.getJSONArray("coverages");
						for(int i=0;i<cs.length();i++){
							JSONObject l = cs.getJSONObject(i);
							add(new LayerTuple(
								l.getString("name"),
								l.getString("title"), 
								new StoreTuple(
									l.getJSONObject("store").getString("name"),
									StoreTypes.COVERAGE, 
									new WorkspaceTuple(l.getJSONObject("workspace"))
								)
							));
						}
					} 
					catch (JSONException |NullPointerException e) {
		    			log.debug("Element 'layers'.'coverages' not found in response: "+e.getMessage(),e);
		    		}    	
				}});
				
				put(StoreTypes.WMS,new ArrayList<LayerTuple>(){{
					try {
						JSONArray wmss = p.getJSONArray("wmsLayers");
						for(int i=0;i<wmss.length();i++){
							JSONObject l = wmss.getJSONObject(i);
							add(new LayerTuple(
								l.getString("name"),
								l.getString("title"), 
								new StoreTuple(
									l.getJSONObject("store").getString("name"),
									StoreTypes.WMS, 
									new WorkspaceTuple(l.getJSONObject("workspace"))
								)
							));
						}
					} 
					catch (JSONException |NullPointerException e) {
		    			log.debug("Element 'layers'.'wmsLayers' not found in response: "+e.getMessage(),e);
		    		}    										
				}});
				
				put(StoreTypes.LAYERGROUP,new ArrayList<LayerTuple>(){{
					try {
						JSONArray lgs = p.getJSONArray("layergroups");
						for(int i=0;i<lgs.length();i++){
							JSONObject l = lgs.getJSONObject(i);
							add(new LayerTuple(
								l.getString("name"),
								l.getString("title"), 
								new StoreTuple(
									null,
									StoreTypes.LAYERGROUP, 
									new WorkspaceTuple(l.getJSONObject("workspace"))
								)
							));
						}
					} 
					catch (JSONException |NullPointerException e) {
		    			log.debug("Element 'layers'.'layergroups' not found in response: "+e.getMessage(),e);
		    		}    	
				}});
			} 
    		catch (JSONException |NullPointerException e) {
    			log.debug("Element 'layers' not found in response: "+e.getMessage(),e);
    		}    		
    	}};
    }
    
    /* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#getWorkspaces()
     */
    @Override
    @SuppressWarnings("serial")
    public ArrayList<WorkspaceTuple> getWorkspaces(){
        return new ArrayList<WorkspaceTuple>(){{
            try {
                JSONObject p = new JSONObject(makeRequest(WORKSPACES_SERVICE));
                try {
                    JSONArray arr = p.getJSONObject("workspaces").getJSONArray("workspace");
                    for(int i=0;i<arr.length();i++){
                        JSONObject o = arr.getJSONObject(i);
                        add(new WorkspaceTuple(o));
                    }
                } 
                catch (JSONException e) {
                    log.debug("No 'workspaces' or 'workspace' in json >>"+p+"<<",e);
                }
            }
            catch (ProcessingException e) {
                log.error("Can't get workspaces list from geoserver", e);
            }
        }};
    }
	
    /* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#addWorkspace(java.lang.String)
     */
    @Override
    public boolean addWorkspace(String ws){
        return publisher.createWorkspace(ws);
    }

    /* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#delWorkspace(java.lang.String)
     */
    @Override
    public boolean delWorkspace(String ws){
        return publisher.removeWorkspace(ws,true);
    }
            	
    /* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#getPostgisStores(it.egeos.geoserver.restmanagers.tuples.WorkspaceTuple)
     */
    @Override
    @SuppressWarnings("serial")
    public List<StoreTuple> getPostgisStores(final WorkspaceTuple workspace){
        return new ArrayList<StoreTuple>(){{
            RESTDataStoreList dsl = reader.getDatastores(workspace.name);
            if(dsl!=null)
                for(int i=0;i<dsl.size();i++){
                    NameLinkElem item = dsl.get(i);
                    RESTDataStore ds = reader.getDatastore(workspace.name, item.getName());                    
                    if (ds.getType().equals(DBType.POSTGIS))
                        add(new StoreTuple(item.getName(),StoreTypes.DATA,workspace));
                }
        }};
    }
	
    /* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#getCoverageStores(it.egeos.geoserver.restmanagers.tuples.WorkspaceTuple)
     */
    @Override
    @SuppressWarnings("serial")
    public ArrayList<StoreTuple> getCoverageStores(final WorkspaceTuple workspace){
        return new ArrayList<StoreTuple>(){{						
            try {
                JSONObject p = new JSONObject(makeRequest(String.format(COVERAGESTORES_SERVICE, workspace.name)));
                JSONArray arr = p.getJSONObject("coverageStores").getJSONArray("coverageStore");
                for(int i=0;i<arr.length();i++)		
                    add(new StoreTuple(arr.getJSONObject(i).getString("name"),StoreTypes.COVERAGE,workspace));
            } 
            catch (JSONException e) {
                //json is not parsable, so we returns an empty list
            }
            catch (ProcessingException e) {
                log.error("Can't get coveragestores list from workspace "+workspace.name, e);
            }                
        }};
    }
	
    /* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#getWmsStores(it.egeos.geoserver.restmanagers.tuples.WorkspaceTuple)
     */
    @Override
    @SuppressWarnings("serial")
    public ArrayList<StoreTuple> getWmsStores(final WorkspaceTuple workspace){
        return new ArrayList<StoreTuple>(){{
            try {
                JSONObject p = new JSONObject(makeRequest(String.format(WMSSTORES_SERVICE, workspace.name)));
                JSONArray arr = p.getJSONObject("wmsStores").getJSONArray("wmsStore");
                for(int i=0;i<arr.length();i++)		
                    add(new StoreTuple(arr.getJSONObject(i).getString("name"),StoreTypes.WMS,workspace));
            } 
            catch (JSONException e) {
                //json is not parsable, so we returns an empty list
            }
            catch (ProcessingException e) {
                log.error("Can't get wmstores list from workspace "+workspace.name, e);
            }
        }};
    }

    @Override
    public WmsStoreTuple getWmsStore(final String workspace,String name){
        RESTWmsStore st = reader.getWmsStore(workspace, name);
        return new WmsStoreTuple(st.getName(), StoreTypes.WMS, new WorkspaceTuple(st.getWorkspaceName()),st.getCapabilitiesURL(),st.getUser(),st.getPassword());
    }

    /* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#createWmsStore(it.egeos.geoserver.restmanagers.tuples.WorkspaceTuple, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public String createWmsStore(final WorkspaceTuple workspace,final String name,final String url,final String usr, final String pwd){
        JSONObject json=new JSONObject(){{
            put("wmsStore", new JSONObject(){{
                put("name",name.replaceAll("\\W","_"));
                put("type","WMS");
                put("capabilitiesURL",url);
                if(usr!=null && pwd!=null){
                    put("user",usr);
                    put("password",pwd);
                }
            }});
        }};  

        return makePost(String.format(WMSSTORES_SERVICE,workspace.name),json, String.class);
    }

    /* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#deleteWMSStore(it.egeos.geoserver.restmanagers.tuples.WorkspaceTuple, java.lang.String)
     */
    @Override
    public String deleteWMSStore(final WorkspaceTuple workspace,String name){		
        Form form =new Form();	
        form.param("recurse","true");			
        return makeDelete(String.format(WMSSTORE_SERVICE,workspace.name,name), form.asMap(), String.class);
    }
        				
    /* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#getLayerStyles(java.lang.String)
     */
    @Override
    @SuppressWarnings("serial")
    public List<StyleTuple> getLayerStyles(final String layer){
        return new ArrayList<StyleTuple>(){{
            JSONObject p = new JSONObject(makeRequest(String.format(LAYER_SERVICE,layer)));			
            try {
                JSONArray styles = p.getJSONObject("layer").getJSONObject("styles").getJSONArray("style");
                if(styles!=null)
                for(int i=0;i<styles.length();i++)
                	try {					    
					    add(new StyleTuple(new JSONObject(makeRequest(new URL(styles.getJSONObject(i).getString("href")))).getJSONObject("style")));
					} 
					catch (MalformedURLException |JSONException e) {
						log.debug("Generic error parsing "+styles.getJSONObject(i)+": "+e.getMessage());
					}
                
            } 
            catch (JSONException e) {
                //Response is not parsable
                log.debug("JSONException in getLayerStyles("+layer+"): "+e.getMessage(),e);
            }
        }};
    }
		
    
    /* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#getLayerGroups(java.lang.String)
     */
    @Override
    @SuppressWarnings("serial")
    public List<LayerGroupTuple> getLayerGroups(final String workspace){	    
        return new ArrayList<LayerGroupTuple>(){{
            RESTLayerGroupList lgs = reader.getLayerGroups(workspace);
            if(lgs!=null)
                for(int i=0;i<lgs.size();i++){
                    RESTLayerGroup lg = reader.getLayerGroup(workspace, lgs.get(i).getName());
    	            add(new LayerGroupTuple(
	                    lg.getName(),
	                    lg.getTitle(),
	                    lg.getMaxX(),
	                    lg.getMaxY(),
	                    lg.getMinX(),
	                    lg.getMinY(),
	                    lg.getCRS()
                    ));
                }
        }};
    }

    /*
        * returns a list of <name,style> merging 'publishables' and 'styles' properties 
        */	
    /* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#getSubLayers(java.lang.String, java.lang.String)
     */
    @Override
    @SuppressWarnings("serial")
    public LinkedHashMap<LayerTuple,StyleTuple> getSubLayers(final String workspace,final String layergroup){
        return new LinkedHashMap<LayerTuple,StyleTuple>(){{
            RESTLayerGroup lg = reader.getLayerGroup(workspace, layergroup);        
            RESTPublishedList layers = lg.getPublishedList();       
            RESTStyleList styles = lg.getStyleList();
            for(int i=0;i<layers.size();i++){			    
                RESTPublished p = layers.get(i);
                NameLinkElem nle = styles.get(i);
                    
                LayerTuple lt=null;
                try {
                    RESTLayer l = reader.getLayer(workspace, trimName(p.getName()));
                    lt=new LayerTuple(l.getName(), l.getTitle(), new StoreTuple());
                }
                catch (Exception e) {
                    RESTLayerGroup lgs= reader.getLayerGroup(workspace,trimName(p.getName()));
                    lt=new LayerTuple(lgs.getName(), lgs.getTitle(), new StoreTuple());
                }

                if (lt!=null){
                    StyleTuple st =null;
                    if (nle!=null && nle.getName()!=null){
                        String name=nle.getName();
                        st= new StyleTuple(name, null, null,new WorkspaceTuple(prefixName(name)));
                    }
                    put(lt,st);
                }
            }
        }};
    }
	
    private String prefixName(String name){
        String res=null;
        try {
            String[] parts = name.split(":");
            res=parts.length>1? parts[0]:null;         
        } 
        catch (ArrayIndexOutOfBoundsException|NullPointerException e) {
            //name is null so no split is needed or no contains :
        }       
        return res;
    }
	
    private String trimName(String name){
        String res=name;
        try {
            res=name.split(":")[1];         
        } 
        catch (ArrayIndexOutOfBoundsException|NullPointerException e) {
            //name is null so no split is needed or no contains :
        }       
        return res;
    }
	
    /*
        * Sets 'publishables' and 'styles' using subs, a <name,style> list 
        */	
    /* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#assignSubLayers(java.lang.String, java.lang.String, java.util.LinkedHashMap)
     */
    @Override
    public Boolean assignSubLayers(String workspace,String glayer,LinkedHashMap<LayerTuple, String> subs){
        GSLayerGroupEncoder23 conf = new GSLayerGroupEncoder23();	
        for(LayerTuple sl:subs.keySet())
            if (sl.isLayerGroup())
                conf.addLayerGroup(sl.getCompleteName());
            else
                conf.addLayer(sl.name, subs.get(sl));					
        return publisher.configureLayerGroup(workspace, glayer,conf);
    }
	
    /* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#addLayerGroup(java.lang.String, java.lang.String, java.util.List)
     */
    @Override
    public String addLayerGroup(final String workspace,final String layer,final List<LayerTuple> subL){
        JSONObject payload=new JSONObject(){{
            put("layerGroup",new JSONObject(){{
                put("name",layer);
                put("workspace",new JSONObject(){{
                    put("name",workspace);
                }});
                put("publishables",new JSONObject(){{
                    put("published",new JSONArray(){{
                        for(final LayerTuple lt:subL){
                            put(new JSONObject(){{
                                put("@type",lt.isLayerGroup()?"layerGroup":"layer");
                                put("name",lt.getCompleteName());
                            }});
                        }
                    }});
                }});				
            }});
        }};
        return makePost(String.format(WS_LAYERGROUPS_SERVICE,workspace), payload, String.class);
    }
	
    /* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#deleteLayerGroup(java.lang.String, java.lang.String)
     */
    @Override
    public Boolean deleteLayerGroup(String workspace,String layer){
        return publisher.removeLayerGroup(workspace, layer);
    }
		
    /* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#getFeatureLayersList(java.lang.String)
     */
    @Override
    @SuppressWarnings("serial")
    public ArrayList<LayerTuple> getFeatureLayersList(final String workspace){
        return new ArrayList<LayerTuple>(){{			 
            try{			   			   
                RESTFeatureTypeList fts = reader.getFeatureTypes(workspace);
                for(int i=0;i<fts.size();i++){ 
                    NameLinkElem ft = fts.get(i);
                    try {
                        add(res2lt(reader.getFeatureType(reader.getLayer(workspace, ft.getName())),workspace));
                    }
                    catch (NullPointerException e) {
                        log.debug("Layer "+workspace+":"+ft.getName(),e);
                    }
                }
            } 
            catch (JSONException e) {
                log.debug("JSONException in getFeatureLayersList("+workspace+"): "+e.getMessage());
            }
            catch (ProcessingException e) {
                log.error("Can't get featurelayers list from workspace/store "+workspace, e);
            }
        }};
    }
    
    @Override
    public LayerTuple getFeatureLayer(final String workspace,String name){
        LayerTuple res=null;        
        try{                           
            res=res2lt(reader.getFeatureType(reader.getLayer(workspace,name)),workspace);
        }       
        catch (ProcessingException e) {
            log.error("Can't get featuretype list from workspace "+workspace, e);
        }
        return res;
    }
    
    @Override
    public LayerTuple getWMSLayer(final String workspace,String name){
        LayerTuple res=null;        
        try{                           
            res=res2lt(reader.getWms(reader.getLayer(workspace,name)),workspace);
        } 
        catch (ProcessingException e) {
            log.error("Can't get wms list from workspace "+workspace, e);
        }
        return res;
    }
    
    @Override
    public LayerTuple getCoverageLayer(final String workspace,String name){
        LayerTuple res=null;        
        try{                           
            res=res2lt(reader.getCoverage(reader.getLayer(workspace,name)),workspace);
        } 
        catch (ProcessingException e) {
            log.error("Can't get wms list from workspace "+workspace, e);
        }
        return res;
    }
    
    private LayerTuple res2lt(RESTResource ft, String ws){
        LayerTuple lt = new LayerTuple(ft.getName(), ft.getTitle(), new StoreTuple(ft.getStoreName(), ft.getStoreType(), new WorkspaceTuple(ws)));
        RESTBoundingBox b = ft.getNativeBoundingBox();        
        lt.minX=b.getMinX();
        lt.minY=b.getMinY();
        lt.maxX=b.getMaxX();
        lt.maxY=b.getMaxY();
        lt.crs=ft.getSrs();       
        lt.nativeName=ft.getNativeName();
        return lt;
    }
	
    /* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#getCoverageLayersList(java.lang.String, java.lang.String)
     */
    @Override
    @SuppressWarnings("serial")
    public ArrayList<LayerTuple> getCoverageLayersList(final String workspace,final String store){
        return new ArrayList<LayerTuple>(){{			
            try {
                RESTCoverageList cs = reader.getCoverages(workspace, store);
                for(int i=0;i<cs.size();i++){ 
                    NameLinkElem c = cs.get(i);
                    add(res2lt(reader.getCoverage(reader.getLayer(workspace, c.getName())),workspace));
                }
            } 
            catch (JSONException e) {
                log.debug("JSONException in getCoverageLayersList("+workspace+","+store+"): "+e.getMessage());
            }
            catch (ProcessingException e) {
                log.error("Can't get coveragelayers list from workspace/store "+workspace+"/"+store, e);
            }
        }};
    }

    /* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#getWmsLayersList(java.lang.String, java.lang.String)
     */
    @Override
    @SuppressWarnings("serial")
    public ArrayList<LayerTuple> getWmsLayersList(final String workspace,final String store){
        return new ArrayList<LayerTuple>(){{
            try {				    
                RESTWmsList wsl = reader.getWms(workspace, store);
                for(int i=0;i<wsl.size();i++){
                    NameLinkElem w = wsl.get(i);
                    add(res2lt(reader.getWms(reader.getLayer(workspace, w.getName())),workspace));
                }
            } 
            catch (JSONException e) {
                //json is not parsable, so we returns an empty list
                log.debug("JSONException in getWmsLayersList("+workspace+","+store+"): "+e.getMessage());
            }
            catch (ProcessingException e) {
                log.error("Can't get wmslayers list from workspace/store "+workspace+"/"+store, e);
            }
        }}; 
    }
		
	/* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#addSQLLayer(it.egeos.geoserver.restmanagers.tuples.SqlLayerTuple)
     */
	@SuppressWarnings("serial")
    @Override
    public boolean addSQLLayer(final SqlLayerTuple slt){
        return publisher.publishDBLayer(
            slt.store.workspace.name, 
            slt.store.name, 
            new GSFeatureTypeEncoder(){{
                setName(slt.name);
                setNativeName(slt.name);
                setTitle(slt.name);
                addKeyword("features");
                addKeyword(slt.name);       
                setEnabled(true);        
                setMetadataVirtualTable(
                    new GSVirtualTableEncoder(
                        slt.name,
                        slt.sql, 
                        null, 
                        new ArrayList<VTGeometryEncoder>(){{ 
                            for(VTGeometryTuple g:slt.geomEncList)
                                add(new VTGeometryEncoder(g.getName(), g.getGeometryType(), g.getSrid()));            
                        }}, 
                        new ArrayList<VTParameterEncoder>(){{
                            for(VTParameterTuple p:slt.paramEncList)
                                add(new VTParameterEncoder(p.getName(), p.getDefaultValue(), p.getRegexpValidator()));
                        }}
                    )
                );
            }}, 
            new GSLayerEncoder()
        );	    
	}
		
	/* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#getSQLLayer(java.lang.String, java.lang.String, java.lang.String)
     */
	@Override
    @SuppressWarnings("serial")
    public SqlLayerTuple getSQLLayer(String workspace,String store,final String feature_name){	   
	    SqlLayerTuple res=null;
		JSONObject virtualTable=null;
		String parsed_title=null;
		String path = String.format(FEATURETYPE_SERVICE, workspace,store,feature_name);
		try {		    
		    JSONObject ft = new JSONObject(makeRequest(path)).getJSONObject("featureType");		   
		    parsed_title=ft.getString("title");			
		    virtualTable = ft
			    .getJSONObject("metadata")
			    .getJSONObject("entry")
			    .getJSONObject("virtualTable");
		} 
		catch (NotFoundException e) {
			log.debug("Layer "+workspace+"/"+store+"/"+feature_name+" is not an SQL View "+e.getMessage(),e);
		}
		catch (JSONException e) {
			//No Metadata here, this is not an sql layer
			log.debug("Layer "+workspace+"/"+store+"/"+feature_name+" is not an SQL View "+e.getMessage(),e);
		}

		if (virtualTable!=null) {
			try {			    
			    final JSONArray geoms = mkArray(virtualTable,"geometry");				
			    List<VTGeometryTuple> geomEncList=new ArrayList<VTGeometryTuple>(){{
			        if (geoms!=null)
    			        for(int i=0;i< geoms.length();i++){
    			            JSONObject g = geoms.getJSONObject(i);
    			            add(new VTGeometryTuple(g.getString("name"), g.getString("type"), g.getInt("srid")+""));
    			        }
			    }};
						    
			    final JSONArray params =mkArray(virtualTable,"parameter");
			    List<VTParameterTuple> paramEncList=new ArrayList<VTParameterTuple>(){{
			        if (params!=null)
    			        for(int i=0;i<params.length();i++){
    			            JSONObject p = params.getJSONObject(i);
    			            add(new VTParameterTuple(p.getString("name"), p.getInt("defaultValue")+"", p.getString("regexpValidator")));
    			        }
			    }};			   
			    
			    res=new SqlLayerTuple(
		            feature_name, 
		            parsed_title, 
		            new StoreTuple(store, StoreTypes.DATA, new WorkspaceTuple(workspace)),
		            virtualTable.getString("sql"), 
		            geomEncList, 
		            paramEncList
	            );				
			} 
			catch (JSONException e) {
				log.debug("Layer "+workspace+"/"+store+"/"+feature_name+" is not an SQL View: "+e.getMessage(),e);
			}
		}
		else
		    log.warn("No virtualTable founds in "+path);
		return res;
	}
	
	private JSONArray mkArray(final JSONObject json, final String key){
	    //This function transform a polymorphic field in a jsonarray
	    JSONArray res=null;	    
        try {
            res=json.getJSONArray(key);
        }
        catch (JSONException e) {
            try {
                res=new JSONArray(){{;
                    put(json.getJSONObject(key));
                }};
            }
            catch (JSONException e1) {
               log.debug("No "+key+" found in this layer.");
            }
        }
	    return res;
	}
	
	/* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#updateSQLLayer(it.egeos.geoserver.restmanagers.tuples.SqlLayerTuple)
     */
	@SuppressWarnings("serial")
    @Override
    public boolean updateSQLLayer(final SqlLayerTuple slt){
        return publisher.updateDBLayer(
            slt.store.workspace.name, 
            slt.store.name, 
            new GSFeatureTypeEncoder(){{
                setName(slt.name);
                setNativeName(slt.name);
                setTitle(slt.name);
                addKeyword("features");
                addKeyword(slt.name);       
                setEnabled(true);        
                setMetadataVirtualTable(
                    new GSVirtualTableEncoder(
                        slt.name,
                        slt.sql, 
                        null, 
                        new ArrayList<VTGeometryEncoder>(){{ 
                            for(VTGeometryTuple g:slt.geomEncList)
                                add(new VTGeometryEncoder(g.getName(), g.getGeometryType(), g.getSrid()));            
                        }}, 
                        new ArrayList<VTParameterEncoder>(){{
                            for(VTParameterTuple p:slt.paramEncList)
                                add(new VTParameterEncoder(p.getName(), p.getDefaultValue(), p.getRegexpValidator()));
                        }}
                    )
                );
            }}
        );
	}
	
	/* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#getDefaultStyle(java.lang.String)
     */
	@Override
    public String getDefaultStyle(String layer){
		String res=null;
		JSONObject p=null;
        try {
            p = new JSONObject(makeRequest(String.format(LAYER_SERVICE,layer)));
        }
        catch (NotFoundException e) {
            log.debug("Layer "+layer+" not exists (404): are you trying to get style of a layergroup?", e);
        }
		try{
			res=p.getJSONObject("layer").getJSONObject("defaultStyle").getString("name");
		}
		catch (NullPointerException |JSONException e) {
			//Response is not parsable
			log.debug("JSONException getDefaultStyle("+layer+"): "+e.getMessage(),e);
		}
		return res;
	}
	
	/* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#assignStyle(java.lang.String, java.lang.String, java.lang.String)
     */
	@Override
    public String assignStyle(final String workspace,final String layer,final String style){		
		JSONObject payload=new JSONObject(){{
			put("layer",new JSONObject(){{
				put("name",layer);		
				put("defaultStyle",new JSONObject(){{
					put("name",style);
					if(workspace!=null && !workspace.isEmpty())
						put("workspace",workspace);
				}});
			}});
		}};
		return makePut(String.format(LAYER_SERVICE,layer), payload, String.class);
	}

	/* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#assignOptStyle(java.lang.String, java.lang.String, java.lang.String)
     */
	@Override
    public String assignOptStyle(final String workspace,final String layer,final String style){		
		JSONObject payload=new JSONObject(){{
			put("layer",new JSONObject(){{
				put("name",layer);		
				JSONObject styles =null;
				try {
					styles = new JSONObject(makeRequest(String.format(LAYER_SERVICE,layer))).getJSONObject("layer").getJSONObject("styles");										
				} 
				catch (JSONException e) {
					//Response is not parsable
					log.debug("JSONException in assignOptStyle("+layer+"): "+e.getMessage(),e);
					styles = new JSONObject(){{
						put("style",new JSONArray());
					}};
				}
				styles.getJSONArray("style").put(new JSONObject(){{
					put("name",style);
				}});
				
				put("styles",styles);
			}});
		}};
		return makePut(String.format(LAYER_SERVICE,layer), payload, String.class);
	}

	/* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#removeStyle(java.lang.String, java.lang.String)
     */
	@Override
    public String removeStyle(final String layer,final String style){
		JSONObject payload=new JSONObject(){{
			put("layer",new JSONObject(){{
				put("name",layer);														
				try {
					JSONObject styles = new JSONObject(makeRequest(String.format(LAYER_SERVICE,layer))).getJSONObject("layer").getJSONObject("styles");
					JSONArray arr = styles.getJSONArray("style");
					for(int i=0;i<arr.length();)
						if (arr.getJSONObject(i).getString("name").equals(style))
							arr.remove(i);
						else
							i++;
					put("styles",styles);
				} 
				catch (JSONException e) {
					//Response is not parsable
					log.debug("JSONException in removeStyle("+layer+"): "+e.getMessage(),e);
				}				
			}});
		}};
		return makePut(String.format(LAYER_SERVICE,layer), payload, String.class);		
	}

	/* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#addWmsLayer(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.Map)
     */
	@Override
    public String addWmsLayer(String workspace,String store,final String layer,final String name,final String title,final Map<String,String> opts){
		JSONObject payload=new JSONObject(){{
			put("wmsLayer",new JSONObject(){{
				put("name",name);
				put("nativeName",layer);
				put("title",title);				
				for(String opt:opts.keySet())
					put(opt,opts.get(opt));
			}});
		}};	
		return makePost(String.format(LAYERS_BY_WMSSTORE_SERVICE, workspace,store), payload, String.class);
	}
	
	/* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#removeWmsLayer(java.lang.String, java.lang.String, java.lang.String)
     */
	@Override
    public boolean removeWmsLayer(String workspace,String store,String layerName){
		boolean pub=false;
		try {
			pub=publisher.removeLayer(workspace, layerName);
			log.info("Trying to remove "+workspace+"/"+store+"/"+layerName+" by publisher: "+pub);
		} 
		catch (Exception e) {
			log.info("Layer "+workspace+"/"+store+"/"+layerName+" is NOT removed by publisher: "+e.getMessage(),e);
		}		

		boolean man=false;
		try {
			makeDelete(String.format(WMSLAYER_SERVICE,workspace,store,layerName), null, String.class);
			man=true;
			log.info("Trying to remove "+workspace+"/"+store+"/"+layerName+" by manual: "+man);
		} 
		catch (Exception e) {
			log.info("Layer "+workspace+"/"+store+"/"+layerName+" is NOT removed by manual: "+e.getMessage(),e);
		}

		return pub || man;
	}
				
	/* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#getRoles()
     */
	@Override
    public ArrayList<String> getRoles(){
		return toArrayList(makeRequestJSONArray(ROLES_SERVICE), String.class);
	}

	/* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#addRole(java.lang.String, java.lang.String)
     */
	@Override
    public String addRole(final String name,final String parent){
		Form form =new Form();
		form.param("id",name);
		if (parent!=null)
			form.param("parent_id",parent);		
        return makePost(ROLES_SERVICE,form, String.class);
	}
	
	/* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#addRoleToUser(java.lang.String, java.lang.String)
     */
	@Override
    public String addRoleToUser(final String role,final String user){
		Form form =new Form();
		form.param("username",user);
		form.param("roleID", role);		
        return makePost(USERROLES_SERVICE,form, String.class);		
	}
		
	/* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#delRole(java.lang.String)
     */
	@Override
    public String delRole(final String name){
		Form form=new Form();
		form.param("id",name);
		return makeDelete(ROLES_SERVICE,form.asMap(),String.class);
	}
	
	/* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#delRoleRefFromUser(java.lang.String, java.lang.String)
     */
	@Override
    public String delRoleRefFromUser(final String role,final String user){
		Form form=new Form();
		form.param("username",user);
		form.param("roleID",role);
		return makeDelete(USERROLES_SERVICE,form.asMap(),String.class);
	}
		
	/* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#getReaders(java.lang.String, java.lang.String, java.lang.Boolean)
     */
	@Override
    public HashMap<String, ArrayList<String>> getReaders(final String workspace,final String layer,final Boolean strict){
		return getProfile(workspace, layer, READ, strict);
	}

	/* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#getReaders(java.lang.String, java.lang.Boolean)
     */
	@Override
    public HashMap<String, ArrayList<String>> getReaders(final String workspace,final Boolean strict){
		return getProfile(workspace, READ, strict);
	}

	@SuppressWarnings("serial")
	protected HashMap<String, ArrayList<String>> getProfile(final String workspace,final String layer,final char access_mode,final Boolean strict){
	    return new HashMap<String, ArrayList<String>>(){{
	    	String reg=genReg(workspace,layer,access_mode,strict);
	    	JSONObject p = new JSONObject(makeRequest(RULES_SERVICE).replaceAll("=", ":"));
	    	for (final Object k:p.keySet()){
	    		String s=(String)k;
	    		if (s.matches(reg))
	    			put((String)k,toArrayList(p.getJSONArray(s), String.class));
	    	}
	    }};
	}
	
	@SuppressWarnings("serial")
	private HashMap<String, ArrayList<String>> getProfile(final String workspace,final char access_mode,final Boolean strict){
	    return new HashMap<String, ArrayList<String>>(){{
	    	String reg=genReg(workspace,access_mode+"",strict);
	    	JSONObject p = new JSONObject(makeRequest(RULES_SERVICE).replaceAll("=", ":"));
	    	for (final Object k:p.keySet()){
	    		String s=(String)k;
	    		if (s.matches(reg))
	    			put((String)k,toArrayList(p.getJSONArray(s), String.class));
	    	}
	    }};
	}
	
	/* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#deleteRule(java.lang.String)
     */
	@Override
    public String deleteRule(String rule){
		String[] parts = rule.split("\\.");
		String workspace=(parts.length>0)?parts[0]:null;
		String layer=(parts.length>1)?parts[1]:null;
		char access_mode=(parts.length>2)?parts[2].charAt(0):null;
		return deleteRule(workspace, layer, access_mode);
	}
	
	/* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#deleteRule(java.lang.String, java.lang.String, char)
     */
	@Override
    public String deleteRule(final String workspace,final String layer,final char access_mode){
		Form form =new Form();		
		form.param("workspace",workspace);
		form.param("layer",layer);
		form.param("method", access_mode+"");
		form.param("missing","true");
		return makeDelete(RULES_SERVICE,form.asMap(),String.class);		
	}
			
	/* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#addOrCreateRule(java.lang.String, java.util.List, java.lang.Boolean)
     */
	@Override
    public String addOrCreateRule(String rule, List<String> roles,Boolean add_only){
		String[] parts = rule.split("\\.");
		String workspace=(parts.length>0)?parts[0]:null;
		String layer=(parts.length>1)?parts[1]:null;
		char access_mode=(parts.length>2)?parts[2].charAt(0):null;
		return addOrCreateRule(workspace,layer,access_mode,roles,add_only);
	}
	
	/* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#addOrCreateRule(java.lang.String, java.lang.String, char, java.util.List, java.lang.Boolean)
     */
	@Override
    public String addOrCreateRule(final String workspace,final String layer,final char access_mode, final List<String> roles,final Boolean add_only){
		Form form =new Form();
		form.param("workspace",workspace);
		form.param("layer", layer);
		form.param("method", access_mode+"");
		form.param("role", StringUtils.join(roles,RULES_VSEPARATOR));
		form.param("append",add_only+"");
        return makePost(RULES_SERVICE,form, String.class);
	}

	private String genReg(String workspace,String layer,char access_mode,Boolean strict){
		return genReg(workspace, layer, access_mode+"", strict);
	}
	
	private String genReg(String workspace,String layer,String access_mode,Boolean strict){
		String access_reg="["+access_mode+access_mode.toUpperCase()+"]";
		String res=workspace+"\\."+layer+"\\."+access_reg;
		
		if (!strict){
			//Non strict pattern is 
			// ws.layer.am | ws.*.am | *.*.am
			res="("+res+")|("+workspace+"\\.\\*\\."+access_reg+")|(\\*\\.\\*\\."+access_reg+")";
		}
		
		return res;
	}
	
	private String genReg(String workspace,String access_mode,Boolean strict){
		String access_reg="["+access_mode+access_mode.toUpperCase()+"]";
		String res=workspace+"\\.\\*\\."+access_reg;
		
		if (!strict){
			//Non strict pattern is 
			// ws.layer.am | ws.*.am | *.*.am
			res="("+res+")|(\\*\\.\\*\\."+access_reg+")";
		}
		
		return res;
	}
	
	/* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#getAllStyles()
     */
	@Override
    @SuppressWarnings("serial")
	public ArrayList<StyleTuple> getAllStyles(){		
		return new ArrayList<StyleTuple>(){{
			addAll(getStyles(null));			
			for(WorkspaceTuple w:getWorkspaces()){
				addAll(getStyles(w.name));
			}
		}};
	}
	
	/* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#getStyles(java.lang.String)
     */

    @SuppressWarnings("serial")
	private ArrayList<StyleTuple> getStyles(final String workspace){
		return new ArrayList<StyleTuple>(){{
			JSONObject p = new JSONObject(makeRequest(workspace!=null?String.format(WS_STYLES_SERVICE,workspace):STYLES_SERVICE));
			try {
				JSONArray arr = p.getJSONObject("styles").getJSONArray("style");
				for(int i=0;i<arr.length();i++){
					try {
					    String resp = makeRequest(new URL(arr.getJSONObject(i).getString("href")));
					    add(new StyleTuple(new JSONObject(resp).getJSONObject("style")));
					} 
					catch (MalformedURLException |JSONException e) {
						log.debug("Generic error parsing "+arr.getJSONObject(i)+": "+e.getMessage());
					}
				}
			}
			catch (JSONException e) {
				//json is not parsable, so we returns an empty list
				log.debug("Generic error calling getStyles("+workspace+") "+e.getMessage());
			}
		}};
	}

	/* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#upload(java.lang.String, java.lang.String, java.lang.String)
     */
	@Override
    public boolean upload(String name,String sld,String workspace){
		return publisher.publishStyleInWorkspace(workspace, sld, name);
	}

	/* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#addUser(java.lang.String, java.lang.String)
     */
	@Override
    public String addUser(final String name,final String passwd){
		Form form =new Form();
		form.param("login",name);
		form.param("password", passwd);
        return makePost(USERS_SERVICE,form, String.class);
	}
	
	/* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#addGroup(java.lang.String)
     */
	@Override
    public String addGroup(final String name){
		Form form =new Form();
		form.param("name",name);
        return makePost(GROUPS_SERVICE,form, String.class);
	}

	/* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#addMember(java.lang.String, java.lang.String, java.lang.Boolean)
     */
	@Override
    public String addMember(final String name,final String group,final Boolean unique){
		Form form =new Form();
		form.param("username",name);
		form.param("groupname", group);
		form.param("force_unique",unique.toString());
        return makePost(MEMBERS_SERVICE,form, String.class);
	}
	
	/* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#delGroup(java.lang.String)
     */
	@Override
    public String delGroup(final String name){
		Form form=new Form();
		form.param("name",name);
		return makeDelete(GROUPS_SERVICE,form.asMap(),String.class);
	}

	/* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#delMember(java.lang.String, java.lang.String)
     */
	@Override
    public String delMember(final String name,final String group){
		Form form=new Form();
		form.param("username",name);
		form.param("groupname",group);
		return makeDelete(MEMBERS_SERVICE,form.asMap(),String.class);
	}
	
	
	/* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#linkPGSchema(java.lang.String, it.geosolutions.geoserver.rest.encoder.datastore.GSPostGISDatastoreEncoder)
     */
	@Override
    public boolean linkPGSchema(final String workspace,final String name,final String host,final Integer port,final String dbname,final String usr,final String pwd){
	    return storemanager.create(workspace,
            new GSPostGISDatastoreEncoder(name){{
                setHost(host);
                setPort(port);
                setDatabase(dbname);
                setSchema(workspace);
                setUser(usr);
                setPassword(pwd);
            }}
	    );
	}

	/* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#linkPGTable(it.egeos.geoserver.restmanagers.tuples.LayerTuple, java.lang.String)
     */
	@Override
    public boolean linkPGTable(final LayerTuple lt,final String nativeCRS){
        return publisher.publishDBLayer(
            lt.store.workspace.name, 
            lt.store.name, 
            new GSFeatureTypeEncoder(){{
                setName(lt.name);
                setNativeName(lt.name);
                setTitle(lt.name);
                addKeyword("features");
                addKeyword(lt.name);       
                setEnabled(true);    
                setNativeCRS(nativeCRS);
            }}, 
            new GSLayerEncoder()
        );
    }
	
	/* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#removeFeatureType(java.lang.String, java.lang.String, java.lang.String)
     */
	@Override
    public boolean removeFeatureType(String workspace, String storename, String layerName){
	    return publisher.unpublishFeatureType(workspace, storename, layerName);
	}
	
	/* (non-Javadoc)
     * @see it.egeos.geoserver.restmanagers.GeoserverManagerAPI#removeCoverageLayer(java.lang.String, java.lang.String, java.lang.String)
     */
	@Override
    public boolean removeCoverageLayer(String workspace, String storename, String layerName){
	    return publisher.unpublishCoverage(workspace, storename, layerName);
	}
}
