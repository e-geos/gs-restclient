package it.egeos.geoserver.restmanagers.tuples;

public class LayerGroupTuple {
    private String name;
    private String title;
    private double maxX;
    private double maxY;
    private double minX;
    private double minY;
    private String crs;

    public LayerGroupTuple(String name, String title, double maxX, double maxY, double minX, double minY, String crs) {
        super();
        this.name = name;
        this.title = title;
        this.maxX = maxX;
        this.maxY = maxY;
        this.minX = minX;
        this.minY = minY;
        this.crs = crs;
    }

    public String getName() {        
        return name;
    }

    public String getTitle() {
        return title;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMaxY() {
        return maxY;
    }

    public double getMinX() {
        return minX;
    }

    public double getMinY() {
        return minY;
    }

    public String getCRS() {
        return crs;
    }
}
