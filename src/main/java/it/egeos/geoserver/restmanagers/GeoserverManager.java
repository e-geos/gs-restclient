package it.egeos.geoserver.restmanagers;

import it.egeos.geoserver.restmanagers.Abstracts.RestManager;
import it.egeos.geoserver.restmanagers.tuples.LayerTuple;
import it.egeos.geoserver.restmanagers.tuples.SqlLayerTuple;
import it.egeos.geoserver.restmanagers.tuples.StoreTuple;
import it.egeos.geoserver.restmanagers.tuples.StyleTuple;
import it.egeos.geoserver.restmanagers.tuples.WorkspaceTuple;
import it.egeos.geoserver.restmanagers.types.StoreTypes;
import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import it.geosolutions.geoserver.rest.GeoServerRESTReader;
import it.geosolutions.geoserver.rest.decoder.RESTCoverage;
import it.geosolutions.geoserver.rest.decoder.RESTCoverageList;
import it.geosolutions.geoserver.rest.decoder.RESTDataStore;
import it.geosolutions.geoserver.rest.decoder.RESTDataStore.DBType;
import it.geosolutions.geoserver.rest.decoder.RESTDataStoreList;
import it.geosolutions.geoserver.rest.decoder.RESTFeatureType;
import it.geosolutions.geoserver.rest.decoder.RESTFeatureTypeList;
import it.geosolutions.geoserver.rest.decoder.RESTLayer;
import it.geosolutions.geoserver.rest.decoder.RESTLayerGroup;
import it.geosolutions.geoserver.rest.decoder.RESTLayerGroupList;
import it.geosolutions.geoserver.rest.decoder.RESTPublished;
import it.geosolutions.geoserver.rest.decoder.RESTPublishedList;
import it.geosolutions.geoserver.rest.decoder.RESTStyleList;
import it.geosolutions.geoserver.rest.decoder.RESTWms;
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

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Form;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * @author Federico C. Guizzardi - cippinofg <at> gmail.com
 * 
 * RestManager implementation to control a Geoserver
 *
 */

public class GeoserverManager extends RestManager {
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
	
    public boolean addWorkspace(String ws){
        return publisher.createWorkspace(ws);
    }

    public boolean delWorkspace(String ws){
        return publisher.removeWorkspace(ws,true);
    }
            
    @SuppressWarnings("serial")
    public List<StoreTuple> getDataStores(final WorkspaceTuple workspace){
        return new ArrayList<StoreTuple>(){{
            try {
                JSONObject p = new JSONObject(makeRequest(String.format(DATASTORES_SERVICE, workspace.name)));
                JSONArray arr = p.getJSONObject("dataStores").getJSONArray("dataStore");
                for(int i=0;i<arr.length();i++)		
                    add(new StoreTuple(arr.getJSONObject(i).getString("name"),StoreTypes.DATA,workspace));
            } 
            catch (JSONException e) {
                //json is not parsable, so we returns an empty list
            }
            catch (ProcessingException e) {
                log.error("Can't get datastores list from workspace "+workspace.name, e);
            }
        }};
    }
	
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

    public String deleteWMSStore(final WorkspaceTuple workspace,String name){		
        Form form =new Form();	
        form.param("recurse","true");			
        return makeDelete(String.format(WMSSTORE_SERVICE,workspace.name,name), form.asMap(), String.class);
    }
	
    public RESTFeatureType getFetureType(String workspace, String layer){
        return reader.getFeatureType(getLayer(workspace, layer));
    }
	
    public RESTLayer getLayer(String workspace, String layer){
        return reader.getLayer(workspace, layer);
    }
	
    public WorkspaceTuple getWorkspace(final LayerTuple layer){
        WorkspaceTuple res=null;
        JSONObject p = new JSONObject(makeRequest(String.format(LAYER_SERVICE,layer.name)));
        try{
            String ft_ulrl=p.getJSONObject("layer").getJSONObject("resource").getString("href");
            Matcher matcher = WORKSPACE_IN_HREF.matcher(ft_ulrl);
            if (matcher.find())
                res=new WorkspaceTuple(matcher.group(1));
        }
        catch (JSONException e) {
            //Response is not parsable
            log.debug("JSONException getLayerWs("+layer+"): "+e.getMessage(),e);
        }
        return res;
    }

