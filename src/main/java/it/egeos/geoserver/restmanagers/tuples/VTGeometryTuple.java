package it.egeos.geoserver.restmanagers.tuples;

public class VTGeometryTuple {
    private String name;
    private String geometryType;
    private String srid;
    
    public VTGeometryTuple(String name, String geometryType, String srid) {
        super();
        this.name = name;
        this.geometryType = geometryType;
        this.srid = srid;
    }

    public String getName() {
        return name;
    }

    public String getGeometryType() {
        return geometryType;
    }

    public String getSrid() {
        return srid;
    }
}
