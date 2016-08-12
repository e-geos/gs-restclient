package it.egeos.geoserver.restmanagers.tuples;

import org.json.JSONObject;

public class WmsStoreTuple extends StoreTuple {
    String capabilitiesURL;
    String usr;
    String pwd;
    
    public WmsStoreTuple() {
        super();
    }

    public WmsStoreTuple(JSONObject json) {
        super(json);
    }

    public WmsStoreTuple(String name, String type, WorkspaceTuple workspace, String capabilitiesURL, String usr, String pwd) {
        super(name, type, workspace);
        this.capabilitiesURL = capabilitiesURL;
        this.usr = usr;
        this.pwd = pwd;
    }

    public String getCapabilitiesURL() {
        return capabilitiesURL;
    }

    public String getUser() {
        return usr;
    }

    public String getPassword() {
        return pwd;
    }
}
