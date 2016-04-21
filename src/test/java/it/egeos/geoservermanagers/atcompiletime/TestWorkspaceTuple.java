package it.egeos.geoservermanagers.atcompiletime;

import it.egeos.geoserver.restmanagers.tuples.WorkspaceTuple;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class TestWorkspaceTuple {
	@Test
	public void testWorkspaceTuple001(){
		WorkspaceTuple t=new WorkspaceTuple();
		Assert.assertNull(t.name);
	}
	
	@Test
	public void testWorkspaceTuple002(){
		String name="ws001";

		WorkspaceTuple t=new WorkspaceTuple(name);
		Assert.assertEquals(t.name,name);
	}

	@Test
	public void testWorkspaceTuple003(){
		String name="ws001";
		StringBuilder sb=new StringBuilder();
		sb.append("{");
		sb.append("\"name\":\""+name+"\",");
		sb.append("}");

		WorkspaceTuple t=new WorkspaceTuple(new JSONObject(sb.toString()));
		Assert.assertEquals(t.name,name);
	}
}
