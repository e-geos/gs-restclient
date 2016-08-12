package it.egeos.geoserver.restmanagers.interfaces;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import it.egeos.geoserver.restmanagers.tuples.LayerGroupTuple;
import it.egeos.geoserver.restmanagers.tuples.LayerTuple;
import it.egeos.geoserver.restmanagers.tuples.SqlLayerTuple;
import it.egeos.geoserver.restmanagers.tuples.StoreTuple;
import it.egeos.geoserver.restmanagers.tuples.StyleTuple;
import it.egeos.geoserver.restmanagers.tuples.WorkspaceTuple;

public interface GeoserverManagerAPI {
	Map<String,List<LayerTuple>> getAllLayers();
    ArrayList<WorkspaceTuple> getWorkspaces();
    boolean addWorkspace(String ws);
    boolean delWorkspace(String ws);

    List<StoreTuple> getPostgisStores(WorkspaceTuple workspace);

    ArrayList<StoreTuple> getCoverageStores(WorkspaceTuple workspace);

    ArrayList<StoreTuple> getWmsStores(WorkspaceTuple workspace);

    String createWmsStore(WorkspaceTuple workspace, String name, String url, String usr, String pwd);

    String deleteWMSStore(WorkspaceTuple workspace, String name);

    List<StyleTuple> getLayerStyles(String layer);

    List<LayerGroupTuple> getLayerGroups(String workspace);

    /*
     * returns a list of <name,style> merging 'publishables' and 'styles'
     * properties
     */
    LinkedHashMap<LayerTuple, StyleTuple> getSubLayers(String workspace, String layergroup);

    /*
     * Sets 'publishables' and 'styles' using subs, a <name,style> list
     */
    Boolean assignSubLayers(String workspace, String glayer, LinkedHashMap<LayerTuple, String> subs);

    String addLayerGroup(String workspace, String layer, List<LayerTuple> subL);

    Boolean deleteLayerGroup(String workspace, String layer);

    ArrayList<LayerTuple> getFeatureLayersList(String workspace);

    ArrayList<LayerTuple> getCoverageLayersList(String workspace, String store);

    ArrayList<LayerTuple> getWmsLayersList(String workspace, String store);

    boolean addSQLLayer(SqlLayerTuple slt);

    SqlLayerTuple getSQLLayer(String workspace, String store, String feature_name);

    boolean updateSQLLayer(SqlLayerTuple slt);

    String getDefaultStyle(String layer);

    String assignStyle(String workspace, String layer, String style);

    String assignOptStyle(String workspace, String layer, String style);

    String removeStyle(String layer, String style);

    String addWmsLayer(String workspace, String store, String layer, String name, String title,
            Map<String, String> opts);

    boolean removeWmsLayer(String workspace, String store, String layerName);

    ArrayList<String> getRoles();

    String addRole(String name, String parent);

    String addRoleToUser(String role, String user);

    String delRole(String name);

    String delRoleRefFromUser(String role, String user);

    HashMap<String, ArrayList<String>> getReaders(String workspace, String layer, Boolean strict);

    HashMap<String, ArrayList<String>> getReaders(String workspace, Boolean strict);



    String deleteRule(String rule);

    String deleteRule(String workspace, String layer, char access_mode);

    String addOrCreateRule(String rule, List<String> roles, Boolean add_only);

    String addOrCreateRule(String workspace, String layer, char access_mode, List<String> roles, Boolean add_only);

    ArrayList<StyleTuple> getAllStyles();

    boolean upload(String name, String sld, String workspace);

    String addUser(String name, String passwd);

    String addGroup(String name);

    String addMember(String name, String group, Boolean unique);

    String delGroup(String name);

    String delMember(String name, String group);

    boolean linkPGTable(LayerTuple lt, String nativeCRS);

    boolean removeFeatureType(String workspace, String storename, String layerName);

    boolean removeCoverageLayer(String workspace, String storename, String layerName);

    StoreTuple getWmsStore(String workspace, String name);

    boolean linkPGSchema(final String workspace,final String name,final String host,final Integer port,final String dbname,final String usr,final String pwd);
    LayerTuple getFeatureLayer(String workspace, String name);
    LayerTuple getWMSLayer(String workspace, String name);
    LayerTuple getCoverageLayer(String workspace, String name);

}