    public StoreTuple getStore(WorkspaceTuple workspace, LayerTuple layer){		
        RESTLayer l = reader.getLayer(workspace.name, layer.name);
        StoreTuple store=null;			
        try {
            store=new StoreTuple(reader.getFeatureType(l).getStoreName(),StoreTypes.DATA,workspace);
        } 
        catch (Exception e) {
            log.debug("Layer is not a Feature, trying with Coverage and Wms");
            try {
                store=new StoreTuple(reader.getCoverage(l).getStoreName(),StoreTypes.COVERAGE,workspace);
            } 
            catch (Exception e1) {
                log.debug("Layer is not a Coverage, trying with Wms");
                try {
                    store=new StoreTuple(reader.getWms(l).getStoreName(),StoreTypes.WMS,workspace);
                } 
                catch (Exception e2) {
                    log.debug("Layer is not a Feature, Coverage or Wms (may be a LayerGroup??)");
                }
            }
        }
        return store;
    }
		
    @SuppressWarnings("serial")
    public List<StyleTuple> getLayerStyles(final String layer){
        return new ArrayList<StyleTuple>(){{
            JSONObject p = new JSONObject(makeRequest(String.format(LAYER_STYLES_SERVICE,layer)));			
            try {
                JSONArray styles = p.getJSONObject("styles").getJSONArray("style");
                if(styles!=null)
                for(int i=0;i<styles.length();i++)					
                    add(new StyleTuple(styles.getJSONObject(i)));
            } 
            catch (JSONException e) {
                //Response is not parsable
                log.debug("JSONException in getLayerStyles("+layer+"): "+e.getMessage(),e);
            }
        }};
    }
		
    @SuppressWarnings("serial")
    public List<RESTLayerGroup> getLayerGroups(final String workspace){	    
        return new ArrayList<RESTLayerGroup>(){{
            RESTLayerGroupList lgs = reader.getLayerGroups(workspace);
            if(lgs!=null)
                for(int i=0;i<lgs.size();i++)
    	            add(reader.getLayerGroup(workspace, lgs.get(i).getName()));
        }};
    }

    /*
        * returns a list of <name,style> merging 'publishables' and 'styles' properties 
        */	
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

