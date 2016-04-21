package it.egeos.geoservermanagers.handly;

import java.util.ArrayList;

import it.egeos.geoserver.restmanagers.tuples.StoreTuple;
import it.egeos.geoserver.restmanagers.tuples.WorkspaceTuple;
import it.egeos.geoservermanagers.handly.abstracts.AbstractTest;

public class TestStores extends AbstractTest{

	public static void main(String[] args) {
		list();
		add();
		list();
		del();
		list();
	}
	
	public static void list(){
		System.out.println("Workspaces availables with stores");
		for(WorkspaceTuple ws: g.getWorkspaces()){
			System.out.println("\t"+ws);
			System.out.println("\t\tDatastores");
			for(StoreTuple s:g.getDataStores(ws))
				System.out.println("\t\t\t"+s);
			System.out.println("\t\tCoverageStores");
			for(StoreTuple s:g.getCoverageStores(ws))
				System.out.println("\t\t\t"+s);
			System.out.println("\t\tWmsStores");
			for(StoreTuple s:g.getWmsStores(ws))
				System.out.println("\t\t\t"+s);
		}
		System.out.println("end.");		
	}
	
	public static void add(){
		System.out.println("Adding WMS store");
		ArrayList<WorkspaceTuple> wss= g.getWorkspaces();
		if(wss.size()>0){
			WorkspaceTuple ws=wss.get(0);
			g.createWmsStore(ws, "store001", "http://www.regione.lazio.it/geoserver/wms", null,null);
		}
	}
	
	public static void del(){
		System.out.println("Removing WMS store");
		ArrayList<WorkspaceTuple> wss= g.getWorkspaces();
		if(wss.size()>0){
			WorkspaceTuple ws=wss.get(0);
			g.deleteWMSStore(ws, "store001");
		}
	}
	
	
}
