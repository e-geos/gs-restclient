package it.egeos.geoservermanagers.handly;

import java.util.ArrayList;

import it.egeos.geoservermanagers.handly.abstracts.AbstractTest;

public class TestRules  extends AbstractTest{
	private static String ws="TestWS";
	private static String adm="Admin_TesWS";
	private static String rd="Read_TesWS";
	private static String wr="Write_TesWS";
	
	@SuppressWarnings("serial")
	public static void main(String[] args) {
		g.addWorkspace(ws);
		System.out.println("Creato WS "+ws);
		g.addRole(adm);
		System.out.println("Creato ruolo "+adm);
		g.addRole(rd);
		System.out.println("Creato ruolo "+rd);
		g.addRole(wr);
		System.out.println("Creato ruolo "+wr);
		
		g.addOrCreateRule(ws, "*", 'a', new ArrayList<String>(){{
			add(adm);
		}}, false);
		System.out.println("Creata Rule 'a'");
		
		g.addOrCreateRule(ws, "*", 'r', new ArrayList<String>(){{
			add(rd);
		}}, false);
		System.out.println("Creata Rule 'r'");
		
		g.addOrCreateRule(ws, "*", 'w', new ArrayList<String>(){{
			add(wr);
		}}, false);
		System.out.println("Creata Rule 'w'");

		g.deleteRule(ws,null, 'a');
		System.out.println("Eliminata Rule 'a'");
		g.deleteRule(ws, null, 'r');
		System.out.println("Eliminata Rule 'r'");
		g.deleteRule(ws, null, 'w');
		System.out.println("Eliminata Rule 'w'");
		
		g.delRole(adm);
		System.out.println("Eliminato Role "+adm);
		g.delRole(rd);
		System.out.println("Eliminato Role "+rd);
		g.delRole(wr);
		System.out.println("Eliminato Role "+wr);
		g.delWorkspace(ws);
		System.out.println("Eliminato WS "+ws);
	}

}
