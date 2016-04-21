package it.egeos.geoservermanagers.atcompiletime;

import it.egeos.geoserver.restmanagers.tuples.StoreTuple;
import it.egeos.geoserver.restmanagers.types.StoreTypes;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class TestStoreTuple {
	@Test
	public void testStoreTuple001(){
		StoreTuple s=new StoreTuple();
		Assert.assertNull(s.name);
		Assert.assertNull(s.type);		
	}
	
	@Test
	public void testStoreTuple002(){
		String name="Name";
		String type="aType";		
		StoreTuple s=new StoreTuple(name,type,null);
		Assert.assertEquals(s.name,name);
		Assert.assertEquals(s.type,type);
		Assert.assertNull(s.workspace);
	}	

	@Test
	public void testStoreTuple003(){
		String name="Name";
		String type="aType";
		StringBuilder sb=new StringBuilder();
		sb.append("{");
		sb.append("\"name\":\""+name+"\",");
		sb.append("\"@class\":\""+type+"\"");
		sb.append("}");

		StoreTuple s=new StoreTuple(new JSONObject(sb.toString()));
		Assert.assertEquals(s.name,name);
		Assert.assertNull(s.type);		
	}

	@Test
	public void testStoreTuple004(){
		String name="Name";
		String type="wmsStore";
		StringBuilder sb=new StringBuilder();
		sb.append("{");
		sb.append("\"name\":\""+name+"\",");
		sb.append("\"@class\":\""+type+"\"");
		sb.append("}");

		StoreTuple s=new StoreTuple(new JSONObject(sb.toString()));
		Assert.assertEquals(s.name,name);
		Assert.assertEquals(s.type,StoreTypes.WMS);		
	}

	@Test
	public void testStoreTuple005(){
		String name="Name";
		String nameWorkspace="WsName";
		StringBuilder sb=new StringBuilder();
		sb.append("{");
		sb.append("\"name\":\""+name+"\",");		
		sb.append("\"workspace\":{");
		sb.append("\"name\":\""+nameWorkspace+"\",");
		sb.append("}");		
		sb.append("}");

		StoreTuple s=new StoreTuple(new JSONObject(sb.toString()));
		Assert.assertEquals(s.name,name);
		Assert.assertEquals(s.workspace.name, nameWorkspace);		
	}

}
