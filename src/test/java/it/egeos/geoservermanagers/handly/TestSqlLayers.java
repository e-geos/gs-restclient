package it.egeos.geoservermanagers.handly;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import it.egeos.geoserver.restmanagers.tuples.SqlLayerTuple;
import it.egeos.geoserver.restmanagers.tuples.StoreTuple;
import it.egeos.geoserver.restmanagers.tuples.WorkspaceTuple;
import it.egeos.geoserver.restmanagers.types.StoreTypes;
import it.egeos.geoservermanagers.handly.abstracts.AbstractTest;
import it.geosolutions.geoserver.rest.encoder.metadata.virtualtable.VTGeometryEncoder;
import it.geosolutions.geoserver.rest.encoder.metadata.virtualtable.VTParameterEncoder;

public class TestSqlLayers extends AbstractTest{
	
	protected static String FEATURETYPE_SERVICE="/workspaces/%s/datastores/%s/featuretypes/%s.json";
	
    public static void main(String[] args) {
	    String ws="geo";
	    String store="postgis_public";
	    String layer="aaa";

	    //Creates a sql layer, keep attection at parameters
	    //create(ws, store, layer);
	    
	    get(ws,store,layer);
    
	    List<StoreTuple> ps = g.getPostgisStores(new WorkspaceTuple(ws));
         
        if(ps!=null)
	        for(StoreTuple s:ps)
	            System.out.println("\t"+s.name);
        else
            System.out.println("\tno postgis data sources");
        //Updates a sql layer, keep attection at parameters
        //update(ws, store, layer);
        
       
        System.out.println("Fine ");
	}
	
	@SuppressWarnings("serial")
    private static void create(String ws, String store, String layer){
	    List<VTGeometryEncoder> geomEncList=new ArrayList<VTGeometryEncoder>(){{
            add(new VTGeometryEncoder("geom","Geometry", "32633"));
        }};
        List<VTParameterEncoder> paramEncList=new ArrayList<VTParameterEncoder>(){{
            add(new VTParameterEncoder("tortello", "-1", "^[\\w\\d\\s]+$"));
            add(new VTParameterEncoder("oid", "-1", "^[\\w\\d\\s]+$"));
        }};
        
        SqlLayerTuple slt=new SqlLayerTuple(
                layer, 
                layer, 
                new StoreTuple(
                        store, 
                        StoreTypes.DATA, 
                        new WorkspaceTuple(ws)
                ), 
                "select * from egs_comuni_abruzzo where nome='%tortello%' and cod_istat=%oid%", 
                geomEncList, 
                paramEncList
        );
        
        System.out.println("Creazione "+ g.addSQLLayer(slt));
	}
	
	private static void get(String ws, String store, String layer){
	    SqlLayerTuple l = g.getSQLLayer(ws, store, layer);
        
        System.out.println("l "+(l!=null));
        
        System.out.println("Geoms:");
        if (l.geomEncList!=null)
            for(VTGeometryEncoder g:l.geomEncList)
                System.out.println("\t"+g.getName()+"|"+g.getType()+"|"+g.getSrid());
        
        System.out.println("Params:");
        if(l.paramEncList!=null)
            for(VTParameterEncoder p:l.paramEncList){
                System.out.println("\t"+p.getName()+"|"+p.getDefaultValue()+"|"+p.getRegexpValidator());
                p.setDefaultValue(p.getDefaultValue()+1);
            }  
	}
	
	@SuppressWarnings("serial")
    private static void update(String ws, String store, String layer){
	    ArrayList<VTGeometryEncoder> geomEncList = new ArrayList<VTGeometryEncoder>(){{
            add(new VTGeometryEncoder("geom","Geometry", "32632"));
        }};
        ArrayList<VTParameterEncoder> paramEncList = new ArrayList<VTParameterEncoder>(){{
            add(new VTParameterEncoder("tortello", "12", "^[\\w\\d\\s]+$"));
            add(new VTParameterEncoder("oid", "12", "^[\\w\\d\\s]+$"));
        }};
        
        SqlLayerTuple slt = new SqlLayerTuple(
                layer, 
                layer, 
                new StoreTuple(
                        store, 
                        StoreTypes.DATA, 
                        new WorkspaceTuple(ws)
                ), 
                "select * from egs_comuni_abruzzo where nome='%tortello%' and cod_istat=%oid%", 
                geomEncList, 
                paramEncList
        );
        System.err.println("Updated "+g.updateSQLLayer(slt));
	}
}
