package hk.polyu.keepalive;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import hk.polyu.analysis.LocalVariableAnalysis;
import soot.Body;
import soot.Local;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.infoflow.android.manifest.ProcessManifest;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

public class AnalyzeCOW {
	
	// global
	public static boolean outputOverlay;
	
	public static void reset () {
		outputOverlay = false;
	}
	
	public static void analyze(ProcessManifest manifest) {
		// permission check
		/*
		HashSet<String> permissions = new HashSet<String>(manifest.getPermissions());
		if (!permissions.contains("android.permission.SYSTEM_ALERT_WINDOW"))
			return;
		*/
		// code check
		parse(); // set global
	}
	
	private static String WINSetAttributes = "<android.view.Window: void setAttributes(android.view.WindowManager$LayoutParams)>";
	// private static String WINAddFlags = "<android.view.Window: void setType(int)>";
	private static String WINAddFlags = "<android.view.Window: void addFlags(int)>";
	private static void parse() {
		// case-1
		HashSet<SootMethod> appMethods = new HashSet<SootMethod>();
		/*
		try {
			CallGraph cg = Scene.v().getCallGraph();
			SootMethod tgtMethod = Scene.v().getMethod(WINSetAttributes);
			Iterator<Edge> edgeIterator = cg.edgesInto(tgtMethod);
			while(edgeIterator.hasNext()) {
				SootMethod srcMethod = edgeIterator.next().src();
				appMethods.add(srcMethod);
			}
		} catch(Exception e) {}
		// 
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
				if (tgtMethod.getSignature().equals(WINSetAttributes)) {
					Value lpValue = tgtExpr.getArg(0);
					if (!(lpValue instanceof Local))
						continue;
					Local lpLocal = (Local) lpValue;
					// List<Unit> defs = LocalVariableAnalysis.findDefs(body, stmt, lpLocal);
					// System.out.println(defs);
					List<Unit> uses = LocalVariableAnalysis.findUses(body, stmt, lpLocal);
					for (Unit useUnit : uses) {
						Stmt useStmt = (Stmt) useUnit;
						if (!(useStmt instanceof AssignStmt))
							continue;
						Value lhs = ((AssignStmt) useStmt).getLeftOp();
						if (!lhs.toString().contains("android.view.WindowManager$LayoutParams: int type"))
							continue;
						Value rhs = ((AssignStmt) useStmt).getRightOp();
						if (!(rhs instanceof IntConstant))
							continue;
						int windowType = ((IntConstant) rhs).value;
						if (windowType >= 2000)
							outputOverlay = true; // set global
					}
				}
			}
		}
		*/
		// case-2
		appMethods = new HashSet<SootMethod>();
		try {
			CallGraph cg = Scene.v().getCallGraph();
			SootMethod tgtMethod = Scene.v().getMethod(WINAddFlags);
			Iterator<Edge> edgeIterator = cg.edgesInto(tgtMethod);
			while(edgeIterator.hasNext()) {
				SootMethod srcMethod = edgeIterator.next().src();
				appMethods.add(srcMethod);
			}
		} catch(Exception e) {}
		// 
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
				if (tgtMethod.getSignature().equals(WINAddFlags)) {
					Value fgValue = tgtExpr.getArg(0);
					// 
					if (fgValue instanceof IntConstant) {
						int flag = ((IntConstant) fgValue).value;
						// if (flag >= 2000)
						if (flag == 8192)
							outputOverlay = true; // set global
					}
					// 
					if (!(fgValue instanceof Local))
						continue;
					Local fgLocal = (Local) fgValue;
					// List<Unit> defs = LocalVariableAnalysis.findDefs(body, stmt, lpLocal);
					// System.out.println(defs);
					List<Unit> defs = LocalVariableAnalysis.findDefs(body, stmt, fgLocal);
					for (Unit defUnit : defs) {
						Stmt defStmt = (Stmt) defUnit;
						if (!(defStmt instanceof AssignStmt))
							continue;
						Value rhs = ((AssignStmt) defStmt).getRightOp();
						if (!(rhs instanceof IntConstant))
							continue;
						int windowType = ((IntConstant) rhs).value;
						// if (windowType >= 2000)
						if (windowType == 8192)
							outputOverlay = true; // set global
					}
				}
			}
		}
	}
	
	// ---- //
	
	// do nothing
	public static void stub() {}

}
