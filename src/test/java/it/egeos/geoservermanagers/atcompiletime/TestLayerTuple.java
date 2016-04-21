package it.egeos.geoservermanagers.atcompiletime;

import it.egeos.geoserver.restmanagers.tuples.LayerTuple;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class TestLayerTuple {
	@Test
	public void testLayerTuple001(){
		LayerTuple l=new LayerTuple();
		Assert.assertNull(l.name);
		Assert.assertNull(l.title);
		Assert.assertNull(l.store);		
	}
	
	@Test
	public void testLayerTuple002(){
		String name="Name";
		String title="Title";
		
		LayerTuple l=new LayerTuple(name,title,null);
		Assert.assertEquals(l.name,name);
		Assert.assertEquals(l.title,title);
		Assert.assertNull(l.store);		
	}

	@Test
	public void testLayerTuple003(){
		String name="Name";
		String title="Title";
		String nameStore="NameStore";
		String classStore="aTestClass";
		StringBuilder sb=new StringBuilder();
		sb.append("{");
		sb.append("\"name\":\""+name+"\",");
		sb.append("\"title\":\""+title+"\",");
		sb.append("\"store\":{");
		sb.append("\"name\":\""+nameStore+"\",");
		sb.append("\"@class\":\""+classStore+"\"");
		sb.append("}");
		sb.append("}");

		LayerTuple l=new LayerTuple(new JSONObject(sb.toString()));
		Assert.assertEquals(l.name,name);
		Assert.assertEquals(l.title,title);
		Assert.assertNotNull(l.store);
		Assert.assertEquals(l.store.name,nameStore);
		Assert.assertNull(l.store.type);
	}

}
