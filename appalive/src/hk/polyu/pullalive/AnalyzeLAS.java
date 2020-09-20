package hk.polyu.pullalive;

import java.util.Iterator;

import soot.Scene;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

public class AnalyzeLAS {
	
	// global
	public static boolean outputHasScheduledTask;
	
	public static void reset() {
		outputHasScheduledTask = false;
	}
	
	public static void analyze() {
		// analyze Dex file
		// System.out.println(Scene.v().getCallGraph());
		boolean hasScheduledTask = parse();
		// output
		outputHasScheduledTask = hasScheduledTask;
	}
	
	private static final String AlmSetSig = "<android.app.AlarmManager: void set(int,long,android.app.PendingIntent)>";
	private static final String AlmSetAndAllowWhileIdleSig = "<android.app.AlarmManager: void setAndAllowWhileIdle(int,long,android.app.PendingIntent)>";
	private static final String AlmSetExactSig = "<android.app.AlarmManager: void setExact(int,long,android.app.PendingIntent)>";
	private static final String AlmSetExactAndAllowWhileIdleSig = "<android.app.AlarmManager: void setExactAndAllowWhileIdle(int,long,android.app.PendingIntent)>";
	private static final String AlmSetInexactRepeatingSig = "<android.app.AlarmManager: void setInexactRepeating(int,long,long,android.app.PendingIntent)>";
	private static final String AlmSetRepeatingSig = "<android.app.AlarmManager: void setRepeating(int,long,long,android.app.PendingIntent)>";
	private static final String AlmSetWindowSig = "<android.app.AlarmManager: void setWindow(int,long,long,android.app.PendingIntent)>";
	public static boolean parse() {
		boolean haveScheduledTask = false;
		
		try {
			CallGraph cg = Scene.v().getCallGraph();
			SootMethod tgtMethod = Scene.v().getMethod(AlmSetSig);
			Iterator<Edge> edgeIterator = cg.edgesInto(tgtMethod);
			if(edgeIterator.hasNext()) {
				// haveScheduledTask = true; // TODO
			}
		} catch(Exception e) {}
		if (haveScheduledTask == true)
			return haveScheduledTask;
		
		try {
			CallGraph cg = Scene.v().getCallGraph();
			SootMethod tgtMethod = Scene.v().getMethod(AlmSetAndAllowWhileIdleSig);
			Iterator<Edge> edgeIterator = cg.edgesInto(tgtMethod);
			if(edgeIterator.hasNext()) {
				// haveScheduledTask = true; // TODO
			}
		} catch(Exception e) {}
		if (haveScheduledTask == true)
			return haveScheduledTask;
		
		try {
			CallGraph cg = Scene.v().getCallGraph();
			SootMethod tgtMethod = Scene.v().getMethod(AlmSetExactSig);
			Iterator<Edge> edgeIterator = cg.edgesInto(tgtMethod);
			if(edgeIterator.hasNext()) {
				// haveScheduledTask = true; // TODO
			}
		} catch(Exception e) {}
		if (haveScheduledTask == true)
			return haveScheduledTask;
		
		try {
			CallGraph cg = Scene.v().getCallGraph();
			SootMethod tgtMethod = Scene.v().getMethod(AlmSetExactAndAllowWhileIdleSig);
			Iterator<Edge> edgeIterator = cg.edgesInto(tgtMethod);
			if(edgeIterator.hasNext()) {
				// haveScheduledTask = true; // TODO
			}
		} catch(Exception e) {}
		if (haveScheduledTask == true)
			return haveScheduledTask;
		
		try {
			CallGraph cg = Scene.v().getCallGraph();
			SootMethod tgtMethod = Scene.v().getMethod(AlmSetInexactRepeatingSig);
			Iterator<Edge> edgeIterator = cg.edgesInto(tgtMethod);
			if(edgeIterator.hasNext()) {
				haveScheduledTask = true;
			}
		} catch(Exception e) {}
		if (haveScheduledTask == true)
			return haveScheduledTask;
		
		try {
			CallGraph cg = Scene.v().getCallGraph();
			SootMethod tgtMethod = Scene.v().getMethod(AlmSetRepeatingSig);
			Iterator<Edge> edgeIterator = cg.edgesInto(tgtMethod);
			if(edgeIterator.hasNext()) {
				haveScheduledTask = true;
			}
		} catch(Exception e) {}
		if (haveScheduledTask == true)
			return haveScheduledTask;
		
		try {
			CallGraph cg = Scene.v().getCallGraph();
			SootMethod tgtMethod = Scene.v().getMethod(AlmSetWindowSig);
			Iterator<Edge> edgeIterator = cg.edgesInto(tgtMethod);
			if(edgeIterator.hasNext()) {
				// haveScheduledTask = true; // TODO
			}
		} catch(Exception e) {}
		if (haveScheduledTask == true)
			return haveScheduledTask;
		
		return haveScheduledTask;
	}
	
	// ---- //
	
	// do nothing
	public static void stub() {}

}
