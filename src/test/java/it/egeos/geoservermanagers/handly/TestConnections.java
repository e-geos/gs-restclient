package it.egeos.geoservermanagers.handly;

import it.egeos.geoserver.restmanagers.GeoserverManager;
import it.egeos.geoserver.restmanagers.tuples.StoreTuple;
import it.egeos.geoserver.restmanagers.tuples.StyleTuple;
import it.egeos.geoserver.restmanagers.tuples.WorkspaceTuple;
import it.egeos.geoservermanagers.handly.abstracts.AbstractTest;

import java.util.ArrayList;
import java.util.List;

public class TestConnections extends AbstractTest{

	public static void main(String[] args) throws InterruptedException {

		
		List<Thread> tts=new ArrayList<Thread>();
		for(int i=0;i<100;i++){
			System.out.println("Staring..");
			Thread t = new Thread(new Client());
			t.start();			
			tts.add(t);
		}	
				
		for(Thread t:tts)
			t.join();
	
		for(int i=60;i>0;i--){
			System.out.print(".");
			Thread.sleep(1000);
		}
	}
}

class Client extends AbstractTest implements Runnable{

	@Override
	public void run() {
		
		GeoserverManager gm = new GeoserverManager(usr,pwd,url);
		System.out.println("Inizio thread ");
		
		for(WorkspaceTuple ws:gm.getWorkspaces()){
			System.out.println(ws);
			for(StoreTuple ds:gm.getDataStores(ws)){
				//System.out.println("\t"+ds);
			}
			
			for(StyleTuple sts:gm.getStyles(ws.name)){
			    //System.out.println("\t"+sts.name);
			}
		}
		
		gm.getAllStyles();
		gm.shutdown();
		System.out.println("Fine thread");		
	}
	
}
