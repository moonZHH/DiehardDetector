package hk.polyu.keepalive;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import hk.polyu.analysis.LocalVariableAnalysis;
import hk.polyu.analysis.ManifestAnalysis;
import soot.Body;
import soot.Local;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.ClassConstant;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.infoflow.android.manifest.ProcessManifest;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

public class AnalyzePMI {
	
	// global
	public static HashSet<String> outputMultitaskActivity;
	
	public static void reset() {
		outputMultitaskActivity = new HashSet<String>();
	}
	
	public static void analyze(ProcessManifest manifest) {
		// 1 - find different taskAffinity
		HashSet<String> activityList = ManifestAnalysis.taskAffinityComponent;
		
		// 2 - ensure the activity is started with FLAG_ACTIVITY_NEW_TASK
		if (!activityList.isEmpty()) {
			parse(activityList); // set global
		}
	}
	
	// parse startActivity intent flag
	private static String IntentSetFlags = "<android.content.Intent: android.content.Intent setFlags(int)>";
	private static String IntentAddFlags = "<android.content.Intent: android.content.Intent addFlags(int)>";
	private static void parse(HashSet<String> activityList) {
		CallGraph cg = Scene.v().getCallGraph();
		HashSet<SootMethod> appMethods = new HashSet<SootMethod>();
		try {
			Iterator<Edge> edgeIterator = cg.edgesInto(Scene.v().getMethod(IntentSetFlags));
			while(edgeIterator.hasNext()) {
				Edge edge = edgeIterator.next();
				appMethods.add(edge.src());
			}
		} catch(Exception e) {}
		try {
			Iterator<Edge> edgeIterator = cg.edgesInto(Scene.v().getMethod(IntentAddFlags));
			while(edgeIterator.hasNext()) {
				Edge edge = edgeIterator.next();
				appMethods.add(edge.src());
			}
		} catch(Exception e) {}
		
		for (SootMethod appMethod : appMethods) {
			if (!appMethod.isConcrete())
				continue;
			// System.out.println(appMethod.getSignature());
				
			Body body = null;
			try {
				body = appMethod.retrieveActiveBody();
			} catch(Exception e) {
				appMethod.releaseActiveBody();
				body = null;
			}
			if (body == null)
				continue;
			
			for (Unit unit : body.getUnits()) {
				Stmt stmt = (Stmt) unit;
				if (!stmt.containsInvokeExpr())
					continue;
				
				InvokeExpr tgtExpr = stmt.getInvokeExpr();
				SootMethod tgtMethod = tgtExpr.getMethod();
				if (tgtMethod.getSignature().equals(IntentSetFlags) || tgtMethod.getSignature().equals(IntentAddFlags)) {
					Value intentFlags = tgtExpr.getArg(0);
					if (!(intentFlags instanceof IntConstant))
						continue;
					int flags = ((IntConstant) intentFlags).value;
					if (flags == 0x10000000) {
						// FLAG_ACTIVITY_NEW_TASK has been found
						assert tgtExpr instanceof VirtualInvokeExpr;
						Local intentLocal = (Local) ((VirtualInvokeExpr) tgtExpr).getBase();
						// List<Unit> defs = LocalVariableAnalysis.findDefs(body, stmt, intentLocal);
						// System.out.println(defs);
						List<Unit> uses = LocalVariableAnalysis.findUses(body, stmt, intentLocal);
						// System.out.println(uses);
						
						boolean isStarted = false;
						HashSet<String> constantStrings = new HashSet<String>();
						for (Unit use : uses) {
							if (!(use instanceof Stmt))
								continue;
							Stmt useStmt = (Stmt) use;
							if (!useStmt.containsInvokeExpr())
								continue;
							InvokeExpr useExpr = useStmt.getInvokeExpr();
							if (useExpr.getArgCount() == 0)
								continue;
							if (useExpr.getMethod().getSignature().contains("startActivit"))
								isStarted = true;
							for (Value useArg : useExpr.getArgs()) {
								if (useArg instanceof StringConstant) {
									String constantString = ((StringConstant) useArg).value;
									if (constantString.startsWith("L") && constantString.endsWith(";") && constantString.contains("/")) {
										constantString = constantString.substring(1);
										constantString = constantString.replace("/", ".").replace(";", "");
									}
									if (activityList.contains(constantString))
										constantStrings.add(constantString);
								}
								if (useArg instanceof ClassConstant) {
									String constantString = ((ClassConstant) useArg).value;
									if (constantString.startsWith("L") && constantString.endsWith(";") && constantString.contains("/")) {
										constantString = constantString.substring(1);
										constantString = constantString.replace("/", ".").replace(";", "");
									}
									if (activityList.contains(constantString))
										constantStrings.add(constantString);
								}
							}
						}
						
						if (isStarted)
							outputMultitaskActivity.addAll(constantStrings); // set global
					}
				}
			}
		}
	}
	
	// ---- //
	
	// do nothing
	public static void stub() {}

}
