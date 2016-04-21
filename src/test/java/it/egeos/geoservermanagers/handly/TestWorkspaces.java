package it.egeos.geoservermanagers.handly;

import it.egeos.geoserver.restmanagers.tuples.WorkspaceTuple;
import it.egeos.geoservermanagers.handly.abstracts.AbstractTest;

public class TestWorkspaces extends AbstractTest{

	public static void main(String[] args) {
		System.out.println("Initial condition");
		list();
		System.out.println("\nAdding workspaces");
		add();
		System.out.println("\nList after adding");
		list();
		System.out.println("\nRemoving workspaces");
		del();
		System.out.println("\nList after removing");
		list();
		
		clear();
	}
	
	public static void list(){
		System.out.println("Workspaces availables");
		for(WorkspaceTuple ws: g.getWorkspaces())
			System.out.println("\t"+ws);
		System.out.println("end.");		
	}
	
	public static void add(){
		System.out.println("Adding testws001");
		g.addWorkspace("testws001");
		System.out.println("Adding testws002");
		g.addWorkspace("testws002");
		System.out.println("Adding testws003");
		g.addWorkspace("testws003");
		System.out.println("..added.");
	}

	public static void del(){
		System.out.println("Removing testws002");
		g.delWorkspace("testws002");
		System.out.println("Removing testws004");
		g.delWorkspace("testws004");		
		System.out.println("..removed.");
	}
	
	public static void clear(){
		g.delWorkspace("testws001");
		g.delWorkspace("testws003");		
	}
}
