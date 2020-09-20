package hk.polyu.pullalive;

import java.util.Iterator;

import soot.Scene;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

public class AnalyzeUJS {
	
	// global
	public static boolean outputHasScheduledTask;
	
	public static void reset() {
		outputHasScheduledTask = false;
	}
	
	public static void analyze() {
		// analyze Dex file
		boolean hasScheduledTask = parse();
		// output
		outputHasScheduledTask = hasScheduledTask;
	}
	
	private static final String JISetPeriodic = "<android.app.job.JobInfo$Builder: android.app.job.JobInfo$Builder setPeriodic(long,long)>";
	public static boolean parse() {
		boolean haveScheduledTask = false;
		
		try {
			CallGraph cg = Scene.v().getCallGraph();
			SootMethod tgtMethod = Scene.v().getMethod(JISetPeriodic);
			Iterator<Edge> edgeIterator = cg.edgesInto(tgtMethod);
			if(edgeIterator.hasNext()) {
				haveScheduledTask = true;
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
