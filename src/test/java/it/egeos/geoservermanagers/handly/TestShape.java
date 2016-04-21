package it.egeos.geoservermanagers.handly;

import it.egeos.geoserver.restmanagers.tuples.StoreTuple;
import it.egeos.geoserver.restmanagers.tuples.WorkspaceTuple;
import it.egeos.geoserver.restmanagers.types.StoreTypes;
import it.egeos.geoservermanagers.handly.abstracts.AbstractTest;
import it.geosolutions.geoserver.rest.decoder.RESTCoverage;
import it.geosolutions.geoserver.rest.decoder.RESTFeatureType;
import it.geosolutions.geoserver.rest.decoder.RESTLayerGroup;
import it.geosolutions.geoserver.rest.decoder.RESTWms;

public class TestShape  extends AbstractTest{

    public static void main(String[] args) {
        System.out.println("Inizio");
        
        String[] ls=new String[]{
                "66006_Siti_Interesse_Comunitario",
                "66006_Parchi_e_Riserve",
                "66006_Zone_Protezione_Speciale"
        };
        
        for (String l:ls){
            g.removeShapefile("geo", l);
           
        }
        
        for(WorkspaceTuple ws:g.getWorkspaces()){
            for(RESTFeatureType layer: g.getFeatureLayersList(ws.name))                           
                System.out.println("Layer "+layer.getName());              
/*           
            for(StoreTuple store:gm.getCoverageStores(ws))
                for(RESTCoverage layer: gm.getCoverageLayersList(ws.name, store.name))
                    res.add(toEgsLayers(layer, ws.name,StoreTypes.COVERAGE, defaults));            
            
            for(StoreTuple store:gm.getWmsStores(ws))
                for(RESTWms layer: gm.getWmsLayersList(ws.name, store.name))
                    res.add(toEgsLayers(layer, ws.name,StoreTypes.WMS, defaults));         
            for(RESTLayerGroup layer:gm.getLayerGroups(ws.name))
                res.add(toEgsLayers(layer, ws.name, defaults));           
*/        }
        System.out.println("Fine");
    }
}
