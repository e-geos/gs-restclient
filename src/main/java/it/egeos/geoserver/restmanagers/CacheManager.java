package it.egeos.geoserver.restmanagers;

import it.egeos.geoserver.restmanagers.Abstracts.RestManager;
import it.egeos.geoserver.restmanagers.tuples.LayerTupleCache;
import it.egeos.geoserver.restmanagers.tuples.TaskTupleLayerCache;

import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.InternalServerErrorException;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * 
 * @author Eugenio Chiodo - chiodoeugenio <at> gmail.com
 * 
 * RestManager implementation to control a Geoserver Cache
 * 
 */
public class CacheManager extends RestManager {
	// GeoServer CACHE
	protected static String LAYERS_SERVICE_CACHE = "/layers.xml";
	protected static String LAYER_TASK_CACHE = "/seed/%s.json";
	protected static String LAYERS_TASK_CACHE = "/seed.json";

	public CacheManager(String login, String password, String url) {
		super(login, password, url);
	}
	
	/**
	 * Support method for convert Node to Element
	 * 
	 * @version %I%, %G%
	 * @param Node to be converted
	 */	
	public Element convertToElement(Node nNode){
		Element e=null;
		if (nNode.getNodeType() == Node.ELEMENT_NODE) {
			e=(Element) nNode;
		}
		return e;
	}
	
