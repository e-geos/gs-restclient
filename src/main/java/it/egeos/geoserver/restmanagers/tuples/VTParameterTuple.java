package it.egeos.geoserver.restmanagers.tuples;

public class VTParameterTuple {
    private String name;
    private String defaultValue;
    private String regexpValidator;
    
    public VTParameterTuple(String name, String defaultValue, String regexpValidator) {
        super();
        this.name = name;
        this.defaultValue = defaultValue;
        this.regexpValidator = regexpValidator;
    }

    public String getName() {
        return name;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getRegexpValidator() {
        return regexpValidator;
    }
}
