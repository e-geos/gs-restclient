package it.egeos.geoservermanagers.atcompiletime;

import it.egeos.geoserver.restmanagers.tuples.WmsLayerTuple;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class TestWMSLayerTuple {
	@Test
	public void testWMSLayerTuple001(){
		WmsLayerTuple l=new WmsLayerTuple();
		Assert.assertNull(l.name);
		Assert.assertNull(l.nativeName);
		Assert.assertNull(l.title);
		Assert.assertNull(l.store);		
	}
	
	@Test
	public void testWMSLayerTuple002(){
		String name="Name";
		String nativeName="NativeName";
		String title="Title";
		
		WmsLayerTuple l=new WmsLayerTuple(name,title,null,nativeName);
		Assert.assertEquals(l.name,name);
		Assert.assertEquals(l.title,title);
		Assert.assertNull(l.store);		
		Assert.assertEquals(l.nativeName, nativeName);
	}

	@Test
	public void testWMSLayerTuple003(){
		String name="Name";
		String nativeName="NativeName";
		String title="Title";
		String nameStore="NameStore";
		String classStore="aTestClass";
		StringBuilder sb=new StringBuilder();
		sb.append("{");
		sb.append("\"name\":\""+name+"\",");
		sb.append("\"nativeName\":\""+nativeName+"\",");
		sb.append("\"title\":\""+title+"\",");
		sb.append("\"store\":{");
		sb.append("\"name\":\""+nameStore+"\",");
		sb.append("\"@class\":\""+classStore+"\"");
		sb.append("}");
		sb.append("}");

		WmsLayerTuple l=new WmsLayerTuple(new JSONObject(sb.toString()));
		Assert.assertEquals(l.name,name);
		Assert.assertEquals(l.title,title);
		Assert.assertEquals(l.nativeName, nativeName);
		Assert.assertNotNull(l.store);
		Assert.assertEquals(l.store.name,nameStore);
		Assert.assertNull(l.store.type);		
	}
}
