package it.egeos.geoserver.restmanagers.tuples;

import it.geosolutions.geoserver.rest.encoder.metadata.virtualtable.VTGeometryEncoder;
import it.geosolutions.geoserver.rest.encoder.metadata.virtualtable.VTParameterEncoder;

import java.util.List;

public class SqlLayerTuple extends LayerTuple {
    public String sql=null;
    public List<VTGeometryEncoder> geomEncList=null;
    public List<VTParameterEncoder> paramEncList=null;
    
    
    public SqlLayerTuple(String name, String title, StoreTuple store,String sql, List<VTGeometryEncoder> geomEncList, List<VTParameterEncoder> paramEncList) {
        super(name, title, store);
        this.sql = sql;
        this.geomEncList = geomEncList;
        this.paramEncList = paramEncList;        
    }


    @Override
    public String toString() {
        return "SqlLayerTuple [sql=" + sql + ", geomEncList=" + geomEncList + ", paramEncList=" + paramEncList
                + ", name=" + name + ", title=" + title + ", store=" + store + "]";
    }
    
    
}