	/** 
	 * @version %I%, %G%
	 * @return List of layer names contained in the Geoserver Cache
	 */	
	public List<String> getLayersCache() {
		List<String> layersCache = new ArrayList<String>();
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			String xmlList = makeRequest(LAYERS_SERVICE_CACHE);
			Document doc = factory.newDocumentBuilder().parse(new InputSource(new StringReader(new StringBuffer(1024).append(xmlList).toString())));
			doc.getDocumentElement().normalize();
			NodeList layers = doc.getElementsByTagName("layer");
			for (int i = 0; i < layers.getLength(); i++) {
				Element layer=convertToElement(layers.item(i));
				if(layer!=null){
					String name = layer.getElementsByTagName("name").item(0).getTextContent();
					layersCache.add(name);
				}
			}
		} catch (Exception e) {
			log.error(e);
		}
		return layersCache;
	}
	
	/** 
	 * @version %I%, %G%
	 * @param layer name
	 * @return object layer with the characteristics of layer in Geoserver Cache present
	 */	
	public LayerTupleCache getLayerCache(String layerName) {
		LayerTupleCache layerCache = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			String xmlList = makeRequest(LAYERS_SERVICE_CACHE);
			Document doc = factory.newDocumentBuilder().parse(new InputSource(new StringReader(new StringBuffer(1024).append(xmlList).toString())));
			doc.getDocumentElement().normalize();
			NodeList layers = doc.getElementsByTagName("layer");
			for (int i = 0; i < layers.getLength(); i++) {
				Element layer=convertToElement(layers.item(i));
				if(layer!=null){
					String name = layer.getElementsByTagName("name").item(0).getTextContent();
					if (name.equals(layerName)) {
						NodeList atomLink=layer.getElementsByTagName("atom:link");
						for (int z = 0; z < atomLink.getLength(); z++) {
							Element link=convertToElement(atomLink.item(z));
							if(link!=null){
								String linkLayer = link.getAttribute("href");
								String xmlLayer = makeRequest(new URL(linkLayer));// Effettua chiamata REST sul link
								Document doc1 = factory.newDocumentBuilder().parse(new InputSource(new StringReader(new StringBuffer(1024).append(xmlLayer).toString())));
								doc1.getDocumentElement().normalize();
								NodeList objectLayer = doc1.getElementsByTagName("GeoServerLayer");
								for (int j = 0; j < objectLayer.getLength(); j++) {
									Element objLayer=convertToElement(objectLayer.item(j));
									if(objLayer!=null){
										layerCache = new LayerTupleCache(name,Boolean.parseBoolean(objLayer.getElementsByTagName("enabled").item(0).getTextContent()));
										NodeList formatsImg = objLayer.getElementsByTagName("mimeFormats");
										for (int k = 0; k < formatsImg.getLength(); k++) {
											Element format=convertToElement(formatsImg.item(k));
											if(format!=null){
												for (int h = 0; h < format.getElementsByTagName("string").getLength(); h++) {
													layerCache.addFormat(format.getElementsByTagName("string").item(h).getTextContent());
												}
											}
										}
										NodeList gridsSubset= doc1.getElementsByTagName("gridSubsets");
										for (int k = 0; k < gridsSubset.getLength(); k++) {
											Element grid=convertToElement(gridsSubset.item(k));
											if(grid!=null){
												for (int h = 0; h < grid.getElementsByTagName("gridSetName").getLength(); h++) {
													layerCache.addGridSubset(grid.getElementsByTagName("gridSetName").item(h).getTextContent());
												}
											}
											NodeList coords = grid.getElementsByTagName("coords");
											for (int h = 0; h < coords.getLength(); h++) {
												Element c=convertToElement(coords.item(h));
												if(c!=null){
													layerCache.setBoundingBox(Double.parseDouble(c.getElementsByTagName("double").item(0).getTextContent()),
															Double.parseDouble(c.getElementsByTagName("double").item(1).getTextContent()),
															Double.parseDouble(c.getElementsByTagName("double").item(2).getTextContent()),
															Double.parseDouble(c.getElementsByTagName("double").item(3).getTextContent()));
												}
											}
										}
									}
								}
							}
						}	
						break;
					}
					
				}
			}
		} catch (Exception e) {
			log.error(e);
		}
		return layerCache;
	}

	/** 
	 * @version %I%, %G%
	 * @param layer name
	 * @return the task list running for the selected layer
	 */	
	public List<TaskTupleLayerCache> getTaskLayer(String layerName) {
		List<TaskTupleLayerCache> taskLayerCache = new ArrayList<TaskTupleLayerCache>();
		JSONObject p = new JSONObject(makeRequest(String.format(
				LAYER_TASK_CACHE, layerName)));
		JSONArray arr = p.getJSONArray("long-array-array");
		for (int i = 0; i < arr.length(); i++) {
			taskLayerCache.add(new TaskTupleLayerCache(Long.parseLong(""
					+ arr.getJSONArray(i).get(0)), Long.parseLong(""
					+ arr.getJSONArray(i).get(1)), Long.parseLong(""
					+ arr.getJSONArray(i).get(2)), Long.parseLong(""
					+ arr.getJSONArray(i).get(3)), Long.parseLong(""
					+ arr.getJSONArray(i).get(4))));
		}
		return taskLayerCache;
	}
	
	/** 
	 * @version %I%, %G%
	 * @param layer name
	 * @return the number task running for the selected layer
	 */	
	public int getNumberTaskLayer(String layerName) {
		int n=0;
		JSONObject p = new JSONObject(makeRequest(String.format(LAYER_TASK_CACHE, layerName)));
		JSONArray arr = p.getJSONArray("long-array-array");
		for (int i = 0; i < arr.length(); i++) {
			n++;
		}
		return n;
	}
	
	/** 
	 * @version %I%, %G%
	 * @return the number task running for all layers
	 */	
	public int getNumberTaskLayers() {
		int n=0;
		JSONObject p = new JSONObject(makeRequest(LAYERS_TASK_CACHE));
		JSONArray arr = p.getJSONArray("long-array-array");
		for (int i = 0; i < arr.length(); i++) {
			n++;
		}
		return n;
	}

	/** 
	 * @version %I%, %G%
	 * @return the list task running for all layers
	 */	
	public List<TaskTupleLayerCache> getTaskLayers() {
		List<TaskTupleLayerCache> taskLayerCache = new ArrayList<TaskTupleLayerCache>();
		JSONObject p = new JSONObject(makeRequest(LAYERS_TASK_CACHE));
		JSONArray arr = p.getJSONArray("long-array-array");
		for (int i = 0; i < arr.length(); i++) {
			taskLayerCache.add(new TaskTupleLayerCache(Long.parseLong(""
					+ arr.getJSONArray(i).get(0)), Long.parseLong(""
					+ arr.getJSONArray(i).get(1)), Long.parseLong(""
					+ arr.getJSONArray(i).get(2)), Long.parseLong(""
					+ arr.getJSONArray(i).get(3)), Long.parseLong(""
					+ arr.getJSONArray(i).get(4))));
		}
		return taskLayerCache;
	}
	
	/** 
	 * Creates a new task in geoserver cache
	 * 
	 * @version %I%, %G%
	 * @param layer name
	 * @param JSON object
	 * @return response string call POST
	 */	
	public String curlMessage(String nameLayer,final JSONObject json) throws InternalServerErrorException {
		return makePost(String.format(LAYER_TASK_CACHE, nameLayer),json,String.class);
	}
}
