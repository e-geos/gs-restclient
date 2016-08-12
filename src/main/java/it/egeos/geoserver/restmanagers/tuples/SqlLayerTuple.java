package it.egeos.geoserver.restmanagers.tuples;

import java.util.List;

public class SqlLayerTuple extends LayerTuple {
    public String sql=null;
    public List<VTGeometryTuple> geomEncList=null;
    public List<VTParameterTuple> paramEncList=null;
    
    
    public SqlLayerTuple(String name, String title, StoreTuple store,String sql, List<VTGeometryTuple> geomEncList, List<VTParameterTuple> paramEncList) {
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
