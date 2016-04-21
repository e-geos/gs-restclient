package it.egeos.geoservermanagers.atcompiletime;

import it.egeos.geoserver.restmanagers.tuples.StyleTuple;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class TestStyleTuple {
	@Test
	public void testStyleTuple001(){
		StyleTuple s=new StyleTuple();
		Assert.assertNull(s.name);
		Assert.assertNull(s.format);
		Assert.assertNull(s.filename);
		Assert.assertNull(s.workspace);	
	}
	
	@Test
	public void testStyleTuple002(){
		String name="Name";
		String format="sld";
		String filename="grass_poly.sld";
		
		StyleTuple s=new StyleTuple(name, format, filename,null);
		Assert.assertEquals(s.name,name);
		Assert.assertEquals(s.format,format);
		Assert.assertEquals(s.filename,filename);
		Assert.assertNull(s.workspace);		
	}

	@Test
	public void testStyleTuple003(){
		String name="Name";
		String format="sld";
		String filename="grass_poly.sld";
		String workspace = "ws001";
		
		StringBuilder sb=new StringBuilder();
		sb.append("{");
		sb.append("\"name\":\""+name+"\",");
		sb.append("\"format\":\""+format+"\",");
		sb.append("\"filename\":\""+filename+"\",");		
		sb.append("\"workspace\":{");
		sb.append("\"name\":\""+workspace+"\"");
		sb.append("}");		
		sb.append("}");

		StyleTuple l=new StyleTuple(new JSONObject(sb.toString()));
		Assert.assertEquals(l.name,name);
		Assert.assertEquals(l.format,format);
		Assert.assertEquals(l.filename,filename);
		Assert.assertEquals(l.workspace.name,workspace);			
	}

	@Test
	public void testStyleTuple004(){
		String name="Name";
		String format="sld";
		String filename="grass_poly.sld";
		
		StringBuilder sb=new StringBuilder();
		sb.append("{");
		sb.append("\"name\":\""+name+"\",");
		sb.append("\"format\":\""+format+"\",");
		sb.append("\"filename\":\""+filename+"\",");		
		sb.append("}");

		StyleTuple l=new StyleTuple(new JSONObject(sb.toString()));
		Assert.assertEquals(l.name,name);
		Assert.assertEquals(l.format,format);
		Assert.assertEquals(l.filename,filename);
		Assert.assertNull(l.workspace);			
	}
}
