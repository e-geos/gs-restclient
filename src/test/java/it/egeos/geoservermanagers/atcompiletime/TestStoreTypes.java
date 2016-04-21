package it.egeos.geoservermanagers.atcompiletime;

import it.egeos.geoserver.restmanagers.types.StoreTypes;

import org.junit.Assert;
import org.junit.Test;

public class TestStoreTypes {
    @Test
    public void testTypes(){
    	Assert.assertEquals(StoreTypes.DATA,"datastore");
    	Assert.assertEquals(StoreTypes.COVERAGE,"coveragestore");
    	Assert.assertEquals(StoreTypes.WMS,"wmsstorage");
    	Assert.assertEquals(StoreTypes.LAYERGROUP,"layergroup");    
    }
}