                if (lt!=null)
                    put(lt,new StyleTuple(nle.getName(), null, null,new WorkspaceTuple(prefixName(nle.getName()))));
            }
        }};
    }

    protected JSONArray parseLayersList(JSONObject p){
        JSONArray layers;		
        try {
            layers=p.getJSONObject("publishables").getJSONArray("published");
        } 
        catch (JSONException e) {
            //there's only a layer in publishables so 'published' is not a JSONArray but JSONObject
            layers=new JSONArray();
            layers.put(p.getJSONObject("publishables").getJSONObject("published"));
        }
        return layers;
    }
	
    protected JSONArray parseStylesList(JSONObject p){
        JSONArray styles = p.getJSONObject("styles").getJSONArray("style");
        try {
            JSONArray styles_fix = p.getJSONArray("style");
            for(int i=0; i<styles_fix.length();i++)
                styles.put(styles_fix.get(i));
        } 
        catch (JSONException e) {
            try {
                styles.put(p.getJSONObject("style"));
            } 
            catch (JSONException e1) {
                log.debug("Can't find 'style' at the same level of 'styles'",e);
            }
        }
        return styles;
    }
	
    public String prefixName(String name){
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
	
    public String trimName(String name){
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
    public Boolean assignSubLayers(String workspace,String glayer,LinkedHashMap<LayerTuple, String> subs){
        GSLayerGroupEncoder23 conf = new GSLayerGroupEncoder23();	
        for(LayerTuple sl:subs.keySet())
            if (sl.isLayerGroup())
                conf.addLayerGroup(sl.getCompleteName());
            else
                conf.addLayer(sl.name, subs.get(sl));					
        return publisher.configureLayerGroup(workspace, glayer,conf);
    }
	
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
	
    public Boolean deleteLayerGroup(String workspace,String layer){
        return publisher.removeLayerGroup(workspace, layer);
    }
		
    @SuppressWarnings("serial")
    public ArrayList<RESTFeatureType> getFeatureLayersList(final String workspace){
        return new ArrayList<RESTFeatureType>(){{			 
            try{			   			   
                RESTFeatureTypeList fts = reader.getFeatureTypes(workspace);
                for(int i=0;i<fts.size();i++){ 
                    NameLinkElem ft = fts.get(i);
                    try {
                        add(reader.getFeatureType(reader.getLayer(workspace, ft.getName())));
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
	
    @SuppressWarnings("serial")
    public ArrayList<RESTCoverage> getCoverageLayersList(final String workspace,final String store){
        return new ArrayList<RESTCoverage>(){{			
            try {
                RESTCoverageList cs = reader.getCoverages(workspace, store);
                for(int i=0;i<cs.size();i++){ 
                    NameLinkElem c = cs.get(i);
                    add(reader.getCoverage(reader.getLayer(workspace, c.getName())));
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

    @SuppressWarnings("serial")
    public ArrayList<RESTWms> getWmsLayersList(final String workspace,final String store){
        return new ArrayList<RESTWms>(){{
            try {				    
                RESTWmsList wsl = reader.getWms(workspace, store);
                for(int i=0;i<wsl.size();i++){
                    NameLinkElem w = wsl.get(i);
                    add(reader.getWms(reader.getLayer(workspace, w.getName())));
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
	
	@SuppressWarnings("serial")
	public Map<String,String> getNativeBoundingBox(final String layer){
		return new HashMap<String, String>(){{
			JSONObject p=null;
			String layer_url=String.format(LAYER_SERVICE,layer);
			try {
				p = new JSONObject(makeRequest(layer_url));				
			} 
			catch (JSONException e1) {
				log.error("Can't parse "+layer_url,e1);
			}			
			if (p!=null){
				String href=null;
				try {
					href = p.getJSONObject("layer").getJSONObject("resource").getString("href");
				} 
				catch (JSONException e1) {
					log.error("Can't find layer/resource/href in >>"+p+"<<");
				}
				
				if(href!=null){		
					Matcher matcher = SUBPATH.matcher(href);
					String subpath=null;
					if (matcher.find())
						subpath=matcher.group(1);	
					
					try {
						p = new JSONObject(makeRequest(subpath));
					} 
					catch (JSONException e) {
						log.error("Can't parse "+subpath);
					}
					
					if (p!=null){
						JSONObject content=null;
						try{
							content = p.getJSONObject("featureType");
						}
						catch(JSONException e){
							log.debug("featureType not found in "+p);
							try {
								content = p.getJSONObject("coverage");
							} 
							catch (JSONException e1) {
								log.debug("coverage not found in "+p);
								try{
									content = p.getJSONObject("wmsLayer");
								}
								catch (JSONException e2) {
									log.debug("wmsLayer not found in "+p);
									log.error("No information about native bounding box");
								}								
							}
						}
						
						if (content!=null){
							//parse nativeBoundingBox
							JSONObject bbox = content.getJSONObject("nativeBoundingBox");
							for(Object k:bbox.keySet())
								put((String)k,bbox.get((String)k)+"");													
						}						
					}
				}				
			}
		}};
	}

	@Deprecated //uploads now works using postgis import
	public boolean uploadSHPFile(String workspace,URI fileuri) throws FileNotFoundException {
		return uploadSHPFile(workspace, workspace, fileuri);	
	}

	@Deprecated //uploads now works using postgis import
	public boolean uploadSHPFile(String workspace,String store,URI fileuri) throws FileNotFoundException {
		boolean res=false;		
		try {
			res=publisher.publishShpCollection(workspace, store, fileuri);
		} 
		catch (Exception e) {
			log.error("Generic error calling publisher.publishShpCollection ",e);
		}
		return res;
	}
		
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
                        slt.geomEncList, 
                        slt.paramEncList
                    )
                );
            }}, 
            new GSLayerEncoder()
        );	    
	}
	
	@Deprecated
	public boolean addSQLLayer(String workspace,String store,final String name,final String sql, final List<String> keyColumns, final List<VTGeometryEncoder> geomEncList,final List<VTParameterEncoder> paramEncList){
        return publisher.publishDBLayer(
            workspace, 
            store, 
            new GSFeatureTypeEncoder(){{
                setName(name);
                setNativeName(name);
                setTitle(name);
                addKeyword("features");
                addKeyword(name);       
                setEnabled(true);        
                setMetadataVirtualTable(new GSVirtualTableEncoder(name, sql, keyColumns, geomEncList, paramEncList));
            }}, 
            new GSLayerEncoder()
        );
	}
	
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
			    List<VTGeometryEncoder> geomEncList=new ArrayList<VTGeometryEncoder>(){{
			        if (geoms!=null)
    			        for(int i=0;i< geoms.length();i++){
    			            JSONObject g = geoms.getJSONObject(i);
    			            add(new VTGeometryEncoder(g.getString("name"), g.getString("type"), g.getInt("srid")+""));
    			        }
			    }};

			    
			    
			    final JSONArray params =mkArray(virtualTable,"parameter");
			    List<VTParameterEncoder> paramEncList=new ArrayList<VTParameterEncoder>(){{
			        if (params!=null)
    			        for(int i=0;i<params.length();i++){
    			            JSONObject p = params.getJSONObject(i);
    			            add(new VTParameterEncoder(p.getString("name"), p.getInt("defaultValue")+"", p.getString("regexpValidator")));
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
                        slt.geomEncList, 
                        slt.paramEncList
                    )
                );
            }}
        );
	}
	
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

	public String assignOptStyle(final String workspace,final String layer,final String style){		
		JSONObject payload=new JSONObject(){{
			put("layer",new JSONObject(){{
				put("name",layer);		
				JSONObject styles =null;
				try {
					styles = new JSONObject(makeRequest(String.format(LAYER_STYLES_SERVICE,layer))).getJSONObject("styles");										
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
					if (workspace!=null && !workspace.isEmpty())
						put("workspace",workspace);
				}});
				put("styles",styles);
			}});
		}};
		return makePut(String.format(LAYER_SERVICE,layer), payload, String.class);
	}

	public String removeStyle(final String layer,final String style){
		JSONObject payload=new JSONObject(){{
			put("layer",new JSONObject(){{
				put("name",layer);														
				try {
					JSONObject styles = new JSONObject(makeRequest(String.format(LAYER_STYLES_SERVICE,layer))).getJSONObject("styles");
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
	
	public boolean removeShapefile(String workspace, String layerName){
	    return publisher.removeLayer(workspace, layerName);
	}
	
			
	protected String replaceJson(String json,String id,String value){
		String prefix="\""+id+"\"";
		String fid=prefix+":\".*?\"";
		String val=prefix+":\""+value+"\"";		
		return json.replaceAll(fid, val);
	}
	
	protected String replaceOldJson(String json,String id,String old_value,String new_value){
		String prefix="\""+id+"\"";
		String fid=prefix+":\""+old_value+"\"";
		String val=prefix+":\""+new_value+"\"";
		return json.replaceAll(fid, val);
	}

	public ArrayList<String> getRoles(){
		return toArrayList(makeRequestJSONArray(ROLES_SERVICE), String.class);
	}

	@SuppressWarnings("serial")
	public HashMap<String,ArrayList<String>> getUserRoleRef(){
	    return new HashMap<String, ArrayList<String>>(){{
	    	JSONObject p = new JSONObject(makeRequest(USERROLES_SERVICE).replaceAll("=", ":"));
	    	for (final Object k:p.keySet())
	    		put((String)k,toArrayList(p.getJSONArray((String) k), String.class));
	    }};
	}

	@SuppressWarnings("serial")
	public HashMap<String,ArrayList<String>> getGroupRoleRef(){
	    return new HashMap<String, ArrayList<String>>(){{
	    	JSONObject p = new JSONObject(makeRequest(GROUPROLES_SERVICE).replaceAll("=", ":"));
	    	for (final Object k:p.keySet())
	    		put((String)k,toArrayList(p.getJSONArray((String) k), String.class));
	    }};
	}
	
	public String addRole(final String name){
        return addRole(name, null);
	}

	public String addRole(final String name,final String parent){
		Form form =new Form();
		form.param("id",name);
		if (parent!=null)
			form.param("parent_id",parent);		
        return makePost(ROLES_SERVICE,form, String.class);
	}
	
	public String addUserRoleRef(String name,String role){
        return addRoleToUser(role, name);
	}

	public String addGroupRoleRef(String name,String role){
		return addRoleToGroup(role,name);
	}
	
	public String addRoleToUser(final String role,final String user){
		Form form =new Form();
		form.param("username",user);
		form.param("roleID", role);		
        return makePost(USERROLES_SERVICE,form, String.class);		
	}

	public String addRoleToGroup(final String role,final String group){
		Form form =new Form();
		form.param("groupname",group);
		form.param("roleID", role);
        return makePost(GROUPROLES_SERVICE,form, String.class);		
	}
		
	public String delRole(final String name){
		Form form=new Form();
		form.param("id",name);
		return makeDelete(ROLES_SERVICE,form.asMap(),String.class);
	}
	
	public String delRoleRefFromUser(final String role,final String user){
		Form form=new Form();
		form.param("username",user);
		form.param("roleID",role);
		return makeDelete(USERROLES_SERVICE,form.asMap(),String.class);
	}
	
	public String delRoleRefFromGroup(final String role,final String group){
		Form form=new Form();
		form.param("groupname",group);
		form.param("roleID",role);
		return makeDelete(GROUPROLES_SERVICE,form.asMap(),String.class);
	}

	@SuppressWarnings("serial")
	public HashMap<String, ArrayList<String>> getRules(){
	    return new HashMap<String, ArrayList<String>>(){{
	    	JSONObject p = new JSONObject(makeRequest(RULES_SERVICE).replaceAll("=", ":"));
	    	for (final Object k:p.keySet())
	    		put((String)k,toArrayList(p.getJSONArray((String) k), String.class));
	    }};
	}

	@SuppressWarnings("serial")
	public HashMap<String, ArrayList<String>> getRules(final String workspace){
	    return new HashMap<String, ArrayList<String>>(){{
	    	JSONObject p = new JSONObject(makeRequest(RULES_SERVICE).replaceAll("=", ":"));
	    	for (final Object k:p.keySet()){
	    		String s=(String)k;
	    		if (s.matches(workspace+"\\.(.*)"))
	    			put((String)k,toArrayList(p.getJSONArray(s), String.class));
	    	}
	    }};
	}
	
	@SuppressWarnings("serial")
	public HashMap<String, ArrayList<String>> getRules(final String workspace,final String layer){
	    return new HashMap<String, ArrayList<String>>(){{
	    	JSONObject p = new JSONObject(makeRequest(RULES_SERVICE).replaceAll("=", ":"));
	    	for (final Object k:p.keySet()){
	    		String s=(String)k;
	    		if (s.matches(workspace+"\\."+layer+"\\.(.*)"))
	    			put((String)k,toArrayList(p.getJSONArray(s), String.class));
	    	}
	    }};
	}
	
	public HashMap<String, ArrayList<String>> getReaders(final String workspace,final String layer,final Boolean strict){
		return getProfile(workspace, layer, READ, strict);
	}

	public HashMap<String, ArrayList<String>> getReaders(final String workspace,final Boolean strict){
		return getProfile(workspace, READ, strict);
	}
	
	public HashMap<String, ArrayList<String>> getWrites(final String workspace,final String layer,final Boolean strict){
		return getProfile(workspace, layer, WRITE, strict);
	}
	
	public HashMap<String, ArrayList<String>> getWrites(final String workspace,final Boolean strict){
		return getProfile(workspace, WRITE, strict);
	}

	public HashMap<String, ArrayList<String>> getAdmins(final String workspace,final String layer,final Boolean strict){
		return getProfile(workspace, layer, ADMIN, strict);
	}

	public HashMap<String, ArrayList<String>> getAdmins(final String workspace,final Boolean strict){
		return getProfile(workspace, ADMIN, strict);
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
	protected HashMap<String, ArrayList<String>> getProfile(final String workspace,final char access_mode,final Boolean strict){
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
	
	public String createRule(String workspace,String layer,char access_mode, List<String> roles){
		Form form =new Form();
        form.param("workspace",workspace);
        form.param("layer", layer);
        form.param("method", access_mode+"");
        form.param("role", StringUtils.join(roles,RULES_VSEPARATOR));
        return makePost(RULES_SERVICE,form, String.class);
	}

	public String deleteRule(String rule){
		String[] parts = rule.split("\\.");
		String workspace=(parts.length>0)?parts[0]:null;
		String layer=(parts.length>1)?parts[1]:null;
		char access_mode=(parts.length>2)?parts[2].charAt(0):null;
		return deleteRule(workspace, layer, access_mode);
	}
	
	public String deleteRule(final String workspace,final String layer,final char access_mode){
		Form form =new Form();		
		form.param("workspace",workspace);
		form.param("layer",layer);
		form.param("method", access_mode+"");
		form.param("missing","true");
		return makeDelete(RULES_SERVICE,form.asMap(),String.class);		
	}
			
	public String addToRule(String rule, List<String> roles){
		String[] parts = rule.split("\\.");
		String workspace=(parts.length>0)?parts[0]:null;
		String layer=(parts.length>1)?parts[1]:null;
		char access_mode=(parts.length>2)?parts[2].charAt(0):null;
		return addToRule(workspace,layer,access_mode,roles);
	}

	public String addOrCreateRule(String rule, List<String> roles,Boolean add_only){
		String[] parts = rule.split("\\.");
		String workspace=(parts.length>0)?parts[0]:null;
		String layer=(parts.length>1)?parts[1]:null;
		char access_mode=(parts.length>2)?parts[2].charAt(0):null;
		return addOrCreateRule(workspace,layer,access_mode,roles,add_only);
	}
	
	public String addToRule(String workspace,String layer,char access_mode, List<String> roles){
		return addOrCreateRule(workspace,layer,access_mode,roles,true);
	}
	
	public String addOrCreateRule(final String workspace,final String layer,final char access_mode, final List<String> roles,final Boolean add_only){
		Form form =new Form();
		form.param("workspace",workspace);
		form.param("layer", layer);
		form.param("method", access_mode+"");
		form.param("role", StringUtils.join(roles,RULES_VSEPARATOR));
		form.param("append",add_only+"");
        return makePost(RULES_SERVICE,form, String.class);
	}
	
	public String delToRule(String rule, final List<String> roles,final boolean ignore_missing){
		String[] parts = rule.split("\\.");
		String workspace=(parts.length>0)?parts[0]:null;
		String layer=(parts.length>1)?parts[1]:null;
		char access_mode=(parts.length>2)?parts[2].charAt(0):null;
		return delToRule(workspace,layer,access_mode,roles,ignore_missing);
	}
	
	public String delToRule(final String workspace,final String layer,final char access_mode, final List<String> roles,final boolean ignore_missing){
		Form form =new Form();
		form.param("workspace",workspace);
		form.param("layer", layer);
		form.param("method", access_mode+"");
		form.param("role", StringUtils.join(roles,RULES_VSEPARATOR));
		form.param("missing",ignore_missing+"");
		return makeDelete(RULES_SERVICE,form.asMap(),String.class);
	}

	protected String genReg(String workspace,String layer,char access_mode,Boolean strict){
		return genReg(workspace, layer, access_mode+"", strict);
	}
	
	protected String genReg(String workspace,String layer,String access_mode,Boolean strict){
		String access_reg="["+access_mode+access_mode.toUpperCase()+"]";
		String res=workspace+"\\."+layer+"\\."+access_reg;
		
		if (!strict){
			//Non strict pattern is 
			// ws.layer.am | ws.*.am | *.*.am
			res="("+res+")|("+workspace+"\\.\\*\\."+access_reg+")|(\\*\\.\\*\\."+access_reg+")";
		}
		
		return res;
	}
	
	protected String genReg(String workspace,String access_mode,Boolean strict){
		String access_reg="["+access_mode+access_mode.toUpperCase()+"]";
		String res=workspace+"\\.\\*\\."+access_reg;
		
		if (!strict){
			//Non strict pattern is 
			// ws.layer.am | ws.*.am | *.*.am
			res="("+res+")|(\\*\\.\\*\\."+access_reg+")";
		}
		
		return res;
	}

	public RESTWmsStore getWmsStore(final String workspace,String name){
		return reader.getWmsStore(workspace, name);
	}
	
	@SuppressWarnings("serial")
	public ArrayList<StyleTuple> getAllStyles(){		
		return new ArrayList<StyleTuple>(){{
		    
		    log.info("Calling getStyles()");
			addAll(getStyles());
			
			for(WorkspaceTuple w:getWorkspaces()){
			    log.info("Calling getStyles("+w+")");
				addAll(getStyles(w.name));
			}
		}};
	}
	
	public ArrayList<StyleTuple> getStyles(){
		return getStyles(null);
	}
	
	@SuppressWarnings("serial")
	public ArrayList<StyleTuple> getStyles(final String workspace){
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

	public boolean upload(String name,String sld,String workspace){
		return publisher.publishStyleInWorkspace(workspace, sld, name);
	}

	public boolean upload(String name,String sld){
		return publisher.publishStyle(sld, name);
	}

	public ArrayList<String> getUsers(){	
		return toArrayList(makeRequestJSONArray(USERS_SERVICE), String.class);		
	}

	public ArrayList<String> getGroups(){
	    return toArrayList(makeRequestJSONArray(GROUPS_SERVICE),String.class);
	}
	
	@SuppressWarnings("serial")
	public HashMap<String,ArrayList<String>> getMembers(){
	    return new HashMap<String, ArrayList<String>>(){{
	    	JSONObject p = new JSONObject(makeRequest(MEMBERS_SERVICE).replaceAll("=", ":"));
	    	for (final Object k:p.keySet())
	    		put((String)k,toArrayList(p.getJSONArray((String) k), String.class));
	    }};
	}
	
	public String addUser(final String name,final String passwd){
		Form form =new Form();
		form.param("login",name);
		form.param("password", passwd);
        return makePost(USERS_SERVICE,form, String.class);
	}
	
	public String addGroup(final String name){
		Form form =new Form();
		form.param("name",name);
        return makePost(GROUPS_SERVICE,form, String.class);
	}

	public String addMember(final String name,final String group,final Boolean unique){
		Form form =new Form();
		form.param("username",name);
		form.param("groupname", group);
		form.param("force_unique",unique.toString());
        return makePost(MEMBERS_SERVICE,form, String.class);
	}
	
	public String[] addUserGroup(final String name,final String passwd,String group){
		String res_usr=addUser(name, passwd);
		String res_grp=addGroup(group);
		String res_mbr=addMember(name, group,false);
		return new String[]{res_usr,res_grp,res_mbr};
	}
	
	public String delUser(final String name){
		Form form=new Form();
		form.param("login",name);
		return makeDelete(USERS_SERVICE,form.asMap(),String.class);
	}

	public String delGroup(final String name){
		Form form=new Form();
		form.param("name",name);
		return makeDelete(GROUPS_SERVICE,form.asMap(),String.class);
	}

	public String delMember(final String name,final String group){
		Form form=new Form();
		form.param("username",name);
		form.param("groupname",group);
		return makeDelete(MEMBERS_SERVICE,form.asMap(),String.class);
	}
	
	public String getSrsLayer(String layerName){		
		String res=null;
		try {
			JSONObject layer = new JSONObject(makeRequest(
				new URL(new JSONObject(makeRequest(String.format(LAYER_SERVICE,layerName))).getJSONObject("layer").getJSONObject("resource").getString("href"))
			));
			res=layer.getJSONObject((String) layer.keys().next()).getString("srs");
		} 
		catch (JSONException | MalformedURLException e) {
			log.error("Wrong call for getSrsLayer("+layerName+")",e);
		}
		return res;
	}
	
	public boolean linkPGSchema(String workspace,GSPostGISDatastoreEncoder store){
	    return storemanager.create(workspace,store);
	}

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
	
	public boolean removeFeatureType(String workspace, String storename, String layerName){
	    return publisher.unpublishFeatureType(workspace, storename, layerName);
	}
	
	public boolean removeCoverageLayer(String workspace, String storename, String layerName){
	    return publisher.unpublishCoverage(workspace, storename, layerName);
	}
}
